package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.Cpd;
import de.aaschmid.gradle.plugins.cpd.CpdCsvFileReport;
import de.aaschmid.gradle.plugins.cpd.test.GradleExtension;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemLocationProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@ExtendWith(GradleExtension.class)
class CpdCsvFileReportImplTest {

    @Test
    void CpdCsvFileReportImpl_shouldHaveDefaults(TaskProvider<Cpd> cpdCheck) {
        // When:
        CpdCsvFileReportImpl result = new CpdCsvFileReportImpl("csv", cpdCheck.get()) {
            // Very ugly but from 6.1 on the first two methods are implement by Gradle internally
            @Override
            public Property<Boolean> getRequired() {
                return mock(Property.class);
            }

            @Override
            public RegularFileProperty getOutputLocation() {
                return mock(RegularFileProperty.class);
            }

            @Override
            protected ProjectLayout getProjectLayout() {
                return mock(ProjectLayout.class);
            }
        };

        // Then:
        assertThat(result.getSeparator()).isEqualTo(CpdCsvFileReport.DEFAULT_SEPARATOR);
        assertThat(result.isIncludeLineCount()).isEqualTo(CpdCsvFileReport.DEFAULT_INCLUDE_LINE_COUNT);
    }

    @Test
    void setSeparator_shouldThrowInvalidUserDataExceptionIfSeparatorIsSetToNull(TaskProvider<Cpd> cpdCheck) {
        // Given:
        CpdCsvFileReportImpl underTest = new CpdCsvFileReportImpl("csv", cpdCheck.get()) {
            // Very ugly but from 6.1 and up the first two methods are implement by Gradle internally
            @Override
            public Property<Boolean> getRequired() {
                return mock(Property.class);
            }

            @Override
            public RegularFileProperty getOutputLocation() {
                return mock(RegularFileProperty.class);
            }

            @Override
            protected ProjectLayout getProjectLayout() {
                return mock(ProjectLayout.class);
            }
        };

        // Expect:
        assertThatThrownBy(() -> underTest.setSeparator(null))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessage("CSV report 'separator' must not be null.");
    }
}
