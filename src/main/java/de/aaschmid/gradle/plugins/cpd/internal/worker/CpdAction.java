package de.aaschmid.gradle.plugins.cpd.internal.worker;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import net.sourceforge.pmd.cpd.AnyLanguage;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.Language;
import net.sourceforge.pmd.cpd.LanguageFactory;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.Tokenizer;
import org.gradle.api.GradleException;
import org.gradle.workers.WorkAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CpdAction implements WorkAction<CpdWorkParameters> {

    private static final Logger logger = LoggerFactory.getLogger(CpdAction.class);

    private final CpdExecutor executor;
    private final CpdReporter reporter;

    public CpdAction() {
        executor = new CpdExecutor(createCpdConfiguration(getParameters()), getParameters().getSourceFiles().getFiles());
        reporter = new CpdReporter(getParameters().getReportParameters().get());
    }

    // Visible for testing
    CpdAction(CpdExecutor executor, CpdReporter reporter) {
        this.executor = executor;
        this.reporter = reporter;
    }

    @Override
    public void execute() {
        CpdExecutor executor = new CpdExecutor(createCpdConfiguration(getParameters()), getParameters().getSourceFiles().getFiles());
        CpdReporter reporter = new CpdReporter(getParameters().getReportParameters().get());

        List<Match> matches = executor.run();
        reporter.generate(matches);
        logResult(matches);
    }

    private CPDConfiguration createCpdConfiguration(CpdWorkParameters config) {
        CPDConfiguration result = new CPDConfiguration();
        result.setEncoding(config.getEncoding().get());
        result.setLanguage(createLanguage(config.getLanguage().get(), createLanguageProperties(config)));
        result.setMinimumTileSize(config.getMinimumTokenCount().get());
        result.setSkipDuplicates(config.getSkipDuplicateFiles().get());
        result.setSkipLexicalErrors(config.getSkipLexicalErrors().get());
        return result;
    }

    private void logResult(List<Match> matches) {
        if (matches.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("No duplicates over {} tokens found.", getParameters().getMinimumTokenCount().get());
            }
        } else {
            String message = "CPD found duplicate code.";
            CpdReportParameters report = getParameters().getReportParameters().get().get(0);
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

    private Properties createLanguageProperties(CpdWorkParameters config) {
        Properties languageProperties = new Properties();

        if (config.getIgnoreAnnotations().get()) {
            languageProperties.setProperty(Tokenizer.IGNORE_ANNOTATIONS, "true");
        }
        if (config.getIgnoreIdentifiers().get()) {
            languageProperties.setProperty(Tokenizer.IGNORE_IDENTIFIERS, "true");
        }
        if (config.getIgnoreLiterals().get()) {
            languageProperties.setProperty(Tokenizer.IGNORE_LITERALS, "true");
        }
        languageProperties.setProperty(Tokenizer.OPTION_SKIP_BLOCKS, Boolean.toString(config.getSkipBlocks().get()));
        languageProperties.setProperty(Tokenizer.OPTION_SKIP_BLOCKS_PATTERN, config.getSkipBlocksPattern().get());
        return languageProperties;
    }

    private Language createLanguage(String language, Properties languageProperties) {
        ClassLoader previousContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Workaround for https://github.com/pmd/pmd/issues/1788 as Gradle Worker API uses special classloader internally
            Thread.currentThread().setContextClassLoader(CpdExecutor.class.getClassLoader());

            Language result = LanguageFactory.createLanguage(language, languageProperties);
            logger.info("Using CPD language class '{}' for checking duplicates.", result);
            if (result instanceof AnyLanguage) {
                logger.warn("Could not detect CPD language for '{}', using 'any' as fallback language.", language);
            }
            return result;
        } finally {
            Thread.currentThread().setContextClassLoader(previousContextClassLoader);
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
