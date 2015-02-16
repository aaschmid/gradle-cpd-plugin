/* Copied from https://github.com/gradle/gradle, moved to different packages and adjusted to my custom need */
package de.aaschmid.gradle.plugins.cpd.test.fixtures

import de.aaschmid.gradle.plugins.cpd.test.fixtures.executer.ForkingGradleExecuter
import de.aaschmid.gradle.plugins.cpd.test.fixtures.executer.GradleExecuter
import de.aaschmid.gradle.plugins.cpd.test.fixtures.executer.IntegrationTestBuildContext
import de.aaschmid.gradle.plugins.cpd.test.fixtures.executer.OutputScrapingExecutionFailure
import de.aaschmid.gradle.plugins.cpd.test.fixtures.executer.OutputScrapingExecutionResult
import de.aaschmid.gradle.plugins.cpd.test.fixtures.file.TestDirectoryProvider
import de.aaschmid.gradle.plugins.cpd.test.fixtures.file.TestFile
import de.aaschmid.gradle.plugins.cpd.test.fixtures.file.TestNameTestDirectoryProvider
import org.gradle.api.Action
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import spock.lang.Specification


/**
 * Spockified version of AbstractIntegrationTest.
 *
 * Plan is to bring features over as needed.
 */
class AbstractIntegrationSpec extends Specification implements TestDirectoryProvider {

    @Rule
    final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider() {

        @Override
        Statement apply(Statement base, FrameworkMethod method, Object target) {
            return super.apply(new Statement() {

                @Override
                void evaluate() throws Throwable {
                    try {
                        base.evaluate()
                    } finally {
                        cleanupWhileTestFilesExist()
                    }
                }
            }, method, target)
        }
    }

    IntegrationTestBuildContext buildContext = new IntegrationTestBuildContext()
    GradleExecuter executer = new ForkingGradleExecuter(buildContext, temporaryFolder)

    OutputScrapingExecutionResult result
    OutputScrapingExecutionFailure failure

    /**
     * Adds an init script to the executer which adds the gradle-cpd-plugin to the build scripts classpath.
     */
    protected void addClasspathDependencyForCpdPlugin() {
        executer.usingInitScript(file('addClasspathDependencyForCpdPluginInit.gradle') << """
            allprojects {
                buildscript {
                    dependencies {
                        classpath files('${executer.buildContext.gradleCpdPluginJar}')
                    }
                }
            }
            """.stripIndent())
    }

    protected void cleanupWhileTestFilesExist() {
    }

    protected TestFile getBuildFile() {
        testDirectory.file('build.gradle')
    }

    protected TestFile buildScript(String script) {
        buildFile.text = script
        buildFile
    }

    protected TestFile getSettingsFile() {
        testDirectory.file('settings.gradle')
    }

    protected TestNameTestDirectoryProvider getTestDirectoryProvider() {
        temporaryFolder
    }

    TestFile getTestDirectory() {
        temporaryFolder.testDirectory
    }

    protected TestFile file(Object... path) {
        if (path.length == 1 && path[0] instanceof TestFile) {
            return path[0] as TestFile
        }
        getTestDirectory().file(path);
    }

    protected GradleExecuter inDirectory(String path) {
        inDirectory(file(path))
    }

    protected GradleExecuter inDirectory(File directory) {
        executer.inDirectory(directory);
    }

    protected GradleExecuter projectDir(path) {
        executer.usingProjectDirectory(file(path))
    }

    protected GradleExecuter requireOwnGradleUserHomeDir() {
        executer.requireOwnGradleUserHomeDir()
        executer
    }

    protected GradleExecuter requireGradleHome() {
        executer.requireGradleHome()
        executer
    }

    /**
     * Synonym for succeeds()
     */
    protected OutputScrapingExecutionResult run(String... tasks) {
        succeeds(*tasks)
    }

    protected GradleExecuter args(String... args) {
        executer.withArguments(args)
    }

    protected GradleExecuter withDebugLogging() {
        executer.withArgument("-d")
    }

    protected OutputScrapingExecutionResult succeeds(String... tasks) {
        result = executer.withTasks(*tasks).run()
    }

    protected OutputScrapingExecutionFailure runAndFail(String... tasks) {
        fails(*tasks)
    }

    protected OutputScrapingExecutionFailure fails(String... tasks) {
        failure = executer.withTasks(*tasks).runWithFailure()
        result = failure
    }

    protected List<String> getExecutedTasks() {
        assertHasResult()
        result.executedTasks
    }

    protected Set<String> getSkippedTasks() {
        assertHasResult()
        result.skippedTasks
    }

    protected List<String> getNonSkippedTasks() {
        executedTasks - skippedTasks
    }

    protected void executedAndNotSkipped(String... tasks) {
        tasks.each{
            assert it in executedTasks
            assert !skippedTasks.contains(it)
        }
    }

    protected void skipped(String... tasks) {
        tasks.each{
            assert it in executedTasks
            assert skippedTasks.contains(it)
        }
    }

    protected void notExecuted(String... tasks) {
        tasks.each{
            assert !(it in executedTasks)
        }
    }

    protected void executed(String... tasks) {
        tasks.each{
            assert (it in executedTasks)
        }
    }

    protected void failureHasCause(String cause) {
        failure.assertHasCause(cause)
    }

    protected void failureDescriptionStartsWith(String description) {
        failure.assertThatDescription(CoreMatchers.startsWith(description))
    }

    protected void failureDescriptionContains(String description) {
        failure.assertThatDescription(CoreMatchers.containsString(description))
    }

    private assertHasResult() {
        assert result != null: "result is null, you haven't run succeeds()"
    }

    String getOutput() {
        result.output
    }

    String getErrorOutput() {
        result.error
    }

    public GradleExecuter using(Action<GradleExecuter> action) {
        action.execute(executer)
        executer
    }

    def createZip(String name, Closure cl) {
        TestFile zipRoot = file("${name}.root")
        TestFile zip = file(name)
        zipRoot.create(cl)
        zipRoot.zipTo(zip)
    }

    def createDir(String name, Closure cl) {
        TestFile root = file(name)
        root.create(cl)
    }
}