package de.aaschmid.gradle.plugins.cpd.test

import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskExecutionException

class CpdAcceptanceTest extends BaseSpec {

    def setup() {
        project.repositories{
            mavenLocal()
            mavenCentral()
        }
    }


    def "'Cpd' task inputs are set correctly"() {
        given:
        project.cpdCheck{
            reports{
                text{
                    enabled = true
                    destination = project.file("${project.buildDir}/cpd.text")
                }
            }
            source = testFile('de/aaschmid/clazz/')
        }

        def task = project.tasks.findByName('cpdCheck')

        expect:
        task.inputs.files.filter{ file -> !file.name =~ /.java/ }.empty // TODO requires mavenLocal() etc.
    }

    // TODO Test incremental build feature? how?
    // TODO Test reports feature? how?

    def "'Cpd' task will be skipped if no source is set"() {
        given:
        project.cpdCheck{
            include '**/*.java'
            exclude '**/*1.java'
            exclude '**/*z.java'
            //source
        }

        def task = project.tasks.findByName('cpdCheck')

        expect:
        task.execute()

        // TODO how to find out if task was executed or not?
        // TODO ask task graph?
    }

    def "'CpdPlugin' allows configuring tool dependencies explicitly via toolVersion property"() {
        given:
        project.cpd{ toolVersion '5.2.1' }
        project.cpdCheck{ source = testFile('.') }

        when:
        def result = project.configurations.cpd.resolve()

        then:
        result.any{ file -> file.name == 'pmd-core-5.2.1.jar' }
    }

    def "'CpdPlugin' allows configuring tool dependencies explicitly via configuration"() {
        given:
        project.dependencies{ cpd 'net.sourceforge.pmd:pmd:5.0.2' }
        project.cpdCheck{ source = testFile('.') }

        when:
        def result = project.configurations.cpd.resolve()

        then:
        result.any{ file -> file.name == 'pmd-5.0.2.jar' }
    }

    // TODO use pmd dependency if pmd plugin applied?

    def "executing 'Cpd' task throws wrapped 'InvalidUserDataException' if no report is enabled"() {
        given:
        project.cpdCheck{
            reports{
                csv.enabled = false
                text.enabled = false
                xml.enabled = false
            }
            source = testFile('.')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        !project.file('build/reports/cpdCheck.csv').exists()

        def e = thrown(TaskExecutionException)
        e.cause instanceof InvalidUserDataException
        e.cause.message == '''Task 'cpdCheck' requires exactly one report to be enabled but was: [].'''
    }

    def "executing 'Cpd' task throws wrapped 'InvalidUserDataException' if more than one report is enabled"() {
        given:
        project.cpdCheck{
            reports{
                csv.enabled = false
                text.enabled = true
                xml.enabled = true
            }
            source = testFile('.')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        !project.file('build/reports/cpdCheck.csv').exists()

        def e = thrown(TaskExecutionException)
        e.cause instanceof InvalidUserDataException
        e.cause.message == '''Task 'cpdCheck' requires exactly one report to be enabled but was: [text, xml].'''
    }

    def "executing 'Cpd' task on non-duplicate 'java' source will produce empty 'cpdCheck.xml'"() {
        given:
        project.cpd{
            encoding = 'ISO-8859-1'
            minimumTokenCount = 5
        }
        project.cpdCheck.source = testFile('de/aaschmid/foo')

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        def report = project.file('build/reports/cpd/cpdCheck.xml')
        report.exists()
        // TODO do better?
        report.text =~ /encoding="ISO-8859-1"/
        report.text =~ /<pmd-cpd\/>/
    }

    def "executing 'Cpd' task on duplicate 'java' source should throw 'GradleException' and produce 'cpdCheck.csv' with one warning"() {
        given:
        project.cpdCheck{
            minimumTokenCount = 15
            reports{
                csv.enabled = true
                xml.enabled = false
            }
            source = testFile('de/aaschmid/clazz')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        def e = thrown GradleException
        e.cause.message =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpdCheck.csv/

        def report = project.file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        report.text =~ /7,19,2,5,.*Clazz1.java,5,.*Clazz2.java/
    }

    def "executing 'Cpd' task on duplicate 'java' source should not throw 'GradleException' if 'ignoreFailures' and produce 'cpdCheck.csv' with one warning"() {
        given:
        project.cpdCheck{
            ignoreFailures = true
            minimumTokenCount = 15
            reports{
                csv.enabled = true
                xml.enabled = false
            }
            source = testFile('de/aaschmid/clazz')
        }

        when:
        project.tasks.getByName('cpdCheck').execute()

        then:
        notThrown GradleException

        def report = project.file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        report.text =~ /7,19,2,5,.*Clazz1.java,5,.*Clazz2.java/
    }

    // TODO further tests
}
