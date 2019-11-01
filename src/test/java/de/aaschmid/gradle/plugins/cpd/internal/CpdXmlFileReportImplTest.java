package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.Cpd;
import de.aaschmid.gradle.plugins.cpd.test.GradleExtension;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(GradleExtension.class)
class CpdXmlFileReportImplTest {

    @Test
    void CpdXmlFileReportImpl_shouldHaveNullAsDefaultEncoding(TaskProvider<Cpd> cpdCheck) {
        // When:
        CpdXmlFileReportImpl result = new CpdXmlFileReportImpl("csv", cpdCheck.get());

        // Then:
        assertThat(result.getEncoding()).isNull();
    }
}
