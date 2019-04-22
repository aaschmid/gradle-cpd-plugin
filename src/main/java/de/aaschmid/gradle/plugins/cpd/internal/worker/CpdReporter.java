package de.aaschmid.gradle.plugins.cpd.internal.worker;

import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReportConfiguration.CpdCsvReport;
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReportConfiguration.CpdTextReport;
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReportConfiguration.CpdXmlReport;
import net.sourceforge.pmd.cpd.CSVRenderer;
import net.sourceforge.pmd.cpd.FileReporter;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.Renderer;
import net.sourceforge.pmd.cpd.ReportException;
import net.sourceforge.pmd.cpd.SimpleRenderer;
import net.sourceforge.pmd.cpd.XMLRenderer;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.lang.reflect.Field;
import java.util.List;

class CpdReporter {

    private static final Logger logger = Logging.getLogger(CpdReporter.class);

    private final List<CpdReportConfiguration> reports;

    CpdReporter(List<CpdReportConfiguration> reports) {
        this.reports = reports;
    }

    void generate(List<Match> matches) {
        if (logger.isInfoEnabled()) {
            logger.info("Generating reports");
        }
        for (CpdReportConfiguration report : reports) {

            Renderer renderer = createRendererFor(report);
            String renderedMatches;

            ClassLoader previousContextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                // Workaround for Gradle Worker API uses special classloaders which Xerces dynamic implementation loading does not like
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                renderedMatches = renderer.render(matches.iterator());
            } finally {
                Thread.currentThread().setContextClassLoader(previousContextClassLoader);
            }

            FileReporter reporter = new FileReporter(report.getDestination(), report.getEncoding());
            try {
                reporter.report(renderedMatches);

            } catch (ReportException e) {
                throw new GradleException(e.getMessage(), e);
            }
        }
    }

    /**
     * @param report the configured reports used
     * @return a full configured {@link Renderer} to generate a CPD single file reports.
     */
    private Renderer createRendererFor(CpdReportConfiguration report) { // TODO CpdRenderer exists since 6.1.0 <- update minimal version!!
        if (report instanceof CpdCsvReport) {
            char separator = ((CpdCsvReport) report).getSeparator();

            if (logger.isDebugEnabled()) {
                logger.debug("Creating renderer to generate CSV file separated by '{}'.", separator);
            }
            return new CSVRenderer(separator);

        } else if (report instanceof CpdTextReport) {
            String lineSeparator = ((CpdTextReport) report).getLineSeparator();
            boolean trimLeadingCommonSourceWhitespaces = ((CpdTextReport) report).getTrimLeadingCommonSourceWhitespaces();

            if (logger.isDebugEnabled()) {
                logger.debug("Creating renderer to generate simple text file separated by '{}' and trimmed '{}'.", lineSeparator, trimLeadingCommonSourceWhitespaces);
            }
            Renderer result = new SimpleRenderer(lineSeparator);
            setTrimLeadingWhitespacesByReflection(result, trimLeadingCommonSourceWhitespaces);
            return result;

        } else if (report instanceof CpdXmlReport) {
            String encoding = report.getEncoding();
            if (logger.isDebugEnabled()) {
                logger.debug("Creating XML renderer to generate with encoding '{}'.", encoding);
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
    private void setTrimLeadingWhitespacesByReflection(Renderer result, boolean trimLeadingCommonSourceWhitespaces) {
        String fieldName = "trimLeadingWhitespace";
        if (logger.isDebugEnabled()) {
            logger.debug("Try setting '{}' field to '{}' for '{}' by reflection.", fieldName, trimLeadingCommonSourceWhitespaces, result);
        }
        try {
            Field field = SimpleRenderer.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(result, trimLeadingCommonSourceWhitespaces);

        } catch (Exception e) {
            if (logger.isWarnEnabled()) { // TODO test if it is really logged?
                logger.warn(String.format("Could not set field '%s' on created SimpleRenderer by reflection due to:", fieldName), e);
            }
        }
    }
}
