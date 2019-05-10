package de.aaschmid.gradle.plugins.cpd;

import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdAction;
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdExecutionConfiguration;
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReportConfiguration;
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReportConfiguration.CpdCsvReport;
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReportConfiguration.CpdTextReport;
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReportConfiguration.CpdXmlReport;
import de.aaschmid.gradle.plugins.cpd.internal.CpdReportsImpl;
import groovy.lang.Closure;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.VerificationTask;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.util.DeprecationLogger;
import org.gradle.workers.WorkerConfiguration;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.util.ArrayList;
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
@Getter
@Setter
public class Cpd extends SourceTask implements VerificationTask, Reporting<CpdReports> {

    @Internal
    private final WorkerExecutor workerExecutor;

    @Nested
    private final CpdReportsImpl reports;

    @Inject
    public Cpd(Instantiator instantiator, WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor;
        this.reports = DeprecationLogger.whileDisabled(() -> instantiator.newInstance(CpdReportsImpl.class, this));
    }

    @TaskAction
    public void run() {
        checkTaskState();

        workerExecutor.submit(CpdAction.class, (WorkerConfiguration config) -> {
            config.setClasspath(getPmdClasspath());
            config.setDisplayName("CPD worker");
            config.setParams(createCpdExecutionConfiguration(), createCpdReportConfigurations());
        });
    }

    private void checkTaskState() {
        if (getEncoding() == null) {
            throw new InvalidUserDataException(String.format("Task '%s' requires 'encoding' but was: %s.", getName(), getEncoding()));
        }
        if (getMinimumTokenCount() <= 0) {
            throw new InvalidUserDataException(String.format("Task '%s' requires 'minimumTokenCount' to be greater than zero.", getName()));
        }
        if (reports.getEnabled().isEmpty()) {
            throw new InvalidUserDataException(String.format("Task '%s' requires at least one enabled report.", getName()));
        }
    }

    private CpdExecutionConfiguration createCpdExecutionConfiguration() {
        return new CpdExecutionConfiguration(
                getEncoding(),
                isIgnoreAnnotations(),
                getIgnoreFailures(),
                isIgnoreIdentifiers(),
                isIgnoreLiterals(),
                getLanguage(),
                getMinimumTokenCount(),
                isSkipBlocks(),
                getSkipBlocksPattern(),
                isSkipDuplicateFiles(),
                isSkipLexicalErrors(),
                getSource().getFiles()
        );
    }

    private List<CpdReportConfiguration> createCpdReportConfigurations() {
        List<CpdReportConfiguration> result = new ArrayList<>();
        for (Report report : getReports()) {
            if (!report.isEnabled()) {
                continue;
            }

            if (report instanceof CpdCsvFileReport) {
                Character separator = ((CpdCsvFileReport) report).getSeparator();
                result.add(new CpdCsvReport(getEncoding(), report.getDestination(), separator));

            } else if (report instanceof CpdTextFileReport) {
                String lineSeparator = ((CpdTextFileReport) report).getLineSeparator();
                boolean trimLeadingCommonSourceWhitespaces = ((CpdTextFileReport) report).isTrimLeadingCommonSourceWhitespaces();
                result.add(new CpdTextReport(getEncoding(), report.getDestination(), lineSeparator, trimLeadingCommonSourceWhitespaces));

            } else if (report instanceof CpdXmlFileReport) {
                String encoding = getXmlRendererEncoding((CpdXmlFileReport) report);
                result.add(new CpdXmlReport(encoding, report.getDestination()));

            } else {
                throw new IllegalArgumentException(String.format("Report of type '%s' not available.", report.getClass().getSimpleName()));
            }
        }
        return result;
    }

    // VisibleForTesting
    String getXmlRendererEncoding(CpdXmlFileReport report) {
        String encoding = report.getEncoding();
        if (encoding == null) {
            encoding = getEncoding();
        }
        if (encoding == null) {
            encoding = System.getProperty("file.encoding");
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
    private String encoding;

    /**
     * Ignore annotations because more and more modern frameworks use annotations on classes and methods which can be very
     * redundant and causes false positives.
     * <p>
     * Example: {@code ignoreAnnotations = true}
     *
     * @return if annotations should be ignored
     */
    @Input
    private boolean ignoreAnnotations;

    /**
     * Whether or not to allow the build to continue if there are warnings.
     * <p>
     * Example: {@code ignoreFailures = true}
     * {@inheritDoc}
     */
    @Input
    private boolean ignoreFailures;

    @Override
    public boolean getIgnoreFailures() {
        return ignoreFailures;
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
    private boolean ignoreIdentifiers;

    /**
     * Option if CPD should ignore literal value differences when evaluating a duplicate block. This means e.g. that
     * {@code foo=42;} and {@code foo=43;} will be seen as equivalent.
     * <p>
     * Example: {@code ignoreLiterals = true}
     *
     * @return whether literals should be ignored
     */
    @Input
    private boolean ignoreLiterals;

    /**
     * Flag to select the appropriate language.
     * <p>
     * Example: {@code language = 'java'}
     *
     * @return used language
     */
    @Input
    private String language;

    /**
     * A positive integer indicating the minimum token count to trigger a CPD match; defaults to
     * {@link CpdExtension#getMinimumTokenCount()}.
     * <p>
     * Example: {@code minimumTokenCount = 25}
     *
     * @return the minimum token cound
     */
    @Input
    private int minimumTokenCount;

    /**
     * The classpath containing the PMD library which contains the CPD library to be used.
     *
     * @return CPD libraries classpath
     */
    @InputFiles
    @PathSensitive(PathSensitivity.NAME_ONLY)
    private FileCollection pmdClasspath;

    /**
     * Ignore multiple copies of files of the same name and length in comparison.
     * <p>
     * Example: {@code skipDuplicateFiles = true}
     *
     * @return whether duplicate files should be skipped
     */
    @Input
    private boolean skipDuplicateFiles;

    /**
     * Skip files which cannot be tokenized due to invalid characters instead of aborting CPD.
     * <p>
     * Example: {@code skipLexicalErrors = true}
     *
     * @return whether lexical errors should be skipped
     */
    @Input
    private boolean skipLexicalErrors;

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
    private boolean skipBlocks;

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
    private String skipBlocksPattern;
}
