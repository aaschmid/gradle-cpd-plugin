package de.aaschmid.gradle.plugins.cpd.internal;

import de.aaschmid.gradle.plugins.cpd.Cpd;
import de.aaschmid.gradle.plugins.cpd.test.GradleExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(GradleExtension.class)
class CpdXmlFileReportImplTest {

    @Test
    void CpdXmlFileReportImpl_shouldHaveNullAsDefaultEncoding(Cpd cpd) {
        // When:
        CpdXmlFileReportImpl result = new CpdXmlFileReportImpl("csv", cpd);

        // Then:
        assertThat(result.getEncoding()).isNull();;
    }
}
