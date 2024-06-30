/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

class TypeNameTest {
    @Test
    void testHashCode() {
        var baseType1 = JavaTypeName.create("org.opendaylight.yangtools.test", "Test");
        var baseType2 = JavaTypeName.create("org.opendaylight.yangtools.test", "Test2");
        assertNotEquals(baseType1.hashCode(), baseType2.hashCode());
    }

    @Test
    void testToString() {
        var baseType = JavaTypeName.create("org.opendaylight.yangtools.test", "Test");
        assertTrue(baseType.toString().contains("org.opendaylight.yangtools.test.Test"));
        baseType = JavaTypeName.create(byte[].class);
        assertTrue(baseType.toString().contains("byte[]"));
    }

    @Test
    void testEquals() {
        final var baseType1 = JavaTypeName.create("org.opendaylight.yangtools.test", "Test");
        final var baseType2 = JavaTypeName.create("org.opendaylight.yangtools.test", "Test2");
        final var baseType4 = JavaTypeName.create("org.opendaylight.yangtools.test", "Test");
        final var baseType5 = JavaTypeName.create("org.opendaylight.yangtools.test1", "Test");

        assertFalse(baseType1.equals(baseType2));
        assertFalse(baseType1.equals(null));
        assertTrue(baseType1.equals(baseType4));
        assertFalse(baseType1.equals(baseType5));
        assertFalse(baseType1.equals(null));
    }
}
