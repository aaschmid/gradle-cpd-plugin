package de.aaschmid.gradle.plugins.cpd.internal.worker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdWorkParameters.Report;
import de.aaschmid.gradle.plugins.cpd.test.TestTag;
import net.sourceforge.pmd.cpd.CSVRenderer;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.SimpleRenderer;
import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.SourceCode.StringCodeLoader;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.XMLRenderer;
import net.sourceforge.pmd.cpd.renderer.CPDRenderer;
import org.gradle.api.GradleException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.contentOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CpdReporterTest {

    @InjectMocks
    CpdReporter underTest;

    @Test
    void generate_shouldReThrowRendererThrownIoExceptionAsGradleException(@TempDir Path tempDir) throws Exception {
        // Given:
        Report report = mock(Report.class);
        when(report.getDestination()).thenReturn(tempDir.resolve("report.file").toFile());

        CPDRenderer cpdRenderer = mock(CPDRenderer.class);
        doThrow(new IOException("foo")).when(cpdRenderer).render(any(), any());

        CpdReporter underTestSpy = spy(underTest);
        doReturn(cpdRenderer).when(underTestSpy).createRendererFor(report);

        // Expect:
        assertThatThrownBy(() -> underTestSpy.generate(singletonList(report), emptyList()))
                .isInstanceOf(GradleException.class)
                .hasMessage("foo")
                .hasCauseInstanceOf(IOException.class);

    }


    @Tag(TestTag.INTEGRATION_TEST)
    @Test
    void generate_shouldGenerateReport(@TempDir Path tempDir) {
        // Given:
        File csvReportFile = tempDir.resolve("cpd.csv").toFile();
        Report.Csv csvReport = new Report.Csv("UTF-8", csvReportFile, ',');

        File textReportFile = tempDir.resolve("cpd.text").toFile();
        Report.Text textReport = new Report.Text("UTF-8", textReportFile, "#######", false);

        Mark mark = new Mark(new TokenEntry("1", "Clazz1.java", 1));
        mark.setLineCount(1);
        mark.setSourceCode(new SourceCode(new StringCodeLoader("String str = \"I am a duplicate\";")));

        Match match = new Match(5, mark, mark);

        // When:
        underTest.generate(asList(csvReport, textReport), asList(match, match));

        // Then:
        assertThat(contentOf(csvReportFile)).startsWith("lines,tokens,occurrences\n");
        assertThat(contentOf(textReportFile)).startsWith("Found a 1 line (5 tokens) duplication in the following files: \n");
    }

    @Test
    void createRendererFor_shouldReturnCorrectlyConfiguredCsvRenderer() {
        // Given:
        Report.Csv report = new Report.Csv("UTF-8", new File("cpd.csv"), ';');

        // When:
        CPDRenderer result = underTest.createRendererFor(report);

        // Then:
        assertThat(result)
                .isInstanceOf(CSVRenderer.class)
                .hasFieldOrPropertyWithValue("separator", ';');
    }

    @Test
    void createRendererFor_shouldReturnCorrectlyConfiguredSimpleRenderer() {
        // Given:
        Report.Text report = new Report.Text("UTF-8", new File("cpd.txt"), "---", true);

        // When:
        CPDRenderer result = underTest.createRendererFor(report);

        // Then:
        assertThat(result)
                .isInstanceOf(SimpleRenderer.class)
                .hasFieldOrPropertyWithValue("separator", "---")
                .hasFieldOrPropertyWithValue("trimLeadingWhitespace", true);
    }

    @Test
    void createRendererFor_shouldReturnCorrectlyConfiguredXmlRenderer() {
        // Given:
        Report.Xml report = new Report.Xml("ISO-8859-1", new File("cpd.xml"));

        // When:
        CPDRenderer result = underTest.createRendererFor(report);

        // Then:
        assertThat(result)
                .isInstanceOf(XMLRenderer.class)
                .hasFieldOrPropertyWithValue("encoding", "ISO-8859-1");
    }

    @Test
    void createRendererFor_shouldThrowGradleExceptionOnUnknownReportType() {
        // Given:
        Report report = new Report("ISO-8859-1", new File("cpd.xml")) {
        };

        // Expect:
        assertThatThrownBy(() -> underTest.createRendererFor(report))
                .isInstanceOf(GradleException.class)
                .hasMessage("Cannot create reports for unsupported type '" + report.getClass() + "'.");
    }
}
