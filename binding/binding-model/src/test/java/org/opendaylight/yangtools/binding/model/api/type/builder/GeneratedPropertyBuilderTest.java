/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api.type.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.ri.Types;

class GeneratedPropertyBuilderTest {
    @Test
    void generatedPropertyBuilderImplTest() {
        final var generatedPropertyBuilderImpl = new GeneratedPropertyBuilder("myPropertyName")
            .setValue("myValue")
            .setReadOnly(false)
            .setComment(null)
            .setReturnType(Types.BOOLEAN);

        assertEquals(
            "GeneratedPropertyBuilder{name=myPropertyName, returnType=ConcreteType{name=java.lang.Boolean}}",
            generatedPropertyBuilderImpl.toString());

        var instance = generatedPropertyBuilderImpl.toInstance();
        assertNotNull(instance);
        assertEquals("""
            GeneratedPropertyImpl [name=myPropertyName, annotations=[], comment=null, \
            returnType=ConcreteType{name=java.lang.Boolean}, isReadOnly=false]""", instance.toString());

        assertFalse(instance.isReadOnly());
        assertEquals("myValue", instance.getValue());
        assertNull(instance.getComment());
        assertEquals(Types.BOOLEAN, instance.getReturnType());
    }

    @Test
    void generatedPropertyBuilderImplEqualsAndHashCodeTest() {
        final var generatedPropertyBuilderImpl = new GeneratedPropertyBuilder("myPropertyName");
        final var generatedPropertyBuilderImpl2 = new GeneratedPropertyBuilder("myPropertyName");
        final var generatedPropertyBuilderImpl3 = new GeneratedPropertyBuilder("myPropertyName3");
        final var generatedPropertyBuilderImpl4 = new GeneratedPropertyBuilder("myPropertyName");

        assertNotNull(generatedPropertyBuilderImpl);
        assertNotNull(generatedPropertyBuilderImpl2);
        assertNotNull(generatedPropertyBuilderImpl3);
        assertNotNull(generatedPropertyBuilderImpl4);

        generatedPropertyBuilderImpl.setReturnType(Types.BOOLEAN);
        generatedPropertyBuilderImpl2.setReturnType(Types.BOOLEAN);
        generatedPropertyBuilderImpl3.setReturnType(Types.BOOLEAN);
        generatedPropertyBuilderImpl4.setReturnType(Types.STRING);

        assertFalse(generatedPropertyBuilderImpl.equals(null));
        assertFalse(generatedPropertyBuilderImpl.equals(new Object()));
        assertTrue(generatedPropertyBuilderImpl.equals(generatedPropertyBuilderImpl));
        assertTrue(generatedPropertyBuilderImpl.equals(generatedPropertyBuilderImpl2));
        assertFalse(generatedPropertyBuilderImpl.equals(generatedPropertyBuilderImpl3));
        assertFalse(generatedPropertyBuilderImpl.equals(generatedPropertyBuilderImpl4));

        assertEquals(generatedPropertyBuilderImpl.hashCode(), generatedPropertyBuilderImpl.hashCode());
        assertEquals(generatedPropertyBuilderImpl.hashCode(), generatedPropertyBuilderImpl2.hashCode());
        assertNotEquals(generatedPropertyBuilderImpl.hashCode(), generatedPropertyBuilderImpl3.hashCode());
        assertNotEquals(generatedPropertyBuilderImpl.hashCode(), generatedPropertyBuilderImpl4.hashCode());
    }
}
