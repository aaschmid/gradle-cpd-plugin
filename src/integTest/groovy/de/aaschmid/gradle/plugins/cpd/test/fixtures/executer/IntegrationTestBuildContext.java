/* Copied from https://github.com/gradle/gradle, moved to different packages and adjusted to my custom need */
package de.aaschmid.gradle.plugins.cpd.test.fixtures.executer;

import de.aaschmid.gradle.plugins.cpd.test.fixtures.file.TestFile;

import java.io.File;

/**
 * Provides values that are set during the build, or defaulted when not running in a build context (e.g. IDE).
 */
public class IntegrationTestBuildContext {

    public TestFile getGradleHomeDir() {
        return file("integTest.gradleHomeDir", null);
    }

    public String getIntegTestAdditionalClasspath() {
        return property("integTest.additionalClasspath", null);
    }

    public TestFile getIntegTestWorkerDir() {
        return file("integTest.workDir", "build/integTestWorkDir").file("worker-1");
    }

    private static String property(String propertyName, String defaultString) {
        String property = System.getProperty(propertyName, defaultString);
        if (property == null) {
            throw new RuntimeException(String.format("You must set the '%s' property to run the integration tests. The default passed was: '%s'",
                    propertyName, defaultString));
        }
        return property;
    }

    private static TestFile file(String propertyName, String defaultFile) {
        return new TestFile(new File(property(propertyName, defaultFile)));
    }
}
