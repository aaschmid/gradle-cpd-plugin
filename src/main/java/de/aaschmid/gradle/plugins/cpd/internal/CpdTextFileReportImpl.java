package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.CpdTextFileReport;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;

@Getter
@Setter
public class CpdTextFileReportImpl extends TaskGeneratedSingleFileReport implements CpdTextFileReport {

    private String lineSeparator = CpdTextFileReport.DEFAULT_LINE_SEPARATOR;
    private boolean trimLeadingCommonSourceWhitespaces = CpdTextFileReport.DEFAULT_TRIM_LEADING_COMMON_SOURCE_WHITESPACE;

    public CpdTextFileReportImpl(String name, Task task) {
        super(name, task);
    }

}
