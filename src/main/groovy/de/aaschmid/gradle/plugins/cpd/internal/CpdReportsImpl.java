package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.CpdReports;
import org.gradle.api.Task;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;
import org.gradle.api.reporting.internal.TaskReportContainer;

public class CpdReportsImpl extends TaskReportContainer<SingleFileReport> implements CpdReports {

    public CpdReportsImpl(Task task) {
        super(SingleFileReport.class, task);

        add(TaskGeneratedSingleFileReport.class, "csv", task);
        add(TaskGeneratedSingleFileReport.class, "text", task);
        add(TaskGeneratedSingleFileReport.class, "xml", task);
    }

    public SingleFileReport getCsv() {
        return getByName("csv");
    }

    public SingleFileReport getText() {
        return getByName("text");
    }

    public SingleFileReport getXml() {
        return getByName("xml");
    }
}
