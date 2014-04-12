package de.aaschmid.gradle.plugins.cpd

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.reporting.ReportingExtension
import org.gradle.util.VersionNumber


/**
 *  A plugin for the <a href="http://pmd.sourceforge.net/cpd-usage.html">CPD</a> detection
 *  (which is a part of <a href="http://pmd.sourceforge.net/">PMD</a>).
 * <p>
 * Declares a <tt>cpd</tt> configuration which needs to be configured with the CPD library to be used.
 * <p>
 * A {@link Cpd} task is created and configured to analyze complete source code at once.
 * <p>
 * The created {@link Cpd} tasks is added to the <tt>check</tt> lifecycle task.
 * <p>
 * Copied partly from {@link org.gradle.api.plugins.quality.PmdPlugin} and adjusted
 *
 * @see CpdExtension
 * @see Cpd
 */
class CpdPlugin implements Plugin<Project> {


    protected Project project
    protected CpdExtension extension

    @Override
    void apply(Project project) {
        this.project = project

        this.extension = project.extensions.create('cpd', CpdExtension, project)
        extension.with{
            toolVersion = '5.1.0'
        }

        project.configurations.create('cpd').with{
            visible = false
            transitive = true
            description = "The CPD libraries to be used for this project."
            // Don't need these things, they're provided by the runtime
            exclude group: 'ant', module: 'ant'
            exclude group: 'org.apache.ant', module: 'ant'
        }

        Cpd task = project.tasks.create(name: 'cpd', type: Cpd, group: 'check', description: 'Run CPD analysis for all sources')

        project.plugins.apply(ReportingBasePlugin)
        extension.conventionMapping.with{
            reportsDir = { project.extensions.getByType(ReportingExtension).baseDir }
        }

        configureTaskDefaults(task)
    }

    protected void configureTaskDefaults(Cpd task) {
        def config = project.configurations['cpd']
        config.incoming.beforeResolve{
            if (config.dependencies.empty) {
                VersionNumber version = VersionNumber.parse(extension.toolVersion)
                String dependency = (version < VersionNumber.parse('5.0.0')) ?
                        "pmd:pmd:${extension.toolVersion}" : "net.sourceforge.pmd:pmd:${extension.toolVersion}"
                config.dependencies.add(project.dependencies.create(dependency))
            }
        }
        task.conventionMapping.with{
            classpath = { config }
            ignoreFailures = { extension.ignoreFailures }
            task.reports.all{ report ->
                report.conventionMapping.with{
                    enabled = { true }
                    destination = { new File(extension.reportsDir, "cpd.${report.name}") }
                }
            }
        }
    }
}
