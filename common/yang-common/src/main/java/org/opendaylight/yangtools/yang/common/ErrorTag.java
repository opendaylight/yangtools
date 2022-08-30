/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Extensible enumeration of {@code error-tag} values, as defined in
 * <a href="https://datatracker.ietf.org/doc/html/rfc6241#appendix-A">RFC6241</a>. These values are an extensible
 * enumeration, since YANG does not place restriction on possible values in
 * <a href="https://datatracker.ietf.org/doc/html/rfc8040#section-3.9">RFC8040</a>.
 *
 * <p>
 * Error tag defines overall error semantics. Additional tag-specific information may be associated with a particular
 * error tag.
 */
@NonNullByDefault
public final class ErrorTag implements Serializable {
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
     * {@code unknown-element} {@link ErrorTag}. Covers mechanics specified in
     * <a href="https://datatracker.ietf.org/doc/html/rfc6241#page-80">RFC6241, Appendix A, last item</a>. Note the
     * specification expressly forbids reporting this tag for NETCONF clients older than {@code :base:1.1}.
     */
    public static final ErrorTag MALFORMED_MESSAGE = new ErrorTag("malformed-message");
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

    private static final long serialVersionUID = 1L;

    private final String elementBody;

    public ErrorTag(final String elementBody) {
        this.elementBody = requireNonNull(elementBody);
    }

    /**
     * Return the XML element body of this object.
     *
     * @return element body of this object
     */
    public String elementBody() {
        return elementBody;
    }

    @Override
    public int hashCode() {
        return elementBody.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof ErrorTag other && elementBody.equals(other.elementBody);
    }

    @Override
    public String toString() {
        return elementBody;
    }

    @Serial
    private void readObject(final @Nullable ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Use Serialization Proxy instead.");
    }

    @Serial
    private Object writeReplace() {
        return new ETv1(this);
    }
}
