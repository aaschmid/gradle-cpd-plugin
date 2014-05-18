package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.test.BaseSpec
import net.sourceforge.pmd.cpd.*

class CpdXmlFileReportImplTest extends BaseSpec {

    def underTest

    def setup() {
        underTest = new CpdXmlFileReportImpl('xml', tasks.cpd)
    }

    def "test 'createRenderer()' should return 'XmlRenderer' with set 'encoding'"() {
        given:
        underTest.encoding = 'ISO-8859-1'

        when:
        def result = underTest.createRenderer()

        then:
        result instanceof XMLRenderer
        result.encoding == 'ISO-8859-1'
    }

    def "test 'createRenderer()' should return 'XmlRenderer' with task 'encoding'"() {
        given:
        underTest.encoding = null
        tasks.cpd {
            encoding = 'US-ASCII'
        }

        when:
        def result = underTest.createRenderer()

        then:
        result instanceof XMLRenderer
        result.encoding == 'US-ASCII'
    }

    def "test 'createRenderer()' should return 'XmlRenderer' with java system property 'encoding'"() {
        given:
        underTest = new CpdXmlFileReportImpl('xml', project.task("tmp"))
        underTest.encoding = null

        System.setProperty('file.encoding', 'ASCII')

        when:
        def result = underTest.createRenderer()

        then:
        result instanceof XMLRenderer
        result.encoding == 'ASCII'
    }
}
