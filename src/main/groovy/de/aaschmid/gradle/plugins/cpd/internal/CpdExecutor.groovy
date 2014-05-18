package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.Cpd
import net.sourceforge.pmd.cpd.CPD
import net.sourceforge.pmd.cpd.CPDConfiguration
import net.sourceforge.pmd.cpd.LanguageFactory
import net.sourceforge.pmd.cpd.Match
import net.sourceforge.pmd.cpd.ReportException
import org.apache.tools.ant.BuildException
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

public class CpdExecutor {

    private static final Logger logger = Logging.getLogger(CpdExecutor.class);

    private final Cpd task;

    public CpdExecutor(Cpd task) {
        if (task == null) {
            throw new NullPointerException("task must not be null");
        }
        this.task = task;
    }

    public void canRun() {
        if (task.getMinimumTokenCount() <= 0) {
            throw new GradleException("'minimumTokenCount' must be greater than zero.");
        }
    }

    public List<Match> run() {
        Properties p = new Properties();
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Starting CPD, minimumTokenCount is {}", task.getMinimumTokenCount());
            }
            def language = new LanguageFactory().createLanguage("java", p);
            def config = new CPDConfiguration(task.getMinimumTokenCount(), language, task.getEncoding());
            def cpd = new CPD(config);

            if (logger.isInfoEnabled()) {
                logger.info("Tokenizing files");
            }
            for (File file : task.getSource()) {
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
            throw new BuildException("IOException during task execution", e);
        } catch (ReportException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.toString(), e);
            }
            throw new BuildException("ReportException during task execution", e);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return [];
    }
}
