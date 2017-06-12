/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Utility methods and constants to work with built-in YANG types.
 */
public final class BaseTypes {

    private BaseTypes() {
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

    private static final Set<QName> BUILT_IN_TYPES = ImmutableSet.<QName>builder()
            .add(BINARY_QNAME)
            .add(BITS_QNAME)
            .add(BOOLEAN_QNAME)
            .add(DECIMAL64_QNAME)
            .add(EMPTY_QNAME)
            .add(ENUMERATION_QNAME)
            .add(IDENTITYREF_QNAME)
            .add(INSTANCE_IDENTIFIER_QNAME)
            .add(INT8_QNAME)
            .add(INT16_QNAME)
            .add(INT32_QNAME)
            .add(INT64_QNAME)
            .add(LEAFREF_QNAME)
            .add(STRING_QNAME)
            .add(UINT8_QNAME)
            .add(UINT16_QNAME)
            .add(UINT32_QNAME)
            .add(UINT64_QNAME)
            .add(UNION_QNAME)
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
     * <p>
     * See package documentation for description of base types.
     *
     * @param type A type name
     * @return true if type is built-in YANG Types.
     */
    public static boolean isYangBuildInType(final String type) {
        return !Strings.isNullOrEmpty(type) && BUILT_IN_TYPES.contains(
                QName.create(YangConstants.RFC6020_YANG_MODULE, type));
    }

    /**
     * Returns true if supplied type is representation of built-in YANG type as
     * per RFC 6020.
     *
     * <p>
     * See package documentation for description of base types.
     *
     * @param type Type definition
     * @return true if type is built-in YANG Types.
     */
    public static boolean isYangBuildInType(final TypeDefinition<?> type) {
        return type != null && BUILT_IN_TYPES.contains(type.getQName());
    }
}
