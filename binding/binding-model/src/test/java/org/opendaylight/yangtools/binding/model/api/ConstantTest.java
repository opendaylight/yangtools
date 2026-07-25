/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConstantTest {
    @Test
    void testMethodsOfConstantImpl() {
        final var type = TypeRef.of(JavaTypeName.create("org.opendaylight.yangtools.test.v1", "BaseType"));
        final var constImpl = new Constant(type, "IpAddress", "127.0.0.1");
        final var constImpl2 = new Constant(type, "IpAddress", "127.0.0.1");
        final var constImpl3 = new Constant(type, "IpAddress", "127.0.0.0");
        final var constImpl4 = constImpl;

        assertSame(type, constImpl.type());
        assertEquals("IpAddress", constImpl.name());
        assertEquals("127.0.0.1", constImpl.value());
        assertEquals(
            "Constant[type=TypeRef{name=org.opendaylight.yangtools.test.v1.BaseType}, name=IpAddress, value=127.0.0.1]",
            constImpl.toString());
        assertEquals(constImpl.hashCode(), constImpl2.hashCode());
        assertNotNull(constImpl.type());
        assertNotNull(constImpl.name());
        assertNotNull(constImpl.value());
        assertNotNull(constImpl.hashCode());
        assertFalse(constImpl.equals(null));
        assertFalse(constImpl.equals("test"));

        assertTrue(constImpl.equals(constImpl2));
        assertFalse(constImpl.equals(constImpl3));
        assertTrue(constImpl.equals(constImpl4));
    }
}
