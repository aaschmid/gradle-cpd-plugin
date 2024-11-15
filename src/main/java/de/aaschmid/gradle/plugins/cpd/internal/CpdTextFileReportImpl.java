package de.aaschmid.gradle.plugins.cpd.internal;

import javax.inject.Inject;

import de.aaschmid.gradle.plugins.cpd.CpdTextFileReport;
import org.gradle.api.Task;

public abstract class CpdTextFileReportImpl extends CpdReportInternal implements CpdTextFileReport {

    private String lineSeparator = CpdTextFileReport.DEFAULT_LINE_SEPARATOR;
    private boolean trimLeadingCommonSourceWhitespaces = CpdTextFileReport.DEFAULT_TRIM_LEADING_COMMON_SOURCE_WHITESPACE;

    @Inject
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
