package de.aaschmid.gradle.plugins.cpd;

/**
 * The single file CSV report for code/paste (= duplication) detection.
 *
 * @see CpdPlugin
 */
public interface CpdCsvFileReport extends CpdFileReport {

    /** Default CSV separator. */
    char DEFAULT_SEPARATOR = ',';

    /**
     * @return the separator {@link Character} used to generate CSV report; defaults to {@link #DEFAULT_SEPARATOR}.
     */
    Character getSeparator();

    /**
     * @param separator to be used when generating the CSV report; defaults to {@link #DEFAULT_SEPARATOR}.
     * @throws org.gradle.api.InvalidUserDataException iif supplied {@code separator} is {@code null} ({@code char}
     *                                                 cannot be wrapped by Gradle interally, such that unboxable
     *                                                 {@code null}s must be checked in setter)
     */
    void setSeparator(Character separator);

}
