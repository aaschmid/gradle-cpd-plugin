/* Copied from https://github.com/gradle/gradle, moved to different packages and adjusted to my custom need */
package de.aaschmid.gradle.plugins.cpd.test.fixtures.file

class ExecOutput {

    ExecOutput(String rawOutput, String error) {
        this.rawOutput = rawOutput
        this.out = rawOutput.replaceAll("\r\n|\r", "\n")
        this.error = error
    }

    String rawOutput
    String out
    String error
}
