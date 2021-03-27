package de.aaschmid.gradle.plugins.cpd.test

import de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.Lang
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildResultException
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static de.aaschmid.gradle.plugins.cpd.test.TestFileResolver.*
import static org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading.*

abstract class IntegrationBaseSpec extends Specification {

    @Rule
    protected final TemporaryFolder testProjectDir = new TemporaryFolder()

    protected File buildFile
    protected File settingsFile

    protected static String testPath(Lang lang, String...relativePaths) {
        return "['${relativePaths.collect{testFile(lang, it).getAbsolutePath()}.join("', '")}']"
    }

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        settingsFile = testProjectDir.newFile('settings.gradle')
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
        return testProjectDir.getRoot().toPath().resolve(pathWithinProjectFolder).toFile()
    }

    protected BuildResult runWithoutPluginClasspath(String... arguments) {
        try {
            return GradleRunner.create()
                    .withProjectDir(testProjectDir.root)
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
                    .withProjectDir(testProjectDir.root)
                    .withArguments(arguments)
                    .withPluginClasspath()
//                    .withDebug(true)
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
