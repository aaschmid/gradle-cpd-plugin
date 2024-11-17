package de.aaschmid.gradle.plugins.cpd.internal.worker;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import javax.inject.Inject;

import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdWorkParameters.Report;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.CPDReport;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.lang.LanguageRegistry;
import org.gradle.api.GradleException;
import org.gradle.workers.WorkAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CpdAction implements WorkAction<CpdWorkParameters> {

    private static final Logger logger = LoggerFactory.getLogger(CpdAction.class);

    private final CpdExecutor executor;
    private final CpdReporter reporter;

    @Inject
    public CpdAction() {
        executor = new CpdExecutor();
        reporter = new CpdReporter();
    }

    // Visible for testing
    CpdAction(CpdExecutor executor, CpdReporter reporter) {
        this.executor = executor;
        this.reporter = reporter;
    }

    @Override
    public void execute() {
        CPDReport cpdReport = executor.run(createCpdConfiguration(getParameters()), getParameters().getSourceFiles().getFiles());
        reporter.generate(getParameters().getReportParameters().get(), cpdReport);
        logResult(cpdReport.getMatches());
    }

    private CPDConfiguration createCpdConfiguration(CpdWorkParameters config) {
        CPDConfiguration result = new CPDConfiguration();
        result.setSourceEncoding(getEncoding(config));
        result.setFailOnError(config.getFailOnError().get());
        result.setFailOnViolation(config.getFailOnViolation().get());
        result.setIgnoreAnnotations(config.getIgnoreAnnotations().get());
        result.setIgnoreIdentifiers(config.getIgnoreIdentifiers().get());
        result.setIgnoreLiterals(config.getIgnoreLiterals().get());
        result.setNoSkipBlocks(!config.getSkipBlocks().get());
        result.setSkipBlocksPattern(config.getSkipBlocksPattern().get());
        result.setOnlyRecognizeLanguage(createLanguage(config.getLanguage().get()));
        result.setMinimumTileSize(config.getMinimumTokenCount().get());
        result.setSkipDuplicates(config.getSkipDuplicateFiles().get());
        return result;
    }

    private void logResult(List<Match> matches) {
        if (matches.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("No duplicates over {} tokens found.", getParameters().getMinimumTokenCount().get());
            }
        } else {
            String message = "CPD found duplicate code.";
            Report report = getParameters().getReportParameters().get().get(0);
            if (report != null) {
                File reportUrl = report.getDestination();
                message += " See the report at " + asClickableFileUrl(reportUrl);
            }
            if (getParameters().getIgnoreFailures().get()) {
                if (logger.isWarnEnabled()) {
                    logger.warn(message);
                }
            } else {
                throw new GradleException(message);
            }
        }
    }

    private Language createLanguage(String language) {
        Language result = LanguageRegistry.CPD.getLanguageById(language);
        if (result == null) {
            throw new GradleException(String.format("Could not detect CPD language for '%s'.", language));
        }
        logger.info("Using CPD language class '{}' for checking duplicates.", result);
        return result;
    }

    private String asClickableFileUrl(File path) {
        // File.toURI().toString() leads to an URL like this on Mac: file:/reports/index.html
        // This URL is not recognized by the Mac console (too few leading slashes). We solve
        // this be creating a URI with an empty authority.
        try {
            return new URI("file", "", path.toURI().getPath(), null, null).toString();
        } catch (URISyntaxException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    private Charset getEncoding(CpdWorkParameters config) {
        String encodingString = config.getEncoding().get();
        return Charset.forName(encodingString);
    }
}
