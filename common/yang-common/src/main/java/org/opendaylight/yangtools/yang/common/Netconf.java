/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
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
     * Enumeration of {@code error-severity} values.
     */
    enum ErrorSeverity implements ElementBodyEquivalent {
        /**
         * An error preventing an operation from completing successfully.
         */
        ERROR("error"),
        /**
         * A warning not affecting an operation's ability to complete successfully.
         */
        WARNING("warning");

        private static final Map<String, ErrorSeverity> BY_ELEMENT_BODY =
            Maps.uniqueIndex(Arrays.asList(values()), ErrorSeverity::toElementBody);

        private final String elementBody;

        ErrorSeverity(final String elementName) {
            this.elementBody = requireNonNull(elementName);
        }

        @Override
        public String toElementBody() {
            return elementBody;
        }

        public static @Nullable ErrorSeverity forElementBody(final String elementBody) {
            return BY_ELEMENT_BODY.get(requireNonNull(elementBody));
        }
    }

    /**
     * Enumeration of {@code error-type} values. These provide glue between {@link Layer} and various sources of
     * such errors.
     */
    public enum ErrorType implements ElementBodyEquivalent {
        /**
         * A {@link Layer#TRANSPORT} layer error. This typically happens on transport endpoints, where a protocol
         * plugin needs to report a NETCONF-equivalent condition.
         */
        TRANSPORT("transport", Layer.TRANSPORT),
        /**
         * A {@link Layer#RPC} layer error. This typically happens on request routers, where a request may end up
         * being resolved due to implementation-internal causes, such as timeouts and state loss.
         */
        RPC("rpc", Layer.RPC),
        /**
         * A {@link Layer#OPERATIONS} layer error. These typically happen in a NETCONF protocol implementation.
         */
        PROTOCOL("protocol", Layer.OPERATIONS),
        /**
         * A {@link Layer#CONTENT} layer error. These typically happen due to YANG data handling, such as
         * type checking and structural consistency.
         */
        APPLICATION("application", Layer.CONTENT);

        private static final Map<String, ErrorType> BY_ELEMENT_BODY =
            Maps.uniqueIndex(Arrays.asList(values()), ErrorType::toElementBody);

        private final String elementBody;
        private final Layer layer;

        ErrorType(final String elementName, final Layer layer) {
            this.elementBody = requireNonNull(elementName);
            this.layer = requireNonNull(layer);
        }

        @Override
        public String toElementBody() {
            return elementBody;
        }

        public Layer layer() {
            return layer;
        }

        public static @Nullable ErrorType forElementBody(final String elementBody) {
            return BY_ELEMENT_BODY.get(requireNonNull(elementBody));
        }
    }

    // FIXME: document this as coming from RFC6241, Appendix A
    // FIXME: note how this can be extended for forward-proofing
    public static final class ErrorTag implements ElementBodyEquivalent {
        // violation of a value constraint, 8.3.1 bullet 1
        public static final ErrorTag INVALID_VALUE = new ErrorTag("invalid-value");
        // missing a mandatory leaf (explicit, or implied by key), 8.3.1 bullet 2, 7.13.2
        public static final ErrorTag MISSING_ELEMENT = new ErrorTag("missing-element");
        // choice/case exclusions, 8.3.1 bullet 3
        public static final ErrorTag BAD_ELEMENT = new ErrorTag("bad-element");
        // unknown node encountered: might be if-feature, when, or whatever, 8.3.1 bullets 4, 5
        public static final ErrorTag UNKNOWN_ELEMENT = new ErrorTag("unknown-element");
        // violation of a structural constraint, 13.1-13.4
        public static final ErrorTag OPERATION_FAILED = new ErrorTag("operation-failed");
        // violation of a data reference constraint, 13.5-13.7
        public static final ErrorTag DATA_MISSING = new ErrorTag("data-missing");
        // violation of a attribute value requirement, 13.8
        public static final ErrorTag BAD_ATTRIBUTE = new ErrorTag("bad-attribute");

        public static final ErrorTag IN_USE = new ErrorTag("in-use");
        public static final ErrorTag TOO_BIG = new ErrorTag("too-big");
        public static final ErrorTag MISSING_ATTRIBUTE = new ErrorTag("missing-attribute");
        public static final ErrorTag UNKNOWN_ATTRIBUTE = new ErrorTag("unknown-attribute");
        public static final ErrorTag UNKNOWN_NAMESPACE = new ErrorTag("unknown-namespace");
        public static final ErrorTag ACCESS_DENIED = new ErrorTag("access-denied");
        public static final ErrorTag LOCK_DENIED = new ErrorTag("lock-denied");
        public static final ErrorTag RESOURCE_DENIED = new ErrorTag("resource-denied");
        public static final ErrorTag ROLLBACK_FAILED = new ErrorTag("rollback-failed");
        public static final ErrorTag DATA_EXISTS = new ErrorTag("data-exists");
        public static final ErrorTag OPERATION_NOT_SUPPORTED = new ErrorTag("operation-not-supported");
        @Deprecated
        public static final ErrorTag PARTIAL_OPERATION = new ErrorTag("partial-operation");

        private final String elementBody;

        ErrorTag(final String elementBody) {
            this.elementBody = requireNonNull(elementBody);
        }

        @Override
        public String toElementBody() {
            return elementBody;
        }

        @Override
        public int hashCode() {
            return elementBody.hashCode();
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return obj == this || obj instanceof ErrorTag && elementBody.equals(((ErrorTag) obj).elementBody);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("elementBody", elementBody).toString();
        }
    }

    // FIXME: document this
    public interface ElementBodyEquivalent {

        String toElementBody();
    }

    // FIXME: document
    public interface YangError {

        ErrorTag errorTag();

        default @Nullable String errorAppTag() {
            return null;
        }

        default @Nullable String errorMessage() {
            return null;
        }

        // FIXME: @Nullable YangInstanceIdentifier errorPath() :(

        // FIXME: as per RFC8040 mapping -- anydata children, grouped by lists
        default ListMultimap<QName, Object> getErrorInfo() {
            return ImmutableListMultimap.of();
        }
    }
}
