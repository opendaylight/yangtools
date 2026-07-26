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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;
import org.opendaylight.yangtools.binding.model.ri.Types;

class GeneratedPropertyImplTest {
    @Test
    void generatedPropertyImplTest() {
        var instance = new GeneratedPropertyImpl("myPropertyName", TypeMemberComment.contractOf("myComment"),
            Types.BOOLEAN, false, "myValue");

        assertFalse(instance.isReadOnly());
        assertEquals("myValue", instance.getValue());
        assertEquals(TypeMemberComment.contractOf("myComment"), instance.getComment());
        assertEquals(Types.BOOLEAN, instance.getReturnType());

        assertEquals("""
            GeneratedPropertyImpl [name=myPropertyName, comment=TypeMemberComment{contract=myComment}, \
            returnType=ConcreteType{name=java.lang.Boolean}, isReadOnly=false]""", instance.toString());
    }

    @Test
    void generatedPropertyImplEqualsAndHashCodeTest() {
        final var property = new GeneratedPropertyImpl("myPropertyName", null, Types.BOOLEAN, true, null);
        final var property2 = new GeneratedPropertyImpl("myPropertyName", null, Types.BOOLEAN, true, null);
        final var property3 = new GeneratedPropertyImpl("myPropertyName3", null, Types.BOOLEAN, true, null);
        final var property4 = new GeneratedPropertyImpl("myPropertyName", null, Types.STRING, true, null);

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
