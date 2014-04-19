package de.aaschmid.gradle.plugins.cpd

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration.State
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class CpdPluginTest extends Specification {

    Project project = ProjectBuilder.builder().build()

    def setup() {
        project.plugins.apply(CpdPlugin)
    }

    def "applying 'CpdPlugin' applies required reporting-base plugin"() {
        expect:
        project.plugins.hasPlugin(ReportingBasePlugin)
    }

    def "applying 'CpdPlugin' creates and configures extension 'cpd'"() {
        given:
        CpdExtension ext = project.extensions.findByName('cpd')

        expect:
        ext.encoding == ''
        ext.minimumTokenCount == 50
        ext.reportsDir == project.file('build/reports')
        ext.toolVersion == '5.1.0'
    }

    def "applying 'CpdPlugin' creates and configures configuration 'cpd'"() {
        given:
        def config = project.configurations.findByName('cpd')

        expect:
        config != null
        config.dependencies.empty
        config.description == 'The CPD libraries to be used for this project.'
        config.extendsFrom.empty
        config.state == State.UNRESOLVED
        config.transitive
        !config.visible
    }

    def "applying 'CpdPlugin' creates and configures task 'cpd' with correct default values"() {
        given:
        Cpd task = project.tasks.findByName('cpd')

        expect:
        task instanceof Cpd
        task.description == 'Run CPD analysis for all sources'
        task.group == null

        task.encoding == ''
        task.minimumTokenCount == 50

        task.pmdClasspath == project.configurations.findByName('cpd')

        task.reports.csv.destination == project.file('build/reports/cpd.csv')
        !task.reports.csv.enabled
        task.reports.text.destination == project.file('build/reports/cpd.text')
        !task.reports.text.enabled
        task.reports.xml.destination == project.file('build/reports/cpd.xml')
        task.reports.xml.enabled

        task.source.empty
    }

    def "applying 'CpdPlugin' configures additional tasks of type 'Cpd' using defaults"() {
        given:
        def task = project.tasks.create('cpdCustom', Cpd)

        expect:
        task instanceof Cpd
        task.description == null
        task.group == null

        task.encoding == ''
        task.minimumTokenCount == 50

        task.pmdClasspath == project.configurations.cpd

        task.reports.csv.destination == project.file('build/reports/cpd.csv')
        !task.reports.csv.enabled
        task.reports.text.destination == project.file('build/reports/cpd.text')
        !task.reports.text.enabled
        task.reports.xml.destination == project.file('build/reports/cpd.xml')
        task.reports.xml.enabled

        task.source.empty
    }

    def "applying 'CpdPlugin' and 'JavaBasePlugin' adds cpd tasks to check lifecycle task"() {
        given:
        project.plugins.apply(JavaBasePlugin)

        def checkTask = project.tasks.findByName('check')
        def cpdTask = project.tasks.findByName('cpd')

        expect:
        checkTask.taskDependencies.getDependencies(checkTask).find{ Task task ->
            task == cpdTask
        }
    }

    def "applying 'CpdPlugin' and 'JavaBasePlugin' adds cpd tasks to check lifecycle task if 'JavaBasePlugin' is applied first"() {
        given:
        Project project = ProjectBuilder.builder().build()

        project.plugins.apply(JavaBasePlugin)
        project.plugins.apply(CpdPlugin)

        def checkTask = project.tasks.findByName('check')
        def cpdTask = project.tasks.findByName('cpd')

        expect:
        checkTask.taskDependencies.getDependencies(checkTask).find{ Task task ->
            task == cpdTask
        }
    }

    def "'Cpd' task can be customized via extension"() {
        given:
        project.cpd{
            encoding = 'UTF-8'
            minimumTokenCount = 25
            reportsDir = project.file('cpd-reports')
        }

        def task = project.tasks.findByName('cpd')

        expect:
        task instanceof Cpd
        task.description == 'Run CPD analysis for all sources'
        task.group == null

        task.encoding == 'UTF-8'
        task.minimumTokenCount == 25

        task.pmdClasspath == project.configurations.findByName('cpd')

        task.reports.csv.destination == project.file('cpd-reports/cpd.csv')
        !task.reports.csv.enabled
        task.reports.text.destination == project.file('cpd-reports/cpd.text')
        !task.reports.text.enabled
        task.reports.xml.destination == project.file('cpd-reports/cpd.xml')
        task.reports.xml.enabled

        task.source.empty
    }

    def "'Cpd' task can be customized via 'cpd' task"() {
        given:
        project.tasks.cpd{
            encoding = 'ISO-8859-1'
            minimumTokenCount = 10
            reports{
                csv{
                    enabled = true
                    destination = project.file("${project.buildDir}/cpd.csv")
                }
                text{
                    enabled = false
                    destination = project.file("${project.buildDir}/cpd.text")
                }
                xml.enabled = false
            }
            include '**.java'
            exclude '**Test*'
            source = project.file('src/')
        }

        def task = project.tasks.findByName('cpd')

        expect:
        task instanceof Cpd
        task.description == 'Run CPD analysis for all sources'
        task.group == null

        task.encoding == 'ISO-8859-1'
        task.minimumTokenCount == 10

        task.pmdClasspath == project.configurations.findByName('cpd')

        task.reports.csv.destination == project.file('build/cpd.csv')
        task.reports.csv.enabled
        task.reports.text.destination == project.file('build/cpd.text')
        !task.reports.text.enabled
        task.reports.xml.destination == project.file('build/reports/cpd.xml')
        !task.reports.xml.enabled

        task.source.empty

    }

    def "custom 'Cpd' task can be customized via extension if task is created before extension is configured"() {
        given:
        def task = project.tasks.create('cpdCustom', Cpd)

        project.cpd{
            minimumTokenCount = 10
        }

        expect:
        task.minimumTokenCount == 10
    }

    def "custom 'Cpd' task can be customized via extension if task is created after extension is configured"() {
        given:
        project.cpd{
            minimumTokenCount = 250
        }

        def task = project.tasks.create('cpdCustom', Cpd)

        expect:
        task.minimumTokenCount == 250
    }
}
