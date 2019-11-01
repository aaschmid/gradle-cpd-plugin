package de.aaschmid.gradle.plugins.cpd.internal.worker;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;

import net.sourceforge.pmd.cpd.CPD;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.Match;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

class CpdExecutor {

    private static final Logger logger = Logging.getLogger(CpdExecutor.class);

    List<Match> run(CPDConfiguration cpdConfig, Set<File> sourceFiles) {
        if (logger.isInfoEnabled()) {
            logger.info("Starting CPD, minimumTokenCount is {}", cpdConfig.getMinimumTileSize());
        }
        try {
            CPD cpd = new CPD(cpdConfig);
            tokenizeSourceFiles(cpd, sourceFiles);
            analyzeSourceCode(cpd);
            return stream(spliteratorUnknownSize(cpd.getMatches(), Spliterator.ORDERED), false).collect(toList());
        } catch (IOException e) {
            throw new GradleException("Exception during CPD execution: " + e.getMessage(), e);
        } catch (Throwable t) {
            throw new GradleException(t.getMessage(), t);
        }
    }

    private void tokenizeSourceFiles(CPD cpd, Set<File> sourceFiles) throws IOException {
        for (File file : sourceFiles) {
            if (logger.isDebugEnabled()) {
                logger.debug("Tokenize {}", file.getAbsolutePath());
            }
            cpd.add(file);
        }
    }

    private void analyzeSourceCode(CPD cpd) {
        if (logger.isInfoEnabled()) {
            logger.info("Starting to analyze code");
        }
        long start = System.currentTimeMillis();
        cpd.go();
        long stop = System.currentTimeMillis();

        long timeTaken = stop - start;
        if (logger.isInfoEnabled()) {
            logger.info("Successfully analyzed code - took {} milliseconds", timeTaken);
        }
    }
}
