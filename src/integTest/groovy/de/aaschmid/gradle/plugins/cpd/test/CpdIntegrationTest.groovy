package de.aaschmid.gradle.plugins.cpd.test

import de.aaschmid.gradle.plugins.cpd.test.fixtures.AbstractIntegrationSpec
import org.gradle.util.TextUtil
import spock.lang.Issue

class CpdIntegrationTest extends AbstractIntegrationSpec {

    def "execution of task 'check' execute 'cpdCheck' and don't show WARNING"() {
        given:
        addAdditionalBuildClasspath()

        buildFile << """\
            apply plugin: 'cpd'
            apply plugin: 'java'
            """.stripIndent()

        when:
        succeeds('check')

        then:
        executed(':cpdCheck')
        assertThatOutputDoesNotContain('WARNING: Due to the absence of JavaBasePlugin on root project')
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/8')
    def "execution of task before 'check' should not lead to NullPointerException and don't show WARNING"() {
        given:
        addAdditionalBuildClasspath()

        buildFile << """\
            apply plugin: 'cpd'
            apply plugin: 'java'
            """.stripIndent()

        when:
        succeeds('assemble')

        then:
        notExecuted(':cpdCheck')
        assertThatOutputDoesNotContain('WARNING: Due to the absence of JavaBasePlugin on root project')
    }

    def "execution of task 'build' should not show WARNING if rootProject does apply 'JavaBasePlugin'"() {
        given:
        addAdditionalBuildClasspath()

        settingsFile << """\
            include 'sub'
            """.stripIndent()

        buildFile << """\
            apply plugin: 'cpd'
            apply plugin: 'java'

            project(':sub') {
                apply plugin: 'java'
            }
            """.stripIndent()

        when:
        succeeds('build')

        then:
        executed(':cpdCheck')
        assertThatOutputDoesNotContain("WARNING: Due to the absence of JavaBasePlugin")
    }

    def "execution of task 'build' should show WARNING if rootProject does not apply at least 'JavaBasePlugin'"() {
        given:
        addAdditionalBuildClasspath()

        settingsFile << """\
            include 'sub'
            """.stripIndent()

        buildFile << """\
            apply plugin: 'cpd'

            project(':sub') {
                apply plugin: 'java'
            }
            """.stripIndent()

        when:
        succeeds('build')

        then:
        notExecuted(':cpdCheck')

        def rootProjectName = executer.workingDir.name
        assertThatOutputContains("""\
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

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/10')
    def "CpdPlugin can be added using full qualified name"() {
        given:
        addAdditionalBuildClasspath()

        buildFile << "apply plugin: 'de.aaschmid.cpd'"

        when:
        succeeds('cpdCheck')

        then:
        executed(':cpdCheck')
    }

    void assertThatOutputContains(String text) {
        assert result.output.contains(TextUtil.toPlatformLineSeparators(text.trim()))
    }

    void assertThatOutputDoesNotContain(String text) {
        assert !result.output.contains(TextUtil.toPlatformLineSeparators(text.trim()))
    }
}
