package de.aaschmid.gradle.plugins.cpd.internal

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

    def "test 'getXmlRendererEncoding' should return set encoding"() {

        given:
        project.cpdCheck.encoding = 'ISO-8859-15'
        def result = new CpdXmlFileReportImpl('xml', project.cpdCheck)
        result.setEncoding('ISO-8859-1')

        expect:
        result.xmlRendererEncoding == 'ISO-8859-1'
    }

    def "test 'getXmlRendererEncoding' should return task encoding if no specific is set"() {

        given:
        project.cpdCheck.encoding = 'ISO-8859-15'
        def result = new CpdXmlFileReportImpl('xml', project.cpdCheck)
        result.setEncoding(null)

        expect:
        result.xmlRendererEncoding == 'ISO-8859-15'
    }

    def "test 'getXmlRendererEncoding' should return system encoding if non is set"() {

        given:
        project.cpdCheck.encoding = null
        def result = new CpdXmlFileReportImpl('xml', project.cpdCheck)
        result.setEncoding(null)

        expect:
        result.xmlRendererEncoding == System.getProperty("file.encoding")
    }
}
