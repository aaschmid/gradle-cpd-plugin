package de.aaschmid.gradle.plugins.cpd.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.*

import static org.gradle.testkit.runner.TaskOutcome.*

class CpdIntegrationTest extends Specification {

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "execution of task 'check' execute 'cpdCheck' and don't show WARNING"() {
        given:
        buildFile << """\
            plugins {
                id 'java'
                id 'de.aaschmid.cpd'
            }
            """.stripIndent()

        when:
        def result = run('check')

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.task(':cpdCheck').outcome == UP_TO_DATE
        !result.output.contains('WARNING: Due to the absence of JavaBasePlugin on root project')
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/8')
    def "execution of task before 'check' should not lead to NullPointerException and don't show WARNING"() {
        given:
        buildFile << """\
            plugins {
                id 'de.aaschmid.cpd'
                id 'java'
            }
            """.stripIndent()

        when:
        def result = run('assemble')

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.task(':cpdCheck') == null
        !result.output.contains('WARNING: Due to the absence of JavaBasePlugin on root project')
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/3')
    def "execution of task 'build' should show WARNING if rootProject does not apply at least 'JavaBasePlugin'"() {
        given:
        testProjectDir.newFile("settings.gradle") << """\
            include 'sub'
            """.stripIndent()

        buildFile << """\
            plugins {
                id 'de.aaschmid.cpd'
            }

            project(':sub') {
                apply plugin: 'java'
            }
            """.stripIndent()

        when:
        def result = run('build')

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.task(':cpdCheck') == null

        def rootProjectName = testProjectDir.root.name
        result.output.contains("""\
WARNING: Due to the absence of JavaBasePlugin on root project '${rootProjectName}' the task ':cpdCheck' could not be\
 added to task graph and therefore will not be executed. SUGGESTION: add a dependency to task ':cpdCheck' manually to a\
 subprojects 'check' task, e.g. to project ':sub' using

    check.dependsOn(':cpdCheck')

or to root project '${rootProjectName}' using

    project(':sub') {
        plugins.withType(JavaBasePlugin) { // <- just required if 'java' plugin is applied within subproject
            check.dependsOn(cpdCheck)
        }
    }
""")
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/14')
    def "execution of task 'build' should not show WARNING if a subproject applies 'JavaBasePlugin' and puts a dependency to 'cpdCheck' task"() {
        given:
        testProjectDir.newFile("settings.gradle") << """\
            include 'sub'
            """.stripIndent()

        buildFile << """\
            plugins {
                id 'de.aaschmid.cpd'
            }

            project(':sub') {
                apply plugin: 'java'

                check.dependsOn(':cpdCheck')
            }
            """.stripIndent()

        when:
        def result = run('build')

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.task(':cpdCheck').outcome == UP_TO_DATE
        !result.output.contains('WARNING: Due to the absence of JavaBasePlugin on root project')
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/16')
    def "CpdPlugin can be disabled"() {
        given:
        buildFile << """
            plugins {
                id 'de.aaschmid.cpd'
            }

            cpdCheck.enabled = hasProperty('cpd_enabled')
            """.stripIndent()

        when:
        def result = run('cpdCheck')

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.task(':cpdCheck').outcome == SKIPPED
    }

    private BuildResult run(String... arguments) {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(arguments)
                .withPluginClasspath()
                .withDebug(true)
                .build()
    }
}
