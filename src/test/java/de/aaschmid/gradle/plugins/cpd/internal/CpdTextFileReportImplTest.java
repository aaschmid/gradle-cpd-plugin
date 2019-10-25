package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.Cpd;
import de.aaschmid.gradle.plugins.cpd.CpdTextFileReport;
import de.aaschmid.gradle.plugins.cpd.test.GradleExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(GradleExtension.class)
class CpdTextFileReportImplTest {

    @Test
    void CpdTextFileReportImpl_shouldHaveDefaultLineSeparatorAndTrimLeadingCommonSourceWhitespaces(Cpd cpd) {
        // When:
        CpdTextFileReportImpl result = new CpdTextFileReportImpl("text", cpd);

        // Then:
        assertThat(result.getLineSeparator()).isEqualTo(CpdTextFileReport.DEFAULT_LINE_SEPARATOR);
        assertThat(result.getTrimLeadingCommonSourceWhitespaces()).isEqualTo(CpdTextFileReport.DEFAULT_TRIM_LEADING_COMMON_SOURCE_WHITESPACE);
    }
}
