/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.ri.Types;

class GeneratedTOBuilderImplTest {
    @Test
    void testCreateNewInstance() {
        final var genTOBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Test"));
        assertNotNull(genTOBuilder);
    }

    @Test
    void testSetExtendsType() {
        final var genTOBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Test"));
        final var extendedTypeBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "ExtendedType"));
        final var extendedType = extendedTypeBuilder.build();
        genTOBuilder.setExtendsType(extendedType);
        final var genTO = genTOBuilder.build();

        assertEquals("ExtendedType", genTO.getSuperType().simpleName());
    }

    @Test
    void testAddMethod() {
        final var genTOBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Test"));
        final var methodSignatureBuilder = genTOBuilder.addMethod("testMethod");
        assertEquals(methodSignatureBuilder, genTOBuilder.getMethodDefinitions().get(0));
        assertEquals("testMethod", genTOBuilder.getMethodDefinitions().get(0).getName());
    }

    @Test
    void testAddEqualsIdentity() {
        final var genTO = new CodegenGeneratedTOBuilder(JavaTypeName.create("org.opendaylight.yangtools.test", "Test"))
            .addEqualsIdentity(new GeneratedPropertyBuilderImpl("testProperty").setReturnType(Types.STRING))
            .build();
        assertEquals(1, genTO.getEqualsIdentifiers().size());
        assertEquals("testProperty", genTO.getEqualsIdentifiers().get(0).getName());
    }

    @Test
    void testAddHashIdentity() {
        final var genTO = new CodegenGeneratedTOBuilder(JavaTypeName.create("org.opendaylight.yangtools.test", "Test"))
            .addHashIdentity(new GeneratedPropertyBuilderImpl("testProperty").setReturnType(Types.STRING))
            .build();
        assertEquals(1, genTO.getHashCodeIdentifiers().size());
        assertEquals("testProperty", genTO.getHashCodeIdentifiers().get(0).getName());
    }

    @Test
    void testAddToStringProperty() {
        final var genTO = new CodegenGeneratedTOBuilder(JavaTypeName.create("org.opendaylight.yangtools.test", "Test"))
            .addToStringProperty(new GeneratedPropertyBuilderImpl("testProperty").setReturnType(Types.STRING))
            .build();
        assertEquals(1, genTO.getToStringIdentifiers().size());
        assertEquals("testProperty", genTO.getToStringIdentifiers().get(0).getName());
    }

    @Test
    void testSetRestrictions() {
        final var genTOBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Test"));
        genTOBuilder.setRestrictions(Restrictions.empty());
        final var genTO = genTOBuilder.build();

        assertNotNull(genTO.getRestrictions());
    }

    @Test
    void testSetSUID() {
        final var genTOBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Test"));
        genTOBuilder.setSUID(new GeneratedPropertyBuilderImpl("testProperty").setReturnType(Types.STRING));
        final var genTO = genTOBuilder.build();
        assertEquals("testProperty", genTO.getSUID().getName());
    }

    @Test
    void testToStringMethod() {
        final var genTOBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Test"));
        assertNotNull(genTOBuilder.toString());
    }

    @Test
    void testSetterMethods() {
        final var builder = new CodegenUnionTypeObjectArchetypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Test"));
        builder.setTypedef(true);
        builder.setDescription("test description");
        builder.setModuleName("test-module");
        builder.setReference("http://tools.ietf.org/html/rfc6020");

        final var archetype = builder.build();

        assertTrue(archetype.isTypedef());
        assertEquals("test description", archetype.getDescription());
        assertEquals("test-module", archetype.getModuleName());
        assertEquals("http://tools.ietf.org/html/rfc6020", archetype.getReference());
    }

    @Test
    void testMethodsOfGeneratedTransferObjectImpl() {
        final var genTOBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Test"));
        final var genTO = genTOBuilder.build();

        assertNotNull(genTO.toString());
    }
}
