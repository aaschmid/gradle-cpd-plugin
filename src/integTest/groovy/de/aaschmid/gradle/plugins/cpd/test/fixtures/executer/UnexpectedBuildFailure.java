/* Copied from https://github.com/gradle/gradle, moved to different packages and adjusted to my custom need */
package de.aaschmid.gradle.plugins.cpd.test.fixtures.executer;

public class UnexpectedBuildFailure extends RuntimeException {
    public UnexpectedBuildFailure(String message) {
        super(message);
    }

    public UnexpectedBuildFailure(Exception e) {
        super(e);
    }
}
