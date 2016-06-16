package de.aaschmid.gradle.plugins.cpd.internal;

import org.gradle.api.UncheckedIOException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Wraps a checked exception. Carries no other context.
 */
public final class CpdUncheckedException extends RuntimeException {
    public CpdUncheckedException(Throwable cause) {
        super(cause);
    }

    /**
     * Note: always throws the failure in some form. The return value is to keep the compiler happy.
     * @param t to be rethrown as Runtime Exception
     * @return an instance of RuntimeException
     */
    public static RuntimeException throwAsUncheckedException(Throwable t) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        if (t instanceof IOException) {
            throw new UncheckedIOException(t);
        }
        throw new CpdUncheckedException(t);
    }

    /**
     * Uwraps passed InvocationTargetException hence making the stack of exceptions cleaner without losing information.
     *
     * Note: always throws the failure in some form. The return value is to keep the compiler happy.
     *
     * @param e to be unwrapped
     * @return an instance of RuntimeException based on the target exception of the parameter.
     */
    public static RuntimeException unwrapAndRethrow(InvocationTargetException e) {
        return CpdUncheckedException.throwAsUncheckedException(e.getTargetException());
    }
}