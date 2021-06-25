/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public final class Netconf {
    /**
     * Enumeration of NETCONF layers, as established in
     * <a href="https://datatracker.ietf.org/doc/html/rfc4741#section-1.1">NETCONF</a>. This enumeration exists because
     * its semantics are implied by RFC6020 references to {@code error-tag} and its XML encoding.
     *
     * <p>
     * This enumeration corresponds to the {@code Layer} in:
     * <pre><code>
     *   NETCONF can be conceptually partitioned into four layers:
     *
     *              Layer                      Example
     *         +-------------+      +-----------------------------+
     *     (4) |   Content   |      |     Configuration data      |
     *         +-------------+      +-----------------------------+
     *                |                           |
     *         +-------------+      +-----------------------------+
     *     (3) | Operations  |      | &lt;get-config&gt;, &lt;edit-config&gt; |
     *         +-------------+      +-----------------------------+
     *                |                           |
     *         +-------------+      +-----------------------------+
     *     (2) |     RPC     |      |    &lt;rpc&gt;, &lt;rpc-reply&gt;       |
     *         +-------------+      +-----------------------------+
     *                |                           |
     *         +-------------+      +-----------------------------+
     *     (1) |  Transport  |      |   BEEP, SSH, SSL, console   |
     *         |   Protocol  |      |                             |
     *         +-------------+      +-----------------------------+
     * </code></pre>
     * as acknowledged in <a href="https://datatracker.ietf.org/doc/html/rfc6241#section-1.2">RFC6241</a>:
     * <pre>
     *   The YANG data modeling language [RFC6020] has been developed for
     *   specifying NETCONF data models and protocol operations, covering the
     *   Operations and the Content layers of Figure 1.
     * </pre>
     */
    public enum Layer {
        /**
         * Content layer, for example configuration data. This layer is implied indirectly in constructs defined in
         * {@link YangError}.
         */
        CONTENT(ErrorType.APPLICATION),
        /**
         * Operations layer, for example {@code <get-config>}, {@code <edit-config>} configuration data.
         */
        OPERATIONS(ErrorType.PROTOCOL),
        /**
         * Transport protocol layer, for example BEEP, SSH, TLS, console.
         */
        RPC(ErrorType.RPC),
        /**
         * RPC layer, for example {@code <rpc>}, {@code <rpc-reply>}.
         */
        TRANSPORT(ErrorType.TRANSPORT);

        private final ErrorType errorType;

        Layer(final ErrorType errorType) {
            this.errorType = requireNonNull(errorType);
        }

        public ErrorType errorType() {
            return errorType;
        }
    }

    /**
     * Enumeration of {@code error-type} values. These provide glue between {@link Layer} and various sources of
     * such errors.
     */
    public enum ErrorType {
        /**
         * A {@link Layer#TRANSPORT} layer error. This typically happens on transport endpoints, where a protocol
         * plugin needs to report a NETCONF-equivalent condition.
         */
        TRANSPORT("transport"),
        /**
         * A {@link Layer#RPC} layer error. This typically happens on request routers, where a request may end up
         * being resolved due to implementation-internal causes, such as timeouts and state loss.
         */
        RPC("rpc"),
        /**
         * A {@link Layer#OPERATIONS} layer error. These typically happen in a NETCONF protocol implementation.
         */
        PROTOCOL("protocol"),
        /**
         * A {@link Layer#CONTENT} layer error. These typically happen due to YANG data handling, such as
         * type checking and structural consistency.
         */
        APPLICATION("application");

        private static final Map<String, ErrorType> BY_NETCONF_STRING =
            Maps.uniqueIndex(Arrays.asList(values()), ErrorType::netconfString);

        private final String netconfString;

        ErrorType(final String netconfString) {
            this.netconfString = requireNonNull(netconfString);
        }

        public String netconfString() {
            return netconfString;
        }

        public static @Nullable ErrorType forNetconfString(final String netconfString) {
            return BY_NETCONF_STRING.get(requireNonNull(netconfString));
        }
    }
}
