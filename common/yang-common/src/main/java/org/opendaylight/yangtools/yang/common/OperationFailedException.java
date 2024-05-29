/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import java.io.Serial;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

/**
 * A general base exception for an operation failure.
 *
 * @author Thomas Pantelis
 */
public class OperationFailedException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    private final ImmutableList<RpcError> errorList;

    /**
     * Constructs a new instance with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     * @throws NullPointerException if {@code message} is {@code null}
     */
    public OperationFailedException(final String message, final Throwable cause) {
        super(requireNonNull(message), cause);
        errorList = null;
    }

    /**
     * Constructs a new instance with the specified detail message and error.
     *
     * @param message the detail message
     * @param error {@link RpcError} instance that provides additional error information about this exception
     * @throws NullPointerException if any argument is {@code null}
     */
    public OperationFailedException(final String message, final RpcError error) {
        super(requireNonNull(message));
        errorList = ImmutableList.of(error);
    }

    /**
     * Constructs a new instance with the specified detail message, cause and errors.
     *
     * @param message the detail message
     * @param cause the cause
     * @param errors {@link RpcError} instances that provide additional error information about this exception
     * @throws NullPointerException if either {@code message} or {@code errors} is {@code null}, or if {@code errors}
     *                              contains a {@code null} element.
     */
    public OperationFailedException(final String message, final Throwable cause, final Collection<RpcError> errors) {
        super(requireNonNull(message), cause);
        errorList = errors.isEmpty() ? null : ImmutableList.copyOf(errors);
    }

    /**
     * Constructs a new instance with the specified detail message and errors.
     *
     * @param message the detail message
     * @param errors {@link RpcError} instances that provide additional error information about this exception
     * @throws NullPointerException if any argument is, or {@code errors} contains, {@code null}
     */
    public OperationFailedException(final String message, final Collection<? extends RpcError> errors) {
        super(requireNonNull(message));
        errorList = ImmutableList.copyOf(errors);
    }

    /**
     * Constructs a new instance with the specified detail message and errors.
     *
     * @param message the detail message
     * @param errors {@link RpcError} instances that provide additional error information about this exception
     * @throws NullPointerException if any argument is, or {@code errors} contains, {@code null}
     */
    public OperationFailedException(final String message, final RpcError... errors) {
        this(message, null, errors);
    }

    /**
     * Constructs a new instance with the specified detail message, cause and errors.
     *
     * @param message the detail message
     * @param cause the cause
     * @param errors {@link RpcError} instances that provide additional error information about this exception
     */
    public OperationFailedException(final String message, final Throwable cause, final RpcError... errors) {
        this(message, cause, Arrays.asList(errors));
    }

    /**
     * Returns additional error information about this exception.
     *
     * @return a List of RpcErrors. There is always at least one RpcError.
     */
    public List<RpcError> getErrorList() {
        return errorList != null ? errorList : ImmutableList.of(
            RpcResultBuilder.newError(ErrorType.APPLICATION, null, getMessage(), null, null, getCause()));
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("message", getMessage()).add("errorList", getErrorList());
    }

    protected static <X extends OperationFailedException> X ofCaught(final Throwable caught, final Class<X> type,
            final String operationName, final BiFunction<String, Throwable, X> toException) {
        // If the cause is a TransactionCommitFailedException, return that
        if (type.isInstance(caught)) {
            return type.cast(caught);
        }

        // Unmap ExecutionException's cause if possible
        if (caught instanceof ExecutionException e) {
            final var cause = e.getCause();
            if (type.isInstance(cause)) {
                return type.cast(caught);
            } else if (cause != null) {
                return toException.apply(operationName + " execution failed", cause);
            }
        }

        // Otherwise return an instance of the specified type with the original cause.
        final String message;
        if (caught instanceof InterruptedException) {
            message = operationName + " was interupted";
        } else if (caught instanceof CancellationException) {
            message = operationName + " was cancelled";
        } else {
            // We really should not get here but need to cover it anyway for completeness.
            message = operationName + " encountered unexpected failure";
        }
        return toException.apply(message, caught);
    }
}
