package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.CpdXmlFileReport;
import org.gradle.api.Task;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;

public class CpdXmlFileReportImpl extends TaskGeneratedSingleFileReport implements CpdXmlFileReport {

    private String encoding;

    public CpdXmlFileReportImpl(String name, Task task) {
        super(name, task);
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
