/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;

class TypesTest {
    @Test
    void testPrimitiveType() {
        final var primitiveType = ConcreteType.ofClass(String[].class);
        assertEquals("String[]", primitiveType.simpleName());
    }

    @Test
    void testMapTypeFor() {
        final var mapType = Types.mapTypeFor(Types.OBJECT, Types.OBJECT);
        assertEquals("Map", mapType.simpleName());
    }

    @Test
    void testMapTypeForNull() {
        assertThrows(NullPointerException.class, () -> Types.mapTypeFor(null, null));
    }

    @Test
    void testSetTypeFor() {
        final var setType = Types.setTypeFor(Types.OBJECT);
        assertEquals("Set", setType.simpleName());
    }

    @Test
    void testSetTypeForNull() {
        assertThrows(NullPointerException.class, () -> Types.setTypeFor(null));
    }

    @Test
    void testListTypeFor() {
        final var listType = Types.listTypeFor(Types.OBJECT);
        assertEquals("List", listType.simpleName());
    }

    @Test
    void testListTypeForNull() {
        assertThrows(NullPointerException.class, () -> Types.listTypeFor(null));
    }
}
