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
}
