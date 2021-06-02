package de.aaschmid.gradle.plugins.cpd;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import de.aaschmid.gradle.plugins.cpd.test.GradleExtension;
import net.sourceforge.pmd.cpd.Tokenizer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Configuration.State;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.ReportingBasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LanguageBasePlugin;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.language.cpp.plugins.CppPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.Lang.JAVA;
import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.createProjectFiles;
import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.testFile;
import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.testFilesRecurseIn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.*;

@ExtendWith(GradleExtension.class)
class CpdPluginTest {

    @Test
    void CpdPlugin_shouldApplyRequiredReportingBasePlugin(Project project) {
        project.getPlugins().hasPlugin(ReportingBasePlugin.class);
    }

    @Test
    void CpdPlugin_shouldCreateAndConfigureCpdExtension(CpdExtension cpd) {
        assertThat(cpd.getEncoding()).isNull();
        assertThat(cpd.isIgnoreAnnotations()).isFalse();
        assertThat(cpd.isIgnoreIdentifiers()).isFalse();
        assertThat(cpd.isIgnoreFailures()).isFalse();
        assertThat(cpd.isIgnoreLiterals()).isFalse();
        assertThat(cpd.getLanguage()).isEqualTo("java");
        assertThat(cpd.getMinimumTokenCount()).isEqualTo(50);
        assertThat(cpd.isSkipDuplicateFiles()).isFalse();
        assertThat(cpd.isSkipLexicalErrors()).isFalse();
        assertThat(cpd.isSkipBlocks()).isTrue();
        assertThat(cpd.getSkipBlocksPattern()).isEqualTo(Tokenizer.DEFAULT_SKIP_BLOCKS_PATTERN);
        assertThat(cpd.getToolVersion()).isEqualTo("6.14.0");
    }

    @Test
    void CpdPlugin_shouldCreateAndConfigureCpdConfiguration(Configuration cpdConfiguration) {
        assertThat(cpdConfiguration).isNotNull();
        assertThat(cpdConfiguration.getDependencies()).isEmpty();
        assertThat(cpdConfiguration.getDescription()).isEqualTo("The CPD libraries to be used for this project.");
        assertThat(cpdConfiguration.getExtendsFrom()).isEmpty();
        assertThat(cpdConfiguration.getState()).isEqualTo(State.UNRESOLVED);
        assertThat(cpdConfiguration.isTransitive()).isTrue();
        assertThat(cpdConfiguration.isVisible()).isFalse();
    }

    @Test
    void CpdPlugin_shouldCreateAndConfigureCpdCheckTaskWithCorrectDefaultValues(Project project, Configuration cpdConfiguration, TaskProvider<Cpd> cpdCheck) {
        Cpd t = cpdCheck.get();

        assertThat(t).isInstanceOf(Cpd.class);
        assertThat(t.getDescription()).isEqualTo("Run CPD analysis for all sources");
        assertThat(t.getGroup()).isNull();

        assertThat(t.getEncoding()).isNull();
        assertThat(t.getEncodingOrFallback()).isEqualTo(System.getProperty("file.encoding"));
        assertThat(t.getIgnoreAnnotations()).isFalse();
        assertThat(t.getIgnoreFailures()).isFalse();
        assertThat(t.getIgnoreIdentifiers()).isFalse();
        assertThat(t.getIgnoreLiterals()).isFalse();
        assertThat(t.getLanguage()).isEqualTo("java");
        assertThat(t.getMinimumTokenCount()).isEqualTo(50);

        assertThat(t.getPmdClasspath()).isEqualTo(cpdConfiguration);

        assertThat(t.getReports().getCsv().getOutputLocation().get().getAsFile()).isEqualTo(project.file("build/reports/cpd/cpdCheck.csv"));
        assertThat(t.getReports().getCsv().getRequired().get()).isFalse();
        assertThat(t.getReports().getText().getOutputLocation().get().getAsFile()).isEqualTo(project.file("build/reports/cpd/cpdCheck.text"));
        assertThat(t.getReports().getText().getRequired().get()).isFalse();
        assertThat(t.getReports().getVs().getOutputLocation().get().getAsFile()).isEqualTo(project.file("build/reports/cpd/cpdCheck.vs"));
        assertThat(t.getReports().getVs().getRequired().get()).isFalse();
        assertThat(t.getReports().getXml().getOutputLocation().get().getAsFile()).isEqualTo(project.file("build/reports/cpd/cpdCheck.xml"));
        assertThat(t.getReports().getXml().getRequired().get()).isTrue();

        assertThat(t.getSkipDuplicateFiles()).isFalse();
        assertThat(t.getSkipLexicalErrors()).isFalse();
        assertThat(t.getSkipBlocks()).isTrue();
        assertThat(t.getSkipBlocksPattern()).isEqualTo(Tokenizer.DEFAULT_SKIP_BLOCKS_PATTERN);

        assertThat(t.getSource()).isEmpty();
    }

    @Test
    void CpdPlugin_shouldConfigureProperDefaultsForAdditionalCpdTask(Project project, Configuration cpdConfiguration) {
        Cpd t = project.getTasks().create("cpdCustom", Cpd.class);

        // expect:
        assertThat(t).isInstanceOf(Cpd.class);
        assertThat(t.getDescription()).isNull();
        assertThat(t.getGroup()).isNull();

        assertThat(t.getEncoding()).isNull();
        assertThat(t.getEncodingOrFallback()).isEqualTo(System.getProperty("file.encoding"));
        assertThat(t.getIgnoreAnnotations()).isFalse();
        assertThat(t.getIgnoreFailures()).isFalse();
        assertThat(t.getIgnoreIdentifiers()).isFalse();
        assertThat(t.getIgnoreLiterals()).isFalse();
        assertThat(t.getLanguage()).isEqualTo("java");
        assertThat(t.getMinimumTokenCount()).isEqualTo(50);

        assertThat(t.getPmdClasspath()).isEqualTo(cpdConfiguration);

        assertThat(t.getReports().getCsv().getOutputLocation().get().getAsFile()).isEqualTo(project.file("build/reports/cpd/cpdCustom.csv"));
        assertThat(t.getReports().getCsv().getRequired().get()).isFalse();
        assertThat(t.getReports().getText().getOutputLocation().get().getAsFile()).isEqualTo(project.file("build/reports/cpd/cpdCustom.text"));
        assertThat(t.getReports().getText().getRequired().get()).isFalse();
        assertThat(t.getReports().getVs().getOutputLocation().get().getAsFile()).isEqualTo(project.file("build/reports/cpd/cpdCustom.vs"));
        assertThat(t.getReports().getVs().getRequired().get()).isFalse();
        assertThat(t.getReports().getXml().getOutputLocation().get().getAsFile()).isEqualTo(project.file("build/reports/cpd/cpdCustom.xml"));
        assertThat(t.getReports().getXml().getRequired().get()).isTrue();

        assertThat(t.getSkipDuplicateFiles()).isFalse();
        assertThat(t.getSkipLexicalErrors()).isFalse();
        assertThat(t.getSkipBlocks()).isTrue();
        assertThat(t.getSkipBlocksPattern()).isEqualTo(Tokenizer.DEFAULT_SKIP_BLOCKS_PATTERN);

        assertThat(t.getSource()).isEmpty();
    }

    static Stream<Class<? extends Plugin>> CpdPlugin_shouldAddCpdCheckTaskAsDependencyOfCheckLifecycleTaskIfPluginIsApplied() {
        return Stream.of(
                LifecycleBasePlugin.class,
                BasePlugin.class,
                LanguageBasePlugin.class,
                JavaBasePlugin.class,

                JavaPlugin.class,
                GroovyPlugin.class,
                CppPlugin.class
            );
    }

    @ParameterizedTest
    @MethodSource
    void CpdPlugin_shouldAddCpdCheckTaskAsDependencyOfCheckLifecycleTaskIfPluginIsApplied(Class<? extends Plugin> pluginClass, Project project, TaskProvider<Cpd> cpdCheck) {
        // When:
        project.getPlugins().apply(pluginClass);

        // Then:
        Task check = project.getTasks().getByName("check");
        @SuppressWarnings("unchecked") Set<Task> dependencies = (Set<Task>) check.getTaskDependencies().getDependencies(check);

        assertThat(check.getDependsOn()).contains(cpdCheck);
        assertThat(dependencies).contains(cpdCheck.get());
    }

    @Test
    void CpdPlugin_shouldAddCpdCheckTaskAsDependencyOfCheckLifecycleTaskIfJavaPluginIsApplied(Project project, TaskProvider<Cpd> cpdCheck) {
        // When:
        project.getPlugins().apply(JavaBasePlugin.class);

        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().create("tmp", (SourceSet sourceSet) ->
                sourceSet.getJava().srcDir(testFile(JAVA, ".")));

        Task checkTask = project.getTasks().getByName("check");

        // Then:
        assertThat((Set<Task>) checkTask.getTaskDependencies().getDependencies(checkTask)).contains(cpdCheck.get());
        assertThat(cpdCheck.get().getSource()).containsExactlyInAnyOrderElementsOf(testFilesRecurseIn(JAVA, "."));
    }

    @Test
    void CpdPlugin_shouldSetCpdCheckSourceEqualsToMainAndTestSourceSetsIfJavaPluginIsApplied(Project project, TaskProvider<Cpd> cpd) {
        // Given:
        String mainFile = "src/main/java/Clazz.java";
        String testFile = "src/test/java/ClazzTest.java";

        // When:
        project.getPlugins().apply(JavaPlugin.class);
        createProjectFiles(project, mainFile, "src/resources/java/message.properties", testFile);

        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME, sourceSet -> {
            sourceSet.getJava().srcDir(testFile(JAVA, "de/aaschmid/annotation"));
            sourceSet.getAllJava().srcDir(testFile(JAVA, "de/aaschmid/clazz"));
            sourceSet.getResources().srcDir(testFile(JAVA, "de/aaschmid/foo"));
        });
        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME, sourceSet ->
                sourceSet.getJava().srcDir(testFile(JAVA, "de/aaschmid/test")));

        // Then:
        List<File> expected = testFilesRecurseIn(JAVA, "de/aaschmid/annotation", "de/aaschmid/clazz", "de/aaschmid/test");
        expected.add(project.file(mainFile));
        expected.add(project.file(testFile));

        assertThat(cpd.get().getSource()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void CpdPlugin_shouldAddSourcesOfSubProjectsEvenIfAppliedOnlyOnParentProject(Project project, TaskProvider<Cpd> cpdCheck) {
        // When:
        Project subProject1 = ProjectBuilder.builder().withName("sub1").withParent(project).build();
        subProject1.getPlugins().apply(JavaPlugin.class);
        createProjectFiles(subProject1, "src/main/java/Clazz.java", "src/test/java/ClazzTest.java");

        Project subProject2 = ProjectBuilder.builder().withName("sub2").withParent(project).build();
        subProject2.getPlugins().apply(GroovyPlugin.class);
        createProjectFiles(subProject2, "src/main/groovy/Clazz.groovy", "src/main/resources/clazz.properties");

        subProject1.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME, sourceSet -> {
            sourceSet.getJava().srcDir(testFile(JAVA, "de/aaschmid/annotation"));
            sourceSet.getAllJava().srcDir(testFile(JAVA, "de/aaschmid/clazz"));
            sourceSet.getResources().srcDir(testFile(JAVA, "de/aaschmid/foo"));
        });
        subProject2.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME, sourceSet ->
                sourceSet.getJava().srcDir(testFile(JAVA, "de/aaschmid/test")));

        // Then:
        List<File> expected = testFilesRecurseIn(JAVA, "de/aaschmid/annotation", "de/aaschmid/clazz", "de/aaschmid/test");
        expected.add(subProject1.file("src/main/java/Clazz.java"));
        expected.add(subProject1.file("src/test/java/ClazzTest.java"));
        expected.add(subProject2.file("src/main/groovy/Clazz.groovy"));

        assertThat(cpdCheck.get().getSource()).containsExactlyInAnyOrderElementsOf(expected);

        assertThat(subProject1.getTasks().findByName("cpdCheck")).isNull();
        assertThat(subProject2.getTasks().findByName("cpdCheck")).isNull();
    }

    @Test
    void CpdPlugin_shouldAllowConfigureToolDependenciesExplicitlyViaToolVersionProperty(Project project, Configuration cpdConfiguration, CpdExtension cpd, TaskProvider<Cpd> cpdCheck) {
        // Given:
        project.getRepositories().mavenLocal();
        project.getRepositories().mavenCentral();

        // When:
        cpd.setToolVersion("5.2.1");

        // Then:
        assertThat(cpdCheck.get().getPmdClasspath()).isEqualTo(cpdConfiguration);
        assertThat(cpdConfiguration.resolve()).anyMatch(file -> file.getName().equals("pmd-core-5.2.1.jar"));
    }

    @Test
    void CpdPlugin_shouldAllowConfigureToolDependenciesExplicitlyViaConfiguration(Project project, Configuration cpdConfiguration, TaskProvider<Cpd> cpdCheck) {
        // Given:
        project.getRepositories().mavenLocal();
        project.getRepositories().mavenCentral();

        // When:
        project.getDependencies().add("cpd", "net.sourceforge.pmd:pmd:5.0.2");

        // Then:
        assertThat(cpdCheck.get().getPmdClasspath()).isEqualTo(cpdConfiguration);
        assertThat(cpdConfiguration.resolve()).anyMatch(file -> file.getName().equals("pmd-5.0.2.jar"));
    }

    @Test
    void CpdPlugin_shouldAllowConfigureCpdCheckTaskViaCpdExtension(Project project, CpdExtension cpd, TaskProvider<Cpd> cpdCheck) {
        // Given:
        cpd.setEncoding("UTF-8");
        cpd.setIgnoreAnnotations(true);
        cpd.setIgnoreFailures(true);
        cpd.setIgnoreIdentifiers(true);
        cpd.setIgnoreLiterals(true);
        cpd.setLanguage("ruby");
        cpd.setMinimumTokenCount(25);
        cpd.setReportsDir(project.file("cpd-reports"));
        cpd.setSkipDuplicateFiles(true);
        cpd.setSkipLexicalErrors(true);
        cpd.setSkipBlocks(false);
        cpd.setSkipBlocksPattern("<|>");

        // When:
        Cpd task = cpdCheck.get();

        // Then:
        assertThat(task.getEncoding()).isEqualTo("UTF-8");
        assertThat(task.getIgnoreAnnotations()).isTrue();
        assertThat(task.getIgnoreFailures()).isTrue();
        assertThat(task.getIgnoreIdentifiers()).isTrue();
        assertThat(task.getIgnoreLiterals()).isTrue();
        assertThat(task.getLanguage()).isEqualTo("ruby");
        assertThat(task.getMinimumTokenCount()).isEqualTo(25);
        assertThat(task.getReports().getCsv().getOutputLocation().get().getAsFile()).isEqualTo(project.file("cpd-reports/cpdCheck.csv"));
        assertThat(task.getReports().getText().getOutputLocation().get().getAsFile()).isEqualTo(project.file("cpd-reports/cpdCheck.text"));
        assertThat(task.getReports().getVs().getOutputLocation().get().getAsFile()).isEqualTo(project.file("cpd-reports/cpdCheck.vs"));
        assertThat(task.getReports().getXml().getOutputLocation().get().getAsFile()).isEqualTo(project.file("cpd-reports/cpdCheck.xml"));
        assertThat(task.getSkipDuplicateFiles()).isTrue();
        assertThat(task.getSkipLexicalErrors()).isTrue();
        assertThat(task.getSkipBlocks()).isFalse();
        assertThat(task.getSkipBlocksPattern()).isEqualTo("<|>");
    }

    @Test
    void CpdPlugin_shouldAllowConfigureAdditionalCpdTaskViaExtension(Project project, CpdExtension cpd) {
        // Given:
        cpd.setLanguage("php");
        cpd.setMinimumTokenCount(250);

        // When:
        Cpd task = project.getTasks().create("cpdCustom", Cpd.class);

        // Then:
        assertThat(task.getLanguage()).isEqualTo("php");
        assertThat(task.getMinimumTokenCount()).isEqualTo(250);
    }
}
