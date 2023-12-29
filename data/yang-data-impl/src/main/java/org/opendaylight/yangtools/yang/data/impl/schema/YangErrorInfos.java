/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static org.opendaylight.yangtools.yang.common.YangConstants.BAD_ATTRIBUTE_QNAME;
import static org.opendaylight.yangtools.yang.common.YangConstants.BAD_ELEMENT_QNAME;
import static org.opendaylight.yangtools.yang.common.YangConstants.BAD_NAMESPACE_QNAME;
import static org.opendaylight.yangtools.yang.common.YangConstants.ERR_ELEMENT_QNAME;
import static org.opendaylight.yangtools.yang.common.YangConstants.MISSING_CHOICE_QNAME;
import static org.opendaylight.yangtools.yang.common.YangConstants.NON_UNIQUE_QNAME;
import static org.opendaylight.yangtools.yang.common.YangConstants.NOOP_ELEMENT_QNAME;
import static org.opendaylight.yangtools.yang.common.YangConstants.OK_ELEMENT_QNAME;
import static org.opendaylight.yangtools.yang.common.YangConstants.SESSION_ID_QNAME;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.YangErrorInfo;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

@Beta
@NonNullByDefault
public final class YangErrorInfos {
    private static final NodeIdentifier BAD_ATTRIBUTE_NODEID = NodeIdentifier.create(BAD_ATTRIBUTE_QNAME);
    private static final NodeIdentifier BAD_ELEMENT_NODEID = NodeIdentifier.create(BAD_ELEMENT_QNAME);
    private static final NodeIdentifier BAD_NAMESPACE_NODEID = NodeIdentifier.create(BAD_NAMESPACE_QNAME);
    private static final NodeIdentifier MISSING_CHOICE_NODEID = NodeIdentifier.create(MISSING_CHOICE_QNAME);
    private static final NodeIdentifier NON_UNIQUE_NODEID = NodeIdentifier.create(NON_UNIQUE_QNAME);
    private static final NodeIdentifier SESSION_ID_NODEID = NodeIdentifier.create(SESSION_ID_QNAME);
    @Deprecated(since = "RFC6241")
    private static final NodeIdentifier ERR_ELEMENT_NODEID = NodeIdentifier.create(ERR_ELEMENT_QNAME);
    @Deprecated(since = "RFC6241")
    private static final NodeIdentifier NOOP_ELEMENT_NODEID = NodeIdentifier.create(NOOP_ELEMENT_QNAME);
    @Deprecated(since = "RFC6241")
    private static final NodeIdentifier OK_ELEMENT_NODEID = NodeIdentifier.create(OK_ELEMENT_QNAME);

    private YangErrorInfos() {
        // Hidden on purpose
    }

    public static YangErrorInfo of(final QName name, final Object value) {
        return of(new NodeIdentifier(name), value);
    }

    public static YangErrorInfo of(final NodeIdentifier name, final Object value) {
        return YangErrorInfo.of(ImmutableNodes.leafNode(name, value));
    }

    /**
     * {@code bad-attribute} with the name of the attribute.
     */
    public static YangErrorInfo badAttribute(final QName attributeName) {
        return of(BAD_ATTRIBUTE_NODEID, attributeName);
    }

    /**
     * {@code bad-element} with the name of the element.
     */
    public static YangErrorInfo badElement(final QName elementName) {
        return of(BAD_ELEMENT_NODEID, elementName);
    }

    /**
     * {@code bad-namespace} with the name of the namespace.
     */
    public static YangErrorInfo badNamespace(final QName namespaceName) {
        return of(BAD_NAMESPACE_NODEID, namespaceName);
    }

    /**
     * {@code session-id} with the session identifier, as modeled in {@code SessionIdOrZero}.
     */
    public static YangErrorInfo sessionId(final Uint32 sessionId) {
        return of(SESSION_ID_NODEID, sessionId);
    }

    /**
     * {@code ok-element} with the name of the element.
     *
     * @deprecated This error-info specified by {@link ErrorTag#PARTIAL_OPERATION}.
     */
    @Deprecated(since = "RFC6241")
    public static YangErrorInfo errElement(final QName elementName) {
        return of(ERR_ELEMENT_NODEID, elementName);
    }

    /**
     * {@code noop-element}, with the name of the element.
     *
     * @deprecated This error-info specified by {@link ErrorTag#PARTIAL_OPERATION}.
     */
    @Deprecated(since = "RFC6241")
    public static YangErrorInfo noopElement(final QName elementName) {
        return of(NOOP_ELEMENT_NODEID, elementName);
    }

    /**
     * {@code ok-element}, with the name of the element.
     *
     * @deprecated This error-info specified by {@link ErrorTag#PARTIAL_OPERATION}.
     */
    @Deprecated(since = "RFC6241")
    public static YangErrorInfo okElement(final QName elementName) {
        return of(OK_ELEMENT_NODEID, elementName);
    }

    /**
     * {@code non-unique} as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-13.1">RFC6020, section 13.1</a>. Note this
     * is only a prototype, which needs to be bound to a path representation type.
     */
    public static YangErrorInfo nonUnique(final YangInstanceIdentifier leafPath) {
        return of(NON_UNIQUE_NODEID, leafPath);
    }

    /**
     * {@code missing-choice} as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-13.7">RFC6020, section 13.7</a>.
     */
    public static YangErrorInfo missingChoice(final NodeIdentifier choiceName) {
        return of(MISSING_CHOICE_NODEID, choiceName);
    }
}
