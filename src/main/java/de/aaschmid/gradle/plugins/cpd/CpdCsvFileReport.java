package de.aaschmid.gradle.plugins.cpd;

import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

/**
 * The single file CSV report for code/paste (= duplication) detection.
 *
 * @see CpdPlugin
 */
public interface CpdCsvFileReport extends SingleFileReport {

    /** Default CSV separator. */
    char DEFAULT_SEPARATOR = ',';

    /**
     * @return the separator {@link Character} used to generate CSV report; defaults to {@link #DEFAULT_SEPARATOR}.
     */
    @Input
    Character getSeparator();

    /**
     * @param separator to be used when generating the CSV report; defaults to {@link #DEFAULT_SEPARATOR}.
     * @throws org.gradle.api.InvalidUserDataException iif supplied {@code separator} is {@code null} ({@code char}
     *                                                 cannot be wrapped by Gradle interally, such that unboxable
     *                                                 {@code null}s must be checked in setter)
     */
    void setSeparator(Character separator);

}
