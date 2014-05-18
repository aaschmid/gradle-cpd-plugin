package de.aaschmid.gradle.plugins.cpd;

/**
 * The single file simple text report for code/paste (= duplication) detection.
 *
 * @see de.aaschmid.gradle.plugins.cpd.CpdPlugin
 */
public interface CpdXmlFileReport extends CpdFileReport {

    /**
     * @return the encoding used to generate XML report; defaults to
     *                 {@link de.aaschmid.gradle.plugins.cpd.Cpd#getEncoding()} or if also not set to
     *                 {@code System.getProperty("file.encoding")}.
     */
    String getEncoding();

    /**
     * @param encoding to be used when generating the XML report; defaults to
     *                 {@link de.aaschmid.gradle.plugins.cpd.Cpd#getEncoding()} or if also not set to
     *                 {@code System.getProperty("file.encoding")}.
     * @see Cpd#getEncoding()
     */
    void setEncoding(String encoding);
}
