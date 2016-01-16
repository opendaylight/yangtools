/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;

/**
 * Utility methods and constants to work with built-in YANG types
 *
 *
 */
public final class BaseTypes {

    private BaseTypes() {
        throw new UnsupportedOperationException();
    }

    public static final QName BINARY_QNAME = constructQName("binary");
    public static final QName BITS_QNAME = constructQName("bits");
    public static final QName BOOLEAN_QNAME = constructQName("boolean");
    public static final QName DECIMAL64_QNAME = constructQName("decimal64");
    public static final QName EMPTY_QNAME = constructQName("empty");
    public static final QName ENUMERATION_QNAME = constructQName("enumeration");
    public static final QName IDENTITYREF_QNAME = constructQName("identityref");
    public static final QName INSTANCE_IDENTIFIER_QNAME = constructQName("instance-identifier");
    public static final QName INT8_QNAME = constructQName("int8");
    public static final QName INT16_QNAME = constructQName("int16");
    public static final QName INT32_QNAME = constructQName("int32");
    public static final QName INT64_QNAME = constructQName("int64");
    public static final QName LEAFREF_QNAME = constructQName("leafref");
    public static final QName STRING_QNAME = constructQName("string");
    public static final QName UINT8_QNAME = constructQName("uint8");
    public static final QName UINT16_QNAME = constructQName("uint16");
    public static final QName UINT32_QNAME = constructQName("uint32");
    public static final QName UINT64_QNAME = constructQName("uint64");
    public static final QName UNION_QNAME = constructQName("union");

    private static final Set<String> BUILT_IN_TYPES = ImmutableSet.<String>builder()
            .add(BINARY_QNAME.getLocalName())
            .add(BITS_QNAME.getLocalName())
            .add(BOOLEAN_QNAME.getLocalName())
            .add(DECIMAL64_QNAME.getLocalName())
            .add(EMPTY_QNAME.getLocalName())
            .add(ENUMERATION_QNAME.getLocalName())
            .add(IDENTITYREF_QNAME.getLocalName())
            .add(INSTANCE_IDENTIFIER_QNAME.getLocalName())
            .add(INT8_QNAME.getLocalName())
            .add(INT16_QNAME.getLocalName())
            .add(INT32_QNAME.getLocalName())
            .add(INT64_QNAME.getLocalName())
            .add(LEAFREF_QNAME.getLocalName())
            .add(STRING_QNAME.getLocalName())
            .add(UINT8_QNAME.getLocalName())
            .add(UINT16_QNAME.getLocalName())
            .add(UINT32_QNAME.getLocalName())
            .add(UINT64_QNAME.getLocalName())
            .add(UNION_QNAME.getLocalName())
            .build();

    /**
     * Construct QName for Built-in base Yang type. The namespace for built-in
     * base yang types is defined as: urn:ietf:params:xml:ns:yang:1
     *
     * @param typeName
     *            yang type name
     * @return built-in base yang type QName.
     */
    public static QName constructQName(final String typeName) {
        return QName.create(YangConstants.RFC6020_YANG_MODULE, typeName).intern();
    }

    /**
     * Returns true if supplied type is representation of built-in YANG type as
     * per RFC 6020.
     *
     * See package documentation for description of base types.
     *
     * @param type
     * @return true if type is built-in YANG Types.
     */
    public static boolean isYangBuildInType(final String type) {
        return BUILT_IN_TYPES.contains(type);
    }
}
