/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;

/**
 * Utility exception mapper which translates an Exception to a specified type of Exception. This mapper is intended to
 * be primarily used with {@code com.google.common.util.concurrent.Futures.makeChecked()}
 * <ul>
 *   <li>if exception is the specified type or one of its subclasses, it returns original exception.
 *   <li>if exception is {@link ExecutionException} and the cause is of the specified type, it returns the cause
 *   <li>otherwise returns an instance of the specified exception type with original exception as the cause.
 * </ul>
 *
 * @param <X> the exception type
 * @author Thomas Pantelis
 */
@SuppressModernizer
public abstract class ExceptionMapper<X extends Exception> implements Function<Exception, X> {

    private final Class<X> exceptionType;
    private final String opName;

    /**
     * Constructor.
     *
     * @param opName the String prefix for exception messages.
     * @param exceptionType the exception type to which to translate.
     */
    public ExceptionMapper(final String opName, final Class<X> exceptionType) {
        this.exceptionType = requireNonNull(exceptionType);
        this.opName = requireNonNull(opName);
    }

    /**
     * Return the exception class produced by this instance.
     *
     * @return Exception class.
     */
    protected final Class<X> getExceptionType() {
        return exceptionType;
    }

    /**
     * Invoked to create a new exception instance of the specified type.
     *
     * @param message the message for the new exception.
     * @param cause the cause for the new exception.
     *
     * @return an instance of the exception type.
     */
    protected abstract X newWithCause(String message, Throwable cause);

    @Override
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
    public X apply(final Exception input) {

        // If exception is of the specified type,return it.
        if (exceptionType.isInstance(input)) {
            return exceptionType.cast(input);
        }

        // If exception is ExecutionException whose cause is of the specified
        // type, return the cause.
        if (input instanceof ExecutionException) {
            final var cause = input.getCause();
            if (exceptionType.isInstance(cause)) {
                return exceptionType.cast(cause);
            } else if (cause != null) {
                return newWithCause(opName + " execution failed", cause);
            }
        }

        // Otherwise return an instance of the specified type with the original
        // cause.

        if (input instanceof InterruptedException) {
            return newWithCause(opName + " was interupted.", input);
        }

        if (input instanceof CancellationException) {
            return newWithCause(opName + " was cancelled.", input);
        }

        // We really shouldn't get here but need to cover it anyway for completeness.
        return newWithCause(opName + " encountered an unexpected failure", input);
    }
}
