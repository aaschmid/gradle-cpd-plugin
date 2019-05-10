package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.CpdXmlFileReport;
import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Task;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;

@Getter
@Setter
public class CpdXmlFileReportImpl extends TaskGeneratedSingleFileReport implements CpdXmlFileReport {

    private String encoding;

    public CpdXmlFileReportImpl(String name, Task task) {
        super(name, task);
    }

}
