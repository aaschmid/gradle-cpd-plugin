package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.CpdCsvFileReport;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Task;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;

public class CpdCsvFileReportImpl extends TaskGeneratedSingleFileReport implements CpdCsvFileReport {

    private char separator = CpdCsvFileReport.DEFAULT_SEPARATOR;
    private boolean includeLineCount = CpdCsvFileReport.DEFAULT_INCLUDE_LINE_COUNT;

    public CpdCsvFileReportImpl(String name, Task task) {
        super(name, task);
    }

    @Override
    public Character getSeparator() {
        return separator;
    }

    @Override
    public void setSeparator(Character separator) {
        if (separator == null) {
            throw new InvalidUserDataException("CSV report 'separator' must not be null.");
        }
        this.separator = separator;
    }

    @Override
    public boolean isIncludeLineCount() {
        return includeLineCount;
    }

    @Override
    public void setIncludeLineCount(boolean includeLineCount) {
        this.includeLineCount = includeLineCount;
    }
}
