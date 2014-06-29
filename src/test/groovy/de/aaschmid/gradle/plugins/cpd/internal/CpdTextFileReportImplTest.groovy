package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.CpdTextFileReport
import de.aaschmid.gradle.plugins.cpd.test.BaseSpec

class CpdTextFileReportImplTest extends BaseSpec {

    def "test 'new CpdTextFileReportImpl(...)' should have default 'lineSeparator' and 'trimLeading...'"() {
        when:
        def result = new CpdTextFileReportImpl('text', tasks.cpd)

        then:
        result.lineSeparator == CpdTextFileReport.DEFAULT_LINE_SEPARATOR
        result.trimLeadingCommonSourceWhitespaces == CpdTextFileReport.DEFAULT_TRIM_LEADING_COMMON_SOURCE_WHITESPACE
    }
}
