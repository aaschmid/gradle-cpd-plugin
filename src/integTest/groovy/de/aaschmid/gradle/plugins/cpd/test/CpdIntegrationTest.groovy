package de.aaschmid.gradle.plugins.cpd.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Issue

import static org.gradle.testkit.runner.TaskOutcome.*

class CpdIntegrationTest extends BaseSpec {

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
        result.task(':cpdCheck').outcome == NO_SOURCE
        !result.output.contains('WARNING: Due to the absence of \'LifecycleBasePlugin\' on root project')
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
        !result.output.contains('WARNING: Due to the absence of \'LifecycleBasePlugin\' on root project')
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/3')
    def "execution of task 'build' should show WARNING if rootProject does not apply at least 'LifecycleBasePlugin'"() {
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
        result.task(':cpdCheck').outcome == NO_SOURCE
        !result.output.contains('WARNING')
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/37')
    def "execution of task 'build' should not show WARNING if subproject applies 'CPD' plugin and 'LifecycleBasePlugin'"() {
        given:
        testProjectDir.newFile("settings.gradle") << """\
            include 'sub'
            """.stripIndent()

        buildFile << """\
            plugins {
                id 'de.aaschmid.cpd' apply false
            }

            project(':sub') {
                apply plugin: 'cpd'
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
        buildFile << createBuildscriptWithClasspathOfGradleTestkitMechanism() << """
            apply plugin: 'de.aaschmid.cpd'
            """.stripIndent()

        when:
        def result = run('cpdCheck')

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.task(':cpdCheck').outcome == NO_SOURCE
    }

    @Issue('https://github.com/aaschmid/gradle-cpd-plugin/issues/16')
    def "CpdPlugin can be disabled"() {
        given:
        buildFile << """\
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

    def "executing 'Cpd' task on duplicate 'java' source should not throw 'GradleException' if 'ignoreFailures' and produce 'cpdCheck.csv' with one warning"() {
        given:
        buildFile << """\
            plugins {
                id 'de.aaschmid.cpd'
            }
            repositories {
                mavenLocal()
            }
            cpdCheck{
                ignoreFailures = true
                minimumTokenCount = 15
                reports{
                    csv.enabled = true
                    xml.enabled = false
                }
                source = file('${testFile('java', 'de/aaschmid/clazz').path}')
            }
        """.stripIndent()

        when:
        def result = run('cpdCheck')

        then:
        result.output.contains("BUILD SUCCESSFUL")
        result.task(':cpdCheck').outcome == SUCCESS

        def report = testProjectDir.getRoot().toPath().resolve('build/reports/cpd/cpdCheck.csv').toFile()
        report.exists()
        report.text =~ /4,15,2,[79],.*Clazz[12]\.java,[79],.*Clazz[12]\.java/
    }

    private BuildResult run(String... arguments) {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(arguments)
                .withPluginClasspath()
                .withDebug(true)
                .build()
    }

    /**
     * As the Gradle testkit does not support the old plugin mechanism, this method generates a {@code bundlescript}
     * code block with the same dependencies as {@link GradleRunner#withPluginClasspath()} or
     * {@link PluginUnderTestMetadataReading#readImplementationClasspath()}, respectively.
     * <p>
     * <b>Note:</b> While debugging the problem appears to be that the used {@link org.gradle.api.plugins.PluginManager}
     * (=> {@link org.gradle.api.internal.plugins.DefaultPluginManager}) does not get the correct
     * {@link org.gradle.api.internal.plugins.PluginRegistry} containing the correct
     * {@link org.gradle.api.internal.initialization.ClassLoaderScope} with the injected classpath dependencies ... :-(
     *
     * @return a {@link String} containing all the dependencies which {@link GradleRunner#withPluginClasspath()} uses
     */

    def createBuildscriptWithClasspathOfGradleTestkitMechanism() {
        """\
            buildscript {
                dependencies {
                    classpath files(
                        '${PluginUnderTestMetadataReading.readImplementationClasspath().join("',\n                        '")}'
                    )
                }
            }
            """.stripIndent()
    }
}
