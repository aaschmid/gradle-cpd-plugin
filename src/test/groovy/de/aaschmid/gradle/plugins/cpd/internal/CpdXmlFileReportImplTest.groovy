package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.test.BaseSpec

class CpdXmlFileReportImplTest extends BaseSpec {

    def "test 'new CpdXmlFileReportImpl(...)' should have 'null' default 'encoding'"() {
        when:
        def result = new CpdXmlFileReportImpl('csv', project.cpdCheck)

        then:
        result.encoding == null
    }
}
