package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.Cpd
import de.aaschmid.gradle.plugins.cpd.CpdFileReport
import net.sourceforge.pmd.cpd.FileReporter
import net.sourceforge.pmd.cpd.Match
import net.sourceforge.pmd.cpd.Renderer
import net.sourceforge.pmd.cpd.ReportException
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport

public class CpdReporter {

    private static final Logger logger = Logging.getLogger(CpdReporter.class);

    private final Cpd task;

    public CpdReporter(Cpd task) {
        if (task == null) {
            throw new NullPointerException("task must not be null");
        }
        this.task = task;
    }

    public void canGenerate() {
        if (task.getEncoding() == null) {
            throw new InvalidUserDataException(
                    "Task '${task.name}' requires encoding but was: ${task.getEncoding()}.");
        }

        CpdReportsImpl reports = task.reports;
        if (reports.getEnabled().isEmpty() || reports.getEnabled().size() > 1) {
            throw new InvalidUserDataException(
                    "Task '${task.name}' requires exactly one report to be enabled but was: ${reports.enabled*.name}.");
        }
        if (reports.getFirstEnabled().getDestination() == null) {
            throw new InvalidUserDataException("'${reports.firstEnabled}' requires valid destination but was 'null'.");
        }
    }

    public void generate(List<Match> matches) {

        if (logger.isInfoEnabled()) {
            logger.info("Generating report");
        }

        TaskGeneratedSingleFileReport report = (TaskGeneratedSingleFileReport) task.reports.getFirstEnabled();

        FileReporter reporter = new FileReporter(report.getDestination(), task.getEncoding());
        Renderer renderer;
        if (report instanceof CpdFileReport) {
            renderer = ((CpdFileReport) report).createRenderer();
        } else {
            throw new GradleException("Tried to create report for unsupported class ${report.class.canonicalName}");
        }
        String renderedMatches = renderer.render(matches.iterator());

        try {
            reporter.report(renderedMatches);

        } catch (ReportException e) {
            throw new GradleException(e.getMessage(), e);
        }
    }
}
