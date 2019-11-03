package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.CpdVsFileReport;
import org.gradle.api.Task;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;

public class CpdVsFileReportImpl extends TaskGeneratedSingleFileReport implements CpdVsFileReport {

    public CpdVsFileReportImpl(String name, Task task) {
        super(name, task);
    }
}
