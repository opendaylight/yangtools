/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.java.api.generator.TOGenerator;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class ClassCodeGeneratorTest {

    private final static List<File> testModels = new ArrayList<File>();

    @BeforeClass
    public static void loadTestResources() throws URISyntaxException {
        final File listModelFile = new File(ClassCodeGeneratorTest.class
                .getResource("/list-composite-key.yang").toURI());
        testModels.add(listModelFile);
    }

    @Test
    public void compositeKeyClassTest() {
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(testModels);
        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        assertTrue(genTypes != null);
        assertEquals(7, genTypes.size());

        int genTypesCount = 0;
        int genTOsCount = 0;
        for (final Type type : genTypes) {
            if (type instanceof GeneratedType
                    && !(type instanceof GeneratedTransferObject)) {
                genTypesCount++;
            } else if (type instanceof GeneratedTransferObject) {
                final GeneratedTransferObject genTO = (GeneratedTransferObject) type;

                if (genTO.getName().equals("CompositeKeyListKey")) {
                    final List<GeneratedProperty> properties = genTO
                            .getProperties();
                    int propertyCount = 0;
                    for (final GeneratedProperty prop : properties) {
                        if (prop.getName().equals("key1")) {
                            propertyCount++;
                        } else if (prop.getName().equals("key2")) {
                            propertyCount++;
                        }
                    }

                    final TOGenerator clsGen = new TOGenerator();
                    final String outputStr = clsGen.generate(genTO);

                    assertNotNull(outputStr);
                    assertTrue(outputStr.contains("public CompositeKeyListKey(Byte _key1, String _key2)"));

                    assertEquals(2, propertyCount);
                    genTOsCount++;
                } else if (genTO.getName().equals("InnerListKey")) {
                    final List<GeneratedProperty> properties = genTO
                            .getProperties();
                    assertEquals(1, properties.size());
                    genTOsCount++;
                }
            }
        }

        assertEquals(5, genTypesCount);
        assertEquals(2, genTOsCount);
    }

    @Ignore
    @Test
    public void defaultConstructorTest() {
        final GeneratedTOBuilder toBuilder = new GeneratedTOBuilderImpl(
                "simple.pack", "DefCtor");

        GeneratedPropertyBuilder propBuilder = toBuilder.addProperty("foo");
        propBuilder.setReturnType(Types.typeForClass(String.class));
        propBuilder.setReadOnly(false);

        propBuilder = toBuilder.addProperty("bar");
        propBuilder.setReturnType(Types.typeForClass(Integer.class));
        propBuilder.setReadOnly(false);

        final GeneratedTransferObject genTO = toBuilder.toInstance();

        final TOGenerator clsGen = new TOGenerator();
        final String outputStr = clsGen.generate(genTO);

        assertNotNull(outputStr);
        assertTrue(outputStr.contains("public DefCtor()"));
    }

    @Test
    public void toStringTest() {
        final GeneratedTOBuilder toBuilder = new GeneratedTOBuilderImpl(
                "simple.pack", "DefCtor");

        GeneratedPropertyBuilder propBuilder = toBuilder.addProperty("foo");
        propBuilder.setReturnType(Types.typeForClass(String.class));
        propBuilder.setReadOnly(false);
        toBuilder.addToStringProperty(propBuilder);

        propBuilder = toBuilder.addProperty("bar");
        propBuilder.setReturnType(Types.typeForClass(Integer.class));
        propBuilder.setReadOnly(false);
        toBuilder.addToStringProperty(propBuilder);
        final GeneratedTransferObject genTO = toBuilder.toInstance();
        final TOGenerator clsGen = new TOGenerator();
        assertNotNull(clsGen.generate(genTO));
    }
}
