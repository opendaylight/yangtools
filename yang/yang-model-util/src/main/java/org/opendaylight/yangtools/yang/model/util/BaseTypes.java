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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

/**
 * Utility methods and constants to work with built-in YANG types.
 */
public final class BaseTypes {

    private BaseTypes() {
    }

    private static final ImmutableSet<QName> BUILT_IN_TYPES = ImmutableSet.<QName>builder()
            .add(BinaryTypeDefinition.QNAME)
            .add(BitsTypeDefinition.QNAME)
            .add(BooleanTypeDefinition.QNAME)
            .add(DecimalTypeDefinition.QNAME)
            .add(EmptyTypeDefinition.QNAME)
            .add(EnumTypeDefinition.QNAME)
            .add(IdentityrefTypeDefinition.QNAME)
            .add(InstanceIdentifierTypeDefinition.QNAME)
            .add(Int8TypeDefinition.QNAME)
            .add(Int16TypeDefinition.QNAME)
            .add(Int32TypeDefinition.QNAME)
            .add(Int64TypeDefinition.QNAME)
            .add(LeafrefTypeDefinition.QNAME)
            .add(StringTypeDefinition.QNAME)
            .add(Uint8TypeDefinition.QNAME)
            .add(Uint16TypeDefinition.QNAME)
            .add(Uint32TypeDefinition.QNAME)
            .add(Uint64TypeDefinition.QNAME)
            .add(UnionTypeDefinition.QNAME)
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
