package de.aaschmid.gradle.plugins.cpd

import org.gradle.api.Incubating
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin

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
 * The created {@link Cpd} task is added to the {@code check} lifecycle task of {@link LifecycleBasePlugin} if it is also
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
 *     // As PMD was split with v5.2.0 and CPD has moved to 'pmd-core', 'toolVersion' is just available for 5.2.0 and higher
 *     toolVersion = 5.2.1
 * }
 *
 * tasks.cpd {
 *     allprojects.findAll{ p -> p.hasProperty('sourceSets') }.each{ p ->
 *         p.sourceSets.all{ sourceSet -> source sourceSet.allJava }
 *     }
 * }
 * </pre>
 *
 * @see CpdExtension
 * @see Cpd
 */
@Incubating
class CpdPlugin implements Plugin<Project> {

    private static final Logger logger = Logging.getLogger(Cpd.class);
    private static final String TASK_NAME_CPD_CHECK = 'cpdCheck'

    protected Project project
    protected CpdExtension extension

    @Override
    void apply(Project project) {
        this.project = project

        project.plugins.apply(ReportingBasePlugin)

        extension = createExtension(project)
        createConfiguration(project, extension)
        setupTaskDefaults(project, extension)

        TaskProvider<Cpd> taskProvider = project.tasks.register(TASK_NAME_CPD_CHECK, Cpd) { Cpd task ->
            task.description = 'Run CPD analysis for all sources'
            project.getAllprojects().each { p ->
                p.plugins.withType(JavaBasePlugin) {
                    p.sourceSets.all { SourceSet sourceSet ->
                        task.source({
                            sourceSet.allJava.srcDirTrees.each { srcDirTree ->
                                task.source(srcDirTree)
                            }
                        })
                    }
                }
            }
        }
        project.plugins.withType(LifecycleBasePlugin){
            project.tasks.findByName(LifecycleBasePlugin.CHECK_TASK_NAME).dependsOn(taskProvider)
        }
        project.gradle.taskGraph.whenReady{ TaskExecutionGraph graph ->
            if (!graph.hasTask(":${TASK_NAME_CPD_CHECK}")) {
                if (logger.isWarnEnabled()) {
                    def lastCheckTask = graph.allTasks.reverse().find{ t -> t.name.endsWith(LifecycleBasePlugin.CHECK_TASK_NAME) }
                    if (lastCheckTask) { // it is possible to just execute a task before 'check', e.g. "compileJava"
                        logger.warn("""\
                                WARNING: Due to the absence of '${LifecycleBasePlugin.simpleName}' on ${project}
                                the ${taskProvider} could not be added to task graph and therefore will not be executed.
                                SUGGESTION: add a dependency to ${taskProvider.name} manually to a subprojects '${LifecycleBasePlugin.CHECK_TASK_NAME}'
                                task, e.g. to ${lastCheckTask.project} using

                                    ${lastCheckTask.name}.dependsOn('${taskProvider.name}')

                                or to ${project} using

                                    project('${lastCheckTask.project.path}') {
                                        plugins.withType(LifecycleBasePlugin) { // <- just required if 'java' plugin is applied within subproject
                                            ${lastCheckTask.name}.dependsOn(${taskProvider.name})
                                        }
                                    }
                            """.stripIndent())
                    }
                }
            }
        }
    }

    private CpdExtension createExtension(Project project) {
        CpdExtension extension = project.extensions.create('cpd', CpdExtension, project)

        // set constant values directly
        extension.with{
            toolVersion = '5.4.2'
        }
        // use conventionMapping for values derived based on some external value
        extension.conventionMapping.with{
            reportsDir = { project.extensions.getByType(ReportingExtension).file('cpd') }
        }
        return extension
    }

    /** Set up task defaults for every created {@link Cpd} task. */
    private void setupTaskDefaults(Project project, CpdExtension extension) {
        project.tasks.withType(Cpd).configureEach { Cpd task ->
            task.conventionMapping.with{
                encoding = { extension.encoding }
                ignoreAnnotations = { extension.ignoreAnnotations }
                ignoreFailures = { extension.ignoreFailures }
                ignoreIdentifiers = { extension.ignoreIdentifiers }
                ignoreLiterals = { extension.ignoreLiterals }
                language = { extension.language }
                minimumTokenCount = { extension.minimumTokenCount }
                pmdClasspath = { project.configurations.findByName('cpd') }
                skipDuplicateFiles = { extension.skipDuplicateFiles }
                skipLexicalErrors = { extension.skipLexicalErrors }
                skipBlocks = { extension.skipBlocks }
                skipBlocksPattern = { extension.skipBlocksPattern }
            }
            task.reports.all{ report ->
                report.conventionMapping.with{
                    enabled = { report.name == 'xml' }
                    destination = { new File(extension.reportsDir, "${task.name}.${report.name}") }
                }
            }
        }
    }

    private Configuration createConfiguration(Project project, CpdExtension extension) {
        Configuration configuration = project.configurations.create('cpd')
        configuration.with{
            description = 'The CPD libraries to be used for this project.'

            configuration.defaultDependencies{ d ->
                d.add(project.getDependencies().create("net.sourceforge.pmd:pmd-dist:${extension.toolVersion}"))
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
