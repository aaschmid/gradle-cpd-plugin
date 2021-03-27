package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.Cpd;
import de.aaschmid.gradle.plugins.cpd.CpdTextFileReport;
import de.aaschmid.gradle.plugins.cpd.test.GradleExtension;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(GradleExtension.class)
class CpdTextFileReportImplTest {

    @Test
    void CpdTextFileReportImpl_shouldHaveDefaultLineSeparatorAndTrimLeadingCommonSourceWhitespaces(TaskProvider<Cpd> cpdCheck) {
        // When:
        CpdTextFileReportImpl result = new CpdTextFileReportImpl("text", cpdCheck.get()) {
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
        assertThat(result.getLineSeparator()).isEqualTo(CpdTextFileReport.DEFAULT_LINE_SEPARATOR);
        assertThat(result.getTrimLeadingCommonSourceWhitespaces()).isEqualTo(CpdTextFileReport.DEFAULT_TRIM_LEADING_COMMON_SOURCE_WHITESPACE);
    }
}
