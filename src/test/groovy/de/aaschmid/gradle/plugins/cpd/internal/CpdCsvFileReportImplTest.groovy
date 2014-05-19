package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.test.BaseSpec
import net.sourceforge.pmd.cpd.CSVRenderer
import org.gradle.api.InvalidUserDataException

class CpdCsvFileReportImplTest extends BaseSpec {

    def underTest

    def setup() {
        underTest = new CpdCsvFileReportImpl('csv', tasks.cpd)
    }

    def "test 'createRenderer()' should return 'CsvRenderer' with default 'separator'"() {
        when:
        def result = underTest.createRenderer()

        then:
        result instanceof CSVRenderer
        result.separator == ','
    }

    def "test 'setSeparator(Character)' should throw 'InvalidUserDataException' if 'separator' is set to 'null'"() {
        when:
        underTest.separator = null

        then:
        def e = thrown InvalidUserDataException
        e.getMessage() ==~ /CSV report 'separator' must not be null./
    }

    def "test 'createRenderer()' should return 'CsvRenderer' with set 'separator'"() {
        given:
        underTest.separator = ';'

        when:
        def result = underTest.createRenderer()

        then:
        result instanceof CSVRenderer
        result.separator == ';'
    }
}
