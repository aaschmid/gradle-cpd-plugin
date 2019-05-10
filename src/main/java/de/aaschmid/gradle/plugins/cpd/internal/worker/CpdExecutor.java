package de.aaschmid.gradle.plugins.cpd.internal.worker;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pmd.cpd.AnyLanguage;
import net.sourceforge.pmd.cpd.CPD;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.Language;
import net.sourceforge.pmd.cpd.LanguageFactory;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.Tokenizer;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Spliterator;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Slf4j
class CpdExecutor {

    private final CPDConfiguration cpdConfig;
    private final Collection<File> sourceFiles;

    CpdExecutor(CpdExecutionConfiguration config) {
        this.cpdConfig = new CPDConfiguration();
        cpdConfig.setEncoding(config.getEncoding());
        cpdConfig.setLanguage(createLanguage(config.getLanguage(), createLanguageProperties(config)));
        cpdConfig.setMinimumTileSize(config.getMinimumTokenCount());
        cpdConfig.setSkipDuplicates(config.isSkipDuplicateFiles());
        cpdConfig.setSkipLexicalErrors(config.isSkipLexicalErrors());

        this.sourceFiles = config.getSourceFiles();
    }

    private Properties createLanguageProperties(CpdExecutionConfiguration config) {
        Properties languageProperties = new Properties();

        if (config.isIgnoreAnnotations()) {
            languageProperties.setProperty(Tokenizer.IGNORE_ANNOTATIONS, "true");
        }
        if (config.isIgnoreIdentifiers()) {
            languageProperties.setProperty(Tokenizer.IGNORE_IDENTIFIERS, "true");
        }
        if (config.isIgnoreLiterals()) {
            languageProperties.setProperty(Tokenizer.IGNORE_LITERALS, "true");
        }
        languageProperties.setProperty(Tokenizer.OPTION_SKIP_BLOCKS, Boolean.toString(config.isSkipBlocks()));
        languageProperties.setProperty(Tokenizer.OPTION_SKIP_BLOCKS_PATTERN, config.getSkipBlocksPattern());
        return languageProperties;
    }

    private Language createLanguage(String language, Properties languageProperties) {
        ClassLoader previousContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Workaround for https://github.com/pmd/pmd/issues/1788 as Gradle Worker API uses special classloader internally
            Thread.currentThread().setContextClassLoader(CpdExecutor.class.getClassLoader());

            Language result = LanguageFactory.createLanguage(language, languageProperties);
            log.info("Using CPD language class '{}' for checking duplicates.", result);
            if (result instanceof AnyLanguage) {
                log.warn("Could not detect CPD language for '{}', using 'any' as fallback language.", language);
            }
            return result;
        } finally {
            Thread.currentThread().setContextClassLoader(previousContextClassLoader);
        }
    }

    List<Match> run() {
        if (log.isInfoEnabled()) {
            log.info("Starting CPD, minimumTokenCount is {}", cpdConfig.getMinimumTileSize());
        }
        try {
            CPD cpd = new CPD(cpdConfig);
            tokenizeSourceFiles(cpd);
            analyzeSourceCode(cpd);
            return stream(spliteratorUnknownSize(cpd.getMatches(), Spliterator.ORDERED), false).collect(toList());
        } catch (IOException e) {
            throw new GradleException("Exception during CPD execution: " + e.getMessage(), e);
        } catch (Throwable t) {
            throw new GradleException(t.getMessage(), t);
        }
    }

    private void tokenizeSourceFiles(CPD cpd) throws IOException {
        for (File file : sourceFiles) {
            if (log.isDebugEnabled()) {
                log.debug("Tokenize {}", file.getAbsolutePath());
            }
            cpd.add(file);
        }
    }

    private void analyzeSourceCode(CPD cpd) {
        if (log.isInfoEnabled()) {
            log.info("Starting to analyze code");
        }
        long start = System.currentTimeMillis();
        cpd.go();
        long stop = System.currentTimeMillis();

        long timeTaken = stop - start;
        if (log.isInfoEnabled()) {
            log.info("Successfully analyzed code - took {} milliseconds", timeTaken);
        }
    }
}
