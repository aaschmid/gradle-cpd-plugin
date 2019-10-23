package de.aaschmid.gradle.plugins.cpd

import de.aaschmid.gradle.plugins.cpd.internal.CpdXmlFileReportImpl
import de.aaschmid.gradle.plugins.cpd.test.BaseSpec
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskExecutionException

class CpdTest extends BaseSpec {

    def "'Cpd' task inputs are set correctly"() {
        given:
        project.cpdCheck{
            reports{
                text{
                    enabled = true
                    destination = project.file("${project.buildDir}/cpdCheck.text")
                }
            }
            source = testFile('de/aaschmid/clazz/')
        }

        def task = project.tasks.findByName('cpdCheck')

        expect:
        task.inputs.sourceFiles.files == project.files(
                testFile('de/aaschmid/clazz/Clazz.java'),
                testFile('de/aaschmid/clazz/impl/Clazz1.java'),
                testFile('de/aaschmid/clazz/impl/Clazz2.java'),
            ) as Set
        task.inputs.properties.size() == 44
    }

    def "'Cpd' task is aware of includes and excludes"() {
        given:
        project.cpdCheck{
            include '**/*.java'
            exclude '**/*1.java'
            exclude '**/*z.java'
            exclude '**/test/*'
            source = testFile('.')
        }

        def task = project.tasks.findByName('cpdCheck')

        expect:
        task.inputs.sourceFiles.files == project.files(
                testFile('de/aaschmid/annotation/Employee.java'),
                testFile('de/aaschmid/annotation/Person.java'),
                testFile('de/aaschmid/clazz/impl/Clazz2.java'),
                testFile('de/aaschmid/duplicate/Test.java'),
                testFile('de/aaschmid/foo/Bar.java'),
                testFile('de/aaschmid/identifier/Identifier2.java'),
                testFile('de/aaschmid/lexical/Error.java'),
                testFile('de/aaschmid/literal/Literal2.java'),
            ) as Set
    }

    def "'Cpd' task outputs are set correctly"() {
        given:
        project.cpdCheck{
            reports{
                csv{
                    enabled = false
                    destination = project.file("${project.buildDir}/cpd.csv")
                }
                text{
                    enabled = true
                    destination = project.file("cpdCheck.txt")
                }
            }
            source = testFile('.')
        }

        def task = project.tasks.findByName('cpdCheck')

        expect:
        task.outputs.files.files == project.files(
                "${project.buildDir}/cpd.csv",
                "cpdCheck.txt",
                "${project.buildDir}/reports/cpd/cpdCheck.xml",
        ) as Set
    }

    def "'Cpd' task ignoreFailures is 'false' by default"() {
        expect:
        !project.tasks.findByName('cpdCheck').ignoreFailures
    }

    def "'Cpd' should throw 'InvalidUserDataException' if encoding is 'null'"() {
        given:
        project.cpdCheck{
            encoding = null
        }

        def task = project.tasks.findByName('cpdCheck')

        when:
        task.actions.each{ it.execute(task) }

        then:
        def e = thrown InvalidUserDataException
        e.getMessage() == "Task 'cpdCheck' requires 'encoding' but was: null."
    }

    def "'Cpd' should throw 'InvalidUserDataException' if 'minimumTokenCount' is '-1'"() {
        given:
        project.cpdCheck{
            minimumTokenCount = -1
        }

        def task = project.tasks.findByName('cpdCheck')

        when:
        task.actions.each{ it.execute(task) }

        then:
        def e = thrown InvalidUserDataException
        e.getMessage() ==~ /Task 'cpdCheck' requires 'minimumTokenCount' to be greater than zero./
    }

    def "'Cpd' should throw 'InvalidUserDataException' if two reports are enabled"() {
        given:
        project.cpdCheck.reports{
            csv.enabled = false
            text.enabled = false
            xml.enabled = false
        }

        def task = project.tasks.findByName('cpdCheck')

        when:
        task.actions.each{ it.execute(task) }

        then:
        def e = thrown InvalidUserDataException
        e.getMessage() == "Task 'cpdCheck' requires at least one enabled report."
    }

    def "'Cpd' should throw 'IllegalArgumentException' if destination of enabled report is 'null' (non-optional property)"() {
        when:
        project.cpdCheck.reports {
            csv.destination = null
        }

        then:
        def e = thrown IllegalArgumentException
        e.getMessage() == "Cannot set the value of a property using a null provider."
    }

    def "test 'getXmlRendererEncoding' should return set encoding"() {
        given:
        project.cpdCheck.encoding = 'ISO-8859-15'

        def report = new CpdXmlFileReportImpl('xml', project.cpdCheck)
        report.setEncoding('ISO-8859-1')

        expect:
        project.cpdCheck.getXmlRendererEncoding(report) == 'ISO-8859-1'
    }

    def "test 'getXmlRendererEncoding' should return task encoding if no specific is set"() {
        given:
        project.cpdCheck.encoding = 'ISO-8859-15'

        def report = new CpdXmlFileReportImpl('xml', project.cpdCheck)
        report.setEncoding(null)

        expect:
        project.cpdCheck.getXmlRendererEncoding(report) == 'ISO-8859-15'
    }

    def "test 'getXmlRendererEncoding' should return system encoding if non is set"() {
        given:
        project.cpdCheck.encoding = null

        def report = new CpdXmlFileReportImpl('xml', project.cpdCheck)
        report.setEncoding(null)

        expect:
        project.cpdCheck.getXmlRendererEncoding(report) == System.getProperty("file.encoding")
    }
}
