package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReportParameters.CpdCsvReport
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReportParameters.CpdTextReport
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReportParameters.CpdXmlReport
import de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReporter
import net.sourceforge.pmd.cpd.CSVRenderer
import net.sourceforge.pmd.cpd.Mark
import net.sourceforge.pmd.cpd.Match
import net.sourceforge.pmd.cpd.SimpleRenderer
import net.sourceforge.pmd.cpd.SourceCode
import net.sourceforge.pmd.cpd.SourceCode.StringCodeLoader
import net.sourceforge.pmd.cpd.TokenEntry
import net.sourceforge.pmd.cpd.XMLRenderer
import spock.lang.Specification

class CpdReporterTest extends Specification {

    def underTest

    def setup() {
        underTest = new CpdReporter([])
    }

    def "test 'createRendererFor(...)' should return 'CsvRenderer'"() {
        when:
        def result = underTest.createRendererFor(new CpdCsvReport("UTF-8", new File("test.csv"), ';' as char))

        then:
        result instanceof CSVRenderer
        result.separator == ';'
    }

    def "test 'createRendererFor(...)' should return 'SimpleRenderer'"() {
        when:
        def result = underTest.createRendererFor(new CpdTextReport("UTF-8", new File("rep.txt"), "---", true))

        then:
        result instanceof SimpleRenderer
        result.separator == '---'
        result.trimLeadingWhitespace == true
    }

    def "test 'createRendererFor(...)' should return 'XmlRenderer'"() {
        when:
        def result = underTest.createRendererFor(new CpdXmlReport("ISO-8859-1", new File("cpd.xml")))

        then:
        result instanceof XMLRenderer
        result.encoding == 'ISO-8859-1'
    }

    def "test 'generate' should generate a report"() { // TODO more and better tests or let is be as acceptance test? otherwise also do for executor => integration test
        given:
        def reportFile = File.createTempFile("cpd", "text")
        def report = new CpdTextReport("UTF-8", reportFile, "#######", false)

        def mark1 = new Mark(new TokenEntry('1', 'Clazz1.java', 1))
        def mark2 = new Mark(new TokenEntry('2', 'Clazz2.java', 1))
        mark1.lineCount = mark2.lineCount = 1
        mark1.sourceCode = mark2.sourceCode = new SourceCode(new StringCodeLoader("def str = 'I am a duplicate'"))
        def match = new Match(5, mark1, mark2)

        def underTest = new de.aaschmid.gradle.plugins.cpd.internal.worker.CpdReporter([ report])

        when:
        underTest.generate([ match, match ])

        then:
        def content = reportFile.text
        content.contains("Found a 1 line (5 tokens) duplication in the following files: ")
    }
}
