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
