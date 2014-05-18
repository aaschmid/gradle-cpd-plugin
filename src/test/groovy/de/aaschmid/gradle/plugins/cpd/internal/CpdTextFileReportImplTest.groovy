package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.test.BaseSpec
import net.sourceforge.pmd.cpd.CSVRenderer
import net.sourceforge.pmd.cpd.*

class CpdTextFileReportImplTest extends BaseSpec {

    def underTest

    def setup() {
        underTest = new CpdTextFileReportImpl('text', tasks.cpd)
    }

    def "test 'createRenderer()' should return 'SimpleRenderer' with default 'lineSeparator' and 'trimLeadingCommonSourceWhitespaces'"() {
        when:
        def result = underTest.createRenderer()

        then:
        result instanceof SimpleRenderer
        result.separator == '====================================================================='
        result.trimLeadingWhitespace == false
    }

    def "test 'createRenderer()' should return 'SimpleRenderer' with set 'lineSeparator'"() {
        given:
        underTest.lineSeparator = '---------------------------------------------------------------------'

        when:
        def result = underTest.createRenderer()

        then:
        result instanceof SimpleRenderer
        result.separator == '---------------------------------------------------------------------'
    }

    def "test 'createRenderer()' should return 'SimpleRenderer' with set 'trimLeadingCommonSourceWhitespaces'"() {
        given:
        underTest.trimLeadingCommonSourceWhitespaces = true

        when:
        def result = underTest.createRenderer()

        then:
        result instanceof SimpleRenderer
        result.trimLeadingWhitespace == true
    }

    def "test 'createRenderer()' should return 'SimpleRenderer' with set 'lineSeparator' and 'trimLeadingCommonSourceWhitespaces'"() {
        given:
        underTest.lineSeparator = '/////////////////////////////////////////////////////////////////////'
        underTest.trimLeadingCommonSourceWhitespaces = true

        when:
        def result = underTest.createRenderer()

        then:
        result instanceof SimpleRenderer
        result.separator == '/////////////////////////////////////////////////////////////////////'
        result.trimLeadingWhitespace == true
    }
}
