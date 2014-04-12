package de.aaschmid.gradle.plugins.cpd

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration.State
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.plugins.quality.TargetJdk
import org.gradle.api.tasks.SourceSet
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class CpdPluginTest extends Specification {

    Project project = ProjectBuilder.builder().build()
//            withProjectDir(TestNameTestDirectoryProvider.newInstance().testDirectory).

    def setup() {
        project.plugins.apply(CpdPlugin)
    }

//    def "applying 'CpdPlugin' applies also reporting-base plugin"() {
//        expect:
//        project.plugins.hasPlugin(ReportingBasePlugin)
//    }

    def "applying 'CpdPlugin' configures 'cpd' configuration"() {
        given:
        def config = project.configurations.cpd  // .findByName('cpd')

        expect:
        config != null
        config.dependencies.isEmpty()
        config.description == 'The CPD libraries to be used for this project.'
        config.extendsFrom.isEmpty()
        config.state == State.UNRESOLVED
        config.transitive
        !config.visible
    }

    def "applying 'CpdPlugin' configures 'cpd' extension"() {
        given:
        CpdExtension ext = project.extensions.cpd // .findByName('cpd')

        expect:
        !ext.ignoreFailures
        ext.reportsDir == project.file("build/reports")
        ext.toolVersion == '5.1.0'
        // TODO add more tests as soon as plugin is ready for it
    }

    def "applying 'CpdPlugin' configures one 'cpd' task for sources in all source sets"() {
        given:
        project.plugins.apply(JavaBasePlugin)
        project.sourceSets{
            main
            test
            other
        }

        Cpd task = project.tasks.findByName('cpd')

        expect:
//        configuresCpdTask("cpd", project.sourceSets.main)

//        task.classpath == project.configurations.cpd
        task.description == 'Run CPD analysis for all sources'
//        task.source as List == project.sourceSets.all as List
        task.reports.csv.destination == project.file("build/reports/cpd.csv")
        task.reports.csv.enabled
        task.reports.text.destination == project.file("build/reports/cpd.text")
        task.reports.text.enabled
        task.reports.xml.destination == project.file("build/reports/cpd.xml")
        task.reports.xml.enabled
        task.ignoreFailures == false
    }

//    def "applying 'CpdPlugin' configures one 'cpd' task for all source set on multi-project build"() {
//        project.plugins.apply(JavaBasePlugin)
//        project.sourceSets{
//            main
//            test
//            other
//        }
//
//        expect:
//        configuresCpdTask("cpdMain", project.sourceSets.main)
//        configuresCpdTask("cpdTest", project.sourceSets.test)
//        configuresCpdTask("cpdOther", project.sourceSets.other)
//    }
//
//    def "configures cpd targetjdk based on sourcecompatibilityLevel"() {
//        project.plugins.apply(JavaBasePlugin)
//        when:
//        project.setSourceCompatibility(sourceCompatibility)
//        project.sourceSets{ main }
//        then:
//        project.tasks.getByName("cpd").targetJdk == targetJdk
//
//        where:
//        sourceCompatibility | targetJdk
//        1.3 | TargetJdk.VERSION_1_3
//        1.4 | TargetJdk.VERSION_1_4
//        1.5 | TargetJdk.VERSION_1_5
//        1.6 | TargetJdk.VERSION_1_6
//        1.7 | TargetJdk.VERSION_1_7
//        // 1.4 is the default in the cpd plugin so we use it as a default too
//        1.8 | TargetJdk.VERSION_1_4
//        1.1 | TargetJdk.VERSION_1_4
//        1.2 | TargetJdk.VERSION_1_4
//    }
//
//    private void configuresCpdTask(String taskName, SourceSet sourceSet) {
//        def task = project.tasks.findByName(taskName)
//        assert task instanceof Cpd
//        task.with{
//            assert description == "Run CPD analysis for ${sourceSet.name} classes"
//            source as List == sourceSet.allJava as List
//            assert classpath == project.configurations.cpd
////            assert ruleSets == ["basic"]
////            assert ruleSetFiles.empty
//            assert reports.xml.destination == project.file("build/reports/cpd/${sourceSet.name}.xml")
//            assert reports.html.destination == project.file("build/reports/cpd/${sourceSet.name}.html")
//            assert ignoreFailures == false
//        }
//    }
//
//    def "configures any additional CPD tasks"() {
//        def task = project.tasks.create("cpdCustom", Cpd)
//
//        expect:
//        task.description == null
//        task.source.empty
//        task.classpath == project.configurations.cpd
//        task.ruleSets == [ "basic" ]
//        task.ruleSetFiles.empty
//        task.reports.xml.destination == project.file("build/reports/cpd/custom.xml")
//        task.reports.html.destination == project.file("build/reports/cpd/custom.html")
//        task.ignoreFailures == false
//    }
//
//    def "adds cpd tasks to check lifecycle task"() {
//        project.plugins.apply(JavaBasePlugin)
//        project.sourceSets{
//            main
//            test
//            other
//        }
//
//        expect:
//        that(project.tasks['check'], dependsOn(hasItems("cpd")))
//    }
//
//    def "can customize settings via extension"() {
//        project.plugins.apply(JavaBasePlugin)
//        project.sourceSets{
//            main
//            test
//            other
//        }
//
//        project.cpd{
//            sourceSets = [ project.sourceSets.main ]
//            ruleSets = [ "braces", "unusedcode" ]
//            ruleSetFiles = project.files("my-ruleset.xml")
//            reportsDir = project.file("cpd-reports")
//            ignoreFailures = true
//        }
//
//        expect:
//        hasCustomizedSettings("cpdMain", project.sourceSets.main)
//        hasCustomizedSettings("cpdOther", project.sourceSets.other)
//        that(project.check, org.gradle.api.tasks.TaskDependencyMatchers.dependsOn(hasItem('cpdMain')))
//        that(project.check, org.gradle.api.tasks.TaskDependencyMatchers.dependsOn(not(hasItems('cpdTest', 'cpdOther'))))
//    }
//
//    private void hasCustomizedSettings(String taskName, SourceSet sourceSet) {
//        def task = project.tasks.findByName(taskName)
//        assert task instanceof Cpd
//        task.with{
//            assert description == "Run CPD analysis for ${sourceSet.name} classes"
//            source as List == sourceSet.allJava as List
//            assert classpath == project.configurations.cpd
//            assert ruleSets == [ "braces", "unusedcode" ]
//            assert ruleSetFiles.files == project.files("my-ruleset.xml").files
//            assert reports.xml.destination == project.file("cpd-reports/${sourceSet.name}.xml")
//            assert reports.html.destination == project.file("cpd-reports/${sourceSet.name}.html")
//            assert ignoreFailures == true
//        }
//    }
//
//    def "can customize any additional CPD tasks via extension"() {
//        def task = project.tasks.create("cpdCustom", Cpd)
//        project.cpd{
//            ruleSets = [ "braces", "unusedcode" ]
//            ruleSetFiles = project.files("my-ruleset.xml")
//            reportsDir = project.file("cpd-reports")
//            ignoreFailures = true
//        }
//
//        expect:
//        task.description == null
//        task.source.empty
//        task.classpath == project.configurations.cpd
//        task.ruleSets == [ "braces", "unusedcode" ]
//        task.ruleSetFiles.files == project.files("my-ruleset.xml").files
//        task.reports.xml.destination == project.file("cpd-reports/custom.xml")
//        task.reports.html.destination == project.file("cpd-reports/custom.html")
//        task.outputs.files.files == task.reports.enabled*.destination as Set
//        task.ignoreFailures == true
//    }
}
