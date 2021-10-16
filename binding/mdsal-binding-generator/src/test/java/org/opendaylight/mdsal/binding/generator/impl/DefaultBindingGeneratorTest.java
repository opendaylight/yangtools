/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2021 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.mdsal.binding.yang.types.TypeProviderTest;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * General test suite revolving around {@link DefaultBindingGenerator}. This class holds tests originally aimed at
 * specific implementation methods, but now they really are all about integration testing.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultBindingGeneratorTest {
    public static final String BASE_YANG_TYPES =
        "org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.base.yang.types.rev140914";
    public static final String TEST_TYPE_PROVIDER =
        "org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.model.rev140912";
    public static final String TEST_TYPE_PROVIDER_B =
        "org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.b.model.rev140915";
    public static final JavaTypeName CONSTRUCTION_TYPE_TEST =
        JavaTypeName.create(TEST_TYPE_PROVIDER, "ConstructionTypeTest");

    public static EffectiveModelContext SCHEMA_CONTEXT;
    public static List<GeneratedType> TYPES;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResources(TypeProviderTest.class,
            "/base-yang-types.yang", "/test-type-provider-b.yang", "/test-type-provider.yang");
        TYPES = DefaultBindingGenerator.generateFor(SCHEMA_CONTEXT);
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
        TYPES = null;
    }

    @Test
    public void javaTypeForSchemaDefinitionLeafrefToEnumTypeTest() {
        final var bData = assertGeneratedType(JavaTypeName.create(TEST_TYPE_PROVIDER_B, "TestTypeProviderBData"));
        final var bDataMethods = bData.getMethodDefinitions();
        assertEquals(8, bDataMethods.size());

        final var bEnumType = assertGeneratedMethod(bDataMethods, "getEnum").getReturnType();
        assertThat(bEnumType, instanceOf(Enumeration.class));
        assertEquals(TEST_TYPE_PROVIDER + ".Foo.ResolveDirectUseOfEnum", bEnumType.getFullyQualifiedName());

        final var bEnumsType = assertGeneratedMethod(bDataMethods, "getEnums").getReturnType();

        assertThat(bEnumsType, instanceOf(ParameterizedType.class));
        final var enumsType = (ParameterizedType) bEnumsType;

        assertEquals(Types.typeForClass(List.class), enumsType.getRawType());
        final var enumsTypeArgs = enumsType.getActualTypeArguments();
        assertEquals(1, enumsTypeArgs.length);
        assertEquals(TEST_TYPE_PROVIDER + ".Foo.ListOfEnums", enumsTypeArgs[0].getFullyQualifiedName());
    }

    @Test
    public void generatedTypeForExtendedDefinitionTypeWithIdentityrefBaseTypeTest() {
        final var type = assertGeneratedMethod(CONSTRUCTION_TYPE_TEST, "getAesIdentityrefType").getReturnType();
        assertThat(type, instanceOf(ParameterizedType.class));
        final var pType = (ParameterizedType) type;
        assertEquals(Types.CLASS, pType.getRawType());
        final var pTypeArgs = pType.getActualTypeArguments();
        assertEquals(1, pTypeArgs.length);
        assertEquals(TEST_TYPE_PROVIDER + ".Aes", pTypeArgs[0].getFullyQualifiedName());
    }

    @Test
    public void generatedTypeForExtendedDefinitionTypeWithLeafrefBaseTypeTest() {
        final var type = assertGeneratedMethod(CONSTRUCTION_TYPE_TEST, "getBarId").getReturnType();
        assertThat(type, instanceOf(GeneratedTransferObject.class));
        final var gto = (GeneratedTransferObject) type;
        assertEquals(JavaTypeName.create(BASE_YANG_TYPES, "YangInt16"), gto.getIdentifier());
    }

    @Test
    public void generatedTypeForExtendedDefinitionTypeWithInnerExtendedTypeTest() {
        assertGTO(JavaTypeName.create(TEST_TYPE_PROVIDER, "ExtendedYangInt8"));
    }

    @Test
    public void generatedTypeForExtendedDefinitionTypeTest() {
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangBoolean"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangEmpty"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangInt8"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangInt8Restricted"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangInt16"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangInt32"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangInt64"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangString"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangDecimal64"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangUint8"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangUint16"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangUint32"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangUint64"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangUnion"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangBinary"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangInstanceIdentifier"));
        assertGTO(JavaTypeName.create(BASE_YANG_TYPES, "YangBits"));
        assertEnumeration(JavaTypeName.create(BASE_YANG_TYPES, "YangEnumeration"));
    }

    @Test
    public void provideGeneratedTOBuilderForUnionTypeDefWithInnerUnionTypesTest() {
        final var gto = assertGTO(JavaTypeName.create(TEST_TYPE_PROVIDER, "ComplexUnion"));
        assertEquals(3, gto.getProperties().size());
        assertEquals(List.of(), gto.getEnumerations());
        final var enclosed = gto.getEnclosedTypes();
        assertEquals(1, enclosed.size());

        final var union1 = enclosed.get(0);
        assertThat(union1, instanceOf(GeneratedTransferObject.class));
        assertEquals(TEST_TYPE_PROVIDER + ".ComplexUnion.ComplexUnion$1", union1.getFullyQualifiedName());
        assertEquals(1, union1.getProperties().size());
        assertEquals(List.of(), union1.getEnclosedTypes());

        final var enums = union1.getEnumerations();
        assertEquals(1, enums.size());
        assertEquals(TEST_TYPE_PROVIDER + ".ComplexUnion.ComplexUnion$1.Enumeration",
            enums.get(0).getFullyQualifiedName());
    }

    @Test
    public void provideGeneratedTOBuilderForUnionTypeDefWithInnerUnionAndSimpleTypeTest() {
        final var gto = assertGTO(JavaTypeName.create(TEST_TYPE_PROVIDER, "ComplexStringIntUnion"));
        assertEquals(2, gto.getProperties().size());
        assertEquals(List.of(), gto.getEnumerations());
        assertEquals(List.of(), gto.getEnclosedTypes());
    }

    @Test
    public void javaTypeForSchemaDefinitionForExtUnionWithSimpleTypesTest() {
        final var type = assertGeneratedMethod(
            JavaTypeName.create(TEST_TYPE_PROVIDER, "UseOfUnions"), "getSimpleIntTypesUnion").getReturnType();
        assertThat(type, instanceOf(GeneratedTransferObject.class));
        assertEquals(JavaTypeName.create(BASE_YANG_TYPES, "YangUnion"), type.getIdentifier());
    }

    @Test
    public void javaTypeForSchemaDefinitionForExtUnionWithInnerUnionAndSimpleTypeTest() {
        final var type = assertGeneratedMethod(
            JavaTypeName.create(TEST_TYPE_PROVIDER, "UseOfUnions"), "getComplexStringIntUnion").getReturnType();
        assertThat(type, instanceOf(GeneratedTransferObject.class));
        assertEquals(JavaTypeName.create(TEST_TYPE_PROVIDER, "ComplexStringIntUnion"), type.getIdentifier());
    }

    @Test
    public void javaTypeForSchemaDefinitionForExtComplexUnionWithInnerUnionTypesTest() {
        final var type = assertGeneratedMethod(
            JavaTypeName.create(TEST_TYPE_PROVIDER, "UseOfUnions"), "getComplexUnion").getReturnType();
        assertThat(type, instanceOf(GeneratedTransferObject.class));
        assertEquals(JavaTypeName.create(TEST_TYPE_PROVIDER, "ComplexUnion"), type.getIdentifier());
    }

    @Test
    public void javaTypeForSchemaDefinitionIdentityrefExtTypeTest() {
        final var type = assertGeneratedMethod(JavaTypeName.create(TEST_TYPE_PROVIDER, "Foo"), "getCrypto")
            .getReturnType();
        assertThat(type, instanceOf(ParameterizedType.class));
        final var param = (ParameterizedType) type;
        assertEquals(JavaTypeName.create(Class.class), param.getIdentifier());
        final var params = param.getActualTypeArguments();
        assertEquals(1, params.length);
        assertEquals(Types.wildcardTypeFor(JavaTypeName.create(TEST_TYPE_PROVIDER, "CryptoAlg")), params[0]);
    }

    @Test
    public void javaTypeForSchemaDefinitionEmptyStringPatternTypeTest() {
        final var restrictions = assertGTO(JavaTypeName.create(TEST_TYPE_PROVIDER, "EmptyPatternString"))
            .getRestrictions();
        final var patterns = restrictions.getPatternConstraints();
        assertEquals(1, patterns.size());
        final var pattern = patterns.get(0);
        assertEquals("", pattern.getRegularExpressionString());
        assertEquals("^(?:)$", pattern.getJavaPatternString());
    }

    @Test
    public void testUnresolvedLeafref() {
        assertEquals(Types.objectType(), assertGeneratedMethod(JavaTypeName.create(TEST_TYPE_PROVIDER_B, "Grp"),
            "getUnresolvableLeafref").getReturnType());
    }

    @Test
    public void javaTypeForSchemaDefinitionInvalidLeafrefPathTest() {
        final var ctx = YangParserTestUtils.parseYangResources(TypeProviderTest.class, "/unresolvable-leafref.yang");
        final var ex = assertThrows(IllegalArgumentException.class, () -> DefaultBindingGenerator.generateFor(ctx));
        assertEquals("Failed to find leafref target /somewhere/i/belong", ex.getMessage());
        final var cause = ex.getCause();
        assertThat(cause, instanceOf(IllegalArgumentException.class));
        assertEquals("Data tree child (foo)somewhere not present", cause.getMessage());
    }

    private static MethodSignature assertGeneratedMethod(final JavaTypeName typeName, final String methodName) {
        return assertGeneratedMethod(assertGeneratedType(typeName).getMethodDefinitions(), methodName);
    }

    private static MethodSignature assertGeneratedMethod(final List<MethodSignature> methods, final String name) {
        return methods.stream().filter(method -> name.equals(method.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Method " + name + " not present"));
    }

    private static GeneratedType assertGeneratedType(final JavaTypeName name) {
        return TYPES.stream()
            .filter(type -> name.equals(type.getIdentifier()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Generated type " + name + " not present"));
    }

    private static Enumeration assertEnumeration(final JavaTypeName name) {
        final var type = assertGeneratedType(name);
        assertThat(type, instanceOf(Enumeration.class));
        return (Enumeration) type;
    }

    private static GeneratedTransferObject assertGTO(final JavaTypeName name) {
        final var type = assertGeneratedType(name);
        assertThat(type, instanceOf(GeneratedTransferObject.class));
        return (GeneratedTransferObject) type;
    }
}
