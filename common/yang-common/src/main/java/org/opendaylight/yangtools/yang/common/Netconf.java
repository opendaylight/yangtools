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
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ForwardingObject;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Various constants and constructs that leak from
 * <a href="https://datatracker.ietf.org/doc/html/rfc4741">YANG-less NETCONF</a> into
 * <a href="https://datatracker.ietf.org/doc/html/rfc6020">YANG</a>. These were then reflected back into
 * <a href="https://datatracker.ietf.org/doc/html/rfc6241">YANG-aware NETCONF</a>.
 */
@NonNullByDefault
public final class Netconf {
    /**
     * NETCONF protocol elements' namespace, as defined in
     * <a href="https://datatracker.ietf.org/doc/html/rfc4741#section-3.1">RFC4741 section 3.1</a>.
     */
    public static final XMLNamespace NAMESPACE = XMLNamespace.of("urn:ietf:params:xml:ns:netconf:base:1.0").intern();
    /**
     * NETCONF namespace bound to YANG through
     * <a href="https://datatracker.ietf.org/doc/html/rfc6241#section-10.3">ietf-netconf@2011-06-01.yang</a>.
     */
    public static final QNameModule RFC6241_MODULE = QNameModule.create(NAMESPACE, Revision.of("2011-06-01")).intern();

    private Netconf() {
        // Hidden on purpose
    }

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
         * {@link YangError} and corresponds to {@link ErrorType#APPLICATION}
         */
        CONTENT,
        /**
         * Operations layer, for example {@code <get-config>}, {@code <edit-config>} configuration data. This
         * corresponds to {@link ErrorType#PROTOCOL}.
         */
        OPERATIONS,
        /**
         * RPC layer, for example {@code <rpc>}, {@code <rpc-reply>}. This corresponds to {@link ErrorType#RPC}.
         */
        RPC,
        /**
         * Transport protocol layer, for example BEEP, SSH, TLS, console. This corresponds to
         * {@link ErrorType#TRANSPORT}.
         */
        TRANSPORT;
    }

    /**
     * Enumeration of {@code error-severity} values.
     */
    public enum ErrorSeverity implements StringElement {
        /**
         * An error preventing an operation from completing successfully.
         */
        ERROR("error", RpcError.ErrorSeverity.ERROR),
        /**
         * A warning not affecting an operation's ability to complete successfully.
         */
        WARNING("warning", RpcError.ErrorSeverity.ERROR);

        private static final Map<String, ErrorSeverity> BY_ELEMENT_BODY =
            Maps.uniqueIndex(Arrays.asList(values()), ErrorSeverity::elementBody);

        private final RpcError.ErrorSeverity legacy;
        private final String elementBody;

        ErrorSeverity(final String elementName, final RpcError.ErrorSeverity legacy) {
            this.elementBody = requireNonNull(elementName);
            this.legacy = requireNonNull(legacy);
        }

        @Override
        public String elementBody() {
            return elementBody;
        }

        @Deprecated
        public RpcError.ErrorSeverity toLegacy() {
            return legacy;
        }

        public static @Nullable ErrorSeverity forElementBody(final String elementBody) {
            return BY_ELEMENT_BODY.get(requireNonNull(elementBody));
        }
    }

    /**
     * Enumeration of {@code error-type} values. These provide glue between {@link Layer} and various sources of
     * such errors. This enumeration is not extensible in YANG as it is modeled in
     * <a href="https://datatracker.ietf.org/doc/html/rfc8040#section-3.9">RFC8040</a>.
     */
    public enum ErrorType implements StringElement {
        /**
         * A {@link Layer#TRANSPORT} layer error. This typically happens on transport endpoints, where a protocol
         * plugin needs to report a NETCONF-equivalent condition.
         */
        TRANSPORT("transport", Layer.TRANSPORT, RpcError.ErrorType.TRANSPORT),
        /**
         * A {@link Layer#RPC} layer error. This typically happens on request routers, where a request may end up
         * being resolved due to implementation-internal causes, such as timeouts and state loss.
         */
        RPC("rpc", Layer.RPC, RpcError.ErrorType.RPC),
        /**
         * A {@link Layer#OPERATIONS} layer error. These typically happen in a NETCONF protocol implementation.
         */
        PROTOCOL("protocol", Layer.OPERATIONS, RpcError.ErrorType.PROTOCOL),
        /**
         * A {@link Layer#CONTENT} layer error. These typically happen due to YANG data handling, such as
         * type checking and structural consistency.
         */
        APPLICATION("application", Layer.CONTENT, RpcError.ErrorType.APPLICATION);

        private static final Map<String, ErrorType> BY_ELEMENT_BODY =
            Maps.uniqueIndex(Arrays.asList(values()), ErrorType::elementBody);

        private final RpcError.ErrorType legacy;
        private final String elementBody;
        private final Layer layer;

        ErrorType(final String elementName, final Layer layer, final RpcError.ErrorType legacy) {
            this.elementBody = requireNonNull(elementName);
            this.layer = requireNonNull(layer);
            this.legacy = requireNonNull(legacy);
        }

        @Override
        public String elementBody() {
            return elementBody;
        }

        /**
         * Return the {@link Layer} corresponding to this error type.
         *
         * @return A NETCONF layer
         */
        public final Layer layer() {
            return layer;
        }

        @Deprecated
        public final RpcError.ErrorType toLegacy() {
            return legacy;
        }

        public static @Nullable ErrorType forElementBody(final String elementBody) {
            return BY_ELEMENT_BODY.get(requireNonNull(elementBody));
        }
    }

    /**
     * Extensible enumeration of {@code error-tag} values, as defined in
     * <a href="https://datatracker.ietf.org/doc/html/rfc6241#appendix-A">RFC6241</a>. These values are an extensible
     * enumeration, since YANG does not place restriction on possible values in
     * <a href="https://datatracker.ietf.org/doc/html/rfc8040#section-3.9">RFC8040</a>.
     */
    public static final class ErrorTag implements StringElement {
        /**
         * {@code access-denied} {@link ErrorTag}.
         */
        public static final ErrorTag ACCESS_DENIED = new ErrorTag("access-denied");
        /**
         * {@code bad-attribute} {@link ErrorTag}. Covers mechanics specified in RFC6020 section 8.3.1, bullet 6
         * <pre>
         *   For insert handling, if the value for the attributes "before" and
         *   "after" are not valid for the type of the appropriate key leafs,
         *   the server MUST reply with a "bad-attribute" error-tag in the rpc-
         *   error.
         * </pre>
         */
        public static final ErrorTag BAD_ATTRIBUTE = new ErrorTag("bad-attribute");
        /**
         * {@code bad-element} {@link ErrorTag}. Covers mechanics specified in RFC6020 section 8.3.1, bullet 3
         * <pre>
         *   If data for more than one case branch of a choice is present, the
         *   server MUST reply with a "bad-element" in the rpc-error.
         * </pre>
         */
        public static final ErrorTag BAD_ELEMENT = new ErrorTag("bad-element");
        /**
         * {@code data-exists} {@link ErrorTag}.
         */
        public static final ErrorTag DATA_EXISTS = new ErrorTag("data-exists");
        /**
         * {@code data-missing} {@link ErrorTag}. Covers mechanics specified in RFC6020 sections 13.5 through 13.7.
         */
        public static final ErrorTag DATA_MISSING = new ErrorTag("data-missing");
        /**
         * {@code in-use} {@link ErrorTag}.
         */
        public static final ErrorTag IN_USE = new ErrorTag("in-use");
        /**
         * {@code invalid-value} {@link ErrorTag}. Covers mechanics specified in RFC6020 section 8.3.1, bullet 1
         * <pre>
         *   If a leaf data value does not match the type constraints for the
         *   leaf, including those defined in the type's "range", "length", and
         *   "pattern" properties, the server MUST reply with an
         *   "invalid-value" error-tag in the rpc-error, and with the error-
         *   app-tag and error-message associated with the constraint, if any
         *   exist.
         * </pre>
         */
        public static final ErrorTag INVALID_VALUE = new ErrorTag("invalid-value");
        /**
         * {@code lock-denied} {@link ErrorTag}.
         */
        public static final ErrorTag LOCK_DENIED = new ErrorTag("lock-denied");
        /**
         * {@code missing-attribute} {@link ErrorTag}.
         */
        public static final ErrorTag MISSING_ATTRIBUTE = new ErrorTag("missing-attribute");
        /**
         * {@code missing-element} {@link ErrorTag}. Covers mechanics specified in RFC6020 section 8.3.1, bullet 2
         * <pre>
         *   If all keys of a list entry are not present, the server MUST reply
         *   with a "missing-element" error-tag in the rpc-error.
         * </pre>
         * as well as RFC6020 section 7.13.2, paragraph 2
         * <pre>
         *   If a leaf in the input tree has a "mandatory" statement with the
         *   value "true", the leaf MUST be present in a NETCONF RPC invocation.
         *   Otherwise, the server MUST return a "missing-element" error.
         * </pre>
         */
        public static final ErrorTag MISSING_ELEMENT = new ErrorTag("missing-element");
        /**
         * {@code operation-failed} {@link ErrorTag}. Covers mechanics specified in RFC6020 section 13.1 through 13.4.
         */
        public static final ErrorTag OPERATION_FAILED = new ErrorTag("operation-failed");
        /**
         * {@code operation-not-supported} {@link ErrorTag}.
         */
        public static final ErrorTag OPERATION_NOT_SUPPORTED = new ErrorTag("operation-not-supported");
        /**
         * {@code partial-operation} {@link ErrorTag}.
         * @deprecated This error-info is obsolete since RFC6241.
         */
        @Deprecated(since = "RFC6241")
        public static final ErrorTag PARTIAL_OPERATION = new ErrorTag("partial-operation");
        /**
         * {@code resource-denied} {@link ErrorTag}.
         */
        public static final ErrorTag RESOURCE_DENIED = new ErrorTag("resource-denied");
        /**
         * {@code rollback-failed} {@link ErrorTag}.
         */
        public static final ErrorTag ROLLBACK_FAILED = new ErrorTag("rollback-failed");
        /**
         * {@code too-big} {@link ErrorTag}.
         */
        public static final ErrorTag TOO_BIG = new ErrorTag("too-big");
        /**
         * {@code unknown-element} {@link ErrorTag}. Covers mechanics specified in RFC6020 section 8.3.1, bullet 7
         * <pre>
         *   If the attributes "before" and "after" appears in any element that
         *   is not a list whose "ordered-by" property is "user", the server
         *   MUST reply with an "unknown-attribute" error-tag in the rpc-error.
         * </pre>
         */
        public static final ErrorTag UNKNOWN_ATTRIBUTE = new ErrorTag("unknown-attribute");
        /**
         * {@code unknown-element} {@link ErrorTag}. Covers mechanics specified in RFC6020 section 8.3.1, bullet 4
         * <pre>
         *   If data for a node tagged with "if-feature" is present, and the
         *   feature is not supported by the device, the server MUST reply with
         *   an "unknown-element" error-tag in the rpc-error.
         * </pre>
         * as well as RFC6020 section 8.3.1, bullet 5
         * <pre>
         *   If data for a node tagged with "when" is present, and the "when"
         *   condition evaluates to "false", the server MUST reply with an
         *   "unknown-element" error-tag in the rpc-error.
         * </pre>
         */
        public static final ErrorTag UNKNOWN_ELEMENT = new ErrorTag("unknown-element");
        /**
         * {@code unknown-namespace} {@link ErrorTag}.
         */
        public static final ErrorTag UNKNOWN_NAMESPACE = new ErrorTag("unknown-namespace");

        private final String elementBody;

        public ErrorTag(final String elementBody) {
            this.elementBody = requireNonNull(elementBody);
        }

        @Override
        public String elementBody() {
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
            return elementBody;
        }
    }

    /**
     * An element of {@code error-info} container, as modeled in {@code errorInfoType} of
     * <a href="https://datatracker.ietf.org/doc/html/rfc6241#appendix-B">RFC6241, Appendix B</a>.
     *
     * @param <T> Value type
     */
    public abstract static class ErrorInfo<T> {
        /**
         * {@code bad-attribute}, {@link #value()} is the name of the attribute.
         */
        public static final class BadAttribute extends ErrorInfo<QName> {
            public static final QName QNAME = QName.create(RFC6241_MODULE, "bad-attribute").intern();

            public BadAttribute(final QName value) {
                super(QNAME, value);
            }
        }

        /**
         * {@code bad-element}, {@link #value()} is the name of the element.
         */
        public static final class BadElement extends ErrorInfo<QName> {
            public static final QName QNAME = QName.create(RFC6241_MODULE, "bad-element").intern();

            public BadElement(final QName value) {
                super(QNAME, value);
            }
        }

        /**
         * {@code bad-namespace}, {@link #value()} is the name of the namespace.
         */
        public static final class BadNamespace extends ErrorInfo<String> {
            public static final QName QNAME = QName.create(RFC6241_MODULE, "bad-namespace").intern();

            public BadNamespace(final String value) {
                super(QNAME, value);
            }
        }

        /**
         * {@code session-id}, {@link #value()} the session identifier, as modeled in {@code SessionIdOrZero}.
         */
        public static final class SessionId extends ErrorInfo<Uint32> {
            public static final QName QNAME = QName.create(RFC6241_MODULE, "session-id").intern();
            public static final SessionId NON_NETCONF = new SessionId(Uint32.ZERO);

            public SessionId(final Uint32 value) {
                super(QNAME, value);
            }
        }

        /**
         * {@code ok-element}, {@link #value()} is the name of the element.
         *
         * @deprecated This error-info specified by {@link ErrorTag#PARTIAL_OPERATION}.
         */
        @Deprecated(since = "RFC6241")
        public static final class ErrElement extends ErrorInfo<QName> {
            public static final QName QNAME = QName.create(RFC6241_MODULE, "err-element").intern();

            public ErrElement(final QName value) {
                super(QNAME, value);
            }
        }

        /**
         * {@code noop-element}, {@link #value()} is the name of the element.
         *
         * @deprecated This error-info specified by {@link ErrorTag#PARTIAL_OPERATION}.
         */
        @Deprecated(since = "RFC6241")
        public static final class NoopElement extends ErrorInfo<QName> {
            public static final QName QNAME = QName.create(RFC6241_MODULE, "noop-element").intern();

            public NoopElement(final QName value) {
                super(QNAME, value);
            }
        }

        /**
         * {@code ok-element}, {@link #value()} is the name of the element.
         *
         * @deprecated This error-info specified by {@link ErrorTag#PARTIAL_OPERATION}.
         */
        @Deprecated(since = "RFC6241")
        public static final class OkElement extends ErrorInfo<QName> {
            public static final QName QNAME = QName.create(RFC6241_MODULE, "ok-element").intern();

            public OkElement(final QName value) {
                super(QNAME, value);
            }
        }

        private final QName name;
        private final T value;

        protected ErrorInfo(final QName name, final T value) {
            this.name = requireNonNull(name);
            this.value = requireNonNull(value);
        }

        /**
         * The name of this {@code error-info} element.
         *
         * @return Element name.
         */
        public final QName name() {
            return name;
        }

        /**
         * The value of this {@code error-info} element. This may be a simple or a complex type.
         *
         * @return Element value.
         */
        public final T value() {
            return value;
        }

        @Override
        public final String toString() {
            return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
        }

        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("name", name).add("value", value);
        }
    }

    /**
     * Baseline interface for metadata associated with a NETCONF notion of an 'error'. Except what NETCONF regards as
     * an 'error', really means 'notable event' in that it can either be a warning or an error. No warnings were defined
     * at the time this distinction was made, which tells you everything you need to known about how well non-errors are
     * defined.
     */
    public interface EventMetadata {

        ErrorSeverity severity();

        ErrorType type();

        ErrorTag tag();

        // i.e. error-message
        @Nullable String message();

        // i.e. error-app-tag
        @Nullable String appTag();
    }

    /**
     * An {@link Immutable} base representation of {@link EventMetadata}.
     */
    public abstract static class AbstractEventMetadata implements EventMetadata, Immutable {
        private final ErrorSeverity severity;
        private final ErrorType type;
        private final ErrorTag tag;
        private final @Nullable String message;
        private final @Nullable String appTag;

        protected AbstractEventMetadata(final ErrorSeverity severity, final ErrorType type, final ErrorTag tag) {
            this(severity, type, tag, null, null);
        }

        protected AbstractEventMetadata(final ErrorSeverity severity, final ErrorType type, final ErrorTag tag,
                final @Nullable String message, final @Nullable String appTag) {
            this.severity = requireNonNull(severity);
            this.type = requireNonNull(type);
            this.tag = requireNonNull(tag);
            this.message = message;
            this.appTag = appTag;
        }

        @Override
        public final ErrorSeverity severity() {
            return severity;
        }

        @Override
        public final ErrorType type() {
            return type;
        }

        @Override
        public final ErrorTag tag() {
            return tag;
        }

        @Override
        public final @Nullable String message() {
            return message;
        }

        @Override
        public final @Nullable String appTag() {
            return appTag;
        }

        @Override
        public final String toString() {
            return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
        }

        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("severity", severity).add("type", type).add("tag", tag)
                .add("appTag", appTag).add("message", message);
        }
    }

    /**
     * A {@link EventMetadata} backed by a delegate.
     */
    public abstract static class ForwardingEventMetadata extends ForwardingObject implements EventMetadata {
        @Override
        public final ErrorSeverity severity() {
            return delegate().severity();
        }

        @Override
        public final ErrorType type() {
            return delegate().type();
        }

        @Override
        public final ErrorTag tag() {
            return delegate().tag();
        }

        @Override
        public final @Nullable String message() {
            return delegate().message();
        }

        @Override
        public final @Nullable String appTag() {
            return delegate().appTag();
        }

        @Override
        protected abstract @NonNull EventMetadata delegate();
    }

    /**
     * Common interface for constructs which can be mapped to a {@code xs:string}.
     */
    interface StringElement {
        /**
         * Return the XML element body of this object.
         *
         * @return element body of this object
         */
        String elementBody();
    }
}
