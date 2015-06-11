/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

/**
 * Test class for testing BaseYangTypes class.
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
public class BaseYangTypesTest {

    private static SchemaContext schemaContext;

    private static BinaryTypeDefinition binary = null;
    private static DecimalTypeDefinition decimal64 = null;
    private static EnumTypeDefinition enumeration = null;
    private static IntegerTypeDefinition int8 = null;
    private static IntegerTypeDefinition int16 = null;
    private static IntegerTypeDefinition int32 = null;
    private static IntegerTypeDefinition int64 = null;
    private static StringTypeDefinition string = null;
    private static UnsignedIntegerTypeDefinition uint8 = null;
    private static UnsignedIntegerTypeDefinition uint16 = null;
    private static UnsignedIntegerTypeDefinition uint32 = null;
    private static UnsignedIntegerTypeDefinition uint64 = null;
    private static UnionTypeDefinition union = null;
    private static EmptyTypeDefinition empty = null;
    private static BooleanTypeDefinition bool = null;

    @SuppressWarnings("deprecation")
    @BeforeClass
    public static void setup() {
        final List<InputStream> modelsToParse = Collections
            .singletonList(BaseYangTypesTest.class.getResourceAsStream("/base-yang-types.yang"));
        final YangContextParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModelsFromStreams(modelsToParse);
        assertTrue(!modules.isEmpty());
        schemaContext = parser.resolveSchemaContext(modules);
        assertNotNull(schemaContext);
        initTypeDefinitionsFromSchemaContext();
    }

    private static void initTypeDefinitionsFromSchemaContext() {
        Set<TypeDefinition<?>> typedefs = schemaContext.getTypeDefinitions();
        assertTrue(!typedefs.isEmpty());

        for (final TypeDefinition<?> typedef : typedefs) {
            assertNotNull(typedef);
            assertTrue(typedef instanceof ExtendedType);

            final TypeDefinition<?> baseType = typedef.getBaseType();
            if (baseType instanceof BinaryTypeDefinition) {
                binary = (BinaryTypeDefinition) baseType;
            } else if (baseType instanceof DecimalTypeDefinition) {
                decimal64 = (DecimalTypeDefinition) baseType;
            } else if (baseType instanceof EnumTypeDefinition) {
                enumeration = (EnumTypeDefinition) baseType;
            } else if (baseType instanceof IntegerTypeDefinition) {
                String typeName = baseType.getQName().getLocalName();
                switch (typeName) {
                case "int8":
                    int8 = (IntegerTypeDefinition) baseType;
                    break;
                case "int16":
                    int16 = (IntegerTypeDefinition) baseType;
                    break;
                case "int32":
                    int32 = (IntegerTypeDefinition) baseType;
                    break;
                case "int64":
                    int64 = (IntegerTypeDefinition) baseType;
                    break;
                }
            } else if (baseType instanceof StringTypeDefinition) {
                string = (StringTypeDefinition) baseType;
            } else if (baseType instanceof UnsignedIntegerTypeDefinition) {
                String typeName = baseType.getQName().getLocalName();
                switch (typeName) {
                case "uint8":
                    uint8 = (UnsignedIntegerTypeDefinition) baseType;
                    break;
                case "uint16":
                    uint16 = (UnsignedIntegerTypeDefinition) baseType;
                    break;
                case "uint32":
                    uint32 = (UnsignedIntegerTypeDefinition) baseType;
                    break;
                case "uint64":
                    uint64 = (UnsignedIntegerTypeDefinition) baseType;
                    break;
                }
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
        final TypeProvider typeProvider = BaseYangTypes.BASE_YANG_TYPES_PROVIDER;

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
        assertEquals(Short.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint16, uint16);
        assertNotNull(javaType);
        assertEquals(Integer.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint32, uint32);
        assertNotNull(javaType);
        assertEquals(Long.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint64, uint64);
        assertNotNull(javaType);
        assertEquals(BigInteger.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(union, union);
        assertNotNull(javaType);
        assertEquals("Union", javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(empty, empty);
        assertNotNull(javaType);
        assertEquals(Boolean.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(bool, bool);
        assertNotNull(javaType);
        assertEquals(Boolean.class.getCanonicalName(), javaType.getFullyQualifiedName());
    }

    @Test
    public void javaTypeForRestrictedSchemaDefinitionTypeTest() {
        final TypeProvider typeProvider = BaseYangTypes.BASE_YANG_TYPES_PROVIDER;

        Type javaType = typeProvider.javaTypeForSchemaDefinitionType(binary, binary, BindingGeneratorUtil.getRestrictions(binary));
        assertNotNull(javaType);
        assertEquals("byte[]", javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(decimal64, decimal64, BindingGeneratorUtil.getRestrictions(decimal64));
        assertNotNull(javaType);
        assertEquals(BigDecimal.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(enumeration, enumeration, BindingGeneratorUtil.getRestrictions(enumeration));
        assertNotNull(javaType);
        assertEquals(Enum.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(int8, int8, BindingGeneratorUtil.getRestrictions(int8));
        assertNotNull(javaType);
        assertEquals(Byte.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(int16, int16, BindingGeneratorUtil.getRestrictions(int16));
        assertNotNull(javaType);
        assertEquals(Short.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(int32, int32, BindingGeneratorUtil.getRestrictions(int32));
        assertNotNull(javaType);
        assertEquals(Integer.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(int64, int64, BindingGeneratorUtil.getRestrictions(int64));
        assertNotNull(javaType);
        assertEquals(Long.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(string, string, BindingGeneratorUtil.getRestrictions(string));
        assertNotNull(javaType);
        assertEquals(String.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint8, uint8, BindingGeneratorUtil.getRestrictions(uint8));
        assertNotNull(javaType);
        assertEquals(Short.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint16, uint16, BindingGeneratorUtil.getRestrictions(uint16));
        assertNotNull(javaType);
        assertEquals(Integer.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint32, uint32, BindingGeneratorUtil.getRestrictions(uint32));
        assertNotNull(javaType);
        assertEquals(Long.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(uint64, uint64, BindingGeneratorUtil.getRestrictions(uint64));
        assertNotNull(javaType);
        assertEquals(BigInteger.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(union, union, BindingGeneratorUtil.getRestrictions(union));
        assertNotNull(javaType);
        assertEquals("Union", javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(empty, empty, BindingGeneratorUtil.getRestrictions(empty));
        assertNotNull(javaType);
        assertEquals(Boolean.class.getCanonicalName(), javaType.getFullyQualifiedName());

        javaType = typeProvider.javaTypeForSchemaDefinitionType(bool, bool, BindingGeneratorUtil.getRestrictions(bool));
        assertNotNull(javaType);
        assertEquals(Boolean.class.getCanonicalName(), javaType.getFullyQualifiedName());
    }
}
