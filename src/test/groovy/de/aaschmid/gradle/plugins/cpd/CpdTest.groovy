package de.aaschmid.gradle.plugins.cpd

import de.aaschmid.gradle.plugins.cpd.CpdPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Ignore

class CpdTest extends Specification {

    Project project = ProjectBuilder.builder().build()

    def setup() {
        project.plugins.apply(CpdPlugin)
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
            source = testFile('java/de/aaschmid/clazz/')
        }

        def task = project.tasks.findByName('cpd')

        expect:
        task.inputs.files.filter{ file -> !file.name =~ /.jar/ }.empty
        task.inputs.sourceFiles.files == project.files(
                testFile('java/de/aaschmid/clazz/Clazz.java'),
                testFile('java/de/aaschmid/clazz/impl/Clazz1.java'),
                testFile('java/de/aaschmid/clazz/impl/Clazz2.java'),
        ) as Set
        task.inputs.properties.size() == 4
    }

    def "'Cpd' task is aware of includes and excludes"() {
        given:
        project.tasks.cpd{
            include '**/*.java'
            exclude '**/*1.java'
            exclude '**/*z.java'
            source = testFile('java/')
        }

        def task = project.tasks.findByName('cpd')

        expect:
        task.inputs.files.filter{ file -> !file.name =~ /.jar/ }.empty
        task.inputs.sourceFiles.files == project.files(
                testFile('java/de/aaschmid/clazz/impl/Clazz2.java'),
                testFile('java/de/aaschmid/foo/Bar.java'),
                testFile('java/de/aaschmid/test/Test.java'),
        ) as Set
    }

    def "'Cpd' task outputs are set correctly"() {
        given:
        project.tasks.cpd{
            reports{
                text{
                    enabled = true
                    destination = project.file("${project.buildDir}/cpd.text")
                }
            }
            source = testFile('java/')
        }

        def task = project.tasks.findByName('cpd')

        expect:
        task.outputs.files.files == project.files("${project.buildDir}/cpd.text", "${project.buildDir}/reports/cpd.xml") as Set
    }

    def "'CpdPlugin' allows configuring tool dependencies explicitly via toolVersion property"() {
        given:
        project.cpd{
            toolVersion '5.0.1'
        }
        project.tasks.cpd{
            source = testFile('java/')
        }

        when:
        project.tasks.getByName('cpd').execute()

        then:
        project.file('build/reports/cpd.xml').exists()
    }

    def "'CpdPlugin' allows configuring tool dependencies explicitly via configuration"() {
        given:
        project.dependencies{
            cpd 'net.sourceforge.pmd:pmd:5.0.1'
        }
        project.tasks.cpd{
            source = testFile('java/')
        }

        when:
        project.tasks.getByName('cpd').execute()

        then:
        project.file('build/reports/cpd.xml').exists()
    }

    def "executing 'Cpd' task throws wrapped GradleException if no report is enabled"() {
        given:
        project.tasks.cpd{
            reports{
                csv.enabled = false
                text.enabled = false
                xml.enabled = false
            }
            source = testFile('java')
        }

        when:
        project.tasks.getByName('cpd').execute()

        then:
        !project.file('build/reports/cpd.csv').exists()

        def e = thrown(TaskExecutionException)
        e.cause instanceof GradleException
        e.cause.message == '''Task 'cpd' requires exactly one report to be enabled but was: [].'''
    }

    def "executing 'Cpd' task throws wrapped GradleException if more than one report is enabled"() {
        given:
        project.tasks.cpd{
            reports{
                csv.enabled = false
                text.enabled = true
                xml.enabled = true
            }
            source = testFile('java')
        }

        when:
        project.tasks.getByName('cpd').execute()

        then:
        !project.file('build/reports/cpd.csv').exists()

        def e = thrown(TaskExecutionException)
        e.cause instanceof GradleException
        e.cause.message == '''Task 'cpd' requires exactly one report to be enabled but was: [text, xml].'''
    }

    def "executing 'Cpd' task on non-duplicate 'java' source will produce empty 'cpd.xml'"() {
        given:
        project.cpd{
            encoding = 'UTF-8'
            minimumTokenCount = 5
        }
        project.tasks.cpd.source = testFile('java/de/aaschmid/foo')

        when:
        project.tasks.getByName('cpd').execute()

        then:
        def report = project.file('build/reports/cpd.xml')
        report.exists()
        report.text =~ /<pmd-cpd\/>/
    }

    def "executing 'Cpd' task on duplicate 'java' source will produce 'cpd.csv' with one warning"() {
        given:
        project.tasks.cpd{
            minimumTokenCount = 15
            reports{
                csv.enabled = true
                xml.enabled = false
            }
            source = testFile('java/de/aaschmid/clazz')
        }

        when:
        project.tasks.getByName('cpd').execute()

        then:
        def report = project.file('build/reports/cpd.csv')
        report.exists()
        report.text =~ /7,18,2,3,.*Clazz1.java,3,.*Clazz2.java/
    }

    /**
     * Creates a {@link File} with location <code>classpath:/test-data/${releativePath}</code> as absolute path
     *
     * @see Class#getResource(java.lang.String)
     * @see File
     */
    String testFile(String relativePath) {
        def file = this.class.getResource("/test-data/${relativePath}")?.path
        assert file && new File(file)?.exists()
        return file
    }
}
