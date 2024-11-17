package de.aaschmid.gradle.plugins.cpd.internal;

import javax.inject.Inject;

import de.aaschmid.gradle.plugins.cpd.CpdXmlFileReport;
import org.gradle.api.Task;

public abstract class CpdXmlFileReportImpl extends CpdReportInternal implements CpdXmlFileReport {

    private String encoding;

    @Inject
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
