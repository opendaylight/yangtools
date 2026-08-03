/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;

class GeneratedPropertyImplTest {
    @Test
    void generatedPropertyImplTest() {
        var instance = new GeneratedPropertyImpl("myPropertyName", BaseYangTypes.BOOLEAN_TYPE, false);

        assertFalse(instance.isReadOnly());
        assertSame(BaseYangTypes.BOOLEAN_TYPE, instance.getReturnType());

        assertEquals("""
            GeneratedPropertyImpl[name=myPropertyName, returnType=ConcreteType{name=java.lang.Boolean}, \
            isReadOnly=false]""", instance.toString());
    }

    @Test
    void generatedPropertyImplEqualsAndHashCodeTest() {
        final var property = new GeneratedPropertyImpl("myPropertyName", BaseYangTypes.BOOLEAN_TYPE, true);
        final var property2 = new GeneratedPropertyImpl("myPropertyName", BaseYangTypes.BOOLEAN_TYPE, true);
        final var property3 = new GeneratedPropertyImpl("myPropertyName3", BaseYangTypes.BOOLEAN_TYPE, true);
        final var property4 = new GeneratedPropertyImpl("myPropertyName", BaseYangTypes.STRING_TYPE, true);

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
