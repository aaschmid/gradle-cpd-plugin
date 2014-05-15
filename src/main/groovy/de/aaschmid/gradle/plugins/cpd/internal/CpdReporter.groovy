package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.Cpd
import de.aaschmid.gradle.plugins.cpd.CpdCsvFileReport
import de.aaschmid.gradle.plugins.cpd.CpdTextFileReport
import net.sourceforge.pmd.cpd.CSVRenderer
import net.sourceforge.pmd.cpd.FileReporter
import net.sourceforge.pmd.cpd.Match
import net.sourceforge.pmd.cpd.Renderer
import net.sourceforge.pmd.cpd.ReportException
import net.sourceforge.pmd.cpd.SimpleRenderer
import net.sourceforge.pmd.cpd.XMLRenderer
import org.gradle.api.GradleException
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport

public class CpdReporter {

    private final Cpd task;

    public CpdReporter(Cpd task) {
        if (task == null) {
            throw new NullPointerException("task must not be null");
        }
        this.task = task;
    }

    public void canGenerate() {
        if (task.getEncoding() == null) {
            throw new GradleException(
                    "Task '${task.name}' requires encoding but was: ${task.getEncoding()}.");
        }

        CpdReportsImpl reports = task.reports;
        if (reports.getEnabled().isEmpty() || reports.getEnabled().size() > 1) {
            throw new GradleException(
                    "Task '${task.name}' requires exactly one report to be enabled but was: ${reports.enabled*.name}.");
        }
        if (reports.getFirstEnabled().getDestination() == null) {
            throw new GradleException("'${reports.firstEnabled}' requires valid destination but was 'null'.");
        }
    }

    public void generate(List<Match> matches) {
        if (task.logger.isInfoEnabled()) {
            task.logger.info("Generating report");
        }

        TaskGeneratedSingleFileReport report = (TaskGeneratedSingleFileReport) task.reports.getFirstEnabled();

        FileReporter reporter = new FileReporter(report.getDestination(), task.getEncoding());
        Renderer renderer;
        if (report instanceof CpdCsvFileReport) {
            renderer = new CSVRenderer(((CpdCsvFileReport) report).getSeparator());

        } else if (report instanceof CpdTextFileReport) {
            renderer = new SimpleRenderer(((CpdTextFileReport) report).getLineSeparator());

        } else {
            renderer = new XMLRenderer(task.getEncoding());
        }
        String renderedMatches = renderer.render(matches.iterator());

        try {
            reporter.report(renderedMatches);

        } catch (ReportException e) {
            throw new GradleException(e.getMessage(), e);
        }
    }
}
