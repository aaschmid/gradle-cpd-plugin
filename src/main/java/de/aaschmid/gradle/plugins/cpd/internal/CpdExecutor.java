package de.aaschmid.gradle.plugins.cpd.internal;

import net.sourceforge.pmd.cpd.AnyLanguage;
import net.sourceforge.pmd.cpd.CPD;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.Language;
import net.sourceforge.pmd.cpd.LanguageFactory;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.Tokenizer;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class CpdExecutor {

    private static final Logger logger = Logging.getLogger(CpdExecutor.class);

    private final String encoding;
    private final Language language;
    private final int minimumTokenCount;
    private final boolean skipDuplicateFiles;
    private final boolean skipLexicalErrors;
    private final Collection<File> source;

    public CpdExecutor(CpdAction action) {
        if (action == null) {
            throw new NullPointerException("action must not be null");
        }

        if (action.getMinimumTokenCount() <= 0) {
            throw new InvalidUserDataException("'minimumTokenCount' must be greater than zero.");
        }

        this.encoding = action.getEncoding();
        this.language = createLanguage(action);
        this.minimumTokenCount = action.getMinimumTokenCount();
        this.skipLexicalErrors = action.getSkipLexicalErrors();
        this.skipDuplicateFiles = action.getSkipDuplicateFiles();
        this.source = action.getSourceFiles();
    }

    public List<Match> run() {
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Starting CPD, minimumTokenCount is {}", minimumTokenCount);
            }

            CPDConfiguration cpdConfig = new CPDConfiguration();
            cpdConfig.setMinimumTileSize(minimumTokenCount);
            cpdConfig.setLanguage(language);
            cpdConfig.setEncoding(encoding);
            cpdConfig.setSkipLexicalErrors(skipLexicalErrors);
            cpdConfig.setSkipDuplicates(skipDuplicateFiles);

            CPD cpd = new CPD(cpdConfig);

            if (logger.isInfoEnabled()) {
                logger.info("Tokenizing files");
            }
            for (File file : source) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Tokenizing {}", file.getAbsolutePath());
                }
                cpd.add(file);
            }

            if (logger.isInfoEnabled()) {
                logger.info("Starting to analyze code");
            }
            long start = System.currentTimeMillis();
            cpd.go();
            long stop = System.currentTimeMillis();
            long timeTaken = stop - start;
            if (logger.isInfoEnabled()) {
                logger.info("Done analyzing code; took {} milliseconds", timeTaken);
            }
            List<Match> result = new ArrayList<>();
            for (Iterator<Match> it = cpd.getMatches(); it.hasNext(); ) {
                result.add(it.next());
            }
            return result;

        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
            throw new GradleException("IOException during task execution", e);
        } catch (Throwable t) {
            throw new GradleException(t.getMessage(), t);
        }
    }

    private Language createLanguage(CpdAction action) {
        Properties languageProperties = new Properties();

        if (action.getIgnoreLiterals()) {
            languageProperties.setProperty(Tokenizer.IGNORE_LITERALS, "true");
        }
        if (action.getIgnoreIdentifiers()) {
            languageProperties.setProperty(Tokenizer.IGNORE_IDENTIFIERS, "true");
        }
        if (action.getIgnoreAnnotations()) {
            languageProperties.setProperty(Tokenizer.IGNORE_ANNOTATIONS, "true");
        }
        languageProperties.setProperty(Tokenizer.OPTION_SKIP_BLOCKS, Boolean.toString(action.getSkipBlocks()));
        languageProperties.setProperty(Tokenizer.OPTION_SKIP_BLOCKS_PATTERN, action.getSkipBlocksPattern());

        Language result = LanguageFactory.createLanguage(action.getLanguage(), languageProperties);
        logger.info("Using CPD language class '{}' for checking duplicates.", result);
        if (result instanceof AnyLanguage) {
            logger.warn("Could not detect CPD language for '{}', using 'any' as fallback language.", action.getLanguage());
        }
        return result;
    }
}
