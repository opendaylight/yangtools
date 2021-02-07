/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.net.URI;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constant definitions present in RFC documents related to the YANG language.
 */
@NonNullByDefault
public final class YangConstants {
    /**
     * YANG File Extension, as defined in <a href="https://tools.ietf.org/html/rfc6020#section-14.1">RFC6020</a>.
     */
    public static final String RFC6020_YANG_FILE_EXTENSION = ".yang";

    /**
     * YANG Media Type, as defined in <a href="https://tools.ietf.org/html/rfc6020#section-14.1">RFC6020</a>.
     */
    public static final String RFC6020_YANG_MAC_FILE_TYPE = "TEXT";


    /**
     * YANG Media Type, as defined in h<a href="https://tools.ietf.org/html/rfc6020#section-14.1">RFC6020</a>.
     */
    public static final String RFC6020_YANG_MEDIA_TYPE = "application/yang";

    /**
     * YANG namespace, as defined in https://tools.ietf.org/html/rfc6020#section-14, in String format.
     */
    public static final String RFC6020_YANG_NAMESPACE_STRING = "urn:ietf:params:xml:ns:yang:1";

    /**
     * YANG namespace, as defined in https://tools.ietf.org/html/rfc6020#section-14, in URI format.
     */
    public static final XMLNamespace RFC6020_YANG_NAMESPACE = XMLNamespace.of(RFC6020_YANG_NAMESPACE_STRING).intern();

    /**
     * Base QNameModule for all YANG statements.
     */
    public static final QNameModule RFC6020_YANG_MODULE = QNameModule.create(RFC6020_YANG_NAMESPACE).intern();

    /**
     * YIN File Extension, as defined in <a href="https://tools.ietf.org/html/rfc6020#section-14.2">RFC6020</a>.
     */
    public static final String RFC6020_YIN_FILE_EXTENSION = ".yin";

    /**
     * YANG Media Type, as defined in <a href="https://tools.ietf.org/html/rfc6020#section-14.1">RFC6020</a>.
     */
    public static final String RFC6020_MAC_FILE_TYPE = "TEXT";

    /**
     * YANG Media Type, as defined in <a href="https://tools.ietf.org/html/rfc6020#section-14.2">RFC6020</a>.
     */
    public static final String RFC6020_YIN_MEDIA_TYPE = "application/yin+xml";

    /**
     * YIN namespace, as defined in https://tools.ietf.org/html/rfc6020#section-14, in String format.
     */
    public static final String RFC6020_YIN_NAMESPACE_STRING = "urn:ietf:params:xml:ns:yang:yin:1";

    /**
     * YIN namespace, as defined in https://tools.ietf.org/html/rfc6020#section-14, in URI format.
     */
    public static final XMLNamespace RFC6020_YIN_NAMESPACE = XMLNamespace.of(RFC6020_YIN_NAMESPACE_STRING).intern();

    /**
     * Base QNameModule for all YIN statements.
     */
    public static final QNameModule RFC6020_YIN_MODULE = QNameModule.create(RFC6020_YIN_NAMESPACE).intern();

    /**
     * YANG Library NETCONF Capability, as defined in https://tools.ietf.org/html/rfc7950#section-16.
     */
    public static final URI RFC7950_YANG_LIBRARY_CAPABILITY =
        URI.create("urn:ietf:params:netconf:capability:yang-library:1.0");

    /**
     * Prefix for YANG-specific XPath functions.
     */
    public static final String YANG_XPATH_FUNCTIONS_PREFIX = "yang";

    // Dummy template UnqualifiedQName. These are never leaked, but are used for efficient instantiation via
    // UnqualifiedQName#bindTo()
    private static final UnqualifiedQName DUMMY_OPERATION_INPUT = UnqualifiedQName.of("input");
    private static final UnqualifiedQName DUMMY_OPERATION_OUTPUT = UnqualifiedQName.of("output");

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
