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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;

/**
 * An error condition raised as a consequence of a YANG-defined contract. This interface should not be directly
 * implemented, but rather should be attached to a well-defined Exception class.
 *
 * @author Robert Varga
 */
// FIXME: as per RFC8040 definition
@Beta
public interface YangError {

    ErrorType errorType();

    String errorTag();

    @Nullable String errorAppTag();

    @Nullable String errorMessage();

    /**
     * Returns the conceptual layer at which the error occurred.
     *
     * @return an {@link ErrorType} enum.
     */
    default @NonNull ErrorType getErrorType() {
        return ErrorType.APPLICATION;
    }

    /**
     * Returns the error severity, as determined by the application reporting the error.
     *
     * @return an {@link ErrorSeverity} enum.
     */
    default @NonNull ErrorSeverity getSeverity() {
        return ErrorSeverity.ERROR;
    }

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
    default Optional<String> getErrorAppTag() {
        return Optional.empty();
    }

    /**
     * Returns the value of the argument of YANG {@code error-message} statement.
     *
     * @return string with the error message, or empty if it was not provided.
     */
    default Optional<String> getErrorMessage() {
        return Optional.empty();
    }

    // FIXME: decide on naming these and handle everything in RFC6020/RFC7950/RFC7951 at least.

    public interface ConstraintViolation extends YangError {
        @Override
        default String getErrorTag() {
            return "invalid-value";
        }
    }

    // violation of a unique statement, 13.1
    // FIXME: add 13.2 max-elements
    // FIXME: add 13.3 min-elements
    // FIXME: add 13.4 must
    // FIXME: add 13.5 require-instance
    // FIXME: add 13.6 leafref
    // FIXME: add 13.7 mandatory choice
    // FIXME: add 13.8 "insert with invalid "key"/"value"
    public interface UniqueViolation extends YangError {
        @Override
        default String getErrorTag() {
            return "operation-failed";
        }

        @Override
        default Optional<String> getErrorAppTag() {
            return Optional.of("data-not-unique");
        }

        // FIXME: List<YangInstanceIdentifier> getNonUnique()
    }
}
