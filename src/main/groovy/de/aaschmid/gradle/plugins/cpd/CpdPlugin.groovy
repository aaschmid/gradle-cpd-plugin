package de.aaschmid.gradle.plugins.cpd

import org.gradle.api.Incubating
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.reporting.ReportingExtension


/**
 * A plugin for the finding duplicate code using <a href="http://pmd.sourceforge.net/cpd-usage.html">CPD</a> source
 * code analyzer (which is a part of <a href="http://pmd.sourceforge.net/">PMD</a>).
 * <p>
 * Creates and registers a {@code cpd} extension with the default task options for every task of type
 * {@link Cpd}.
 * <p>
 * Declares a {@code cpd} configuration which needs to be configured with the
 * <a href="http://pmd.sourceforge.net/">PMD</a> library containing the
 * <a href="http://pmd.sourceforge.net/cpd-usage.html">CPD</a> library to be used.
 * <p>
 * A {@link Cpd} task named {@code cpd} is created and configured with default options. It can be further configured
 * to analyze the source code you want, e.g. {@code source = project.files('src')}.
 * <p>
 * The created {@link Cpd} task is added to the {@code check} lifecycle task of {@link JavaBasePlugin} if it is also
 * applied, e.g. using {@link org.gradle.api.plugins.JavaPlugin}.
 * <p>
 * Sample:
 *
 * <pre autoTested=''>
 * apply plugin: 'cpd'
 *
 * repositories{
 *     mavenCentral()
 * }
 *
 * cpd {
 *     minimumTokenCount = 25
 *     toolVersion = 5.0.1
 * }
 *
 * tasks.cpd {
 *     source files{
 *         allprojects.findAll{ p -> p.hasProperty('sourceSets') }.collect { p -> p.sourceSets.collect { it.java }
 *     }
 * }
 * </pre>
 *
 * @see CpdExtension
 * @see Cpd
 */
@Incubating
class CpdPlugin implements Plugin<Project> {

    protected Project project
    protected CpdExtension extension

    @Override
    void apply(Project project) {
        this.project = project

        project.plugins.apply(ReportingBasePlugin)

        extension = createExtension(project)
        createConfiguration(project, extension)
        setupTaskDefaults(project, extension)

        Cpd task = project.tasks.create(name: 'cpd', type: Cpd, description: 'Run CPD analysis for all sources')
        project.plugins.withType(JavaBasePlugin){
            project.tasks.findByName('check').dependsOn(task)
        }

    }

    private CpdExtension createExtension(Project project) {
        CpdExtension extension = project.extensions.create('cpd', CpdExtension, project)

        // set constant values directly
        extension.with{
            toolVersion = '5.1.0'
        }
        // use conventionMapping for values derived based on some external value
        extension.conventionMapping.with{
            reportsDir = { project.extensions.getByType(ReportingExtension).baseDir }
        }
        return extension
    }

    /** Set up task defaults for every created {@link Cpd} task. */
    private void setupTaskDefaults(Project project, CpdExtension extension) {
        project.tasks.withType(Cpd){ Cpd task ->
            task.conventionMapping.with{
                encoding = { extension.encoding }
                minimumTokenCount = { extension.minimumTokenCount }
                pmdClasspath = { project.configurations.findByName('cpd') }
            }
            task.reports.all{ report ->
                report.conventionMapping.with{
                    enabled = { report.name == 'xml' }
                    destination = { new File(extension.reportsDir, "cpd.${report.name}") }
                }
            }
        }
    }

    private Configuration createConfiguration(Project project, CpdExtension extension) {
        Configuration configuration = project.configurations.create('cpd')
        configuration.with{
            description = 'The CPD libraries to be used for this project.'
            incoming.beforeResolve{
                if (dependencies.isEmpty()) {
                    dependencies.add(project.dependencies.create("net.sourceforge.pmd:pmd:${extension.toolVersion}"))
                }
            }
            transitive = true
            visible = false

            // don't need these dependencies, they're provided by the runtime
            exclude group: 'ant', module: 'ant'
            exclude group: 'org.apache.ant', module: 'ant'
            exclude group: 'org.apache.ant', module: 'ant-launcher'
        }
        return configuration
    }

}
