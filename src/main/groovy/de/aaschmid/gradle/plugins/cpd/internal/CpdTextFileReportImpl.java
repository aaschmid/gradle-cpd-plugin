package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.CpdTextFileReport;
import org.gradle.api.Task;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;

public class CpdTextFileReportImpl extends TaskGeneratedSingleFileReport implements CpdTextFileReport {

    private String lineSeparator;
    private boolean trimLeadingCommonSourceWhitespaces;

    public CpdTextFileReportImpl(String name, Task task) {
        super(name, task);
    }

    @Override
    public boolean getTrimLeadingCommonSourceWhitespaces() {
        return trimLeadingCommonSourceWhitespaces;
    }

    @Override
    public void setTrimLeadingCommonSourceWhitespaces(boolean trimLeadingCommonSourceWhitespaces) {
        this.trimLeadingCommonSourceWhitespaces = trimLeadingCommonSourceWhitespaces;

    }

    @Override
    public String getLineSeparator() {
        return lineSeparator;
    }

    @Override
    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }
}
