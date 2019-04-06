package de.aaschmid.gradle.plugins.cpd;

import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Nested;

/**
 * The reporting configuration for the {@link Cpd} task.
 * <p>
 * One of
 * <ul>
 * <li>csv
 * <li>text (default)
 * <li>xml
 * </ul>
 * <p>
 * The reporting aspects of a {@link Cpd} task can be configured as such:
 * <pre>
 * cpdCheck {
 *     reports {
 *         csv.enabled = false
 *         text {
 *             enabled = true
 *             destination = file("${buildDir}/cpd.txt"
 *         }
 *     }
 * }
 * </pre>
 *
 * @see Cpd
 */
interface CpdReports extends ReportContainer<SingleFileReport> {

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
     * @return The CPD (single file) 'XML' report
     */
    @Nested
    CpdXmlFileReport getXml();
}
