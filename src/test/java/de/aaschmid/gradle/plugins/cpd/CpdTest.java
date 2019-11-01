package de.aaschmid.gradle.plugins.cpd;

import java.io.File;
import java.util.List;

import de.aaschmid.gradle.plugins.cpd.internal.CpdXmlFileReportImpl;
import de.aaschmid.gradle.plugins.cpd.test.GradleExtension;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.Lang.JAVA;
import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.createProjectFiles;
import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.testFile;
import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.testFilesRecurseIn;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;

@ExtendWith(GradleExtension.class)
class CpdTest {

    @Test
    void Cpd_shouldAllowConfigurationOfDefaultTaskProperties(Project project, TaskProvider<Cpd> cpdCheck) {
        // Given:
        Task dependantTask = project.getTasks().create("dependant");

        // When:
        cpdCheck.configure(task -> {
            task.setDependsOn(singleton(dependantTask));
            task.setDescription("Execute me!");
            task.setEnabled(false);
            task.setExcludes(asList("*.kt", "*.txt"));
            task.setGroup("check");
        });

        // Then:
        Cpd actual = cpdCheck.get();
        assertThat(actual.getDependsOn()).containsOnly(dependantTask);
        assertThat(actual.getDescription()).isEqualTo("Execute me!");
        assertThat(actual.getEnabled()).isFalse();
        assertThat(actual.getExcludes()).containsOnly("*.kt", "*.txt");
        assertThat(actual.getGroup()).isEqualTo("check");
    }

    @Test
    void Cpd_shouldAllowConfigurationOfSourceTaskProperties(TaskProvider<Cpd> cpdCheck) {
        // Given:
        cpdCheck.configure(task -> {
            task.exclude("**/literal/*");
            task.exclude("**/*z*.java");
            task.include("**/*2.java");
            task.source(testFile(JAVA, "."));
        });
        Cpd actual = cpdCheck.get();

        // Expect:
        assertThat(actual.getExcludes()).containsOnly("**/literal/*", "**/*z*.java");
        assertThat(actual.getIncludes()).containsOnly("**/*2.java");
        assertThat(actual.getSource()).containsOnly(testFile(JAVA, "de/aaschmid/identifier/Identifier2.java"));
    }

    @Test
    void Cpd_shouldAllowConfigurationOfCpdTaskProperties(Project project, TaskProvider<Cpd> cpdCheck) {
        // Given:
        List<File> expectedPmdClasspath = createProjectFiles(project, "libs/pmd-classpath/");

        // When:
        cpdCheck.configure(task -> {
            task.setDescription("Execute me!");
            task.setGroup("check");

            task.setEncoding("ISO-8859-1");
            task.setIgnoreAnnotations(true);
            task.setIgnoreFailures(true);
            task.setIgnoreIdentifiers(true);
            task.setIgnoreLiterals(true);
            task.setLanguage("cpp");
            task.setMinimumTokenCount(10);
            task.setPmdClasspath(project.files(expectedPmdClasspath));
            task.setSkipDuplicateFiles(true);
            task.setSkipLexicalErrors(true);
            task.setSkipBlocks(false);
            task.setSkipBlocksPattern("<template|>");
        });

        // Then:
        Cpd actual = cpdCheck.get();
        assertThat(actual.getEncoding()).isEqualTo("ISO-8859-1");
        assertThat(actual.getIgnoreAnnotations()).isTrue();
        assertThat(actual.getIgnoreFailures()).isTrue();
        assertThat(actual.getIgnoreIdentifiers()).isTrue();
        assertThat(actual.getIgnoreLiterals()).isTrue();
        assertThat(actual.getLanguage()).isEqualTo("cpp");
        assertThat(actual.getMinimumTokenCount()).isEqualTo(10);
        assertThat(actual.getPmdClasspath()).containsExactlyInAnyOrderElementsOf(expectedPmdClasspath);
        assertThat(actual.getSkipDuplicateFiles()).isTrue();
        assertThat(actual.getSkipLexicalErrors()).isTrue();
        assertThat(actual.getSkipBlocks()).isFalse();
        assertThat(actual.getSkipBlocksPattern()).isEqualTo("<template|>");
    }

    @Test
    void Cpd_shouldAllowConfigurationOfCpdTaskReportProperties(Project project, TaskProvider<Cpd> cpdCheck) {
        // When:
        cpdCheck.configure(task -> task.reports(reports -> {
            reports.getCsv().setDestination(project.file(project.getBuildDir() + "/cpdCheck.csv"));
            reports.getCsv().setEnabled(true);

            reports.getText().setDestination(project.file(project.getBuildDir() + "/cpdCheck.text"));
            reports.getText().setEnabled(true);

            reports.getXml().setDestination(project.file(project.getBuildDir() + "/reports/cpdCheck.xml"));
            reports.getXml().setEnabled(false);
        }));

        // Then:
        CpdReports actual = cpdCheck.get().getReports();
        assertThat(actual.getCsv().getDestination()).isEqualTo(project.file("build/cpdCheck.csv"));
        assertThat(actual.getCsv().isEnabled()).isTrue();
        assertThat(actual.getText().getDestination()).isEqualTo(project.file("build/cpdCheck.text"));
        assertThat(actual.getText().isEnabled()).isTrue();
        assertThat(actual.getXml().getDestination()).isEqualTo(project.file("build/reports/cpdCheck.xml"));
        assertThat(actual.getXml().isEnabled()).isFalse();
    }

    @Test
    void Cpd_shouldHaveCorrectTaskInputs(Project project, TaskProvider<Cpd> cpdCheck) {
        // Given:
        cpdCheck.configure(task -> {
            task.reports(report -> {
                report.getText().setDestination(project.file(project.getBuildDir() + "/cpdCheck.text"));
                report.getText().setEnabled(true);
            });
            task.source(testFile(JAVA, "de/aaschmid/clazz/"));
        });
        Cpd actual = cpdCheck.get();

        // Expect:
        assertThat(actual.getInputs().getProperties()).hasSize(44);
        assertThat(actual.getInputs().getSourceFiles()).containsExactlyInAnyOrderElementsOf(testFilesRecurseIn(JAVA, "de/aaschmid/clazz"));
    }

    @Test
    void Cpd_shouldHaveCorrectTaskOutputs(Project project, TaskProvider<Cpd> cpdCheck) {
        // Given:
        cpdCheck.configure(task -> {
            task.reports(report -> {
                report.getCsv().setDestination(project.file(project.getBuildDir() + "/cpd.csv"));
                report.getCsv().setEnabled(false);
                report.getText().setDestination(project.file("cpdCheck.txt"));
                report.getText().setEnabled(true);
            });
            task.source(testFile(JAVA, "."));
        });
        Cpd actual = cpdCheck.get();

        // Expect:
        assertThat(actual.getOutputs().getFiles()).containsExactlyInAnyOrder(
                project.file(project.getBuildDir() + "/cpd.csv"),
                project.file("cpdCheck.txt"),
                project.file(project.getBuildDir() + "/reports/cpd/cpdCheck.xml")
        );
    }

    @Test
    void Cpd_shouldThrowInvalidUserDataExceptionIfEncodingIsNull(TaskProvider<Cpd> cpdCheck) {
        // Given:
        cpdCheck.configure(task -> task.setEncoding(null));
        Cpd actual = cpdCheck.get();

        // Expect:
        assertThatThrownBy(() -> actual.getActions().forEach(a -> a.execute(actual)))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessage("Task 'cpdCheck' requires 'encoding' but was: null.");
    }

    @Test
    void Cpd_shouldThrowInvalidUserDataExceptionIfMinimumTokenCountIsMinusOne(TaskProvider<Cpd> cpdCheck) {
        // Given:
        cpdCheck.configure(task -> task.setMinimumTokenCount(-1));
        Cpd actual = cpdCheck.get();

        // Expect:
        assertThatThrownBy(() -> actual.getActions().forEach(a -> a.execute(actual)))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageMatching("Task 'cpdCheck' requires 'minimumTokenCount' to be greater than zero.");
    }

    @Test
    void Cpd_shouldThrowInvalidUserDataExceptionIfTwoReportsAreEnabled(TaskProvider<Cpd> cpdCheck) {
        // Given:
        cpdCheck.configure(task -> task.reports(report -> {
            report.getCsv().setEnabled(false);
            report.getText().setEnabled(false);
            report.getXml().setEnabled(false);
        }));
        Cpd actual = cpdCheck.get();

        // Expect:
        assertThatThrownBy(() -> actual.getActions().forEach(a -> a.execute(actual)))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessage("Task 'cpdCheck' requires at least one enabled report.");
    }

    static Arguments[] getXmlRendererEncoding() {
        return new Arguments[] {
                // @formatter:off
                of("ISO-8859-15",   "ISO-8859-1",   "ISO-8859-1"),
                of(null,            "ISO-8859-1",   "ISO-8859-1"),
                of("ISO-8859-15",   null,           "ISO-8859-15"),
                of("ISO-8859-15",   null,           "ISO-8859-15"),
                of(null,            null,           System.getProperty("file.encoding")),
                // @formatter:on
        };
    }

    @ParameterizedTest
    @MethodSource
    void getXmlRendererEncoding(String taskEncoding, String reportEncoding, String expected, TaskProvider<Cpd> cpdCheck) {
        // Given:
        cpdCheck.configure(task -> task.setEncoding(taskEncoding));

        CpdXmlFileReportImpl report = new CpdXmlFileReportImpl("xml", cpdCheck.get());
        report.setEncoding(reportEncoding);

        // Expect:
        assertThat(cpdCheck.get().getXmlRendererEncoding(report)).isEqualTo(expected);
    }
}
