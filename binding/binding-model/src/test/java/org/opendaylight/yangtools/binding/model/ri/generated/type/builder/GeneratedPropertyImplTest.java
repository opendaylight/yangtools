/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;
import org.opendaylight.yangtools.binding.model.ri.Types;

class GeneratedPropertyImplTest {
    @Test
    void generatedPropertyImplTest() {
        final var generatedPropertyBuilderImpl = new GeneratedPropertyBuilderImpl("myPropertyName")
            .setValue("myValue")
            .setReadOnly(false)
            .setStatic(true)
            .setComment(TypeMemberComment.contractOf("myComment"))
            .setFinal(true)
            .setAccessModifier(AccessModifier.PUBLIC)
            .setReturnType(Types.BOOLEAN);

        var instance = generatedPropertyBuilderImpl.toInstance();

        assertNotNull(instance);

        assertTrue(instance.isFinal());
        assertTrue(instance.isStatic());
        assertFalse(instance.isReadOnly());
        assertEquals("myValue", instance.getValue());
        assertEquals(TypeMemberComment.contractOf("myComment"), instance.getComment());
        assertEquals(AccessModifier.PUBLIC, instance.getAccessModifier());
        assertEquals(Types.BOOLEAN, instance.getReturnType());

        assertEquals("""
            GeneratedPropertyImpl [name=myPropertyName, annotations=[], \
            comment=TypeMemberComment{contract=myComment}, \
            returnType=ConcreteTypeImpl{name=java.lang.Boolean}, isFinal=true, isReadOnly=false, \
            modifier=PUBLIC]""", instance.toString());
    }

    @Test
    void generatedPropertyImplEqualsAndHashCodeTest() {
        var generatedPropertyBuilderImpl = new GeneratedPropertyBuilderImpl("myPropertyName");
        var generatedPropertyBuilderImpl2 = new GeneratedPropertyBuilderImpl("myPropertyName");
        final var generatedPropertyBuilderImpl3 = new GeneratedPropertyBuilderImpl("myPropertyName3");
        final var generatedPropertyBuilderImpl4 = new GeneratedPropertyBuilderImpl("myPropertyName");

        generatedPropertyBuilderImpl.setReturnType(Types.BOOLEAN);
        generatedPropertyBuilderImpl2.setReturnType(Types.BOOLEAN);
        generatedPropertyBuilderImpl3.setReturnType(Types.BOOLEAN);
        generatedPropertyBuilderImpl4.setReturnType(Types.STRING);

        final var property = generatedPropertyBuilderImpl.toInstance();
        final var property2 = generatedPropertyBuilderImpl2.toInstance();
        final var property3 = generatedPropertyBuilderImpl3.toInstance();
        final var property4 = generatedPropertyBuilderImpl4.toInstance();

        assertNotNull(property);
        assertNotNull(property2);
        assertNotNull(property3);
        assertNotNull(property4);

        assertFalse(property.equals(null));
        assertFalse(property.equals(new Object()));
        assertTrue(property.equals(property));
        assertTrue(property.equals(property2));
        assertFalse(property.equals(property3));
        assertFalse(property.equals(property4));

        assertEquals(property.hashCode(), property.hashCode());
        assertEquals(property.hashCode(), property2.hashCode());
        assertNotEquals(property.hashCode(), property3.hashCode());
        assertNotEquals(property.hashCode(), property4.hashCode());
    }
}
