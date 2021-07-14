/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;

/**
 * An error condition raised as a consequence of a YANG-defined contract. This interface should not be directly
 * implemented, but rather should be attached to a well-defined Exception class.
 *
 * @deprecated This interface is not completely sufficient to describe errors, as it lacks the ability to carry instance
 *             path as well as more-info elements.
 */
@Beta
@Deprecated(forRemoval = true, since = "7.0.4")
// FIXME: 8.0.0: remove this class
public interface YangError {
    /**
     * Returns the conceptual layer at which the error occurred.
     *
     * @return an {@link ErrorType} enum.
     */
    @NonNull ErrorType getErrorType();

    /**
     * Returns the error severity, as determined by the application reporting the error.
     *
     * @return an {@link ErrorSeverity} enum.
     */
    @NonNull ErrorSeverity getSeverity();

    /**
     * Returns the error tag, as determined by the application reporting the error.
     *
     * @return an error tag.
     */
    @NonNull String getErrorTag();

    /**
     * Returns the value of the argument of YANG {@code error-app-tag} statement.
     *
     * @return string with the application error tag, or empty if it was not provided.
     */
    Optional<String> getErrorAppTag();

    /**
     * Returns the value of the argument of YANG {@code error-message} statement.
     *
     * @return string with the error message, or empty if it was not provided.
     */
    Optional<String> getErrorMessage();
}
