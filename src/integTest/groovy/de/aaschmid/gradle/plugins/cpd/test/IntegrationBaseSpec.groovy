package de.aaschmid.gradle.plugins.cpd.test

import de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.Lang
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildResultException
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.*
import static org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading.*

abstract class IntegrationBaseSpec extends Specification {

    @TempDir
    protected Path testProjectDir

    protected File buildFile
    protected File settingsFile

    protected static String testPath(Lang lang, String...relativePaths) {
        return "['${relativePaths.collect{testFile(lang, it).getAbsolutePath()}.join("', '")}']"
    }

    def setup() {
        buildFile = testProjectDir.resolve('build.gradle').toFile()
        settingsFile = testProjectDir.resolve('settings.gradle').toFile()
    }

    protected File withSubProjects(String... subProjects) {
        if (subProjects.length > 0) {
            settingsFile << """
                include '${subProjects.join("', '")}'
            """.stripIndent()
        }
        return settingsFile
    }

    protected File buildFileWithPluginAndRepos(List<String> additionalPlugins = [ ], boolean addRepositories = true) {
        buildFile << """
            plugins {
                id 'de.aaschmid.cpd'
                ${additionalPlugins.collect{ "id '${it}'" }.join("\n                ")}
            }
            """.stripIndent()
        if (addRepositories) {
            buildFile << """
                repositories {
                    mavenLocal()
                    mavenCentral()
                }
                """.stripIndent()
        }
        return buildFile
    }

    protected File file(String pathWithinProjectFolder) {
        return testProjectDir.resolve(pathWithinProjectFolder).toFile()
    }

    protected BuildResult runWithoutPluginClasspath(String... arguments) {
        try {
            return GradleRunner.create()
                    .withProjectDir(testProjectDir.toFile())
                    .withArguments(arguments)
                    .withDebug(true)
                    .build()
        } catch (UnexpectedBuildResultException e) {
            return e.buildResult
        }
    }

    protected BuildResult run(String... arguments) {
        try {
            return GradleRunner.create()
                    .withProjectDir(testProjectDir.toFile())
                    .withArguments(arguments)
                    .withPluginClasspath()
                    .withDebug(true)
                    .build()
        } catch (UnexpectedBuildResultException e) {
            return e.buildResult
        }
    }

    protected BuildResult runWithoutDebug(String... arguments) {
        try {
            return GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments(arguments)
                .withPluginClasspath()
                .withDebug(false) // `true` fails if `--configuration-cache`, see https://github.com/gradle/gradle/issues/14125
                .build()
        } catch (UnexpectedBuildResultException e) {
            return e.buildResult
        }
    }

    /**
     * As the Gradle test kit does not support the old plugin mechanism, this method generates a {@code buildscript}
     * code block with the same dependencies as {@link org.gradle.testkit.runner.GradleRunner#withPluginClasspath()} or
     * {@link org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading#readImplementationClasspath()}, respectively. Classpath is also filtered by
     * provided PMD dependencies.
     * <p>
     * <b>Note:</b> While debugging the problem appears to be that the used {@link org.gradle.api.plugins.PluginManager}
     * (=> {@link org.gradle.api.internal.plugins.DefaultPluginManager}) does not get the correct
     * {@link org.gradle.api.internal.plugins.PluginRegistry} containing the correct
     * {@link org.gradle.api.internal.initialization.ClassLoaderScope} with the injected classpath dependencies ... :-(
     *
     * @return a{@link String} containing all the dependencies which {@link org.gradle.testkit.runner.GradleRunner#withPluginClasspath()} uses
     */
    protected static createBuildScriptWithClasspathOfGradleTestKitMechanism() {
        """\
            buildscript {
                dependencies {
                    classpath files(
                        '${readImplementationClasspath()
                               .findAll{ !it.path.contains("net.sourceforge.pmd") }
                               .join("',\n                        '")
                        }'
                    )
                }
            }
            """.stripIndent()
    }
}
