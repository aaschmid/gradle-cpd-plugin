package de.aaschmid.gradle.plugins.cpd.internal.worker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.CPDReport;
import net.sourceforge.pmd.cpd.CpdAnalysis;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

class CpdExecutor {

    private static final Logger logger = Logging.getLogger(CpdExecutor.class);

    CPDReport run(CPDConfiguration cpdConfig, Set<File> sourceFiles) {
        if (logger.isInfoEnabled()) {
            logger.info("Starting CPD, minimumTokenCount is {}", cpdConfig.getMinimumTileSize());
        }

        try (CpdAnalysis cpd = CpdAnalysis.create(cpdConfig)) {
            tokenizeSourceFiles(cpd, sourceFiles);
            return analyzeSourceCode(cpd)
                    .orElseThrow(() -> new GradleException("Analysis did not produce any result"));
        } catch (IOException e) {
            throw new GradleException("Exception during CPD execution: " + e.getMessage(), e);
        } catch (Throwable t) {
            throw new GradleException(t.getMessage(), t);
        }
    }

    private void tokenizeSourceFiles(CpdAnalysis cpd, Set<File> sourceFiles) throws IOException {
        for (File file : sourceFiles) {
            if (logger.isDebugEnabled()) {
                logger.debug("Tokenize {}", file.getAbsolutePath());
            }
            cpd.files().addFile(file.toPath());
        }
    }

    private Optional<CPDReport> analyzeSourceCode(CpdAnalysis cpd) {
        if (logger.isInfoEnabled()) {
            logger.info("Starting to analyze code");
        }
        List<CPDReport> cpdReports = new ArrayList<>();
        long start = System.currentTimeMillis();
        cpd.performAnalysis(cpdReports::add);
        long stop = System.currentTimeMillis();

        long timeTaken = stop - start;
        if (logger.isInfoEnabled()) {
            logger.info("Successfully analyzed code - took {} milliseconds", timeTaken);
        }
        if (cpdReports.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(cpdReports.get(0));
    }
}
