package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.Cpd;
import de.aaschmid.gradle.plugins.cpd.CpdXmlFileReport;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;

public class CpdXmlFileReportImpl extends TaskGeneratedSingleFileReport implements CpdXmlFileReport {

    private static final Logger logger = Logging.getLogger(CpdXmlFileReportImpl.class);

    private final Cpd task;

    private String encoding;

    public CpdXmlFileReportImpl(String name, Task task) {
        super(name, task);
        this.task = (task instanceof Cpd) ? (Cpd) task : null;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getXmlRendererEncoding() {
        String encoding = getEncoding();
        if (encoding == null && task != null) {
            encoding = task.getEncoding();
        }
        if (encoding == null) {
            encoding = System.getProperty("file.encoding");
        }
        return encoding;
    }
}
