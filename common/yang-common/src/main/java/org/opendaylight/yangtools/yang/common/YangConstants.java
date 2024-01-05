/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

/**
 * Constant definitions present in RFC documents related to the YANG language.
 */
@NonNullByDefault
public final class YangConstants {
    /**
     * YANG File Extension, as defined in <a href="https://www.rfc-editor.org/rfc/rfc6020#section-14.1">RFC6020</a>.
     */
    public static final String RFC6020_YANG_FILE_EXTENSION = ".yang";
    /**
     * YANG Media Type, as defined in <a href="https://www.rfc-editor.org/rfc/rfc6020#section-14.1">RFC6020</a>.
     */
    public static final String RFC6020_YANG_MAC_FILE_TYPE = "TEXT";
    /**
     * YANG Media Type, as defined in h<a href="https://www.rfc-editor.org/rfc/rfc6020#section-14.1">RFC6020</a>.
     */
    public static final String RFC6020_YANG_MEDIA_TYPE = "application/yang";
    /**
     * YANG namespace, as defined in https://www.rfc-editor.org/rfc/rfc6020#section-14, in String format.
     */
    public static final String RFC6020_YANG_NAMESPACE_STRING = "urn:ietf:params:xml:ns:yang:1";
    /**
     * YANG namespace, as defined in https://www.rfc-editor.org/rfc/rfc6020#section-14, in URI format.
     */
    public static final XMLNamespace RFC6020_YANG_NAMESPACE = XMLNamespace.of(RFC6020_YANG_NAMESPACE_STRING).intern();
    /**
     * Base QNameModule for all YANG statements.
     */
    public static final QNameModule RFC6020_YANG_MODULE = QNameModule.of(RFC6020_YANG_NAMESPACE).intern();
    /**
     * YIN File Extension, as defined in <a href="https://www.rfc-editor.org/rfc/rfc6020#section-14.2">RFC6020</a>.
     */
    public static final String RFC6020_YIN_FILE_EXTENSION = ".yin";
    /**
     * YANG Media Type, as defined in <a href="https://www.rfc-editor.org/rfc/rfc6020#section-14.1">RFC6020</a>.
     */
    public static final String RFC6020_MAC_FILE_TYPE = "TEXT";
    /**
     * YANG Media Type, as defined in <a href="https://www.rfc-editor.org/rfc/rfc6020#section-14.2">RFC6020</a>.
     */
    public static final String RFC6020_YIN_MEDIA_TYPE = "application/yin+xml";
    /**
     * YIN namespace, as defined in https://www.rfc-editor.org/rfc/rfc6020#section-14, in String format.
     */
    public static final String RFC6020_YIN_NAMESPACE_STRING = "urn:ietf:params:xml:ns:yang:yin:1";
    /**
     * YIN namespace, as defined in https://www.rfc-editor.org/rfc/rfc6020#section-14, in URI format.
     */
    public static final XMLNamespace RFC6020_YIN_NAMESPACE = XMLNamespace.of(RFC6020_YIN_NAMESPACE_STRING).intern();
    /**
     * Base QNameModule for all YIN statements.
     */
    public static final QNameModule RFC6020_YIN_MODULE = QNameModule.of(RFC6020_YIN_NAMESPACE).intern();
    /**
     * Prefix for YANG-specific XPath functions.
     */
    public static final String YANG_XPATH_FUNCTIONS_PREFIX = "yang";
    /**
     * NETCONF protocol elements' namespace, as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc4741#section-3.1">RFC4741 section 3.1</a>, in String format.
     */
    public static final String NETCONF_NAMESPACE_STRING = "urn:ietf:params:xml:ns:netconf:base:1.0";
    /**
     * NETCONF protocol elements' namespace, as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc4741#section-3.1">RFC4741 section 3.1</a>, in String format.
     */
    public static final XMLNamespace NETCONF_NAMESPACE = XMLNamespace.of(NETCONF_NAMESPACE_STRING).intern();
    /**
     * NETCONF namespace bound to YANG through
     * <a href="https://www.rfc-editor.org/rfc/rfc6241#section-10.3">ietf-netconf@2011-06-01.yang</a>.
     */
    public static final QNameModule RFC6241_YANG_MODULE =
        QNameModule.of(NETCONF_NAMESPACE, Revision.of("2011-06-01")).intern();
    /**
     * {@code bad-attribute}, value is the name of the attribute.
     */
    public static final QName BAD_ATTRIBUTE_QNAME = QName.create(RFC6241_YANG_MODULE, "bad-attribute").intern();
    /**
     * {@code bad-element}, value is the name of the element.
     */
    public static final QName BAD_ELEMENT_QNAME = QName.create(RFC6241_YANG_MODULE, "bad-element").intern();
    /**
     * {@code bad-namespace}, value is the name of the namespace.
     */
    public static final QName BAD_NAMESPACE_QNAME = QName.create(RFC6241_YANG_MODULE, "bad-namespace").intern();
    /**
     * {@code session-id}, value the session identifier, as modeled in {@code SessionIdOrZero}.
     */
    public static final QName SESSION_ID_QNAME = QName.create(RFC6241_YANG_MODULE, "session-id").intern();
    @Deprecated(since = "RFC6241")
    public static final QName ERR_ELEMENT_QNAME = QName.create(RFC6241_YANG_MODULE, "err-element").intern();
    @Deprecated(since = "RFC6241")
    public static final QName NOOP_ELEMENT_QNAME = QName.create(RFC6241_YANG_MODULE, "noop-element").intern();
    @Deprecated(since = "RFC6241")
    public static final QName OK_ELEMENT_QNAME = QName.create(RFC6241_YANG_MODULE, "ok-element").intern();
    /**
     * {@code missing-choice} as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-13.7">RFC6020, section 13.7</a>.
     */
    public static final QName MISSING_CHOICE_QNAME = QName.create(RFC6020_YANG_MODULE, "missing-choice").intern();
    /**
     * {@code non-unique} as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-13.1">RFC6020, section 13.1</a>.
     */
    public static final QName NON_UNIQUE_QNAME = QName.create(RFC6020_YANG_MODULE, "non-unique").intern();
    /**
     * The module name assigned to {@code ietf-yang-library}. This constant is required for JSON-like parsers, using
     * module names to reference modules.
     */
    public static final String YANG_LIBRARY_MODULE_NAME = "ietf-yang-library";
    /**
     * The namespace assigned to {@code ietf-yang-library}. This constant is required for XML-like parsers, using
     * XML namespaces to reference modules.
     */
    public static final String YANG_LIBRARY_NAMESPACE_STRING = "urn:ietf:params:xml:ns:yang:ietf-yang-library";
    /**
     * The namespace assigned to {@code ietf-yang-library}. This constant is useful for referencing things in a
     * type-safe manner.
     */
    public static final XMLNamespace YANG_LIBRARY_NAMESPACE = XMLNamespace.of(YANG_LIBRARY_NAMESPACE_STRING).intern();
    /**
     * {@code ietf-yang-library} namespace bound to YANG through
     * <a href="https://www.rfc-editor.org/rfc/rfc7895#section-2.2">ietf-yang-library@2016-06-21.yang</a>.
     */
    public static final QNameModule RFC7895_YANG_MODULE =
        QNameModule.of(YANG_LIBRARY_NAMESPACE, Revision.of("2016-06-21")).intern();
    /**
     * {@code ietf-yang-library} namespace bound to YANG through
     * <a href="https://www.rfc-editor.org/rfc/rfc8525#section-4">ietf-yang-library@2019-01-04.yang</a>.
     */
    public static final QNameModule RFC8525_YANG_MODULE =
        QNameModule.of(YANG_LIBRARY_NAMESPACE, Revision.of("2019-01-04")).intern();

    // Dummy template UnqualifiedQName. These are never leaked, but are used for efficient instantiation via
    // UnqualifiedQName#bindTo()
    private static final Unqualified DUMMY_OPERATION_INPUT = Unqualified.of("input");
    private static final Unqualified DUMMY_OPERATION_OUTPUT = Unqualified.of("output");

    private YangConstants() {
        // Hidden on purpose
    }

    /**
     * Create a {@link QName} representing the 'input' statement of an operation (RPC or action) within specified
     * {@link QNameModule}.
     *
     * @param module Desired module
     * @return A QName representing action or RPC input.
     * @throws NullPointerException if {@code module} is null
     */
    public static QName operationInputQName(final QNameModule module) {
        return DUMMY_OPERATION_INPUT.bindTo(module);
    }

    /**
     * Create a {@link QName} representing the 'output' statement of an operation (RPC or action) within specified
     * {@link QNameModule}.
     *
     * @param module Desired module
     * @return A QName representing action or RPC output.
     * @throws NullPointerException if {@code module} is null
     */
    public static QName operationOutputQName(final QNameModule module) {
        return DUMMY_OPERATION_OUTPUT.bindTo(module);
    }
}
