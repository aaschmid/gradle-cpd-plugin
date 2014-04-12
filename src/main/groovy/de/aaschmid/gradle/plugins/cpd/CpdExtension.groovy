package de.aaschmid.gradle.plugins.cpd

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.plugins.quality.TargetJdk

/**
 * Configuration options for the CPD plugin.
 *
 * @see CpdPlugin
 */
class CpdExtension extends CodeQualityExtension {

    private final Project project

    CpdExtension(Project project) {
        this.project = project
    }
}
