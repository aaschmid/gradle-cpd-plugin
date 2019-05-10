package de.aaschmid.gradle.plugins.cpd;

import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

/**
 * The single file simple text report for code/paste (= duplication) detection.
 *
 * @see CpdPlugin
 */
public interface CpdTextFileReport extends SingleFileReport {

    /** Default line separator */
    String DEFAULT_LINE_SEPARATOR = "=====================================================================";

    /** Default setting if the leading common whitespace of a source code snipped should be trimmed or not. */
    boolean DEFAULT_TRIM_LEADING_COMMON_SOURCE_WHITESPACE = false;

    /**
     * @return if the common leading whitespace of a source code snippet should be trimmed (= {@code true}) or not (=
     * {@code false}); defaults to {@link #DEFAULT_TRIM_LEADING_COMMON_SOURCE_WHITESPACE}.
     */
    @Input
    boolean isTrimLeadingCommonSourceWhitespaces();

    /**
     * @param trimLeadingCommonSourceWhitespaces set to {@code true} if the leading common whitespaces of a single
     * source code snippet should be trimmed, otherwise {@code false}; defaults to {@link
     * #DEFAULT_TRIM_LEADING_COMMON_SOURCE_WHITESPACE}.
     */
    void setTrimLeadingCommonSourceWhitespaces(boolean trimLeadingCommonSourceWhitespaces);

    /**
     * @return the line separator {@link String} used to generate text report; defaults to {@link
     * #DEFAULT_LINE_SEPARATOR}.
     */
    @Input
    String getLineSeparator();

    /**
     * @param lineSeparator to be used when generating the text report; defaults to {@link #DEFAULT_LINE_SEPARATOR}.
     */
    void setLineSeparator(String lineSeparator);
}
