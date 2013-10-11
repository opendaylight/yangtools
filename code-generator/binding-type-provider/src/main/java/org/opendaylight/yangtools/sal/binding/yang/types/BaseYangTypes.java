/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public final class BaseYangTypes {
    /**
     * mapping of basic built-in YANG types (keys) to JAVA
     * {@link org.opendaylight.yangtools.sal.binding.model.api.Type Type}. This
     * map is filled with mapping data in static initialization block
     */
    private static Map<String, Type> typeMap = new HashMap<String, Type>();

    /**
     * <code>Type</code> representation of <code>boolean</code> YANG type
     */
    public static final Type BOOLEAN_TYPE = Types.typeForClass(Boolean.class);

    /**
     * <code>Type</code> representation of <code>empty</code> YANG type
     */
    public static final Type EMPTY_TYPE = Types.typeForClass(Boolean.class);

    /**
     * <code>Type</code> representation of <code>int8</code> YANG type
     */
    public static final Type INT8_TYPE = Types.typeForClass(Byte.class);

    /**
     * <code>Type</code> representation of <code>int16</code> YANG type
     */
    public static final Type INT16_TYPE = Types.typeForClass(Short.class);

    /**
     * <code>Type</code> representation of <code>int32</code> YANG type
     */
    public static final Type INT32_TYPE = Types.typeForClass(Integer.class);

    /**
     * <code>Type</code> representation of <code>int64</code> YANG type
     */
    public static final Type INT64_TYPE = Types.typeForClass(Long.class);

    /**
     * <code>Type</code> representation of <code>string</code> YANG type
     */
    public static final Type STRING_TYPE = Types.typeForClass(String.class);

    /**
     * <code>Type</code> representation of <code>decimal64</code> YANG type
     */
    public static final Type DECIMAL64_TYPE = Types.typeForClass(BigDecimal.class);

    /**
     * <code>Type</code> representation of <code>uint8</code> YANG type
     */
    public static final Type UINT8_TYPE = Types.typeForClass(Short.class);

    /**
     * <code>Type</code> representation of <code>uint16</code> YANG type
     */
    public static final Type UINT16_TYPE = Types.typeForClass(Integer.class);

    /**
     * <code>Type</code> representation of <code>uint32</code> YANG type
     */
    public static final Type UINT32_TYPE = Types.typeForClass(Long.class);

    /**
     * <code>Type</code> representation of <code>uint64</code> YANG type
     */
    public static final Type UINT64_TYPE = Types.typeForClass(BigInteger.class);

    /**
     * <code>Type</code> representation of <code>binary</code> YANG type
     */
    public static final Type BINARY_TYPE = Types.primitiveType("byte[]");

    public static final Type INSTANCE_IDENTIFIER = Types.parameterizedTypeFor(Types
            .typeForClass(InstanceIdentifier.class));

    /**
     * It is undesirable to create instance of this class.
     */
    private BaseYangTypes() {

    }

    static {
        typeMap.put("boolean", BOOLEAN_TYPE);
        typeMap.put("empty", EMPTY_TYPE);
        typeMap.put("int8", INT8_TYPE);
        typeMap.put("int16", INT16_TYPE);
        typeMap.put("int32", INT32_TYPE);
        typeMap.put("int64", INT64_TYPE);
        typeMap.put("string", STRING_TYPE);
        typeMap.put("decimal64", DECIMAL64_TYPE);
        typeMap.put("uint8", UINT8_TYPE);
        typeMap.put("uint16", UINT16_TYPE);
        typeMap.put("uint32", UINT32_TYPE);
        typeMap.put("uint64", UINT64_TYPE);
        typeMap.put("binary", BINARY_TYPE);
        typeMap.put("instance-identifier", INSTANCE_IDENTIFIER );
    }

    public static final TypeProvider BASE_YANG_TYPES_PROVIDER = new TypeProvider() {
        /**
         * Searches <code>Type</code> value to which is YANG <code>type</code>
         * mapped.
         *
         * @param type
         *            string with YANG type name
         * @return java <code>Type</code> representation of <code>type</code>
         */
        @Override
        public Type javaTypeForYangType(String type) {
            return typeMap.get(type);
        }

        /**
         * Searches <code>Type</code> value to which is YANG <code>type</code>
         * mapped.
         *
         * @param type
         *            type definition representation of YANG type
         * @return java <code>Type</code> representation of <code>type</code>.
         *         If <code>type</code> isn't found then <code>null</code> is
         *         returned.
         */
        @Override
        public Type javaTypeForSchemaDefinitionType(TypeDefinition<?> type, SchemaNode parentNode) {
            if (type != null) {
                return typeMap.get(type.getQName().getLocalName());
            }

            return null;
        }
    };
}
