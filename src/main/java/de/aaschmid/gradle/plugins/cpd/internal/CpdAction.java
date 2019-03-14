package de.aaschmid.gradle.plugins.cpd.internal;

import net.sourceforge.pmd.cpd.Match;
import org.gradle.api.GradleException;
import org.gradle.api.reporting.SingleFileReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class CpdAction implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CpdAction.class);

    private final String encoding;
    private final int minimumTokenCount;
    private final String language;
    private final boolean skipLexicalErrors;
    private final boolean skipDuplicateFiles;
    private final Collection<File> sourceFiles;
    private final List<SingleFileReport>  reports;
    private final boolean ignoreLiterals;
    private final boolean ignoreIdentifiers;
    private final boolean ignoreAnnotations;
    private final boolean skipBlocks;
    private final String skipBlocksPattern;
    private final boolean ignoreFailures;

    public CpdAction(String encoding, int minimumTokenCount, String language, boolean skipLexicalErrors, boolean skipDuplicateFiles, Collection<File> sourceFiles, List<SingleFileReport> reports, boolean ignoreFailures, boolean ignoreLiterals, boolean ignoreIdentifiers, boolean ignoreAnnotations, boolean skipBlocks, String skipBlocksPattern) {
        this.encoding = encoding;
        this.minimumTokenCount = minimumTokenCount;
        this.ignoreFailures = ignoreFailures;
        this.language = language;
        this.skipLexicalErrors = skipLexicalErrors;
        this.skipDuplicateFiles = skipDuplicateFiles;
        this.sourceFiles = sourceFiles;
        this.reports = reports;
        this.ignoreLiterals = ignoreLiterals;
        this.ignoreIdentifiers = ignoreIdentifiers;
        this.ignoreAnnotations = ignoreAnnotations;
        this.skipBlocks = skipBlocks;
        this.skipBlocksPattern = skipBlocksPattern;
    }

    @Override
    public void run() {
        CpdExecutor executor = new CpdExecutor(this);
        CpdReporter reporter = new CpdReporter(this);

        List<Match> matches = executor.run();
        reporter.generate(matches);
        logResult(matches);
    }

    private void logResult(List<Match> matches) {
        if (matches.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("No duplicates over {} tokens found.", minimumTokenCount);
            }

        }
        else {
            String message = "CPD found duplicate code.";
            SingleFileReport report = reports.get(0);
            if (report != null) {
                String reportUrl = report.getDestination().toString();
                message += " See the report at " + reportUrl;
            }
            if (ignoreFailures) {
                if (log.isWarnEnabled()) {
                    log.warn(message);
                }
            }
            else {
                throw new GradleException(message);
            }
        }
    }

    public String getEncoding() {
        return encoding;
    }

    public int getMinimumTokenCount() {
        return minimumTokenCount;
    }

    public String getLanguage() {
        return language;
    }

    public boolean getSkipLexicalErrors() {
        return skipLexicalErrors;
    }

    public boolean getSkipDuplicateFiles() {
        return skipDuplicateFiles;
    }

    public Collection<File> getSourceFiles() {
        return sourceFiles;
    }

    public List<SingleFileReport>  getReports() {
        return reports;
    }

    public boolean getIgnoreLiterals() {
        return ignoreLiterals;
    }

    public boolean getIgnoreIdentifiers() {
        return ignoreIdentifiers;
    }

    public boolean getIgnoreAnnotations() {
        return ignoreAnnotations;
    }

    public boolean getSkipBlocks() {
        return skipBlocks;
    }

    public String getSkipBlocksPattern() {
        return skipBlocksPattern;
    }

    public boolean getIgnoreFailures() {
        return ignoreFailures;
    }
}
