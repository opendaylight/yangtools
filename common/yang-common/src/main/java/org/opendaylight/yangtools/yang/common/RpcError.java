/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Representation of an error.
 */
@NonNullByDefault
public interface RpcError {
    /**
     * {@return the error severity, as determined by the application reporting the error}
     */
    ErrorSeverity severity();

    /**
     * {@return the error severity, as determined by the application reporting the error}
     * @deprecated Use {@link #severity()} instead.
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default ErrorSeverity getSeverity() {
        return severity();
    }

    /**
     * {@return the conceptual layer at which the error occurred}
     */
    ErrorType type();

    /**
     * {@return the conceptual layer at which the error occurred}
     * @deprecated Use {@link #type()} instead.
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default ErrorType getErrorType() {
        return type();
    }

    /**
     * {@return an {@link ErrorMessage} suitable for human display that describes the error condition}
     */
    ErrorMessage message();

    /**
     * {@return an {@link ErrorMessage} suitable for human display that describes the error condition}
     * @deprecated Use {@link #message()} instead.
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default String getMessage() {
        return message().elementBody();
    }

    /**
     * Returns a short string that identifies the general type of error condition.
     *
     * <p>The following outlines suggested values as defined by
     * (<a href="https://www.rfc-editor.org/rfc/rfc6241#page-89">RFC6241</a>):
     *
     * <pre>
     *    access-denied
     *    bad-attribute
     *    bad-element
     *    data-exists
     *    data-missing
     *    in-use
     *    invalid-value
     *    lock-denied
     *    malformed-message
     *    missing-attribute
     *    missing-element
     *    operation-failed
     *    operation-not-supported
     *    resource-denied
     *    rollback-failed
     *    too-big
     *    unknown-attribute
     *    unknown-element
     *    unknown-namespace
     * </pre>
     * @return a string if available or null otherwise.
     */
    @Nullable ErrorTag tag();

    /**
     * Returns a short string that identifies the general type of error condition.
     *
     * <p>The following outlines suggested values as defined by
     * (<a href="https://www.rfc-editor.org/rfc/rfc6241#page-89">RFC6241</a>):
     *
     * <pre>
     *    access-denied
     *    bad-attribute
     *    bad-element
     *    data-exists
     *    data-missing
     *    in-use
     *    invalid-value
     *    lock-denied
     *    malformed-message
     *    missing-attribute
     *    missing-element
     *    operation-failed
     *    operation-not-supported
     *    resource-denied
     *    rollback-failed
     *    too-big
     *    unknown-attribute
     *    unknown-element
     *    unknown-namespace
     * </pre>
     * @return a string if available or null otherwise.
     * @deprecated Use {@link #tag()} instead.
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default @Nullable ErrorTag getTag() {
        return tag();
    }

    /**
     * Returns a short string that identifies the specific type of error condition as determined by the application
     * reporting the error.
     *
     * @return a string if available or null otherwise.
     */
    @Nullable String applicationTag();

    /**
     * Returns a short string that identifies the specific type of error condition as determined by the application
     * reporting the error.
     *
     * @return a string if available or null otherwise.
     * @deprecated Use {@link #applicationTag()} instead.
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default @Nullable String getApplicationTag() {
        return applicationTag();
    }

    /**
     * Returns a string containing additional information to provide extended and/or implementation-specific debugging
     * information.
     *
     * @return a string if available or null otherwise.
     */
    // FIXME: YANGTOOLS-765: this is wrong and needs to be modeled at data-api layer with YangErrorInfo
    @Nullable String info();

    /**
     * Returns a string containing additional information to provide extended and/or implementation-specific debugging
     * information.
     *
     * @return a string if available or null otherwise.
     * @deprecated Use {@link #info()} instead.
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default @Nullable String getInfo() {
        return info();
    }

    /**
     * {@return a Throwable if the error was triggered by exception, null otherwise}
     */
    @Nullable Throwable cause();

    /**
     * Returns an exception cause.
     *
     * {@return a Throwable if the error was triggered by exception, null otherwise}
     * @deprecated Use {@link #cause()} instead.
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default @Nullable Throwable getCause() {
        return cause();
    }
}
