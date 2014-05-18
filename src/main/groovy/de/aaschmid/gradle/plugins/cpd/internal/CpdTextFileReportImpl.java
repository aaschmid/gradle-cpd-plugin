package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.CpdTextFileReport;
import net.sourceforge.pmd.cpd.Renderer;
import net.sourceforge.pmd.cpd.SimpleRenderer;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport;

import java.lang.reflect.Field;

public class CpdTextFileReportImpl extends TaskGeneratedSingleFileReport implements CpdTextFileReport {

    private static final Logger logger = Logging.getLogger(CpdReporter.class);

    private String lineSeparator = CpdTextFileReport.DEFAULT_LINE_SEPARATOR;
    private boolean trimLeadingCommonSourceWhitespaces = CpdTextFileReport.DEFAULT_TRIM_LEADING_COMMON_SOURCE_WHITESPACE;

    public CpdTextFileReportImpl(String name, Task task) {
        super(name, task);
    }

    @Override
    public Renderer createRenderer() {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating renderer to generate simple text file separated by '{}' and trimmed '{}'.", getLineSeparator(), getTrimLeadingCommonSourceWhitespaces());
        }
        Renderer result = new SimpleRenderer(getLineSeparator());
        setTrimLeadingWhitespacesByReflection(result);
        return result;
    }

    /** Also set second field to trim leading whitespaces.
     * <p>
     * <i>Information:</i> Use reflection because neither proper constructor for setting both fields nor setter are
     * available.
     */
    private void setTrimLeadingWhitespacesByReflection(Renderer result) {
        String fieldName = "trimLeadingWhitespace";
        if (logger.isDebugEnabled()) {
            logger.debug("Try setting '{}' field to '{}' for '{}' by reflection.", fieldName, getTrimLeadingCommonSourceWhitespaces(), result);
        }
        try {
            Field field = SimpleRenderer.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(result, getTrimLeadingCommonSourceWhitespaces());
        } catch (Exception e) {
            if (logger.isWarnEnabled()) { // TODO test if it is really logged?
                logger.warn(String.format("Could not set field '%s' on created SimpleRenderer by reflection due to:", fieldName), e);
            }
        }
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
