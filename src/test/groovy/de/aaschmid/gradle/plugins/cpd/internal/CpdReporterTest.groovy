package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.test.BaseSpec
import net.sourceforge.pmd.cpd.CSVRenderer
import net.sourceforge.pmd.cpd.Mark
import net.sourceforge.pmd.cpd.Match
import net.sourceforge.pmd.cpd.SimpleRenderer
import net.sourceforge.pmd.cpd.SourceCode
import net.sourceforge.pmd.cpd.SourceCode.StringCodeLoader
import net.sourceforge.pmd.cpd.TokenEntry
import net.sourceforge.pmd.cpd.XMLRenderer
import org.gradle.api.InvalidUserDataException

class CpdReporterTest extends BaseSpec {

    def underTest

    def setup() {
        underTest = new CpdReporter(project.tasks.findByName('cpdCheck'))
    }

    def "test 'new CpdReporter(null)' should throw 'NullPointerException'"() {
        when:
        new CpdReporter(null)

        then:
        def e = thrown NullPointerException
        e.getMessage() ==~ /task must not be null/
    }

    def "test 'new CpdReporter(...)' should throw 'InvalidUserDataException' if encoding is 'null'"() {
        given:
        project.cpdCheck.encoding = null

        when:
        new CpdReporter(project.cpdCheck)

        then:
        def e = thrown InvalidUserDataException
        e.getMessage() ==~ /Task '.+' requires encoding but was: null./
    }

    def "test 'new CpdReporter(...)' should throw 'InvalidUserDataException' if no report is enabled"() {
        given:
        project.cpdCheck.reports{
            csv.enabled = false
            text.enabled = false
            xml.enabled = false
        }

        when:
        new CpdReporter(project.cpdCheck)

        then:
        def e = thrown InvalidUserDataException
        e.getMessage() ==~ /Task '.+' requires exactly one report to be enabled but was: \[\]\./
    }

    def "test 'new CpdReporter(...)' should throw 'InvalidUserDataException' if two reports are enabled"() {
        given:
        project.cpdCheck.reports{
            csv.enabled = true
            text.enabled = false
            xml.enabled = true
        }

        when:
        new CpdReporter(project.cpdCheck)

        then:
        def e = thrown InvalidUserDataException
        e.getMessage() ==~ /Task '.+' requires exactly one report to be enabled but was: \[csv, xml\]\./
    }

    def "test 'new CpdReporter(...)' should throw 'InvalidUserDataException' if destination of enabled report is 'null'"() {
        given:
        project.cpdCheck.reports{
            csv{
                enabled = true
                destination = null
            }
            text.enabled = false
            xml.enabled = false
        }

        when:
        new CpdReporter(project.cpdCheck)

        then:
        def e = thrown InvalidUserDataException
        e.getMessage() ==~ /'.*csv' requires valid destination but was 'null'\./
    }

    def "test 'new CpdReporter(...)' should get correct values from task"() {
        given:
        project.cpdCheck{
            encoding = 'ISO-8859-1'
            reports{
                csv.enabled = false
                text.enabled = true
                xml.enabled = false
            }
        }

        when:
        def result = new CpdReporter(project.cpdCheck)

        then:
        result.encoding == 'ISO-8859-1'
        result.report == project.cpdCheck.reports.text
    }

    def "test 'createRendererFor(...)' should return 'CsvRenderer' with default 'separator'"() {
        when:
        def result = underTest.createRendererFor(project.cpdCheck.reports.csv)

        then:
        result instanceof CSVRenderer
        result.separator == ','
    }

    def "test 'createRendererFor(...)' should return 'CsvRenderer' with set 'separator'"() {
        given:
        project.cpdCheck.reports.csv.separator = ';'

        when:
        def result = underTest.createRendererFor(project.cpdCheck.reports.csv)

        then:
        result instanceof CSVRenderer
        result.separator == ';'
    }

    def "test 'createRendererFor(...)' should return 'SimpleRenderer' with default 'lineSeparator' and 'trimLeadingCommonSourceWhitespaces'"() {
        when:
        def result = underTest.createRendererFor(project.cpdCheck.reports.text)

        then:
        result instanceof SimpleRenderer
        result.separator == '====================================================================='
        result.trimLeadingWhitespace == false
    }

    def "test 'createRendererFor(...)' should return 'SimpleRenderer' with set 'lineSeparator'"() {
        given:
        project.cpdCheck.reports.text.lineSeparator = '---------------------------------------------------------------------'

        when:
        def result = underTest.createRendererFor(project.cpdCheck.reports.text)

        then:
        result instanceof SimpleRenderer
        result.separator == '---------------------------------------------------------------------'
    }

    def "test 'createRendererFor(...)' should return 'SimpleRenderer' with set 'trimLeadingCommonSourceWhitespaces'"() {
        given:
        project.cpdCheck.reports.text.trimLeadingCommonSourceWhitespaces = true

        when:
        def result = underTest.createRendererFor(project.cpdCheck.reports.text)

        then:
        result instanceof SimpleRenderer
        result.trimLeadingWhitespace == true
    }

    def "test 'createRendererFor(...)' should return 'SimpleRenderer' with set 'lineSeparator' and 'trimLeadingCommonSourceWhitespaces'"() {
        given:
        project.cpdCheck.reports.text{
            lineSeparator = '/////////////////////////////////////////////////////////////////////'
            trimLeadingCommonSourceWhitespaces = true
        }

        when:
        def result = underTest.createRendererFor(project.cpdCheck.reports.text)

        then:
        result instanceof SimpleRenderer
        result.separator == '/////////////////////////////////////////////////////////////////////'
        result.trimLeadingWhitespace == true
    }

    def "test 'createRendererFor(...)' should return 'XmlRenderer' with set 'encoding'"() {
        given:
        project.cpdCheck.reports.xml.encoding = 'ISO-8859-1'

        when:
        def result = underTest.createRendererFor(project.cpdCheck.reports.xml)

        then:
        result instanceof XMLRenderer
        result.encoding == 'ISO-8859-1'
    }

    def "test 'createRendererFor(...)' should return 'XmlRenderer' with task 'encoding'"() {
        given:
        project.cpdCheck {
            encoding = 'US-ASCII'
            reports.xml.encoding = null
        }

        when:
        def result = underTest.createRendererFor(project.cpdCheck.reports.xml)

        then:
        result instanceof XMLRenderer
        result.encoding == 'US-ASCII'
    }

    def "test 'generate' should ..."() { // TODO more and better tests or let is be as acceptance test? otherwise also do for executor => integration test
        given:
        project.cpdCheck.reports{
            text{
                lineSeparator = "----------------------"
                enabled = true
                destination = File.createTempFile("test", "text")
            }
            xml.enabled = false
        }

        def mark1 = new Mark(new TokenEntry('1', 'Clazz1.java', 1))
        def mark2 = new Mark(new TokenEntry('2', 'Clazz2.java', 1))
        mark1.lineCount = mark2.lineCount = 1
        mark1.sourceCode = mark2.sourceCode = new SourceCode(new StringCodeLoader("def str = 'I am a duplicate'"))
        def match = new Match(5, mark1, mark2)

        def underTest = new CpdReporter(project.cpdCheck)

        when:
        underTest.generate([ match, match ])

        then:
        project.cpdCheck.reports.text.destination.text
    }
}
