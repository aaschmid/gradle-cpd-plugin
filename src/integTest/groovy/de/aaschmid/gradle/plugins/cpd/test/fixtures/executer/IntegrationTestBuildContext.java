/* Copied from https://github.com/gradle/gradle, moved to different packages and adjusted to my custom need */
package de.aaschmid.gradle.plugins.cpd.test.fixtures.executer;

import de.aaschmid.gradle.plugins.cpd.test.fixtures.file.TestFile;
import org.gradle.util.GradleVersion;

import java.io.File;

/**
 * Provides values that are set during the build, or defaulted when not running in a build context (e.g. IDE).
 */
public class IntegrationTestBuildContext {

    public TestFile getGradleHomeDir() {
        return file("integTest.gradleHomeDir", null); // TODO does not work from IDE
    }

    public TestFile getGradleCpdPluginJar() {
        return file("integTest.cpdPluginJar", null); // TODO does not work from IDE
    }

    public TestFile getGradleUserHomeDir() {
        return file("integTest.gradleUserHomeDir", "build/integTestHomeDir").file("worker-1");
    }

    private static TestFile file(String propertyName, String defaultFile) {
        String path = System.getProperty(propertyName, defaultFile);
        if (path == null) {
            throw new RuntimeException(String.format("You must set the '%s' property to run the integration tests. The default passed was: '%s'",
                    propertyName, defaultFile));
        }
        return new TestFile(new File(path));
    }
}
