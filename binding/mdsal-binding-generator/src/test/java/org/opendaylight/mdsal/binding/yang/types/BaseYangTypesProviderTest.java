/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collection;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.binding.generator.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BaseYangTypesProviderTest {

    private static SchemaContext schemaContext;

    private static BinaryTypeDefinition binary = null;
    private static DecimalTypeDefinition decimal64 = null;
    private static EnumTypeDefinition enumeration = null;
    private static Int8TypeDefinition int8 = null;
    private static Int16TypeDefinition int16 = null;
    private static Int32TypeDefinition int32 = null;
    private static Int64TypeDefinition int64 = null;
    private static StringTypeDefinition string = null;
    private static Uint8TypeDefinition uint8 = null;
    private static Uint16TypeDefinition uint16 = null;
    private static Uint32TypeDefinition uint32 = null;
    private static Uint64TypeDefinition uint64 = null;
    private static UnionTypeDefinition union = null;
    private static EmptyTypeDefinition empty = null;
    private static BooleanTypeDefinition bool = null;

    @BeforeClass
    public static void setup() {
        schemaContext = YangParserTestUtils.parseYangResource("/base-yang-types.yang");
        assertNotNull(schemaContext);
        initTypeDefinitionsFromSchemaContext();
    }

    private static void initTypeDefinitionsFromSchemaContext() {
        Collection<? extends TypeDefinition<?>> typedefs = schemaContext.getTypeDefinitions();
        assertTrue(!typedefs.isEmpty());

        for (final TypeDefinition<?> typedef : typedefs) {
            assertNotNull(typedef);

            final TypeDefinition<?> baseType = typedef.getBaseType();
            assertNotNull(baseType);

            if (baseType instanceof BinaryTypeDefinition) {
                binary = (BinaryTypeDefinition) baseType;
            } else if (baseType instanceof DecimalTypeDefinition) {
                decimal64 = (DecimalTypeDefinition) baseType;
            } else if (baseType instanceof EnumTypeDefinition) {
                enumeration = (EnumTypeDefinition) baseType;
            } else if (baseType instanceof Int8TypeDefinition) {
                int8 = (Int8TypeDefinition) baseType;
            } else if (baseType instanceof Int16TypeDefinition) {
                int16 = (Int16TypeDefinition) baseType;
            } else if (baseType instanceof Int32TypeDefinition) {
                int32 = (Int32TypeDefinition) baseType;
            } else if (baseType instanceof Int64TypeDefinition) {
                int64 = (Int64TypeDefinition) baseType;
            } else if (baseType instanceof StringTypeDefinition) {
                string = (StringTypeDefinition) baseType;
            } else if (baseType instanceof Uint8TypeDefinition) {
                uint8 = (Uint8TypeDefinition) baseType;
            } else if (baseType instanceof Uint16TypeDefinition) {
                uint16 = (Uint16TypeDefinition) baseType;
            } else if (baseType instanceof Uint32TypeDefinition) {
                uint32 = (Uint32TypeDefinition) baseType;
            } else if (baseType instanceof Uint64TypeDefinition) {
                uint64 = (Uint64TypeDefinition) baseType;
            } else if (baseType instanceof UnionTypeDefinition) {
                union = (UnionTypeDefinition) baseType;
            } else if (baseType instanceof EmptyTypeDefinition) {
                empty = (EmptyTypeDefinition) baseType;
            } else if (baseType instanceof BooleanTypeDefinition) {
                bool = (BooleanTypeDefinition) baseType;
            }
        }
        assertNotNull(binary);
        assertNotNull(decimal64);
        assertNotNull(enumeration);
        assertNotNull(int8);
        assertNotNull(int16);
        assertNotNull(int32);
        assertNotNull(int64);
        assertNotNull(string);
        assertNotNull(uint8);
        assertNotNull(uint16);
        assertNotNull(uint32);
        assertNotNull(uint64);
        assertNotNull(union);
        assertNotNull(empty);
        assertNotNull(bool);
    }

    @Test
    public void javaTypeForSchemaDefinitionTypeTest() {
        final TypeProvider typeProvider = BaseYangTypesProvider.INSTANCE;

        Type javaType = typeProvider.javaTypeForSchemaDefinitionType(binary, binary);
        assertNotNull(javaType);
        assertEquals("byte[]", javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(decimal64, decimal64);
        assertNotNull(javaType);
        assertEquals(BigDecimal.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(enumeration, enumeration);
        assertNotNull(javaType);
        assertEquals(Enum.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(int8, int8);
        assertNotNull(javaType);
        assertEquals(Byte.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(int16, int16);
        assertNotNull(javaType);
        assertEquals(Short.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(int32, int32);
        assertNotNull(javaType);
        assertEquals(Integer.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(int64, int64);
        assertNotNull(javaType);
        assertEquals(Long.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(string, string);
        assertNotNull(javaType);
        assertEquals(String.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint8, uint8);
        assertNotNull(javaType);
        assertEquals(Uint8.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint16, uint16);
        assertNotNull(javaType);
        assertEquals(Uint16.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint32, uint32);
        assertNotNull(javaType);
        assertEquals(Uint32.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint64, uint64);
        assertNotNull(javaType);
        assertEquals(Uint64.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(empty, empty);
        assertNotNull(javaType);
        assertEquals(Empty.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(bool, bool);
        assertNotNull(javaType);
        assertEquals(Boolean.class.getCanonicalName(), javaType.getFullyQualifiedName());
    }

    @Test
    public void javaTypeForRestrictedSchemaDefinitionTypeTest() {
        final TypeProvider typeProvider = BaseYangTypesProvider.INSTANCE;

        Type javaType = typeProvider.javaTypeForSchemaDefinitionType(binary, binary,
            BindingGeneratorUtil.getRestrictions(binary));
        assertNotNull(javaType);
        assertEquals("byte[]", javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(decimal64, decimal64,
            BindingGeneratorUtil.getRestrictions(decimal64));
        assertNotNull(javaType);
        assertEquals(BigDecimal.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(enumeration, enumeration,
            BindingGeneratorUtil.getRestrictions(enumeration));
        assertNotNull(javaType);
        assertEquals(Enum.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(int8, int8, BindingGeneratorUtil.getRestrictions(int8));
        assertNotNull(javaType);
        assertEquals(Byte.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(int16, int16,
            BindingGeneratorUtil.getRestrictions(int16));
        assertNotNull(javaType);
        assertEquals(Short.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(int32, int32,
            BindingGeneratorUtil.getRestrictions(int32));
        assertNotNull(javaType);
        assertEquals(Integer.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(int64, int64,
            BindingGeneratorUtil.getRestrictions(int64));
        assertNotNull(javaType);
        assertEquals(Long.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(string, string,
            BindingGeneratorUtil.getRestrictions(string));
        assertNotNull(javaType);
        assertEquals(String.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint8, uint8,
            BindingGeneratorUtil.getRestrictions(uint8));
        assertNotNull(javaType);
        assertEquals(Uint8.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint16, uint16,
            BindingGeneratorUtil.getRestrictions(uint16));
        assertNotNull(javaType);
        assertEquals(Uint16.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint32, uint32,
            BindingGeneratorUtil.getRestrictions(uint32));
        assertNotNull(javaType);
        assertEquals(Uint32.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint64, uint64,
            BindingGeneratorUtil.getRestrictions(uint64));
        assertNotNull(javaType);
        assertEquals(Uint64.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(empty, empty,
            BindingGeneratorUtil.getRestrictions(empty));
        assertNotNull(javaType);
        assertEquals(Empty.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(bool, bool,
            BindingGeneratorUtil.getRestrictions(bool));
        assertNotNull(javaType);
        assertEquals(Boolean.class.getCanonicalName(), javaType.getFullyQualifiedName());
    }
}
