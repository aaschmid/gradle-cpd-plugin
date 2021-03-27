package de.aaschmid.gradle.plugins.cpd;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.aaschmid.gradle.plugins.cpd.internal.CpdReportsImpl;
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdAction;
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdWorkParameters;
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdWorkParameters.Report;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.VerificationTask;
import org.gradle.workers.WorkerExecutor;


/**
 * Runs static code/paste (= duplication) detection on supplied source code files and generates a report of duplications found.
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

    private final ProviderFactory providerFactory;
    private final WorkerExecutor workerExecutor;
    private final CpdReports reports;

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
    public Cpd(ObjectFactory objectFactory, ProviderFactory providerFactory, WorkerExecutor workerExecutor) {
        this.providerFactory = providerFactory;
        this.reports = objectFactory.newInstance(CpdReportsImpl.class, this);
        this.workerExecutor = workerExecutor;
    }

    @TaskAction
    public void run() {
        checkTaskState();

        workerExecutor
                .classLoaderIsolation(action -> action.getClasspath().setFrom(getPmdClasspath()))
                .submit(CpdAction.class, getCpdWorkParameters());
    }

    private void checkTaskState() {
        if (getMinimumTokenCount() <= 0) {
            throw new InvalidUserDataException(String.format("Task '%s' requires 'minimumTokenCount' to be greater than zero.", getName()));
        }
        if (getReports().getEnabled().isEmpty()) {
            throw new InvalidUserDataException(String.format("Task '%s' requires at least one enabled report.", getName()));
        }
    }

    private Action<CpdWorkParameters> getCpdWorkParameters() {
        return parameters -> {
            parameters.getEncoding().set(getEncodingOrFallback());
            parameters.getIgnoreAnnotations().set(getIgnoreAnnotations());
            parameters.getIgnoreFailures().set(getIgnoreFailures());
            parameters.getIgnoreIdentifiers().set(getIgnoreIdentifiers());
            parameters.getIgnoreLiterals().set(getIgnoreLiterals());
            parameters.getLanguage().set(getLanguage());
            parameters.getMinimumTokenCount().set(getMinimumTokenCount());
            parameters.getSkipBlocks().set(getSkipBlocks());
            parameters.getSkipBlocksPattern().set(getSkipBlocksPattern());
            parameters.getSkipDuplicateFiles().set(getSkipDuplicateFiles());
            parameters.getSkipLexicalErrors().set(getSkipLexicalErrors());
            parameters.getSourceFiles().setFrom(getSource().getFiles());
            parameters.getReportParameters().set(createReportParameters(getReports()));
        };
    }

    private List<Report> createReportParameters(CpdReports reports) {
        List<Report> result = new ArrayList<>();
        for (SingleFileReport report : reports) {
            if (!report.isEnabled()) {
                continue;
            }

            if (report instanceof CpdCsvFileReport) {
                Character separator = ((CpdCsvFileReport) report).getSeparator();
                boolean includeLineCount = ((CpdCsvFileReport) report).isIncludeLineCount();
                result.add(new Report.Csv(report.getDestination(), separator, includeLineCount));

            } else if (report instanceof CpdTextFileReport) {
                String lineSeparator = ((CpdTextFileReport) report).getLineSeparator();
                boolean trimLeadingCommonSourceWhitespaces = ((CpdTextFileReport) report).getTrimLeadingCommonSourceWhitespaces();
                result.add(new Report.Text(report.getDestination(), lineSeparator, trimLeadingCommonSourceWhitespaces));

            } else if (report.getName().equals("vs")) {
                result.add(new Report.Vs(report.getDestination()));

            } else if (report instanceof CpdXmlFileReport) {
                String encoding = getXmlRendererEncoding((CpdXmlFileReport) report);
                result.add(new Report.Xml(report.getDestination(), encoding));

            } else {
                throw new IllegalArgumentException(String.format("Report of type '%s' not available.", report.getClass().getSimpleName()));
            }
        }
        return result;
    }

    // VisibleForTesting
    @Internal
    String getEncodingOrFallback() {
        String encoding = getEncoding();
        if (encoding == null) {
            encoding = providerFactory.systemProperty("file.encoding").forUseAtConfigurationTime().get();
        }
        return encoding;
    }

    // VisibleForTesting
    String getXmlRendererEncoding(CpdXmlFileReport report) {
        String encoding = report.getEncoding();
        if (encoding == null) {
            return getEncodingOrFallback();
        }
        return encoding;
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

    @Override
    public CpdReports reports(Action<? super CpdReports> action) {
        action.execute(this.reports);
        return this.reports;
    }


    /**
     * The character set encoding (e.g., UTF-8) to use when reading the source code files but also when producing the report; defaults to
     * {@link CpdExtension#getEncoding()}.
     * <p>
     * Example: {@code encoding = UTF-8}
     *
     * @return the charset encoding
     */
    @Input
    @Optional
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Ignore annotations because more and more modern frameworks use annotations on classes and methods which can be very redundant and
     * causes false positives.
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
     * Example: {@code ignoreFailures = true} {@inheritDoc}
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
     * Option if CPD should ignore identifiers differences, i.e. variable names, methods names, and so forth, when evaluating a duplicate
     * block.
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
     * Option if CPD should ignore literal value differences when evaluating a duplicate block. This means e.g. that {@code foo=42;} and
     * {@code foo=43;} will be seen as equivalent.
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
     * A positive integer indicating the minimum token count to trigger a CPD match; defaults to {@link
     * CpdExtension#getMinimumTokenCount()}.
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
    @Classpath
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
     * @return whether blocks should be skipped by a given pattern
     * @see #skipBlocksPattern
     */
    @Input
    public boolean getSkipBlocks() {
        return skipBlocks;
    }

    public void setSkipBlocks(boolean skipBlocks) {
        this.skipBlocks = skipBlocks;
    }

    /**
     * Configures the pattern, to find the blocks to skip if enabled using {@link #skipBlocks}. It is a {@link String} property and contains
     * of two parts, separated by {@code '|'}. The first part is the start pattern, the second part is the ending pattern.
     * <p>
     * Example: {@code skipBlocksPattern = '#include <|>'}
     *
     * @return the pattern used to skip blocks
     * @see #skipBlocks
     */
    @Input
    public String getSkipBlocksPattern() {
        return skipBlocksPattern;
    }

    public void setSkipBlocksPattern(String skipBlocksPattern) {
        this.skipBlocksPattern = skipBlocksPattern;
    }
}
