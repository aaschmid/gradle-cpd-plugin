package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.Cpd;
import de.aaschmid.gradle.plugins.cpd.CpdCsvFileReport;
import de.aaschmid.gradle.plugins.cpd.test.GradleExtension;
import org.gradle.api.InvalidUserDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(GradleExtension.class)
class CpdCsvFileReportImplTest {

    @Test
    void CpdCsvFileReportImpl_shouldHaveDefaultSeparator(Cpd cpd) {
        // When:
        CpdCsvFileReportImpl result = new CpdCsvFileReportImpl("csv", cpd);

        // Then:
        assertThat(result.getSeparator()).isEqualTo(CpdCsvFileReport.DEFAULT_SEPARATOR);
    }

    @Test
    void setSeparator_shouldThrowInvalidUserDataExceptionIfSeparatorIsSetToNull(Cpd cpd) {
        // Given:
        CpdCsvFileReportImpl underTest = new CpdCsvFileReportImpl("csv", cpd);

        // Expect:
        assertThatThrownBy(() -> underTest.setSeparator(null))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessage("CSV report 'separator' must not be null.");
    }
}
