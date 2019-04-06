package de.aaschmid.gradle.plugins.cpd;

import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

/**
 * The single file simple text report for code/paste (= duplication) detection.
 *
 * @see de.aaschmid.gradle.plugins.cpd.CpdPlugin
 */
public interface CpdXmlFileReport extends SingleFileReport {

    /**
     * @return the encoding used to generate XML report; defaults to
     *                 {@link de.aaschmid.gradle.plugins.cpd.Cpd#getEncoding()} or if also not set to
     *                 {@code System.getProperty("file.encoding")}.
     */
    @Input
    @Optional
    String getEncoding();

    /**
     * @param encoding to be used when generating the XML report; defaults to
     *                 {@link de.aaschmid.gradle.plugins.cpd.Cpd#getEncoding()} or if also not set to
     *                 {@code System.getProperty("file.encoding")}.
     * @see Cpd#getEncoding()
     */
    void setEncoding(String encoding);
}
