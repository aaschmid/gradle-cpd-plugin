package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.Cpd;
import de.aaschmid.gradle.plugins.cpd.CpdCsvFileReport;
import de.aaschmid.gradle.plugins.cpd.CpdReports;
import de.aaschmid.gradle.plugins.cpd.CpdTextFileReport;
import de.aaschmid.gradle.plugins.cpd.CpdXmlFileReport;
import net.sourceforge.pmd.cpd.CSVRenderer;
import net.sourceforge.pmd.cpd.FileReporter;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.Renderer;
import net.sourceforge.pmd.cpd.ReportException;
import net.sourceforge.pmd.cpd.SimpleRenderer;
import net.sourceforge.pmd.cpd.XMLRenderer;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.reporting.SingleFileReport;

import java.lang.reflect.Field;
import java.util.List;

public class CpdReporter {

    private static final Logger logger = Logging.getLogger(CpdReporter.class);

    private final String encoding;
    private final SingleFileReport report;

    public CpdReporter(Cpd task) {
        if (task == null) {
            throw new NullPointerException("task must not be null");
        }

        if (task.getEncoding() == null) {
            throw new InvalidUserDataException(String.format("Task '%s' requires encoding but was: %s.",
                    task.getName(), task.getEncoding()));
        }

        CpdReports reports = task.getReports();
        if (reports.getEnabled().size() != 1) {
            throw new InvalidUserDataException(String.format("Task '%s' requires exactly one report to be enabled but was: %s.",
                    task.getName(), reports.getEnabled().getAsMap().keySet()));
        }
        this.encoding = task.getEncoding();
        this.report = reports.getEnabled().iterator().next();

        try {
            this.report.getDestination();
        } catch (IllegalArgumentException e) {
            throw new InvalidUserDataException(String.format("'%s' requires valid destination but was 'null'.", this.report));
        }
    }

    public void generate(List<Match> matches) {

        if (logger.isInfoEnabled()) {
            logger.info("Generating report");
        }
        String renderedMatches = createRendererFor(report).render(matches.iterator());

        FileReporter reporter = new FileReporter(report.getDestination(), encoding);
        try {
            reporter.report(renderedMatches);

        } catch (ReportException e) {
            throw new GradleException(e.getMessage(), e);
        }
    }

    /**
     * @param report the configured {@link SingleFileReport} used {@code cpdCheck.reports{ ... }}
     * @return a full configured {@link Renderer} to generate a CPD single file report.
     */
    public Renderer createRendererFor(SingleFileReport report) {
        if (report instanceof CpdCsvFileReport) {
            char separator = ((CpdCsvFileReport) report).getSeparator();

            if (logger.isDebugEnabled()) {
                logger.debug("Creating renderer to generate CSV file separated by '{}'.", separator);
            }
            return new CSVRenderer(separator);

        } else if (report instanceof CpdTextFileReport) {
            String lineSeparator = ((CpdTextFileReport) report).getLineSeparator();
            boolean trimLeadingCommonSourceWhitespaces = ((CpdTextFileReport) report).getTrimLeadingCommonSourceWhitespaces();

            if (logger.isDebugEnabled()) {
                logger.debug("Creating renderer to generate simple text file separated by '{}' and trimmed '{}'.", lineSeparator, trimLeadingCommonSourceWhitespaces);
            }
            Renderer result = new SimpleRenderer(lineSeparator);
            setTrimLeadingWhitespacesByReflection(result, trimLeadingCommonSourceWhitespaces);
            return result;

        } else if (report instanceof CpdXmlFileReport) {
            String encoding = ((CpdXmlFileReportImpl) report).getXmlRendererEncoding(); // TODO unchecked cast!
            if (logger.isDebugEnabled()) {
                logger.debug("Creating renderer to generate XML file with encoding '{}'.", encoding);
            }
            return new XMLRenderer(encoding);
        }
        throw new GradleException(String.format("Cannot create report for unsupported %s", report.getClass().getCanonicalName()));
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
