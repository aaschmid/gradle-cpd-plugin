package de.aaschmid.gradle.plugins.cpd;

import de.aaschmid.gradle.plugins.cpd.internal.CpdExecutor;
import de.aaschmid.gradle.plugins.cpd.internal.CpdReporter;
import de.aaschmid.gradle.plugins.cpd.internal.CpdReportsImpl;
import groovy.lang.Closure;
import net.sourceforge.pmd.cpd.LanguageFactory;
import net.sourceforge.pmd.cpd.Match;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.VerificationTask;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.util.DeprecationLogger;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;


/**
 * Runs static code/paste (= duplication) detection on supplied source code files and generates a report of duplications
 * found.
 * <p>
 * Sample:
 *
 * <pre>
 * apply plugin: 'cpd'
 *
 * task cpd(type: Cpd, description: 'Copy/Paste detection for all Ruby scripts') {
 *
 *     // change language of cpd to Ruby
 *     language = 'ruby'
 *
 *     // set minimum token count causing a duplication warning
 *     minimumTokenCount = 10
 *
 *     // enable CSV reports and customize destination, disable xml report
 *     reports {
 *         csv {
 *             enabled = true
 *             destination = file("${buildDir}/cpd.csv")
 *         }
 *         xml.enabled = false
 *     }
 *
 *     // explicitly include all Ruby files and exclude tests
 *     include '**.rb'
 *     exclude '**Test*'
 *
 *     // set source for running duplication check on
 *     source = files('src/ruby')
 * }
 * </pre>
 *
 * @see CpdPlugin
 */
@CacheableTask
public class Cpd extends SourceTask implements VerificationTask, Reporting<CpdReports> {

    private static final Logger logger = Logging.getLogger(Cpd.class);

    private final CpdReportsImpl reports;

    private String encoding;
    private boolean ignoreAnnotations;
    private boolean ignoreFailures;
    private boolean ignoreIdentifiers;
    private boolean ignoreLiterals;
    private String language;
    private int minimumTokenCount;
    private FileCollection pmdClasspath;
    private boolean skipDuplicateFiles;
    private boolean skipLexicalErrors;
    private boolean skipBlocks;
    private String skipBlocksPattern;

    @Inject
    public Cpd(Instantiator instantiator) {
        this.reports = DeprecationLogger.whileDisabled(() -> instantiator.newInstance(CpdReportsImpl.class, this));
    }

    @TaskAction
    void run() {
        // TODO very ugly class loading hack but as using org.gradle.process.internal.WorkerProcess does not work due to classpath problems using my own classes, this is the only why for now :-(
        addPmdClasspathToClassLoader();

        // use getter to access properties that they are resolved correctly using conventionMapping
        CpdReporter reporter = new CpdReporter(this);
        CpdExecutor executor = new CpdExecutor(this);

        List<Match> matches = executor.run();
        reporter.generate(matches);
        logResult(matches);
    }

    private void addPmdClasspathToClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addUrlMethod.setAccessible(true);
            for (File file : getPmdClasspath().getFiles()) {
                addUrlMethod.invoke(cl, file.toURI().toURL());
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | MalformedURLException e) {
            throw new GradleException("Could not add pmd classpath urls to context classloader.", e);
        }
    }


    private void logResult(List<Match> matches) {
        if (matches.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("No duplicates over {} tokens found.", getMinimumTokenCount());
            }

        } else {
            String message = "CPD found duplicate code.";
            SingleFileReport report = reports.getFirstEnabled();
            if (report != null) {
                String reportUrl = asClickableFileUrl(report.getDestination());
                message += " See the report at " + reportUrl;
            }
            if (getIgnoreFailures()) {
                if (logger.isWarnEnabled()) {
                    logger.warn(message);
                }
            } else {
                throw new GradleException(message);
            }
        }
    }

    private String asClickableFileUrl(File path) {
        // File.toURI().toString() leads to an URL like this on Mac: file:/reports/index.html
        // This URL is not recognized by the Mac console (too few leading slashes). We solve
        // this be creating an URI with an empty authority.
        try {
            return new URI("file", "", path.toURI().getPath(), null, null).toString();
        } catch (URISyntaxException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    @Override
    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.RELATIVE)
    public FileTree getSource() {
        return super.getSource();
    }

    @Override
    public CpdReports reports(Closure closure) {
        return (CpdReports) reports.configure(closure);
    }

    @Override
    @Nested
    public CpdReports getReports() {
        return reports;
    }

    //@Override to be compatible with earlier versions too
    public CpdReports reports(Action<? super CpdReports> action) {
        action.execute(this.reports);
        return this.reports;
    }


    /**
     * The character set encoding (e.g., UTF-8) to use when reading the source code files but also when producing the
     * report; defaults to {@link CpdExtension#getEncoding()}.
     * <p>
     * Example: {@code encoding = UTF-8}
     *
     * @return the charset encoding
     */
    @Input
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Ignore annotations because more and more modern frameworks use annotations on classes and methods which can be very
     * redundant and causes false positives.
     * <p>
     * Example: {@code ignoreAnnotations = true}
     *
     * @return if annotations should be ignored
     */
    @Input
    public boolean getIgnoreAnnotations() {
        return ignoreAnnotations;
    }

    public void setIgnoreAnnotations(boolean ignoreAnnotations) {
        this.ignoreAnnotations = ignoreAnnotations;
    }

    /**
     * Whether or not to allow the build to continue if there are warnings.
     * <p>
     * Example: {@code ignoreFailures = true}
     * {@inheritDoc}
     */
    @Override
    @Input
    public boolean getIgnoreFailures() {
        return ignoreFailures;
    }

    @Override
    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures;
    }

    /**
     * Option if CPD should ignore identifiers differences, i.e. variable names, methods names, and so forth,
     * when evaluating a duplicate block.
     * <p>
     * Example: {@code ignoreIdentifiers = true}
     *
     * @return whether identifiers should be ignored
     */
    @Input
    public boolean getIgnoreIdentifiers() {
        return ignoreIdentifiers;
    }

    public void setIgnoreIdentifiers(boolean ignoreIdentifiers) {
        this.ignoreIdentifiers = ignoreIdentifiers;
    }

    /**
     * Option if CPD should ignore literal value differences when evaluating a duplicate block. This means e.g. that
     * {@code foo=42;} and {@code foo=43;} will be seen as equivalent.
     * <p>
     * Example: {@code ignoreLiterals = true}
     *
     * @return whether literals should be ignored
     */
    @Input
    public boolean getIgnoreLiterals() {
        return ignoreLiterals;
    }

    public void setIgnoreLiterals(boolean ignoreLiterals) {
        this.ignoreLiterals = ignoreLiterals;
    }

    /**
     * Flag to select the appropriate language.
     * <p>
     * Example: {@code language = 'java'}
     *
     * @return used language
     */
    @Input
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * A positive integer indicating the minimum token count to trigger a CPD match; defaults to
     * {@link CpdExtension#getMinimumTokenCount()}.
     * <p>
     * Example: {@code minimumTokenCount = 25}
     *
     * @return the minimum token cound
     */
    @Input
    public int getMinimumTokenCount() {
        return minimumTokenCount;
    }

    public void setMinimumTokenCount(int minimumTokenCount) {
        this.minimumTokenCount = minimumTokenCount;
    }

    /**
     * The classpath containing the PMD library which contains the CPD library to be used.
     *
     * @return CPD libraries classpath
     */
    @InputFiles
    @PathSensitive(PathSensitivity.NAME_ONLY)
    public FileCollection getPmdClasspath() {
        return pmdClasspath;
    }

    public void setPmdClasspath(FileCollection pmdClasspath) {
        this.pmdClasspath = pmdClasspath;
    }

    /**
     * Ignore multiple copies of files of the same name and length in comparison.
     * <p>
     * Example: {@code skipDuplicateFiles = true}
     *
     * @return whether duplicate files should be skipped
     */
    @Input
    public boolean getSkipDuplicateFiles() {
        return skipDuplicateFiles;
    }

    public void setSkipDuplicateFiles(boolean skipDuplicateFiles) {
        this.skipDuplicateFiles = skipDuplicateFiles;
    }

    /**
     * Skip files which cannot be tokenized due to invalid characters instead of aborting CPD.
     * <p>
     * Example: {@code skipLexicalErrors = true}
     *
     * @return whether lexical errors should be skipped
     */
    @Input
    public boolean getSkipLexicalErrors() {
        return skipLexicalErrors;
    }

    public void setSkipLexicalErrors(boolean skipLexicalErrors) {
        this.skipLexicalErrors = skipLexicalErrors;
    }

    /**
     * Enables or disables skipping of blocks configured by {@link #skipBlocksPattern}.
     * <p>
     * Example: {@code skipBlocks = false}
     *
     * @see #skipBlocksPattern
     *
     * @return whether blocks should be skipped by a given pattern
     */
    @Input
    public boolean getSkipBlocks() {
        return skipBlocks;
    }

    public void setSkipBlocks(boolean skipBlocks) {
        this.skipBlocks = skipBlocks;
    }

    /**
     * Configures the pattern, to find the blocks to skip if enabled using {@link #skipBlocks}. It is a {@link String}
     * property and contains of two parts, separated by {@code '|'}. The first part is the start pattern, the second part
     * is the ending pattern.
     * <p>
     * Example: {@code skipBlocksPattern = '#include <|>'}
     *
     * @see #skipBlocks
     *
     * @return the pattern used to skip blocks
     */
    @Input
    public String getSkipBlocksPattern() {
        return skipBlocksPattern;
    }

    public void setSkipBlocksPattern(String skipBlocksPattern) {
        this.skipBlocksPattern = skipBlocksPattern;
    }
}
