package de.aaschmid.gradle.plugins.cpd.internal.worker;

import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReportConfiguration.CpdCsvReport;
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReportConfiguration.CpdTextReport;
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReportConfiguration.CpdXmlReport;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pmd.cpd.CSVRenderer;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.SimpleRenderer;
import net.sourceforge.pmd.cpd.XMLRenderer;
import net.sourceforge.pmd.cpd.renderer.CPDRenderer;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
class CpdReporter {

    private final List<CpdReportConfiguration> reports;

    void generate(List<Match> matches) {
        if (log.isInfoEnabled()) {
            log.info("Generating reports");
        }
        for (CpdReportConfiguration report : reports) {

            CPDRenderer renderer = createRendererFor(report);
            try (FileWriter fileWriter = new FileWriter(report.getDestination())){

                ClassLoader previousContextClassLoader = Thread.currentThread().getContextClassLoader();
                try {
                    // Workaround for Gradle Worker API uses special classloaders which Xerces dynamic implementation loading does not like
                    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                    renderer.render(matches.iterator(), fileWriter);
                } finally {
                    Thread.currentThread().setContextClassLoader(previousContextClassLoader);
                }
            } catch (IOException e) {
                throw new GradleException(e.getMessage(), e);
            }
        }
    }

    /**
     * @param report the configured reports used
     * @return a full configured {@link CPDRenderer} to generate a CPD single file reports.
     */
    private CPDRenderer createRendererFor(CpdReportConfiguration report) {
        if (report instanceof CpdCsvReport) {
            char separator = ((CpdCsvReport) report).getSeparator();

            if (log.isDebugEnabled()) {
                log.debug("Creating renderer to generate CSV file separated by '{}'.", separator);
            }
            return new CSVRenderer(separator);

        } else if (report instanceof CpdTextReport) {
            String lineSeparator = ((CpdTextReport) report).getLineSeparator();
            boolean trimLeadingCommonSourceWhitespaces = ((CpdTextReport) report).isTrimLeadingCommonSourceWhitespaces();

            if (log.isDebugEnabled()) {
                log.debug("Creating renderer to generate simple text file separated by '{}' and trimmed '{}'.", lineSeparator, trimLeadingCommonSourceWhitespaces);
            }
            SimpleRenderer result = new SimpleRenderer(lineSeparator);
            setTrimLeadingWhitespacesByReflection(result, trimLeadingCommonSourceWhitespaces);
            return result;

        } else if (report instanceof CpdXmlReport) {
            String encoding = report.getEncoding();
            if (log.isDebugEnabled()) {
                log.debug("Creating XML renderer to generate with encoding '{}'.", encoding);
            }
            return new XMLRenderer(encoding);
        }
        throw new GradleException(String.format("Cannot create reports for unsupported %s", report.getClass().getCanonicalName()));
    }

    /**
     * Also set second field to trim leading whitespaces.
     * <p/>
     * <i>Information:</i> Use reflection because neither proper constructor for setting both fields nor setter are
     * available.
     */
    private void setTrimLeadingWhitespacesByReflection(CPDRenderer result, boolean trimLeadingCommonSourceWhitespaces) {
        String fieldName = "trimLeadingWhitespace";
        if (log.isDebugEnabled()) {
            log.debug("Try setting '{}' field to '{}' for '{}' by reflection.", fieldName, trimLeadingCommonSourceWhitespaces, result);
        }
        try {
            Field field = SimpleRenderer.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(result, trimLeadingCommonSourceWhitespaces);

        } catch (Exception e) {
            if (log.isWarnEnabled()) { // TODO test if it is really logged?
                log.warn(String.format("Could not set field '%s' on created SimpleRenderer by reflection due to:", fieldName), e);
            }
        }
    }
}
