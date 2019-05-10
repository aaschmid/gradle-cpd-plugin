package de.aaschmid.gradle.plugins.cpd.internal.worker;

import net.sourceforge.pmd.cpd.Match;
import org.gradle.api.GradleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class CpdAction implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CpdAction.class);

    private final CpdExecutionConfiguration executionConfig;
    private List<CpdReportConfiguration> reportConfigs;

    @Inject
    public CpdAction(CpdExecutionConfiguration executionConfig, List<CpdReportConfiguration> reportConfigs) {
        this.executionConfig = executionConfig;
        this.reportConfigs = reportConfigs;
    }

    @Override
    public void run() {
        CpdExecutor executor = new CpdExecutor(executionConfig);
        CpdReporter reporter = new CpdReporter(reportConfigs);

        List<Match> matches = executor.run();
        reporter.generate(matches);
        logResult(matches);
    }

    private void logResult(List<Match> matches) {
        if (matches.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("No duplicates over {} tokens found.", executionConfig.getMinimumTokenCount());
            }
        } else {
            String message = "CPD found duplicate code.";
            CpdReportConfiguration report = reportConfigs.get(0);
            if (report != null) {
                File reportUrl = report.getDestination();
                message += " See the report at " + asClickableFileUrl(reportUrl);
            }
            if (executionConfig.isIgnoreFailures()) {
                if (log.isWarnEnabled()) {
                    log.warn(message);
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
}
