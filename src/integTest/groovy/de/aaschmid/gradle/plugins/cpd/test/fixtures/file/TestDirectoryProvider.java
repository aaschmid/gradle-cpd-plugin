/* Copied from https://github.com/gradle/gradle, moved to different packages and adjusted to my custom need */
package de.aaschmid.gradle.plugins.cpd.test.fixtures.file;

/**
 * Implementations provide a working space to be used in tests.
 * <p/>
 * The client is not responsible for removing any files.
 */
public interface TestDirectoryProvider {

    /**
     * The directory to use, guaranteed to exist.
     *
     * @return The directory to use, guaranteed to exist.
     */
    TestFile getTestDirectory();

}
