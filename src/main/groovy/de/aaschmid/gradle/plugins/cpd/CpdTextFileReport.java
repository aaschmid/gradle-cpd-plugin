package de.aaschmid.gradle.plugins.cpd;

import net.sourceforge.pmd.cpd.SimpleRenderer;
import org.gradle.api.reporting.SingleFileReport;

/**
 * The single file simple text report for code/paste (= duplication) detection.
 *
 * @see CpdPlugin
 */
public interface CpdTextFileReport extends SingleFileReport {

    /**
     * @return if the common leading whitespace of a source code snippet should be trimmed (= {@code true}) or not (=
     *         {@code false})
     */
    boolean getTrimLeadingCommonSourceWhitespaces();

    /**
     * @param trimLeadingCommonSourceWhitespaces set to {@code true} if the leading common whitespaces of a single
     *            source code snippet should be trimmed, otherwise {@code false}
     */
    void setTrimLeadingCommonSourceWhitespaces(boolean trimLeadingCommonSourceWhitespaces);

    /**
     * @return the line separator {@link String} used to generate text report.
     * @see SimpleRenderer#DEFAULT_SEPARATOR
     */
    String getLineSeparator();

    /**
     * @param lineSeparator to be used when generating the text report.
     */
    void setLineSeparator(String lineSeparator);
}
