package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.Cpd
import net.sourceforge.pmd.cpd.CPD
import net.sourceforge.pmd.cpd.CPDConfiguration
import net.sourceforge.pmd.cpd.LanguageFactory
import net.sourceforge.pmd.cpd.Match
import net.sourceforge.pmd.cpd.ReportException
import org.apache.tools.ant.BuildException
import org.gradle.api.GradleException

public class CpdExecutor {

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
            if (task.logger.isInfoEnabled()) {
                task.logger.info("Starting CPD, minimumTokenCount is ${task.getMinimumTokenCount()}");
            }
            def language = new LanguageFactory().createLanguage("java", p);
            def config = new CPDConfiguration(task.getMinimumTokenCount(), language, task.getEncoding());
            def cpd = new CPD(config);

            if (task.logger.isInfoEnabled()) {
                task.logger.info("Tokenizing files");
            }
            for (File file : task.getSource()) {
                if (task.logger.isDebugEnabled()) {
                    task.logger.debug("Tokenizing ${file.getAbsolutePath()}");
                }
                cpd.add(file);
            }

            if (task.logger.isInfoEnabled()) {
                task.logger.info("Starting to analyze code");
            }
            long start = System.currentTimeMillis();
            cpd.go();
            long stop = System.currentTimeMillis();
            long timeTaken = stop - start;
            if (task.logger.isInfoEnabled()) {
                task.logger.info("Done analyzing code; took ${timeTaken} milliseconds");
            }
            return cpd.getMatches().toList();

        } catch (IOException ioe) {
            if (task.logger.isErrorEnabled()) {
                task.logger.error(ioe.getMessage(), ioe);
            }
            throw new BuildException("IOException during task execution", ioe);
        } catch (ReportException re) {
            if (task.logger.isErrorEnabled()) {
                task.logger.error(re.toString(), re);
            }
            throw new BuildException("ReportException during task execution", re);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return [];
    }
}
