/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2021 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.TypeRef;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * General test suite revolving around {@link DefaultBindingGenerator}. This class holds tests originally aimed at
 * specific implementation methods, but now they really are all about integration testing.
 *
 * @author Lukas Sedlak
 */
public class DefaultBindingGeneratorTest {
    public static final String BASE_YANG_TYPES =
        "org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914";
    public static final String TEST_TYPE_PROVIDER =
        "org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.model.rev140912";
    public static final String TEST_TYPE_PROVIDER_B =
        "org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.b.model.rev140915";
    public static final JavaTypeName CONSTRUCTION_TYPE_TEST =
        JavaTypeName.create(TEST_TYPE_PROVIDER, "ConstructionTypeTest");
    public static final JavaTypeName TEST_TYPE_PROVIDER_B_DATA =
        JavaTypeName.create(TEST_TYPE_PROVIDER_B, "TestTypeProviderBData");
    public static final JavaTypeName TEST_TYPE_PROVIDER_FOO =
        JavaTypeName.create(TEST_TYPE_PROVIDER, "Foo");

    public static EffectiveModelContext SCHEMA_CONTEXT;
    public static List<Archetype> TYPES;

    @BeforeAll
    static void beforeAll() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResources(DefaultBindingGeneratorTest.class,
            "/base-yang-types.yang", "/test-type-provider-b.yang", "/test-type-provider.yang");
        TYPES = DefaultBindingGenerator.generateFor(SCHEMA_CONTEXT);
    }

    @AfterAll
    static void afterAll() {
        SCHEMA_CONTEXT = null;
        TYPES = null;
    }

    @Test
    void javaTypeForSchemaDefinitionLeafrefToEnumType() {
        final var bData = assertInstanceOf(DataRootArchetype.class, assertGeneratedType(TEST_TYPE_PROVIDER_B_DATA));
        final var bDataMethods = bData.getMethodDefinitions();
        assertEquals(9, bDataMethods.size());

        final var bEnumType = assertInstanceOf(EnumTypeObjectArchetype.class,
            assertGeneratedMethod(bDataMethods, "getEnum").getReturnType());
        assertEquals(TEST_TYPE_PROVIDER + ".Foo.ResolveDirectUseOfEnum", bEnumType.canonicalName());

        final var enumsType = assertInstanceOf(ParameterizedType.class,
            assertGeneratedMethod(bDataMethods, "getEnums").getReturnType());

        assertEquals(Types.typeForClass(Set.class), enumsType.getRawType());
        final var enumsTypeArgs = enumsType.getActualTypeArguments();
        assertEquals(1, enumsTypeArgs.size());
        assertEquals(TEST_TYPE_PROVIDER + ".Foo.ListOfEnums", enumsTypeArgs.getFirst().canonicalName());
    }

    @Test
    void generatedTypeForExtendedDefinitionTypeWithIdentityrefBaseType() {
        assertEquals(TypeRef.of(JavaTypeName.create(TEST_TYPE_PROVIDER, "Aes")),
            assertGeneratedMethod(CONSTRUCTION_TYPE_TEST, "getAesIdentityrefType").getReturnType());
    }

    @Test
    void generatedTypeForExtendedDefinitionTypeWithLeafrefBaseType() {
        final var gto = assertInstanceOf(ScalarTypeObjectArchetype.class,
            assertGeneratedMethod(CONSTRUCTION_TYPE_TEST, "getBarId").getReturnType());
        assertEquals(JavaTypeName.create(BASE_YANG_TYPES, "YangInt16"), gto.name());
    }

    @Test
    void generatedTypeForExtendedDefinitionTypeWithInnerExtendedType() {
        assertScalar(JavaTypeName.create(TEST_TYPE_PROVIDER, "ExtendedYangInt8"));
    }

    @Test
    void generatedTypeForExtendedDefinitionType() {
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangBoolean"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangEmpty"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangInt8"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangInt8Restricted"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangInt16"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangInt32"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangInt64"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangString"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangDecimal64"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangUint8"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangUint16"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangUint32"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangUint64"));
        assertUnion(JavaTypeName.create(BASE_YANG_TYPES, "YangUnion"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangBinary"));
        assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangInstanceIdentifier"));
        assertBits(JavaTypeName.create(BASE_YANG_TYPES, "YangBits"));
        assertEnum(JavaTypeName.create(BASE_YANG_TYPES, "YangEnumeration"));
    }

    @Test
    void provideGeneratedTOBuilderForUnionTypeDefWithInnerUnionTypes() {
        final var gto = assertUnion(JavaTypeName.create(TEST_TYPE_PROVIDER, "ComplexUnion"));

        assertEquals(3, gto.typePropertyNames().size());
        assertEquals(3, gto.typePropertyTypes().size());
        final var enclosed = gto.enclosedTypes();
        assertEquals(1, enclosed.size());

        final var union1 = assertInstanceOf(UnionTypeObjectArchetype.class, enclosed.getFirst());
        assertEquals(TEST_TYPE_PROVIDER + ".ComplexUnion.ComplexUnion$1", union1.canonicalName());
        assertEquals(List.of("enumeration"), union1.typePropertyNames());

        final var types = union1.typePropertyTypes();
        assertEquals(1, types.size());
        final var enumType = assertInstanceOf(EnumTypeObjectArchetype.class, types.getFirst());
        assertEquals(TEST_TYPE_PROVIDER + ".ComplexUnion.ComplexUnion$1.Enumeration", enumType.canonicalName());
        assertEquals(List.of(enumType), union1.enclosedTypes());
    }

    @Test
    void provideGeneratedTOBuilderForUnionTypeDefWithInnerUnionAndSimpleType() {
        final var gto = assertUnion(JavaTypeName.create(TEST_TYPE_PROVIDER, "ComplexStringIntUnion"));
        assertEquals(List.of("innerUnion", "string"), gto.typePropertyNames());
        assertEquals(2, gto.typePropertyTypes().size());
        assertEquals(List.of(), gto.enclosedTypes());
    }

    @Test
    void javaTypeForSchemaDefinitionForExtUnionWithSimpleTypes() {
        final var type = assertInstanceOf(UnionTypeObjectArchetype.class, assertGeneratedMethod(
            JavaTypeName.create(TEST_TYPE_PROVIDER, "UseOfUnions"), "getSimpleIntTypesUnion").getReturnType());
        assertEquals(JavaTypeName.create(BASE_YANG_TYPES, "YangUnion"), type.name());
    }

    @Test
    void javaTypeForSchemaDefinitionForExtUnionWithInnerUnionAndSimpleType() {
        final var type = assertInstanceOf(UnionTypeObjectArchetype.class, assertGeneratedMethod(
            JavaTypeName.create(TEST_TYPE_PROVIDER, "UseOfUnions"), "getComplexStringIntUnion").getReturnType());
        assertEquals(JavaTypeName.create(TEST_TYPE_PROVIDER, "ComplexStringIntUnion"), type.name());
    }

    @Test
    void javaTypeForSchemaDefinitionForExtComplexUnionWithInnerUnionTypes() {
        final var type = assertInstanceOf(UnionTypeObjectArchetype.class, assertGeneratedMethod(
            JavaTypeName.create(TEST_TYPE_PROVIDER, "UseOfUnions"), "getComplexUnion").getReturnType());
        assertEquals(JavaTypeName.create(TEST_TYPE_PROVIDER, "ComplexUnion"), type.name());
    }

    @Test
    void javaTypeForSchemaDefinitionIdentityrefExtType() {
        assertEquals(TypeRef.of(JavaTypeName.create(TEST_TYPE_PROVIDER, "CryptoAlg")),
            assertGeneratedMethod(TEST_TYPE_PROVIDER_FOO, "getCrypto").getReturnType());
    }

    @Test
    void javaTypeForSchemaDefinitionEmptyStringPatternType() {
        final var restrictions = assertScalar(JavaTypeName.create(TEST_TYPE_PROVIDER, "EmptyPatternString"))
            .restrictions();
        assertNotNull(restrictions);
        final var patterns = restrictions.getPatternConstraints();
        assertEquals(1, patterns.size());
        final var pattern = patterns.get(0);
        assertEquals("", pattern.getRegularExpressionString());
        assertEquals("^(?:)$", pattern.getJavaPatternString());
    }

    @Test
    void testUnresolvedLeafref() {
        assertSame(Types.objectType(),
            assertGeneratedMethod(JavaTypeName.create(TEST_TYPE_PROVIDER_B, "Grp"), "getUnresolvableLeafref")
                .getReturnType());
    }

    @Test
    void javaTypeForSchemaDefinitionInvalidLeafrefPath() {
        final var ctx = YangParserTestUtils.parseYangResource("/unresolvable-leafref.yang");

        final var uoe = assertThrows(UnsupportedOperationException.class,
            () -> DefaultBindingGenerator.generateFor(ctx));
        assertEquals("Cannot ascertain type", uoe.getMessage());
        final var ex = assertInstanceOf(IllegalArgumentException.class, uoe.getCause());
        assertEquals("Failed to find leafref target /somewhere/i/belong", ex.getMessage());
        final var cause = assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertEquals("Data tree child (foo)somewhere not present in module (foo)unresolvable-leafref",
            cause.getMessage());
    }

    @Test
    void javaTypeForSchemaDefinitionConditionalLeafref() {
        // Note: previous incarnation did not resolve this, as the expression (pointed to a list)
        assertSame(assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangInt16")),
            assertGeneratedMethod(TEST_TYPE_PROVIDER_B_DATA, "getConditionalLeafref").getReturnType());
    }

    @Test
    void javaTypeForSchemaDefinitionLeafrefExtType() {
        assertSame(assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangInt8")),
            assertGeneratedMethod(JavaTypeName.create(TEST_TYPE_PROVIDER, "Bar"), "getLeafrefValue").getReturnType());
        assertSame(assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangInt16")),
            assertGeneratedMethod(TEST_TYPE_PROVIDER_B_DATA, "getId").getReturnType());
    }

    @Test
    void javaTypeForSchemaDefinitionEnumExtTypeResolve() {
        final var type = assertEnum(JavaTypeName.create(BASE_YANG_TYPES, "YangEnumeration"));

        final var values = type.valueToConstant();
        assertEquals(2, values.size());
        final var it = values.entrySet().iterator();
        final var first = it.next();
        assertEquals("a", first.getKey().getName());
        assertEquals("A", first.getValue());

        final var second = it.next();
        assertEquals("b", second.getKey().getName());
        assertEquals("B", second.getValue());

        assertSame(type, assertGeneratedMethod(TEST_TYPE_PROVIDER_FOO, "getResolveEnumLeaf").getReturnType());
    }

    @Test
    void javaTypeForSchemaDefinitionEnumExtTypeDirect() {
        // Note: this part of the test contained invalid assertion that the return would be java.lang.Enum
        final var type = assertInstanceOf(EnumTypeObjectArchetype.class,
            assertGeneratedMethod(TEST_TYPE_PROVIDER_FOO, "getResolveDirectUseOfEnum").getReturnType());
        assertEquals(TEST_TYPE_PROVIDER_FOO.createEnclosed("ResolveDirectUseOfEnum"), type.name());

        final var values = type.valueToConstant();
        assertEquals(3, values.size());

        final var it = values.entrySet().iterator();
        final var first = it.next();
        assertEquals("x", first.getKey().getName());
        assertEquals("X", first.getValue());

        final var second = it.next();
        assertEquals("y", second.getKey().getName());
        assertEquals("Y", second.getValue());

        final var third = it.next();
        assertEquals("z", third.getKey().getName());
        assertEquals("Z", third.getValue());
    }

    @Test
    void javaTypeForSchemaDefinitionRestrictedExtType() {
        final var expected = assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangInt8Restricted"));
        final var restrictions = expected.restrictions();
        assertNotNull(restrictions);
        final var rangeConstraints = restrictions.getRangeConstraint();
        assertTrue(rangeConstraints.isPresent());
        final var it = rangeConstraints.orElseThrow().getAllowedRanges().asRanges().iterator();
        assertTrue(it.hasNext());
        final var constraint = it.next();
        assertEquals((byte) 1, constraint.lowerEndpoint());
        assertEquals((byte) 100, constraint.upperEndpoint());
        assertFalse(it.hasNext());

        assertSame(expected, assertGeneratedMethod(TEST_TYPE_PROVIDER_FOO, "getRestrictedInt8Type").getReturnType());
    }

    @Test
    void javaTypeForSchemaDefinitionExtType() {
        final var expected = assertScalar(JavaTypeName.create(BASE_YANG_TYPES, "YangInt8"));
        assertSame(expected, assertGeneratedMethod(TEST_TYPE_PROVIDER_FOO, "getYangInt8Type").getReturnType());
    }

    private static MethodSignature assertGeneratedMethod(final JavaTypeName typeName, final String methodName) {
        return assertGeneratedMethod(
            assertInstanceOf(LegacyArchetype.class, assertGeneratedType(typeName)).getMethodDefinitions(), methodName);
    }

    private static MethodSignature assertGeneratedMethod(final List<MethodSignature> methods, final String name) {
        return methods.stream().filter(method -> name.equals(method.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Method " + name + " not present"));
    }

    private static Archetype assertGeneratedType(final JavaTypeName name) {
        return TYPES.stream()
            .filter(type -> name.equals(type.name()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Generated type " + name + " not present"));
    }

    private static BitsTypeObjectArchetype assertBits(final JavaTypeName name) {
        return assertInstanceOf(BitsTypeObjectArchetype.class, assertGeneratedType(name));
    }

    private static EnumTypeObjectArchetype assertEnum(final JavaTypeName name) {
        return assertInstanceOf(EnumTypeObjectArchetype.class, assertGeneratedType(name));
    }

    private static ScalarTypeObjectArchetype assertScalar(final JavaTypeName name) {
        return assertInstanceOf(ScalarTypeObjectArchetype.class, assertGeneratedType(name));
    }

    private static UnionTypeObjectArchetype assertUnion(final JavaTypeName name) {
        return assertInstanceOf(UnionTypeObjectArchetype.class, assertGeneratedType(name));
    }
}
