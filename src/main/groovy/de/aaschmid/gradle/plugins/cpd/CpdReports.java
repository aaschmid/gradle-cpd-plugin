package de.aaschmid.gradle.plugins.cpd;

import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.SingleFileReport;

/**
 * The reporting configuration for the {@link Cpd} task.
 */
public interface CpdReports extends ReportContainer<SingleFileReport> {

    /**
     * @return The cpd (single file) CSV report
     */
    SingleFileReport getCsv();

    /**
     * @return The cpd (single file) text report
     */
    SingleFileReport getText();

    /**
     * @return The cpd (single file) XML report
     */
    SingleFileReport getXml();
}
