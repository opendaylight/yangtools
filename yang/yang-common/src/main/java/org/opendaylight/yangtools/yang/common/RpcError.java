/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

/**
 * Representation of an error.
 *
 */
public interface RpcError {

    enum ErrorSeverity {
        ERROR,
        WARNING
    }

    enum ErrorType {
        /**
         * Indicates an error occurred during transport of data, eg over the network.
         */
        TRANSPORT,

        /**
         * Indicates an error occurred during a remote procedure call.
         */
        RPC,

        /**
         * Indicates an error at a protocol layer, eg if invalid data was passed by the caller.
         */
        PROTOCOL,

        /**
         * Indicates an error occurred during internal processing.
         */
        APPLICATION
    }

    /**
     * Returns the error severity, as determined by the application reporting the error.
     *
     * @return an {@link ErrorSeverity} enum.
     */
    ErrorSeverity getSeverity();

    /**
     * Returns a short string that identifies the general type of error condition.
     *
     * <p>
     * The following outlines suggested values as defined by netconf (<a href="https://tools.ietf.org/html/rfc6241#page-89">RFC 6241</a>):
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
    String getTag();

    /**
     * Returns a short string that identifies the specific type of error condition as
     * determined by the application reporting the error.
     *
     * @return a string if available or null otherwise.
     */
    String getApplicationTag();

    /**
     * Returns a string suitable for human display that describes the error
     * condition.
     *
     * @return a message string.
     */
    String getMessage();

    /**
     * Returns a string containing additional information to provide extended
     * and/or implementation-specific debugging information.
     *
     * @return a string if available or null otherwise.
     */
    String getInfo();

    /**
     * Returns an exception cause.
     *
     * @return a Throwable if the error was triggered by exception, null otherwise.
     */
    Throwable getCause();

    /**
     * Returns the conceptual layer at which the error occurred.
     *
     * @return an {@link ErrorType} enum.
     */
    ErrorType getErrorType();
}
