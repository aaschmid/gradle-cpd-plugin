package de.aaschmid.gradle.plugins.cpd;

import net.sourceforge.pmd.cpd.CSVRenderer;
import org.gradle.api.reporting.SingleFileReport;

/**
 * The single file CSV report for code/paste (= duplication) detection.
 *
 * @see CpdPlugin
 */
public interface CpdCsvFileReport extends SingleFileReport {

    /**
     * @return the separator char used to generate CSV report.
     * @see CSVRenderer#DEFAULT_SEPARATOR
     */
    Character getSeparator();

    /**
     * @param separator to be used when generating the CSV report.
     */
    void setSeparator(Character separator);
}
