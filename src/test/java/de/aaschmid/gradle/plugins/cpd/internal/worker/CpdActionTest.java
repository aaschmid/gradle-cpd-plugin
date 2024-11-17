package de.aaschmid.gradle.plugins.cpd.internal.worker;

import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdWorkParameters.Report;
import de.aaschmid.gradle.plugins.cpd.test.GradleExtension;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.CPDReport;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.lang.LanguageVersionDiscoverer;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static de.aaschmid.gradle.plugins.cpd.test.PropertyUtils.listProperty;
import static de.aaschmid.gradle.plugins.cpd.test.PropertyUtils.property;
import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.Lang.JAVA;
import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.testFile;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(GradleExtension.class)
class CpdActionTest {

    private CpdAction underTest;

    @Mock
    private CpdWorkParameters parameters;
    @Mock
    private CpdExecutor executor;
    @Mock
    private CpdReporter reporter;

    @Captor
    private ArgumentCaptor<CPDConfiguration> cpdConfiguration;

    @BeforeEach
    void setUp() {
        underTest = new CpdAction(executor, reporter) {
            @Override
            public CpdWorkParameters getParameters() {
                return parameters;
            }
        };
        when(executor.run(any(), any())).thenReturn(mock(CPDReport.class));
    }

    @Test
    void execute_shouldForwardCallCorrectly(Project project) {
        // Given:
        Set<File> sourceFiles = singleton(testFile(JAVA, "de/aaschmid/clazz/Clazz.java"));
        List<Report> reports = singletonList(new Report.Csv(new File("cpd.csv"), ';', true));

        CPDReport cpdReport = mockReportFor(mock(Match.class));
        when(executor.run(any(), eq(sourceFiles))).thenReturn(cpdReport);

        stubParametersWithDefaults(project);
        when(parameters.getIgnoreFailures()).thenReturn(property(true));
        when(parameters.getSourceFiles()).thenReturn(project.files(sourceFiles));
        when(parameters.getReportParameters()).thenReturn(listProperty(Report.class, reports));

        // When:
        underTest.execute();

        // Then:
        InOrder inOrder = Mockito.inOrder(executor, reporter);
        inOrder.verify(executor).run(cpdConfiguration.capture(), eq(sourceFiles));
        inOrder.verify(reporter).generate(reports, cpdReport);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void execute_shouldThrowGradleExceptionIfIgnoreFailuresIsFalse(Project project) {
        // Given:
        Report.Xml report = new Report.Xml(new File("cpd.xml"), "UTF-8");

        CPDReport cpdReport = mockReportFor(mock(Match.class));
        when(executor.run(any(), any())).thenReturn(cpdReport);

        stubParametersWithDefaults(project);
        when(parameters.getIgnoreFailures()).thenReturn(property(false));
        when(parameters.getReportParameters()).thenReturn(listProperty(Report.class, singletonList(report)));

        // Expect:
        assertThatThrownBy(() -> underTest.execute())
                .isInstanceOf(GradleException.class)
                .hasMessageMatching("CPD found duplicate code\\. See the report at file://.+/cpd.xml");
    }

    @Test
    void execute_shouldCreateCorrectCpdConfigurationFromParameters(Project project) {
        // Given:
        stubParametersWithDefaults(project);
        when(parameters.getEncoding()).thenReturn(property("US-ASCII"));
        when(parameters.getFailOnError()).thenReturn(property(false));
        when(parameters.getFailOnViolation()).thenReturn(property(false));
        when(parameters.getLanguage()).thenReturn(property("kotlin"));
        when(parameters.getMinimumTokenCount()).thenReturn(property(15));
        when(parameters.getSkipDuplicateFiles()).thenReturn(property(true));

        // When:
        underTest.execute();

        // Then:
        verify(executor).run(cpdConfiguration.capture(), any());

        CPDConfiguration actualCpdConfig = cpdConfiguration.getValue();
        assertThat(actualCpdConfig.isFailOnError()).isFalse();
        assertThat(actualCpdConfig.isFailOnViolation()).isFalse();
        assertThat(actualCpdConfig.getSourceEncoding()).isEqualTo(StandardCharsets.US_ASCII);
        assertLanguage(actualCpdConfig, "kotlin");
        assertThat(actualCpdConfig.getMinimumTileSize()).isEqualTo(15);
        assertThat(actualCpdConfig.isSkipDuplicates()).isTrue();
    }

    @Test
    void execute_shouldSetCorrectJavaLanguageProperties(Project project) {
        // Given:
        stubParametersWithDefaults(project);
        when(parameters.getIgnoreAnnotations()).thenReturn(property(true));
        when(parameters.getIgnoreIdentifiers()).thenReturn(property(true));
        when(parameters.getIgnoreLiterals()).thenReturn(property(true));
        when(parameters.getLanguage()).thenReturn(property("java"));

        // When:
        underTest.execute();

        // Then:
        verify(executor).run(cpdConfiguration.capture(), any());

        CPDConfiguration actualCpdConfig = cpdConfiguration.getValue();
        assertLanguage(actualCpdConfig, "java");
        assertThat(actualCpdConfig.isIgnoreAnnotations()).isTrue();
        assertThat(actualCpdConfig.isIgnoreIdentifiers()).isTrue();
        assertThat(actualCpdConfig.isIgnoreLiterals()).isTrue();
    }

    @Test
    void execute_shouldSetCorrectCppProperties(Project project) {
        // Given:
        stubParametersWithDefaults(project);
        when(parameters.getLanguage()).thenReturn(property("cpp"));
        when(parameters.getSkipBlocks()).thenReturn(property(true));
        when(parameters.getSkipBlocksPattern()).thenReturn(property("template<|>"));

        // When:
        underTest.execute();

        // Then:
        verify(executor).run(cpdConfiguration.capture(), any());

        CPDConfiguration actualCpdConfig = cpdConfiguration.getValue();
        assertLanguage(actualCpdConfig, "cpp");
        assertThat(actualCpdConfig.isNoSkipBlocks()).isFalse();
        assertThat(actualCpdConfig.getSkipBlocksPattern()).isEqualTo("template<|>");
    }

    private void stubParametersWithDefaults(Project project) {
        Set<File> sourceFiles = singleton(testFile(JAVA, "de/aaschmid/clazz/Clazz.java"));
        Report.Text report = new Report.Text(new File("cpd.text"), "\n", false);

        when(parameters.getEncoding()).thenReturn(property("US-ASCII"));
        when(parameters.getFailOnError()).thenReturn(property(true));
        when(parameters.getFailOnViolation()).thenReturn(property(true));
        when(parameters.getIgnoreAnnotations()).thenReturn(property(false));
        when(parameters.getIgnoreIdentifiers()).thenReturn(property(false));
        when(parameters.getIgnoreLiterals()).thenReturn(property(false));
        when(parameters.getLanguage()).thenReturn(property("kotlin"));
        when(parameters.getMinimumTokenCount()).thenReturn(property(15));
        when(parameters.getSkipBlocks()).thenReturn(property(false));
        when(parameters.getSkipBlocksPattern()).thenReturn(property(" "));
        when(parameters.getSkipDuplicateFiles()).thenReturn(property(false));
        when(parameters.getSourceFiles()).thenReturn(project.files(sourceFiles));
        when(parameters.getReportParameters()).thenReturn(listProperty(Report.class, singletonList(report)));
    }

    private CPDReport mockReportFor(Match match) {
        List<Match> matches = singletonList(match);
        CPDReport cpdReport = mock(CPDReport.class);
        when(cpdReport.getMatches()).thenReturn(matches);
        return cpdReport;
    }

    private void assertLanguage(CPDConfiguration configuration, String languageId) {
        String fileThatShouldMatch;
        String fileThatShouldNotMatch = "foo.txt";
        switch (languageId) {
            case "java":
                fileThatShouldMatch = "foo.java";
                break;
            case "kotlin":
                fileThatShouldMatch = "foo.kt";
                break;
            case "cpp":
                fileThatShouldMatch = "foo.cpp";
                break;
            default:
                throw new UnsupportedOperationException("Unsupported language: " + languageId);
        }
        LanguageVersionDiscoverer languageVersionDiscoverer = configuration.getLanguageVersionDiscoverer();
        assertThat(languageVersionDiscoverer.getLanguagesForFile(fileThatShouldMatch)).isNotEmpty();
        assertThat(languageVersionDiscoverer.getLanguagesForFile(fileThatShouldNotMatch)).isEmpty();
    }
}
