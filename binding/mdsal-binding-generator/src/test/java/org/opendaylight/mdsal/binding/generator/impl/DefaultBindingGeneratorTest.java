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

import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
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
    public static final String TEST_TYPE_PROVIDER =
        "org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.model.rev140912";
    public static final String TEST_TYPE_PROVIDER_B =
        "org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.b.model.rev140915";
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
        final var bDataName = JavaTypeName.create(TEST_TYPE_PROVIDER_B, "TestTypeProviderBData");
        final var bData = TYPES.stream()
            .filter(type -> type.getIdentifier().equals(bDataName))
            .findFirst()
            .orElseThrow();

        final var bDataMethods = bData.getMethodDefinitions();
        assertEquals(8, bDataMethods.size());

        final var bEnumType = bDataMethods.stream()
            .filter(method -> method.getName().equals("getEnum"))
            .findFirst()
            .orElseThrow()
            .getReturnType();
        assertThat(bEnumType, instanceOf(Enumeration.class));
        assertEquals(TEST_TYPE_PROVIDER + ".Foo.ResolveDirectUseOfEnum", bEnumType.getFullyQualifiedName());

        final var bEnumsType = bDataMethods.stream()
            .filter(method -> method.getName().equals("getEnums"))
            .findFirst()
            .orElseThrow()
            .getReturnType();

        assertThat(bEnumsType, instanceOf(ParameterizedType.class));
        final var enumsType = (ParameterizedType) bEnumsType;

        assertEquals(Types.typeForClass(List.class), enumsType.getRawType());
        final var enumsTypeArgs = enumsType.getActualTypeArguments();
        assertEquals(1, enumsTypeArgs.length);
        assertEquals(TEST_TYPE_PROVIDER + ".Foo.ListOfEnums", enumsTypeArgs[0].getFullyQualifiedName());
    }

    @Test
    public void generatedTypeForExtendedDefinitionTypeWithIdentityrefBaseTypeTest() {
        final var cttName = JavaTypeName.create(TEST_TYPE_PROVIDER, "ConstructionTypeTest");
        final var ctt = TYPES.stream()
            .filter(type -> type.getIdentifier().equals(cttName))
            .findFirst()
            .orElseThrow();

        final var methods = ctt.getMethodDefinitions();
        assertEquals(56, methods.size());

        final var type = methods.stream().filter(method -> method.getName().equals("getAesIdentityrefType"))
            .findFirst()
            .orElseThrow()
            .getReturnType();
        assertThat(type, instanceOf(ParameterizedType.class));
        final var pType = (ParameterizedType) type;
        assertEquals(Types.CLASS, pType.getRawType());
        final var pTypeArgs = pType.getActualTypeArguments();
        assertEquals(1, pTypeArgs.length);
        assertEquals(TEST_TYPE_PROVIDER + ".Aes", pTypeArgs[0].getFullyQualifiedName());
    }
}
