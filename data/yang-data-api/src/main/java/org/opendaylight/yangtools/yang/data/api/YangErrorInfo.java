/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.ErrorInfo;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

@Beta
@NonNullByDefault
public final class YangErrorInfo<T> extends ErrorInfo<T> {
    private YangErrorInfo(final QName name, final T value) {
        super(name, value);
    }

    public static <T> YangErrorInfo<T> of(final QName nname, final T value) {
        return new YangErrorInfo<>(nname, value);
    }

    /**
     * {@code bad-attribute}, {@link #value()} is the name of the attribute.
     */
    public static YangErrorInfo<QName> badAttribute(final QName attributeName) {
        return new YangErrorInfo<>(BAD_ATTRIBUTE_QNAME, attributeName);
    }

    /**
     * {@code bad-element}, {@link #value()} is the name of the element.
     */
    public static YangErrorInfo<QName> badElement(final QName elementName) {
        return new YangErrorInfo<>(BAD_ELEMENT_QNAME, elementName);
    }

    /**
     * {@code bad-namespace}, {@link #value()} is the name of the namespace.
     */
    public static YangErrorInfo<QName> badNamespace(final QName namespaceName) {
        return new YangErrorInfo<>(BAD_NAMESPACE_QNAME, namespaceName);
    }

    /**
     * {@code session-id}, {@link #value()} the session identifier, as modeled in {@code SessionIdOrZero}.
     */
    public static YangErrorInfo<Uint32> sessionId(final Uint32 sessionId) {
        return new YangErrorInfo<>(SESSION_ID_QNAME, sessionId);
    }

    /**
     * {@code ok-element}, {@link #value()} is the name of the element.
     *
     * @deprecated This error-info specified by {@link ErrorTag#PARTIAL_OPERATION}.
     */
    @Deprecated(since = "RFC6241")
    public static YangErrorInfo<QName> errElement(final QName elementName) {
        return new YangErrorInfo<>(ERR_ELEMENT_QNAME, elementName);
    }

    /**
     * {@code noop-element}, {@link #value()} is the name of the element.
     *
     * @deprecated This error-info specified by {@link ErrorTag#PARTIAL_OPERATION}.
     */
    @Deprecated(since = "RFC6241")
    public static YangErrorInfo<QName> noopElement(final QName elementName) {
        return new YangErrorInfo<>(NOOP_ELEMENT_QNAME, elementName);
    }

    /**
     * {@code ok-element}, {@link #value()} is the name of the element.
     *
     * @deprecated This error-info specified by {@link ErrorTag#PARTIAL_OPERATION}.
     */
    @Deprecated(since = "RFC6241")
    public static YangErrorInfo<QName> okElement(final QName elementName) {
        return new YangErrorInfo<>(OK_ELEMENT_QNAME, elementName);
    }

    /**
     * {@code non-unique} as defined in
     * <a href="https://datatracker.ietf.org/doc/html/rfc6020#section-13.1">RFC6020, section 13.1</a>. Note this
     * is only a prototype, which needs to be bound to a path representation type.
     */
    public static YangErrorInfo<YangInstanceIdentifier> nonUnique(final YangInstanceIdentifier leafPath) {
        return new YangErrorInfo<>(NON_UNIQUE_QNAME, leafPath);
    }

    /**
     * {@code missing-choice} as defined in
     * <a href="https://datatracker.ietf.org/doc/html/rfc6020#section-13.7">RFC6020, section 13.7</a>.
     */
    public static YangErrorInfo<NodeIdentifier> missingChoice(final NodeIdentifier choiceName) {
        return new YangErrorInfo<>(MISSING_CHOICE_QNAME, choiceName);
    }
}
