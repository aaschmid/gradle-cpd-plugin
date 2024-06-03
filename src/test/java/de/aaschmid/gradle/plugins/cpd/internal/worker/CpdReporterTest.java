package de.aaschmid.gradle.plugins.cpd.internal.worker;

import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdWorkParameters.Report;
import de.aaschmid.gradle.plugins.cpd.test.TestTag;
import net.sourceforge.pmd.cpd.CPDReport;
import net.sourceforge.pmd.cpd.CPDReportRenderer;
import net.sourceforge.pmd.cpd.CSVRenderer;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.SimpleRenderer;
import net.sourceforge.pmd.cpd.VSRenderer;
import net.sourceforge.pmd.cpd.XMLRenderer;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.document.FileLocation;
import org.assertj.core.api.SoftAssertions;
import org.gradle.api.GradleException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.util.Arrays.asList;
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

        CPDReportRenderer cpdRenderer = mock(CPDReportRenderer.class);
        doThrow(new IOException("foo")).when(cpdRenderer).render(any(), any());

        CpdReporter underTestSpy = spy(underTest);
        doReturn(cpdRenderer).when(underTestSpy).createRendererFor(report);

        // Expect:
        assertThatThrownBy(() -> underTestSpy.generate(singletonList(report), mock(CPDReport.class)))
                .isInstanceOf(GradleException.class)
                .hasMessage("foo")
                .hasCauseInstanceOf(IOException.class);

    }

    @Tag(TestTag.INTEGRATION_TEST)
    @Test // ParameterizedTest not possible because Source provider does not allow arguments like @TempDir
    void generate_shouldGenerateReport(@TempDir Path tempDir) {
        // Given:
        File csvReportFile = tempDir.resolve("cpd.csv").toFile();
        Report.Csv csvReport = new Report.Csv(csvReportFile, ',', true);

        File csvReportFileWithoutLines = tempDir.resolve("cpdWithoutLines.csv").toFile();
        Report.Csv csvReportWithoutLines = new Report.Csv(csvReportFileWithoutLines, ';', false);

        File textReportFile = tempDir.resolve("cpd.text").toFile();
        Report.Text textReport = new Report.Text(textReportFile, "#######", false);

        File vsReportFile = tempDir.resolve("cpd.vs").toFile();
        Report.Vs vsReport = new Report.Vs(vsReportFile);

        File xmlReportFile = tempDir.resolve("cpd.xml").toFile();
        Report.Xml xmlReport = new Report.Xml(xmlReportFile, "ISO-8859-15");

        Mark mark = mockMarkWithLocation(1, 2);

        Match match = mock(Match.class);
        when(match.getLineCount()).thenReturn(1);
        when(match.getTokenCount()).thenReturn(5);
        when(match.iterator()).thenAnswer(a -> asList(mark, mark).iterator());

        CPDReport cpdReport = mock(CPDReport.class);
        when(cpdReport.getDisplayName(any())).thenReturn("Clazz1.java");
        when(cpdReport.getMatches()).thenReturn(asList(match, match));
        when(cpdReport.getSourceCodeSlice(any())).thenReturn(Chars.EMPTY);

        // When:
        underTest.generate(asList(csvReport, csvReportWithoutLines, textReport, vsReport, xmlReport), cpdReport);

        // Then:
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(contentOf(csvReportFile)).startsWith("lines,tokens,occurrences\n");
            softly.assertThat(contentOf(csvReportFileWithoutLines)).startsWith("tokens;occurrences\n");
            softly.assertThat(contentOf(textReportFile)).startsWith("Found a 1 line (5 tokens) duplication in the following files: \n");
            softly.assertThat(contentOf(vsReportFile)).startsWith("Clazz1.java(1): Between lines 1 and 2\n");
            softly.assertThat(contentOf(xmlReportFile)).startsWith("<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>\n");
        });
    }

    @Test
    void createRendererFor_shouldReturnCorrectlyConfiguredCsvRenderer() {
        // Given:
        Report.Csv report = new Report.Csv(new File("cpd.csv"), ';', true);

        // When:
        CPDReportRenderer result = underTest.createRendererFor(report);

        // Then:
        assertThat(result)
                .isInstanceOf(CSVRenderer.class)
                .hasFieldOrPropertyWithValue("separator", ';')
                .hasFieldOrPropertyWithValue("lineCountPerFile", false);
    }

    @Test
    void createRendererFor_shouldReturnCorrectlyConfiguredSimpleRenderer() {
        // Given:
        Report.Text report = new Report.Text(new File("cpd.txt"), "---", true);

        // When:
        CPDReportRenderer result = underTest.createRendererFor(report);

        // Then:
        assertThat(result)
                .isInstanceOf(SimpleRenderer.class)
                .hasFieldOrPropertyWithValue("separator", "---")
                .hasFieldOrPropertyWithValue("trimLeadingWhitespace", true);
    }

    @Test
    void createRendererFor_shouldReturnCorrectlyConfiguredVsRenderer() {
        // Given:
        Report.Vs report = new Report.Vs(new File("cpd.vs"));

        // When:
        CPDReportRenderer result = underTest.createRendererFor(report);

        // Then:
        assertThat(result).isInstanceOf(VSRenderer.class);
    }

    @Test
    void createRendererFor_shouldReturnCorrectlyConfiguredXmlRenderer() {
        // Given:
        Report.Xml report = new Report.Xml(new File("cpd.xml"), "ISO-8859-1");

        // When:
        CPDReportRenderer result = underTest.createRendererFor(report);

        // Then:
        assertThat(result)
                .isInstanceOf(XMLRenderer.class)
                .hasFieldOrPropertyWithValue("encoding", "ISO-8859-1");
    }

    @Test
    void createRendererFor_shouldThrowGradleExceptionOnUnknownReportType() {
        // Given:
        Report report = new Report(new File("cpd.xml")) {
        };

        // Expect:
        assertThatThrownBy(() -> underTest.createRendererFor(report))
                .isInstanceOf(GradleException.class)
                .hasMessage("Cannot create reports for unsupported type '" + report.getClass() + "'.");
    }

    @SuppressWarnings("SameParameterValue")
    private Mark mockMarkWithLocation(int startLine, int endLine) {
        Mark mark = mock(Mark.class);
        FileLocation location = mock(FileLocation.class);
        when(location.getStartLine()).thenReturn(startLine);
        when(location.getEndLine()).thenReturn(endLine);
        when(mark.getLocation()).thenReturn(location);
        return mark;
    }
}
