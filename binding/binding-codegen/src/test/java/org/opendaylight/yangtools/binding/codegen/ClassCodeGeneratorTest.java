/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.binding.generator.impl.DefaultBindingGenerator;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class ClassCodeGeneratorTest {
    @Test
    public void compositeKeyClassTest() {
        final List<GeneratedType> genTypes = new DefaultBindingGenerator().generateTypes(
            YangParserTestUtils.parseYangResource("/list-composite-key.yang"));

        assertNotNull(genTypes);
        assertEquals(7, genTypes.size());

        int genTypesCount = 0;
        int genTOsCount = 0;
        for (final GeneratedType type : genTypes) {
            if (type instanceof final GeneratedTransferObject genTO) {
                if (genTO.getName().equals("CompositeKeyListKey")) {
                    final List<GeneratedProperty> properties = genTO
                            .getProperties();
                    int propertyCount = 0;
                    for (final GeneratedProperty prop : properties) {
                        if (prop.getName().equals("key1") || prop.getName().equals("key2")) {
                            propertyCount++;
                        }
                    }

                    final TOGenerator clsGen = new TOGenerator();
                    final String outputStr = clsGen.generate(genTO);

                    assertNotNull(outputStr);
                    assertThat(outputStr, containsString(
                        "public CompositeKeyListKey(@NonNull Byte _key1, @NonNull String _key2)"));

                    assertEquals(2, propertyCount);
                    genTOsCount++;
                } else if (genTO.getName().equals("InnerListKey")) {
                    final List<GeneratedProperty> properties = genTO
                            .getProperties();
                    assertEquals(1, properties.size());
                    genTOsCount++;
                }
            } else {
                genTypesCount++;
            }
        }

        assertEquals(5, genTypesCount);
        assertEquals(2, genTOsCount);
    }

    /**
     * Test for testing of false scenario. Test tests value types. Value types are not allowed to have default
     * constructor.
     */
    @Test
    public void defaultConstructorNotPresentInValueTypeTest() {
        final GeneratedTOBuilder toBuilder = new CodegenGeneratedTOBuilder(JavaTypeName.create("simple.pack",
            "DefCtor"));

        GeneratedPropertyBuilder propBuilder = toBuilder.addProperty("foo");
        propBuilder.setReturnType(Types.typeForClass(String.class));
        propBuilder.setReadOnly(false);

        propBuilder = toBuilder.addProperty("bar");
        propBuilder.setReturnType(Types.typeForClass(Integer.class));
        propBuilder.setReadOnly(false);

        final GeneratedTransferObject genTO = toBuilder.build();

        final TOGenerator clsGen = new TOGenerator();
        final String outputStr = clsGen.generate(genTO);

        assertNotNull(outputStr);
        assertFalse(outputStr.contains("public DefCtor()"));
    }

    @Test
    public void toStringTest() {
        final GeneratedTOBuilder toBuilder = new CodegenGeneratedTOBuilder(JavaTypeName.create("simple.pack",
            "DefCtor"));

        GeneratedPropertyBuilder propBuilder = toBuilder.addProperty("foo");
        propBuilder.setReturnType(Types.typeForClass(String.class));
        propBuilder.setReadOnly(false);
        toBuilder.addToStringProperty(propBuilder);

        propBuilder = toBuilder.addProperty("bar");
        propBuilder.setReturnType(Types.typeForClass(Integer.class));
        propBuilder.setReadOnly(false);
        toBuilder.addToStringProperty(propBuilder);
        final GeneratedTransferObject genTO = toBuilder.build();
        final TOGenerator clsGen = new TOGenerator();
        assertEquals("", clsGen.generate(Types.typeForClass(String.class)));
        assertNotNull(clsGen.generate(genTO));
    }
}
