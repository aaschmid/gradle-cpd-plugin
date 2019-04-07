package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.CpdTextFileReport;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;

public class CpdTextFileReportImpl extends TaskGeneratedSingleFileReport implements CpdTextFileReport {

    private static final Logger logger = Logging.getLogger(CpdTextFileReportImpl.class);

    private String lineSeparator = CpdTextFileReport.DEFAULT_LINE_SEPARATOR;
    private boolean trimLeadingCommonSourceWhitespaces = CpdTextFileReport.DEFAULT_TRIM_LEADING_COMMON_SOURCE_WHITESPACE;

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
