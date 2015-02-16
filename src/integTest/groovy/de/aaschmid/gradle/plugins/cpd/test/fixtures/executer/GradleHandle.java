/* Copied from https://github.com/gradle/gradle, moved to different packages and adjusted to my custom need */
package de.aaschmid.gradle.plugins.cpd.test.fixtures.executer;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.gradle.api.Action;
import org.gradle.internal.Factory;
import org.gradle.internal.UncheckedException;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.AbstractExecHandleBuilder;
import org.gradle.process.internal.ExecHandle;
import org.gradle.process.internal.ExecHandleState;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class GradleHandle {
    final private Factory<? extends AbstractExecHandleBuilder> execHandleFactory;

    final private ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
    final private ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
    private final Action<OutputScrapingExecutionResult> resultAssertion;

    private ExecHandle execHandle;
    private final String outputEncoding;

    public GradleHandle(Action<OutputScrapingExecutionResult> resultAssertion, String outputEncoding, Factory<? extends AbstractExecHandleBuilder> execHandleFactory) {
        this.resultAssertion = resultAssertion;
        this.execHandleFactory = execHandleFactory;
        this.outputEncoding = outputEncoding;
    }

    protected OutputScrapingExecutionResult toExecutionResult(String output, String error) {
        return new OutputScrapingExecutionResult(output, error);
    }

    protected OutputScrapingExecutionFailure toExecutionFailure(String output, String error) {
        return new OutputScrapingExecutionFailure(output, error);
    }

    /**
     * Returns the stdout output currently received from the build. This is live.
     */
    public String getStandardOutput() {
        try {
            return standardOutput.toString(outputEncoding);
        } catch (UnsupportedEncodingException e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }

    /**
     * Returns the stderr output currently received from the build. This is live.
     */
    public String getErrorOutput() {
        try {
            return errorOutput.toString(outputEncoding);
        } catch (UnsupportedEncodingException e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }

    public GradleHandle start() {
        if (execHandle != null) {
            throw new IllegalStateException("you have already called start() on this handle");
        }

        AbstractExecHandleBuilder execBuilder = execHandleFactory.create();
        execBuilder.setStandardOutput(new CloseShieldOutputStream(new TeeOutputStream(System.out, standardOutput)));
        execBuilder.setErrorOutput(new CloseShieldOutputStream(new TeeOutputStream(System.err, errorOutput)));
        execHandle = execBuilder.build();

        execHandle.start();

        return this;
    }

    /**
     * Forcefully kills the build and returns immediately. Does not block until the build has finished.
     */
    public GradleHandle abort() {
        getExecHandle().abort();
        return this;
    }

    /**
     * Returns true if the build is currently running.
     */
    public boolean isRunning() {
        return execHandle != null && execHandle.getState() == ExecHandleState.STARTED;
    }

    protected ExecHandle getExecHandle() {
        if (execHandle == null) {
            throw new IllegalStateException("you must call start() before calling this method");
        }

        return execHandle;
    }

    /**
     * Blocks until the build is complete and assert that the build completed successfully.
     */
    public OutputScrapingExecutionResult waitForFinish() {
        return waitForStop(false);
    }

    /**
     * Blocks until the build is complete and assert that the build completed with a failure.
     */
    public OutputScrapingExecutionFailure waitForFailure() {
        return (OutputScrapingExecutionFailure) waitForStop(true);
    }

    protected OutputScrapingExecutionResult waitForStop(boolean expectFailure) {
        ExecHandle execHandle = getExecHandle();
        ExecResult execResult = execHandle.waitForFinish();
        execResult.rethrowFailure(); // nop if all ok

        String output = getStandardOutput();
        String error = getErrorOutput();

        boolean didFail = execResult.getExitValue() != 0;
        if (didFail != expectFailure) {
            String message = String.format("Gradle execution %s in %s with: %s %s%nOutput:%n%s%n-----%nError:%n%s%n-----%n",
                    expectFailure ? "did not fail" : "failed", execHandle.getDirectory(), execHandle.getCommand(), execHandle.getArguments(), output, error);
            throw new UnexpectedBuildFailure(message);
        }

        OutputScrapingExecutionResult executionResult = expectFailure ? toExecutionFailure(output, error) : toExecutionResult(output, error);
        resultAssertion.execute(executionResult);
        return executionResult;
    }
}
