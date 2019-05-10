package de.aaschmid.gradle.plugins.cpd;

import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.internal.artifacts.DefaultExcludeRule;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.ReportingBasePlugin;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.reverseOrder;

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
 * <pre>
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
 *     allprojects.findAll{ p -&gt; p.hasProperty('sourceSets') }.each{ p -&gt;
 *         p.sourceSets.all{ sourceSet -&gt; source sourceSet.allJava }
 *     }
 * }
 * </pre>
 *
 * @see CpdExtension
 * @see Cpd
 */
@Incubating
public class CpdPlugin implements Plugin<Project> {

    private static final String TASK_NAME_CPD_CHECK = "cpdCheck";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(ReportingBasePlugin.class);

        CpdExtension extension = createExtension(project);
        createConfiguration(project, extension);
        setupTaskDefaults(project, extension);

        TaskProvider<Cpd> taskProvider = project.getTasks().register(TASK_NAME_CPD_CHECK, Cpd.class, task -> {
            task.setDescription("Run CPD analysis for all sources");
            project.getAllprojects().forEach(p ->
                p.getPlugins().withType(JavaBasePlugin.class, plugin ->
                    p.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(sourceSet ->
                        sourceSet.getAllJava().getSrcDirTrees().forEach(srcDirTree -> task.source(srcDirTree))
                    )
                )
            );
        });

        project.getPlugins().withType(LifecycleBasePlugin.class, plugin ->
            project.getTasks().findByName(LifecycleBasePlugin.CHECK_TASK_NAME).dependsOn(taskProvider));

        project.getGradle().getTaskGraph().whenReady(graph -> {
            String projectPath = (project.getRootProject() == project) ? project.getPath() : project.getPath() + ":";
            if (!graph.hasTask(projectPath + TASK_NAME_CPD_CHECK)) {
                if (project.getLogger().isWarnEnabled()) {
                    Optional<Task> lastCheckTask = graph.getAllTasks().stream().sorted(reverseOrder()).filter(t ->
                            t.getName().endsWith(LifecycleBasePlugin.CHECK_TASK_NAME)).findFirst();
                    if (lastCheckTask.isPresent()) { // it is possible to just execute a task before 'check', e.g. "compileJava"
                        Task task = lastCheckTask.get();
                        String message = "\n" +
                                "WARNING: Due to the absence of '" + LifecycleBasePlugin.class.getSimpleName() +
                                "' on " + project + " the task ':" + TASK_NAME_CPD_CHECK +
                                "' could not be added to task graph. Therefore CPD will not be executed. To prevent this, manually add a task dependency of ':" +
                                TASK_NAME_CPD_CHECK + "' to a '" + LifecycleBasePlugin.CHECK_TASK_NAME +
                                "' task of a subproject.\n" +
                                "1) Directly to " + task.getProject() + ":\n" +
                                "    " + task.getName() + ".dependsOn(':" + TASK_NAME_CPD_CHECK + "')\n" +
                                "2) Indirectly, e.g. via " + project + ":\n" +
                                "    project('" + task.getProject().getPath() + "') {\n" +
                                "        plugins.withType(LifecycleBasePlugin) { // <- just required if 'java' plugin is applied within subproject\n" +
                                "            " + task.getName() + ".dependsOn(" + TASK_NAME_CPD_CHECK + ")\n" +
                                "        }\n" +
                                "    }\n";
                        project.getLogger().warn(message);
                    }
                }
            }
        });
    }

    private CpdExtension createExtension(Project project) {
        CpdExtension extension = project.getExtensions().create("cpd", CpdExtension.class);
        extension.setToolVersion("6.13.0");
        return extension;
    }

    /** Set up task defaults for every created {@link Cpd} task. */
    private void setupTaskDefaults(Project project, CpdExtension extension) {
        project.getTasks().withType(Cpd.class).configureEach(task -> {
            ConventionMapping taskMapping = task.getConventionMapping();
            taskMapping.map("encoding", extension::getEncoding);
            taskMapping.map("ignoreAnnotations", extension::isIgnoreAnnotations);
            taskMapping.map("ignoreIdentifiers", extension::isIgnoreIdentifiers);
            taskMapping.map("ignoreFailures", extension::isIgnoreFailures);
            taskMapping.map("ignoreLiterals", extension::isIgnoreLiterals);
            taskMapping.map("language", extension::getLanguage);
            taskMapping.map("minimumTokenCount", extension::getMinimumTokenCount);
            taskMapping.map("pmdClasspath", () -> project.getConfigurations().findByName("cpd"));
            taskMapping.map("skipDuplicateFiles", extension::isSkipDuplicateFiles);
            taskMapping.map("skipLexicalErrors", extension::isSkipLexicalErrors);
            taskMapping.map("skipBlocks", extension::isSkipBlocks);
            taskMapping.map("skipBlocksPattern", extension::getSkipBlocksPattern);

            ConventionMapping extensionMapping = ((IConventionAware) extension).getConventionMapping();
            extensionMapping.map("reportsDir", () -> project.getExtensions().getByType(ReportingExtension.class).file("cpd"));

            task.getReports().all(report -> {
                ConventionMapping reportMapping = ((IConventionAware) report).getConventionMapping();
                reportMapping.map("enabled", () -> "xml".equals(report.getName()));
                reportMapping.map("destination", () -> new File(extension.getReportsDir(), task.getName() + "." + report.getName()));
            });
        });
    }

    private void createConfiguration(Project project, CpdExtension extension) {
        Configuration configuration = project.getConfigurations().create("cpd");
        configuration.setDescription("The CPD libraries to be used for this project.");
        configuration.setTransitive(true);
        configuration.setVisible(false);

        configuration.defaultDependencies(d ->
                d.add(project.getDependencies().create("net.sourceforge.pmd:pmd-dist:" + extension.getToolVersion())));
    }

    private Map<String, String> mapOf(DefaultExcludeRule excludeRule) {
        Map<String, String> result = new HashMap<>();
        result.put("group", excludeRule.getGroup());
        result.put("module", excludeRule.getModule());
        return result;
    }
}
