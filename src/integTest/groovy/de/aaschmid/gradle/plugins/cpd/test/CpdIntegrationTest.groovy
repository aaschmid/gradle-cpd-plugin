package de.aaschmid.gradle.plugins.cpd.test

import spock.lang.Issue

import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.Lang.*
import static org.gradle.testkit.runner.TaskOutcome.*

class CpdIntegrationTest extends IntegrationBaseSpec {

    def "execution of task 'check' execute 'cpdCheck' and don't show WARNING"() {
        given:
        buildFile << """
            plugins {
                id 'java'
                id 'de.aaschmid.cpd'
            }
            """.stripIndent()

        when:
        def result = run('check')

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.task(':cpdCheck').outcome == NO_SOURCE
        !result.output.contains('WARNING: Due to the absence of \'LifecycleBasePlugin\' on root project')
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/8')
    def "execution of task before 'check' should not lead to NullPointerException and don't show WARNING"() {
        given:
        buildFile << """
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
        !result.output.contains('WARNING: Due to the absence of \'LifecycleBasePlugin\' on root project')
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/3')
    def "execution of task 'build' should show WARNING if rootProject does not apply at least 'LifecycleBasePlugin'"() {
        given:
        withSubProjects('sub')

        buildFile << """
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

        def rootProjectName = testProjectDir.fileName
        result.output.contains("WARNING: Due to the absence of 'LifecycleBasePlugin' on root project '${rootProjectName}' " +
                "the task ':cpdCheck' could not be added to task graph. Therefore CPD will not be executed. To prevent " +
                "this, manually add a task dependency of ':cpdCheck' to a 'check' task of a subproject.")
        result.output.contains("1) Directly to project ':sub':")
        result.output.contains("    check.dependsOn(':cpdCheck')")
        result.output.contains("2) Indirectly, e.g. via root project '${rootProjectName}':")
        result.output.contains("    project(':sub') {")
        result.output.contains("        plugins.withType(LifecycleBasePlugin) { // <- just required if 'java' plugin is applied within subproject")
        result.output.contains("            check.dependsOn(cpdCheck)")
        result.output.contains("        }")
        result.output.contains("    }")
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/14')
    def "execution of task 'build' should not show WARNING if a subproject applies 'LifecycleBasePlugin' and puts a dependency to 'cpdCheck' task"() {
        given:
        withSubProjects('sub')

        buildFile << """
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
        result.task(':cpdCheck').outcome == NO_SOURCE
        !result.output.contains('WARNING')
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/37')
    def "execution of task 'build' should not show WARNING if subproject applies 'CPD' plugin and 'LifecycleBasePlugin'"() {
        given:
        withSubProjects('sub')

        buildFile << """
            plugins {
                id 'de.aaschmid.cpd' apply false
            }

            project(':sub') {
                apply plugin: 'de.aaschmid.cpd'
                apply plugin: 'java'
            }
            """.stripIndent()

        when:
        def result = run('build')

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.task(':cpdCheck') == null
        result.task(':sub:cpdCheck').outcome == NO_SOURCE
        !result.output.contains('WARNING')
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/10')
    def "CpdPlugin can be added using full qualified name"() {
        given:
        buildFile << createBuildScriptWithClasspathOfGradleTestKitMechanism() << """
            apply plugin: 'de.aaschmid.cpd'
            """.stripIndent()

        when:
        def result = runWithoutPluginClasspath('cpdCheck')

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.task(':cpdCheck').outcome == NO_SOURCE
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/16')
    def "CpdPlugin can be disabled"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck.enabled = hasProperty('cpd_enabled')
            """.stripIndent()

        when:
        def result = run('cpdCheck')

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.task(':cpdCheck').outcome == SKIPPED
    }

    def "executing 'Cpd' task on duplicate 'kotlin' source should not fail if 'ignoreFailures' and produce 'cpdCheck.csv' with one warning"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpdCheck{
                ignoreFailures = true
                language = 'kotlin'
                minimumTokenCount = 5
                source = ${testPath(KOTLIN, 'de/aaschmid/test')}
            }
            """.stripIndent()

        when:
        def result = run('cpdCheck')

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.task(':cpdCheck').outcome == SUCCESS

        def report = file('build/reports/cpd/cpdCheck.xml')
        report.exists()
        report.text.contains('<duplication lines="3" tokens="9">')
        report.text.contains('<duplication lines="4" tokens="9">')
    }

    def "executing 'Cpd' task on duplicate 'java' source with minimal supported PMD version should produce 'cpdCheck.xml'"() {
        given:
        buildFileWithPluginAndRepos() << """
            cpd{
                toolVersion = '7.0.0'
            }
            cpdCheck{
                ignoreFailures = true
                minimumTokenCount = 15
                reports{
                    csv.required = true
                    xml.required = false
                }
                source = ${testPath(JAVA, 'de/aaschmid/clazz')}
            }
            """.stripIndent()

        when:
        def result = run('cpdCheck')

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.task(':cpdCheck').outcome == SUCCESS

        def report = file('build/reports/cpd/cpdCheck.csv')
        report.exists()
        report.text =~ /4,15,2,[79],.*Clazz[12]\.java,[79],.*Clazz[12]\.java/
    }

    def "executing 'Cpd' task on duplicates should fail if language does not exist"() {
        given:
        buildFile << createBuildScriptWithClasspathOfGradleTestKitMechanism() << """
            apply plugin: 'de.aaschmid.cpd'
            repositories {
                mavenLocal()
                mavenCentral()
            }
            cpdCheck{
                language = 'my-lang'
                source = ${testPath(KOTLIN, 'de/aaschmid/test')}
            }
            """.stripIndent()

        when:
        def result = runWithoutPluginClasspath('cpdCheck')

        then:
        result.output.contains("BUILD FAILED")
        result.output.contains("Could not detect CPD language for 'my-lang'.")
    }
}
