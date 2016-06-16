package de.aaschmid.gradle.plugins.cpd

import de.aaschmid.gradle.plugins.cpd.internal.CpdExecutor
import de.aaschmid.gradle.plugins.cpd.internal.CpdReporter
import de.aaschmid.gradle.plugins.cpd.internal.CpdReportsImpl
import de.aaschmid.gradle.plugins.cpd.internal.CpdUncheckedException
import net.sourceforge.pmd.cpd.Match
import org.gradle.api.GradleException
import org.gradle.api.Incubating
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.reporting.Reporting
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import org.gradle.internal.classloader.MutableURLClassLoader
import org.gradle.internal.reflect.Instantiator

import javax.inject.Inject

/**
 * Runs static code/paste (= duplication) detection on supplied source code files and generates a report of duplications
 * found.
 * <p>
 * Sample:
 *
 * <pre autoTested=''>
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
@Incubating
class Cpd extends SourceTask implements VerificationTask, Reporting<CpdReports> {

    private static final Logger logger = Logging.getLogger(Cpd.class);

    /**
     * The character set encoding (e.g., UTF-8) to use when reading the source code files but also when producing the
     * report; defaults to {@link CpdExtension#getEncoding()}.
     * <p>
     * Example: {@code encoding = UTF-8}
     */
    @Input
    String encoding

    /**
     * Ignore annotations because more and more modern frameworks use annotations on classes and methods which can be very
     * redundant and causes false positives.
     * <p>
     * Example: {@code ignoreAnnotations = true}
     */
    @Input
    boolean ignoreAnnotations

    /**
     * Whether or not to allow the build to continue if there are warnings.
     * <p>
     * Example: {@code ignoreFailures = true}
     */
    @Input
    boolean ignoreFailures

    /**
     * Option if CPD should ignore identifiers differences, i.e. variable names, methods names, and so forth,
     * when evaluating a duplicate block.
     * <p>
     * Example: {@code ignoreIdentifiers = true}
     */
    @Input
    boolean ignoreIdentifiers

    /**
     * Option if CPD should ignore literal value differences when evaluating a duplicate block. This means e.g. that
     * {@code foo=42;} and {@code foo=43;} will be seen as equivalent.
     * <p>
     * Example: {@code ignoreLiterals = true}
     */
    @Input
    boolean ignoreLiterals

    /**
     * Flag to select the appropriate language.
     * <p>
     * Example: {@code language = 'java'}
     */
    @Input
    String language

    /**
     * A positive integer indicating the minimum token count to trigger a CPD match; defaults to
     * {@link CpdExtension#getMinimumTokenCount()}.
     * <p>
     * Example: {@code minimumTokenCount = 25}
     */
    @Input
    int minimumTokenCount

    /**
     * The class path containing the PMD library which contains the CPD library to be used.
     */
    @InputFiles
    FileCollection pmdClasspath

    /**
     * Ignore multiple copies of files of the same name and length in comparison.
     * <p>
     * Example: {@code skipDuplicateFiles = true}
     */
    @Input
    boolean skipDuplicateFiles

    /**
    * Skip files which cannot be tokenized due to invalid characters instead of aborting CPD.
     * <p>
     * Example: {@code skipLexicalErrors = true}
     */
    @Input
    boolean skipLexicalErrors

    /**
     * Enables or disables skipping of blocks configured by {@link #skipBlocksPattern}.
     * <p>
     * Example: {@code skipBlocks = false}
     * @see #skipBlocksPattern
     */
    @Input
    boolean skipBlocks

    /**
     * Configures the pattern, to find the blocks to skip if enabled using {@link #skipBlocks}. It is a {@link String}
     * property and contains of two parts, separated by {@code '|'}. The first part is the start pattern, the second part
     * is the ending pattern.
     * <p>
     * Example: {@code skipBlocksPattern = '#include <|>'}
     * @see #skipBlocks
     */
    @Input
    String skipBlocksPattern


    @Nested
    private final CpdReportsImpl reports

    @Inject
    Cpd(Instantiator instantiator) {
        this.reports = instantiator.newInstance(CpdReportsImpl, this)
    }

    @TaskAction
    void run() {
        // TODO very ugly class loading hack but as using org.gradle.process.internal.WorkerProcess does not work due to classpath problems using my own classes, this is the only why for now :-(
        def contextClassLoader = Thread.currentThread().contextClassLoader
        getPmdClasspath().files.each{ file -> contextClassLoader.addURL(file.toURI().toURL()) }

        // use getter to access properties that they are resolved correctly using conventionMapping
        CpdReporter reporter = new CpdReporter(this)
        CpdExecutor executor = new CpdExecutor(this)

        List<Match> matches = executor.run()
        reporter.generate(matches)
        logResult(matches)
    }

    private void logResult(List<Match> matches) {
        if (matches.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info('No duplicates over {} tokens found.', getMinimumTokenCount())
            }

        } else {
            String message = "CPD found duplicate code.";
            SingleFileReport report = reports.getFirstEnabled();
            if (report != null) {
                String reportUrl = asClickableFileUrl(report.getDestination());
                message += " See the report at ${reportUrl}";
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
            throw CpdUncheckedException.throwAsUncheckedException(e);
        }
    }

        /**
     * Configures the reports to be generated by this task.
     */
    @Override
    CpdReports reports(Closure closure) {
        return reports.configure(closure)
    }

    /**
     * Returns the reports to be generated by this task.
     */
    @Override
    CpdReports getReports() {
        return reports
    }
}
