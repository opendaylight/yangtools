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
import org.opendaylight.yangtools.binding.model.UnknownLeafrefType;
import org.opendaylight.yangtools.binding.model.impl.ConcreteTypeImpl;

class TypesTest {
    @Test
    void testPrimitiveType() {
        final var primitiveType = new ConcreteTypeImpl(String[].class);
        assertEquals("String[]", primitiveType.simpleName());
    }

    @Test
    void testSetTypeFor() {
        final var setType = Types.setTypeFor(UnknownLeafrefType.INSTANCE);
        assertEquals("Set", setType.simpleName());
    }

    @Test
    void testSetTypeForNull() {
        assertThrows(NullPointerException.class, () -> Types.setTypeFor(null));
    }

    @Test
    void testListTypeFor() {
        final var listType = Types.listTypeFor(UnknownLeafrefType.INSTANCE);
        assertEquals("List", listType.simpleName());
    }

    @Test
    void testListTypeForNull() {
        assertThrows(NullPointerException.class, () -> Types.listTypeFor(null));
    }
}
