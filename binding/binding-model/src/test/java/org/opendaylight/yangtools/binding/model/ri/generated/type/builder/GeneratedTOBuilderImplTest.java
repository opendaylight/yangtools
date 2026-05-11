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

class GeneratedTOBuilderImplTest {
    @Test
    void testSetExtendsType() {
        final var genTOBuilder = new CodegenScalarTypeObjectArchetypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Test"));
        final var extendedTypeBuilder = new CodegenScalarTypeObjectArchetypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "ExtendedType"));
        final var extendedType = extendedTypeBuilder.build();
        genTOBuilder.setExtendsType(extendedType);
        final var genTO = genTOBuilder.build();
        assertEquals(extendedType, genTO.getSuperType());
    }

    @Test
    void testAddMethod() {
        final var genTOBuilder = new CodegenScalarTypeObjectArchetypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Test"));
        final var methodSignatureBuilder = genTOBuilder.addMethod("testMethod");
        assertEquals(methodSignatureBuilder, genTOBuilder.getMethodDefinitions().get(0));
        assertEquals("testMethod", genTOBuilder.getMethodDefinitions().get(0).getName());
    }

    @Test
    void testSetRestrictions() {
        final var genTOBuilder = new CodegenScalarTypeObjectArchetypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Test"));
        genTOBuilder.setRestrictions(Restrictions.empty());
        final var genTO = genTOBuilder.build();

        assertNotNull(genTO.getRestrictions());
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
}
