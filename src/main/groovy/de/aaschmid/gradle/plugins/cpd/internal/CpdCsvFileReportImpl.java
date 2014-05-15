package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.CpdCsvFileReport;
import org.gradle.api.Task;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;

public class CpdCsvFileReportImpl extends TaskGeneratedSingleFileReport implements CpdCsvFileReport {

    private Character separator = ',';

    public CpdCsvFileReportImpl(String name, Task task) {
        super(name, task);
    }

    @Override
    public Character getSeparator() {
        return separator;
    }

    @Override
    public void setSeparator(Character separator) {
        this.separator = separator;
    }
}
