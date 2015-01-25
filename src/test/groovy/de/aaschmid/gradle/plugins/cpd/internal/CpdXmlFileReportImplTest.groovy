package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.CpdCsvFileReport
import de.aaschmid.gradle.plugins.cpd.test.BaseSpec

class CpdXmlFileReportImplTest extends BaseSpec {

    def "test 'new CpdXmlFileReportImpl(...)' should keep 'Cpd' task and have 'null' default 'encoding'"() {
        when:
        def result = new CpdXmlFileReportImpl('csv', project.cpdCheck)

        then:
        result.task == project.cpdCheck
        result.encoding == null
    }

    def "test 'new CpdXmlFileReportImpl(...)' should not keep non-'Cpd' task"() {

        when:
        def result = new CpdXmlFileReportImpl('xml', project.task("tmp"))

        then:
        result.task == null
    }
}
