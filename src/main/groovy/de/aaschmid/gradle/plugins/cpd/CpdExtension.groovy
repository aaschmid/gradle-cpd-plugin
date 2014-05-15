package de.aaschmid.gradle.plugins.cpd

import org.gradle.api.Project
import org.gradle.api.plugins.quality.CodeQualityExtension


/**
 * Configuration options for the CPD plugin.
 * <p>
 * The sample below shows various configuration options:
 *
 * <pre autoTested=''>
 * apply plugin: 'cpd'
 *
 * cpd {
 *     encoding = 'UTF-8'
 *     minimumTokenCount = 20
 * }
 * </pre>
 *
 * @see CpdPlugin
 */
class CpdExtension extends CodeQualityExtension {

    private final Project project

    CpdExtension(Project project) {
        this.project = project
    }

    /**
     * The character set encoding (e.g., UTF-8) to use when reading the source code files but also when producing the
     * report.
     * <p>
     * Example: {@code encoding = UTF-8}
     */
    String encoding = System.getProperty('file.encoding')

    /**
     * A positive integer indicating the minimum token count to trigger a CPD match; defaults to {@code 50}.
     * <p>
     * Example: {@code minimumTokenCount = 25}
     */
    int minimumTokenCount = 50
}
