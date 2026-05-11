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
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.Types;

class GeneratedTOBuilderTest {
    @Test
    void testBuilder() {
        final var genTypeBuilder = new CodegenScalarTypeObjectArchetypeBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotClassCache"));

        final var suid = genTypeBuilder.addProperty("SUID").setReturnType(BindingTypes.SCALAR_TYPE_OBJECT);
        genTypeBuilder.addMethod("addCount").setReturnType(Types.VOID);

        final var genTO = genTypeBuilder.build();
        assertEquals(List.of(suid.toInstance()), genTO.getProperties());

        genTypeBuilder.setExtendsType(genTO);

        final var propBuilder = genTypeBuilder.addProperty("customProperty").setReturnType(Types.STRING);
        final var genType = genTypeBuilder.build();
        assertNotNull(genType);
        assertSame(genTO, genType.getSuperType());
        assertEquals(List.of(suid.toInstance(), propBuilder.toInstance()), genType.getProperties());
    }

    @Test
    void testToString() {
        final var genTypeBuilder = new CodegenScalarTypeObjectArchetypeBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotClassCache"));
        assertEquals("CodegenScalarTypeObjectArchetypeBuilder{typeName=org.opendaylight.controller.AnnotClassCache}",
            genTypeBuilder.toString());
    }

    @Test
    void testTransferBuilderToString() {
        final var genTypeBuilder1 = new CodegenScalarTypeObjectArchetypeBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotClassCache"));

        var genTO = genTypeBuilder1.build();
        assertEquals("CodegenScalarTO{name=org.opendaylight.controller.AnnotClassCache, extends=null}",
            genTO.toString());
    }
}
