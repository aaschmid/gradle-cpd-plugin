package de.aaschmid.gradle.plugins.cpd;

import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

/**
 * The single file simple text report for code/paste (= duplication) detection.
 *
 * @see CpdPlugin
 */
public interface CpdVsFileReport extends SingleFileReport {

}
