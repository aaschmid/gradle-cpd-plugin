package de.aaschmid.gradle.plugins.cpd.internal

import de.aaschmid.gradle.plugins.cpd.test.BaseSpec
import de.aaschmid.gradle.plugins.test.cpd.*
import net.sourceforge.pmd.cpd.Match
import net.sourceforge.pmd.cpd.TokenEntry
import org.gradle.api.GradleException

import java.nio.file.Files

class CpdReporterTest extends BaseSpec {

    def underTest

    def setup() {
        underTest = new CpdReporter(project.tasks.findByName('cpd'))
    }

    def "test 'new CpdReporter(null)' should throw 'NullPointerException'"() {
        when:
        new CpdReporter(null)

        then:
        def e = thrown NullPointerException
        e.getMessage() ==~ /task must not be null/
    }

    def "test 'canGenerate()' should throw 'GradleException' if encoding is 'null'"() {
        given:
        underTest.task.encoding = null

        when:
        underTest.canGenerate()

        then:
        def e = thrown GradleException
        e.getMessage() ==~ /Task '.+' requires encoding but was: null./
    }

    def "test 'canGenerate()' should throw 'GradleException' if no report is enabled"() {
        given:
        tasks.cpd.reports{
            csv.enabled = false
            text.enabled = false
            xml.enabled = false
        }

        when:
        underTest.canGenerate()

        then:
        def e = thrown GradleException
        e.getMessage() ==~ /Task '.+' requires exactly one report to be enabled but was: \[\]\./
    }

    def "test 'canGenerate()' should throw 'GradleException' if two reports are enabled"() {
        given:
        tasks.cpd.reports{
            csv.enabled = true
            text.enabled = false
            xml.enabled = true
        }

        when:
        underTest.canGenerate()

        then:
        def e = thrown GradleException
        e.getMessage() ==~ /Task '.+' requires exactly one report to be enabled but was: \[csv, xml\]\./
    }

    def "test 'canGenerate()' should throw 'GradleException' if destination of enabled report is 'null'"() {
        given:
        tasks.cpd.reports{
            csv{
                enabled = true
                destination = null
            }
            text.enabled = false
            xml.enabled = false
        }

        when:
        underTest.canGenerate()

        then:
        def e = thrown GradleException
        e.getMessage() ==~ /'.*csv' requires valid destination but was 'null'\./
    }

    def "test 'canGenerate()' should not throw 'GradleException' for default task arguments"() {
        when:
        underTest.canGenerate()

        then:
        notThrown GradleException
    }

    def "test 'generate' should ..."() { // TODO more and better tests
        given:
        tasks.cpd.reports{
            text{
                lineSeparator = "----------------------"
                enabled = true
                destination = Files.createTempFile("test", "text")
            }
        }

        def tokenEntry1 = new TokenEntry('1', 'Clazz1.java', 5)
        def tokenEntry2 = new TokenEntry('2', 'Clazz2.java', 7)
        def match = new Match(5, tokenEntry1, tokenEntry2)

        when:
        underTest.generate([ match, match ])

        then:
        tasks.cpd.reports.text.destination.text
    }
}