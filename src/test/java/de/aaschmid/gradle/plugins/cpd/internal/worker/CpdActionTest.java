package de.aaschmid.gradle.plugins.cpd.internal.worker;

import java.io.File;
import java.util.List;
import java.util.Set;

import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdWorkParameters.Report;
import de.aaschmid.gradle.plugins.cpd.test.GradleExtension;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.CPPLanguage;
import net.sourceforge.pmd.cpd.CPPTokenizer;
import net.sourceforge.pmd.cpd.JavaLanguage;
import net.sourceforge.pmd.cpd.JavaTokenizer;
import net.sourceforge.pmd.cpd.KotlinLanguage;
import net.sourceforge.pmd.cpd.Match;
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
    }

    @Test
    void execute_shouldForwardCallCorrectly(Project project) {
        // Given:
        Set<File> sourceFiles = singleton(testFile(JAVA, "de/aaschmid/clazz/Clazz.java"));
        List<Report> reports = singletonList(new Report.Csv("UTF-8", new File("cpd.csv"), ';'));

        List<Match> matches = singletonList(mock(Match.class));
        when(executor.run(any(), eq(sourceFiles))).thenReturn(matches);

        stubParametersWithDefaults(project);
        when(parameters.getIgnoreFailures()).thenReturn(property(true));
        when(parameters.getSourceFiles()).thenReturn(project.files(sourceFiles));
        when(parameters.getReportParameters()).thenReturn(listProperty(Report.class, reports));

        // When:
        underTest.execute();

        // Then:
        InOrder inOrder = Mockito.inOrder(executor, reporter);
        inOrder.verify(executor).run(cpdConfiguration.capture(), eq(sourceFiles));
        inOrder.verify(reporter).generate(reports, matches);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void execute_shouldThrowGradleExceptionIfIgnoreFailuresIsFalse(Project project) {
        // Given:
        Report.Xml report = new Report.Xml("UTF-8", new File("cpd.xml"));

        List<Match> matches = singletonList(mock(Match.class));
        when(executor.run(any(), any())).thenReturn(matches);

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
        when(parameters.getLanguage()).thenReturn(property("kotlin"));
        when(parameters.getMinimumTokenCount()).thenReturn(property(15));
        when(parameters.getSkipDuplicateFiles()).thenReturn(property(true));
        when(parameters.getSkipLexicalErrors()).thenReturn(property(true));

        // When:
        underTest.execute();

        // Then:
        verify(executor).run(cpdConfiguration.capture(), any());

        CPDConfiguration actualCpdConfig = cpdConfiguration.getValue();
        assertThat(actualCpdConfig.getEncoding()).isEqualTo("US-ASCII");
        assertThat(actualCpdConfig.getLanguage()).isInstanceOf(KotlinLanguage.class);
        assertThat(actualCpdConfig.getMinimumTileSize()).isEqualTo(15);
        assertThat(actualCpdConfig.isSkipDuplicates()).isTrue();
        assertThat(actualCpdConfig.isSkipLexicalErrors()).isTrue();
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
        assertThat(actualCpdConfig.getLanguage()).isInstanceOf(JavaLanguage.class);
        assertThat(actualCpdConfig.getLanguage().getTokenizer())
                .isInstanceOf(JavaTokenizer.class)
                .hasFieldOrPropertyWithValue("ignoreAnnotations", true)
                .hasFieldOrPropertyWithValue("ignoreIdentifiers", true)
                .hasFieldOrPropertyWithValue("ignoreLiterals", true);
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
        assertThat(actualCpdConfig.getLanguage()).isInstanceOf(CPPLanguage.class);
        assertThat(actualCpdConfig.getLanguage().getTokenizer())
                .isInstanceOf(CPPTokenizer.class)
                .hasFieldOrPropertyWithValue("skipBlocks", true)
                .hasFieldOrPropertyWithValue("skipBlocksStart", "template<")
                .hasFieldOrPropertyWithValue("skipBlocksEnd", ">");
    }

    private void stubParametersWithDefaults(Project project) {
        Set<File> sourceFiles = singleton(testFile(JAVA, "de/aaschmid/clazz/Clazz.java"));
        Report.Text report = new Report.Text("UTF-8", new File("cpd.text"), "\n", false);

        when(parameters.getEncoding()).thenReturn(property("US-ASCII"));
        when(parameters.getIgnoreAnnotations()).thenReturn(property(false));
        when(parameters.getIgnoreIdentifiers()).thenReturn(property(false));
        when(parameters.getIgnoreLiterals()).thenReturn(property(false));
        when(parameters.getLanguage()).thenReturn(property("kotlin"));
        when(parameters.getMinimumTokenCount()).thenReturn(property(15));
        when(parameters.getSkipBlocks()).thenReturn(property(false));
        when(parameters.getSkipBlocksPattern()).thenReturn(property(" "));
        when(parameters.getSkipDuplicateFiles()).thenReturn(property(false));
        when(parameters.getSkipLexicalErrors()).thenReturn(property(false));
        when(parameters.getSourceFiles()).thenReturn(project.files(sourceFiles));
        when(parameters.getReportParameters()).thenReturn(listProperty(Report.class, singletonList(report)));
    }
}
