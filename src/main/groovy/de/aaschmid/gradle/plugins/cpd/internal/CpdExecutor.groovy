package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.Cpd
import net.sourceforge.pmd.cpd.CPD
import net.sourceforge.pmd.cpd.CPDConfiguration
import net.sourceforge.pmd.cpd.Language
import net.sourceforge.pmd.cpd.LanguageFactory
import net.sourceforge.pmd.cpd.Match
import net.sourceforge.pmd.cpd.ReportException
import net.sourceforge.pmd.cpd.Tokenizer
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

public class CpdExecutor {

    private static final Logger logger = Logging.getLogger(CpdExecutor.class);

    private final String encoding;
    private final Language language;
    private final int minimumTokenCount;
    private final boolean skipDuplicateFiles;
    private final boolean skipLexicalErrors;
    private final FileTree source;

    public CpdExecutor(Cpd task) {
        if (task == null) {
            throw new NullPointerException("task must not be null");
        }

        if (task.getMinimumTokenCount() <= 0) {
            throw new InvalidUserDataException("'minimumTokenCount' must be greater than zero.");
        }

        this.encoding = task.getEncoding();
        this.language = createLanguage(task);
        this.minimumTokenCount = task.getMinimumTokenCount();
        this.skipLexicalErrors = task.getSkipLexicalErrors();
        this.skipDuplicateFiles = task.getSkipDuplicateFiles();
        this.source = task.getSource();
    }

    public List<Match> run() {
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Starting CPD, minimumTokenCount is {}", minimumTokenCount);
            }

            def cpdConfig = new CPDConfiguration();
            cpdConfig.setMinimumTileSize(minimumTokenCount);
            cpdConfig.setLanguage(language);
            cpdConfig.setEncoding(encoding);
            cpdConfig.setSkipLexicalErrors(skipLexicalErrors);
            cpdConfig.setSkipDuplicates(skipDuplicateFiles);

            def cpd = new CPD(cpdConfig);

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
            return cpd.getMatches().toList();

        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
            throw new GradleException("IOException during task execution", e);
        } catch (ReportException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.toString(), e);
            }
            throw new GradleException("ReportException during task execution", e);
        } catch (Throwable t) {
            throw new GradleException(t.getMessage(), t);
        }
        return [];
    }

    private Language createLanguage(Cpd task) {
        Properties p = new Properties();
        if (task.getIgnoreLiterals()) {
            p.setProperty(Tokenizer.IGNORE_LITERALS, "true");
        }
        if (task.getIgnoreIdentifiers()) {
            p.setProperty(Tokenizer.IGNORE_IDENTIFIERS, "true");
        }
        if (task.getIgnoreAnnotations()) {
            p.setProperty(Tokenizer.IGNORE_ANNOTATIONS, "true");
        }
        p.setProperty(Tokenizer.OPTION_SKIP_BLOCKS, Boolean.toString(task.getSkipBlocks()));
        p.setProperty(Tokenizer.OPTION_SKIP_BLOCKS_PATTERN, task.getSkipBlocksPattern());
        return LanguageFactory.createLanguage(task.getLanguage(), p);
    }
}
