/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

class TypesTest {
    @Test
    void testVoidType() {
        final var voidType = Types.voidType();
        assertEquals("Void", voidType.getName());
        assertNotNull(voidType);
    }

    @Test
    void testPrimitiveType() {
        final var primitiveType = Types.typeForClass(String[].class);
        assertEquals("String[]", primitiveType.getName());
    }

    @Test
    void testMapTypeFor() {
        final var mapType = Types.mapTypeFor(Types.objectType(), Types.objectType());
        assertEquals("Map", mapType.getName());
    }

    @Test
    void testMapTypeForNull() {
        assertThrows(NullPointerException.class, () -> Types.mapTypeFor(null, null));
    }

    @Test
    void testSetTypeFor() {
        final var setType = Types.setTypeFor(Types.objectType());
        assertEquals("Set", setType.getName());
    }

    @Test
    void testSetTypeForNull() {
        assertThrows(NullPointerException.class, () -> Types.setTypeFor(null));
    }

    @Test
    void testListTypeFor() {
        final var listType = Types.listTypeFor(Types.objectType());
        assertEquals("List", listType.getName());
    }

    @Test
    void testListTypeForNull() {
        assertThrows(NullPointerException.class, () -> Types.listTypeFor(null));
    }

    @Test
    void testWildcardTypeFor() {
        final var wildcardType = Types.wildcardTypeFor(JavaTypeName.create("org.opendaylight.yangtools.test",
            "WildcardTypeTest"));
        assertEquals("WildcardTypeTest", wildcardType.getName());
    }
}
