package de.aaschmid.gradle.plugins.cpd;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import de.aaschmid.gradle.plugins.cpd.internal.CpdXmlFileReportImpl;
import de.aaschmid.gradle.plugins.cpd.test.GradleExtension;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.Task;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
            reports.getCsv().getOutputLocation().set(project.file(project.getBuildDir() + "/cpdCheck.csv"));
            reports.getCsv().getRequired().set(true);
            reports.getCsv().setSeparator(';');
            reports.getCsv().setIncludeLineCount(false);

            reports.getText().getOutputLocation().set(project.file(project.getBuildDir() + "/cpdCheck.text"));
            reports.getText().getRequired().set(true);
            reports.getText().setLineSeparator("-_-");
            reports.getText().setTrimLeadingCommonSourceWhitespaces(true);

            reports.getVs().getOutputLocation().set(project.file("cpdCheck.vs"));
            reports.getVs().getRequired().set(true);

            reports.getXml().getOutputLocation().set(project.file(project.getBuildDir() + "/reports/cpdCheck.xml"));
            reports.getXml().getRequired().set(false);
            reports.getXml().setEncoding("UTF-16");
        }));

        // Then:
        CpdReports actual = cpdCheck.get().getReports();
        assertThat(actual.getCsv().getOutputLocation().get().getAsFile()).isEqualTo(project.file("build/cpdCheck.csv"));
        assertThat(actual.getCsv().getRequired().get()).isTrue();
        assertThat(actual.getCsv().getSeparator()).isEqualTo(';');
        assertThat(actual.getCsv().isIncludeLineCount()).isFalse();
        assertThat(actual.getText().getOutputLocation().get().getAsFile()).isEqualTo(project.file("build/cpdCheck.text"));
        assertThat(actual.getText().getRequired().get()).isTrue();
        assertThat(actual.getText().getLineSeparator()).isEqualTo("-_-");
        assertThat(actual.getText().getTrimLeadingCommonSourceWhitespaces()).isTrue();
        assertThat(actual.getVs().getOutputLocation().get().getAsFile()).isEqualTo(project.file("cpdCheck.vs"));
        assertThat(actual.getVs().getRequired().get()).isTrue();
        assertThat(actual.getXml().getOutputLocation().get().getAsFile()).isEqualTo(project.file("build/reports/cpdCheck.xml"));
        assertThat(actual.getXml().getRequired().get()).isFalse();
        assertThat(actual.getXml().getEncoding()).isEqualTo("UTF-16");
    }

    @Test
    void Cpd_shouldHaveCorrectTaskInputs(Project project, TaskProvider<Cpd> cpdCheck) {
        // Given:
        cpdCheck.configure(task -> {
            task.reports(report -> {
                report.getText().getOutputLocation().set(project.file(project.getBuildDir() + "/cpdCheck.text"));
                report.getText().getRequired().set(true);
            });
            task.source(testFile(JAVA, "de/aaschmid/clazz/"));
        });
        Cpd actual = cpdCheck.get();

        // Expect:
        assertThat(actual.getInputs().getProperties()).hasSize(50);
        assertThat(actual.getInputs().getSourceFiles()).containsExactlyInAnyOrderElementsOf(testFilesRecurseIn(JAVA, "de/aaschmid/clazz"));
    }

    @Test
    void Cpd_shouldHaveCorrectTaskOutputs(Project project, TaskProvider<Cpd> cpdCheck) {
        // Given:
        cpdCheck.configure(task -> {
            task.reports(report -> {
                report.getCsv().getOutputLocation().set(project.file(project.getBuildDir() + "/cpd.csv"));
                report.getCsv().getRequired().set(false);
                report.getText().getOutputLocation().set(project.file("cpdCheck.txt"));
                report.getText().getRequired().set(true);
                report.getVs().getOutputLocation().set(project.file("cpd.vs"));
            });
            task.source(testFile(JAVA, "."));
        });
        Cpd actual = cpdCheck.get();

        // Expect:
        assertThat(actual.getOutputs().getFiles()).containsExactlyInAnyOrder(
                project.file(project.getBuildDir() + "/cpd.csv"),
                project.file("cpdCheck.txt"),
                project.file("cpd.vs"),
                project.file(project.getBuildDir() + "/reports/cpd/cpdCheck.xml")
        );
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
            report.getCsv().getRequired().set(false);
            report.getText().getRequired().set(false);
            report.getVs().getRequired().set(false);
            report.getXml().getRequired().set(false);
        }));
        Cpd actual = cpdCheck.get();

        // Expect:
        assertThatThrownBy(() -> actual.getActions().forEach(a -> a.execute(actual)))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessage("Task 'cpdCheck' requires at least one required report.");
    }

    static Arguments[] getXmlRendererEncoding() {
        return new Arguments[] {
                // @formatter:off
                of("ISO-8859-15",   "ISO-8859-1",   "ISO-8859-1"),
                of(null,            "ISO-8859-1",   "ISO-8859-1"),
                of("ISO-8859-15",   null,           "ISO-8859-15"),
                of("ISO-8859-15",   null,           "ISO-8859-15"),
                of(null,            null,           Charset.defaultCharset().displayName()),
                // @formatter:on
        };
    }

    @ParameterizedTest
    @MethodSource
    void getXmlRendererEncoding(String taskEncoding, String reportEncoding, String expected, TaskProvider<Cpd> cpdCheck) {
        // Given:
        cpdCheck.configure(task -> task.setEncoding(taskEncoding));

        CpdXmlFileReportImpl report = mock(CpdXmlFileReportImpl.class);
        when(report.getEncoding()).thenReturn(reportEncoding);

        // Expect:
        assertThat(cpdCheck.get().getXmlRendererEncoding(report)).isEqualTo(expected);
    }
}
