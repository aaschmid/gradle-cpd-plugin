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
        project.tasks.cpd{
            reports{
                text{
                    enabled = true
                    destination = project.file("${project.buildDir}/cpd.text")
                }
            }
            source = testFile('de/aaschmid/clazz/')
        }

        def task = project.tasks.findByName('cpd')

        expect:
        task.inputs.files.filter{ file -> !file.name =~ /.java/ }.empty // TODO requires mavenLocal() etc.
    }

    // TODO Test incremental build feature? how?
    // TODO Test reports feature? how?

    def "'Cpd' task will be skipped if no source is set"() {
        given:
        project.tasks.cpd{
            include '**/*.java'
            exclude '**/*1.java'
            exclude '**/*z.java'
            //source
        }

        def task = project.tasks.findByName('cpd')

        expect:
        project.tasks.getByName('cpd').execute()

        // TODO how to find out if task was executed or not?
        // TODO ask task graph?
    }

    def "'CpdPlugin' allows configuring tool dependencies explicitly via toolVersion property"() {
        given:
        project.cpd{ toolVersion '5.0.1' }
        project.tasks.cpd{ source = testFile('.') }

        when:
        def result = project.configurations.cpd.resolve()

        then:
        result.any{ file -> file.name == 'pmd-5.0.1.jar' }
    }

    def "'CpdPlugin' allows configuring tool dependencies explicitly via configuration"() {
        given:
        project.dependencies{ cpd 'net.sourceforge.pmd:pmd:5.0.2' }
        project.tasks.cpd{ source = testFile('.') }

        when:
        def result = project.configurations.cpd.resolve()

        then:
        result.any{ file -> file.name == 'pmd-5.0.2.jar' }
    }

    // TODO use pmd dependency if pmd plugin applied?

    def "executing 'Cpd' task throws wrapped 'InvalidUserDataException' if no report is enabled"() {
        given:
        project.tasks.cpd{
            reports{
                csv.enabled = false
                text.enabled = false
                xml.enabled = false
            }
            source = testFile('.')
        }

        when:
        project.tasks.getByName('cpd').execute()

        then:
        !project.file('build/reports/cpd.csv').exists()

        def e = thrown(TaskExecutionException)
        e.cause instanceof InvalidUserDataException
        e.cause.message == '''Task 'cpd' requires exactly one report to be enabled but was: [].'''
    }

    def "executing 'Cpd' task throws wrapped 'InvalidUserDataException' if more than one report is enabled"() {
        given:
        project.tasks.cpd{
            reports{
                csv.enabled = false
                text.enabled = true
                xml.enabled = true
            }
            source = testFile('.')
        }

        when:
        project.tasks.getByName('cpd').execute()

        then:
        !project.file('build/reports/cpd.csv').exists()

        def e = thrown(TaskExecutionException)
        e.cause instanceof InvalidUserDataException
        e.cause.message == '''Task 'cpd' requires exactly one report to be enabled but was: [text, xml].'''
    }

    def "executing 'Cpd' task on non-duplicate 'java' source will produce empty 'cpd.xml'"() {
        given:
        project.cpd{
            encoding = 'ISO-8859-1'
            minimumTokenCount = 5
        }
        project.tasks.cpd.source = testFile('de/aaschmid/foo')

        when:
        project.tasks.getByName('cpd').execute()

        then:
        def report = project.file('build/reports/cpd.xml')
        report.exists()
        // TODO do better?
        report.text =~ /encoding="ISO-8859-1"/
        report.text =~ /<pmd-cpd\/>/
    }

    def "executing 'Cpd' task on duplicate 'java' source should throw 'GradleException' and produce 'cpd.csv' with one warning"() {
        given:
        project.tasks.cpd{
            minimumTokenCount = 15
            reports{
                csv.enabled = true
                xml.enabled = false
            }
            source = testFile('de/aaschmid/clazz')
        }

        when:
        project.tasks.getByName('cpd').execute()

        then:
        def e = thrown GradleException
        e.cause.message =~ /CPD found duplicate code\. See the report at file:\/\/.*\/cpd.csv/

        def report = project.file('build/reports/cpd.csv')
        report.exists()
        report.text =~ /7,19,2,5,.*Clazz1.java,5,.*Clazz2.java/
    }

    def "executing 'Cpd' task on duplicate 'java' source should not throw 'GradleException' if 'ignoreFailures' and produce 'cpd.csv' with one warning"() {
        given:
        project.tasks.cpd{
            ignoreFailures = true
            minimumTokenCount = 15
            reports{
                csv.enabled = true
                xml.enabled = false
            }
            source = testFile('de/aaschmid/clazz')
        }

        when:
        project.tasks.getByName('cpd').execute()

        then:
        notThrown GradleException

        def report = project.file('build/reports/cpd.csv')
        report.exists()
        report.text =~ /7,19,2,5,.*Clazz1.java,5,.*Clazz2.java/
    }

    // TODO further tests
}
