package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.CpdCsvFileReport;
import net.sourceforge.pmd.cpd.CSVRenderer;
import net.sourceforge.pmd.cpd.Renderer;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;

public class CpdCsvFileReportImpl extends TaskGeneratedSingleFileReport implements CpdCsvFileReport {

    private static final Logger logger = Logging.getLogger(CpdReporter.class);

    private char separator = CpdCsvFileReport.DEFAULT_SEPARATOR;

    public CpdCsvFileReportImpl(String name, Task task) {
        super(name, task);
    }

    @Override
    public Renderer createRenderer() {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating renderer to generate CSV file separated by '{}'.", getSeparator());
        }
        return new CSVRenderer(getSeparator());
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
}
