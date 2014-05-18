package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.Cpd;
import de.aaschmid.gradle.plugins.cpd.CpdXmlFileReport;
import net.sourceforge.pmd.cpd.Renderer;
import net.sourceforge.pmd.cpd.XMLRenderer;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;

public class CpdXmlFileReportImpl extends TaskGeneratedSingleFileReport implements CpdXmlFileReport {

    private static final Logger logger = Logging.getLogger(CpdReporter.class);

    private final Cpd task;

    private String encoding;

    public CpdXmlFileReportImpl(String name, Task task) {
        super(name, task);
        this.task = (task instanceof Cpd) ? (Cpd) task : null;
    }

    @Override
    public Renderer createRenderer() {
        String encoding = getXmlEncoding();
        if (logger.isDebugEnabled()) {
            logger.debug("Creating renderer to generate XML file with encoding '{}'.", encoding);
        }
        return new XMLRenderer(encoding);
    }

    private String getXmlEncoding() {
        String encoding = getEncoding();
        if (encoding == null && task != null) {
            encoding = task.getEncoding();
        }
        if (encoding == null) {
            encoding = System.getProperty("file.encoding");
        }
        return encoding;
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
