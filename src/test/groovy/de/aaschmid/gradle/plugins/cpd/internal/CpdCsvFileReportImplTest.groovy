package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.test.BaseSpec
import net.sourceforge.pmd.cpd.CSVRenderer
import org.gradle.api.GradleException

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

    def "test 'createRenderer()' should throw 'GradleException' if 'separator' is set to null"() {
        when:
        underTest.separator = null

        then:
        def e = thrown GradleException
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
