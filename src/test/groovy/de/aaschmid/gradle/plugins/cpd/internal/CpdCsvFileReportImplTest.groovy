package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.CpdCsvFileReport
import de.aaschmid.gradle.plugins.cpd.test.BaseSpec
import org.gradle.api.InvalidUserDataException

class CpdCsvFileReportImplTest extends BaseSpec {

    def "test 'new CpdCsvFileReportImpl(...)' should have default 'separator'"() {
        when:
        def result = new CpdCsvFileReportImpl('csv', tasks.cpd)

        then:
        result.separator == CpdCsvFileReport.DEFAULT_SEPARATOR
    }

    def "test 'setSeparator(Character)' should throw 'InvalidUserDataException' if 'separator' is set to 'null'"() {
        given:
        def underTest = new CpdCsvFileReportImpl('csv', tasks.cpd)

        when:
        underTest.separator = null

        then:
        def e = thrown InvalidUserDataException
        e.getMessage() ==~ /CSV report 'separator' must not be null./
    }
}
