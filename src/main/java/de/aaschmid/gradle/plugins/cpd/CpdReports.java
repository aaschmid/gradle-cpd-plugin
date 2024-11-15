package de.aaschmid.gradle.plugins.cpd;

import org.gradle.api.Action;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Nested;

/**
 * The reporting configuration for the {@link Cpd} task.
 * <p>
 * One of
 * <ul>
 * <li>csv
 * <li>text
 * <li>vs
 * <li>xml (default)
 * </ul>
 * <p>
 * The reporting aspects of a {@link Cpd} task can be configured as such:
 * <pre>
 * cpdCheck {
 *     reports {
 *         csv.required = false
 *         text {
 *             required = true
 *             outputLocation = file("${buildDir}/cpd.txt"
 *         }
 *     }
 * }
 * </pre>
 *
 * @see Cpd
 */
public interface CpdReports extends ReportContainer<SingleFileReport> {

    /**
     * @return The CPD (single file) 'CSV' report
     */
    @Nested
    CpdCsvFileReport getCsv();

    /**
     * @return The CPD (single file) 'text' report
     */
    @Nested
    CpdTextFileReport getText();

    /**
     * @return The CPD (single file) 'vs' report
     */
    @Nested
    SingleFileReport getVs();

    /**
     * @return The CPD (single file) 'XML' report
     */
    @Nested
    CpdXmlFileReport getXml();

    /**
     * Configures the csv report.
     *
     * @param action The Configuration closure/action.
     */
    void csv(Action<CpdCsvFileReport> action);

    /**
     * Configures the text report.
     *
     * @param action The Configuration closure/action.
     */
    void text(Action<CpdTextFileReport> action);

    /**
     * Configures the vs report.
     *
     * @param action The Configuration closure/action.
     */
    void vs(Action<SingleFileReport> action);

    /**
     * Configures the xml report.
     *
     * @param action The Configuration closure/action.
     */
    void xml(Action<CpdXmlFileReport> action);
}
