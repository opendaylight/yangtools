/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.BITS_TYPE_OBJECT;
import static org.opendaylight.yangtools.binding.model.ri.BindingTypes.UNION_TYPE_OBJECT;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal406TypeObjectTest {
    private static EffectiveModelContext CONTEXT;

    @BeforeClass
    public static void beforeClass() {
        CONTEXT = YangParserTestUtils.parseYangResources(ExtendedTypedefTest.class,
            "/type-provider/test.yang", "/ietf-models/ietf-inet-types.yang");
    }

    @AfterClass
    public static void afterClass() {
        CONTEXT = null;
    }

    @Test
    public void typeObjectTest() {
        final var generateTypes = DefaultBindingGenerator.generateFor(CONTEXT);
        assertNotNull(generateTypes);

        final var typedefType = generateTypes.stream().filter(type -> type.getFullyQualifiedName()
            .equals("org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.MyBinary")).findFirst()
            .orElseThrow();

        assertNotNull(typedefType.getImplements());
        final var objectType = typedefType.getImplements().stream()
                .filter(type -> type.getFullyQualifiedName()
                .equals("org.opendaylight.yangtools.binding.ScalarTypeObject")).findAny().orElseThrow();
        assertEquals(BindingTypes.scalarTypeObject(Types.BYTE_ARRAY), objectType);
    }

    @Test
    public void bitsTypeObjectForBitsTypedefTest() {
        final var generateTypes = DefaultBindingGenerator.generateFor(CONTEXT);
        assertNotNull(generateTypes);

        final var typedefType = generateTypes.stream().filter(type -> type.getFullyQualifiedName()
                .equals("org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.MyBits")).findFirst()
                .orElseThrow();

        assertNotNull(typedefType.getImplements());
        final var objectType = typedefType.getImplements().stream()
                .filter(type -> type.getFullyQualifiedName()
                        .equals("org.opendaylight.yangtools.binding.BitsTypeObject")).findAny().orElseThrow();
        assertEquals(BITS_TYPE_OBJECT, objectType);
    }

    @Test
    public void typeObjectForUnionTypedefTest() {
        final var generateTypes = DefaultBindingGenerator.generateFor(CONTEXT);
        assertNotNull(generateTypes);

        final var typedefType = generateTypes.stream().filter(type -> type.getFullyQualifiedName()
                .equals("org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.MyUnion")).findFirst()
                .orElseThrow();

        assertNotNull(typedefType.getImplements());
        final var objectType = typedefType.getImplements().stream()
                .filter(type -> type.getFullyQualifiedName()
                        .equals("org.opendaylight.yangtools.binding.UnionTypeObject")).findAny().orElseThrow();
        assertEquals(UNION_TYPE_OBJECT, objectType);
    }
}
