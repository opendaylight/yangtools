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
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;

/**
 * A general base exception for an operation failure.
 *
 * @author Thomas Pantelis
 */
public class OperationFailedException extends Exception {

    private static final long serialVersionUID = 1L;

    private final List<RpcError> errorList;

    /**
     * Constructs a new instance with the specified detail message and errors.
     *
     * @param message the detail message
     * @param errors {@link RpcError} instances that provide additional error information about
     *               this exception
     */
    public OperationFailedException(final String message, final RpcError... errors) {
        this(message, null, errors);
    }

    /**
     * Constructs a new instance with the specified detail message, cause and errors.
     *
     * @param message the detail message
     * @param cause the cause
     * @param errors {@link RpcError} instances that provide additional error information about
     *               this exception
     */
    public OperationFailedException(final String message, final Throwable cause,
                                    final RpcError... errors) {
        super(requireNonNull(message), cause);

        if (errors != null && errors.length > 0) {
            errorList = ImmutableList.copyOf(Arrays.asList(errors));
        } else {
            // Add a default RpcError.
            errorList = ImmutableList.of(RpcResultBuilder.newError(ErrorType.APPLICATION, null,
                    getMessage(), null, null, getCause()));
        }
    }

    /**
     * Returns additional error information about this exception.
     *
     * @return a List of RpcErrors. There is always at least one RpcError.
     */
    public List<RpcError> getErrorList() {
        return errorList;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("message", getMessage())
                .add("errorList", errorList).toString();
    }
}
