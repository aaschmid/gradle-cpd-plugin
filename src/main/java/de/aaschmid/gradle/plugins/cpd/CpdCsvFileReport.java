package de.aaschmid.gradle.plugins.cpd;

import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Input;

/**
 * The single file CSV report for code/paste (= duplication) detection.
 *
 * @see CpdPlugin
 */
public interface CpdCsvFileReport extends SingleFileReport {

    /** Default CSV separator. */
    char DEFAULT_SEPARATOR = ',';

    /** Default if line count column should be part of CSV. */
    boolean DEFAULT_INCLUDE_LINE_COUNT = true;

    /**
     * @return the separator {@link Character} used to generate CSV report; defaults to {@link #DEFAULT_SEPARATOR}.
     */
    @Input
    Character getSeparator();

    /**
     * @param separator to be used when generating the CSV report; defaults to {@link #DEFAULT_SEPARATOR}.
     * @throws org.gradle.api.InvalidUserDataException iif supplied {@code separator} is {@code null} ({@code char} cannot be wrapped by
     * Gradle interally, such that unboxable {@code null}s must be checked in setter)
     */
    void setSeparator(Character separator);

    /**
     * Note: Property is originally named {@code lineCountPerFile} and meaning is inverted, see https://github.com/pmd/pmd/blob/master/pmd-core/src/main/java/net/sourceforge/pmd/cpd/CSVRenderer.java#L63.
     *
     * @return if line count column should be included; defaults to {@link #DEFAULT_INCLUDE_LINE_COUNT}.
     */
    @Input
    boolean isIncludeLineCount();

    /**
     * @param includeLineCount to be used when generating the CSV report; defaults to {@link #DEFAULT_INCLUDE_LINE_COUNT}.
     */
    void setIncludeLineCount(boolean includeLineCount);
}
