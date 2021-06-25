/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Representation of an operation error in YANG world. Due to the unfortunate structuring of RFC6020, where
 * <a href="https://datatracker.ietf.org/doc/html/rfc6020#section-8.3">Section 8.3</a>, explicitly references
 * <a href="https://datatracker.ietf.org/doc/html/rfc4741#section-4.3">RFC4741 NETCONF</a> via {@link #getTag()}
 * allocation and corresponding {@link #getInfo()} structural reference.
 */
public interface RpcError {
    // FIXME: 8.0.0: remove this in favor of Netconf.ErrorSeverity
    enum ErrorSeverity {
        ERROR,
        WARNING
    }

    /**
     * Enumeration of {@code error-type} values. These provide glue between {@link NetconfLayer} and various sources of
     * such errors.
     */
    // FIXME: 8.0.0: remove this in favor of Netconf.ErrorType
    enum ErrorType {
        /**
         * A {@link NetconfLayer#TRANSPORT} layer error. This typically happens on transport endpoints, where a protocol
         * plugin needs to report a NETCONF-equivalent condition.
         */
        TRANSPORT("transport"),
        /**
         * A {@link NetconfLayer#RPC} layer error. This typically happens on request routers, where a request may end up
         * being resolved due to implementation-internal causes, such as timeouts and state loss.
         */
        RPC("rpc"),
        /**
         * A {@link NetconfLayer#OPERATIONS} layer error. These typically happen in a NETCONF protocol implementation.
         */
        PROTOCOL("protocol"),
        /**
         * A {@link NetconfLayer#CONTENT} layer error. These typically happen due to YANG data handling, such as
         * type checking and structural consistency.
         */
        APPLICATION("application");

        private static final Map<String, ErrorType> BY_NETCONF_STRING =
            Maps.uniqueIndex(Arrays.asList(values()), ErrorType::netconfString);

        private final @NonNull String netconfString;

        ErrorType(final @NonNull String netconfString) {
            this.netconfString = requireNonNull(netconfString);
        }

        public @NonNull String netconfString() {
            return netconfString;
        }

        public static @Nullable ErrorType forNetconfString(final String netconfString) {
            return BY_NETCONF_STRING.get(requireNonNull(netconfString));
        }
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
     * The following outlines suggested values as defined by
     * (<a href="https://tools.ietf.org/html/rfc6241#page-89">RFC6241</a>):
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
    // FIXME: return Netconf.ErrorTag here
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
    // FIXME: YANGTOOLS-765: return a Set<Netconf.ErrorInfo> here
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
