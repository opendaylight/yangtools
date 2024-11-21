/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.Types;

class MethodParameterImplTest {
    private MethodParameterImpl parameter1;
    private MethodParameterImpl parameter2;
    private MethodParameterImpl parameter3;
    private MethodParameterImpl parameter4;
    private int hash1;
    private int hash2;
    private int hash3;
    private int hash4;

    @BeforeEach
    void before() {
        String name = "customParameter";
        Type type = Types.STRING;
        parameter1 = new MethodParameterImpl(name, type);
        parameter2 = new MethodParameterImpl(name, type);
        parameter3 = new MethodParameterImpl(name, null);
        parameter4 = new MethodParameterImpl(null, type);

        hash1 = parameter1.hashCode();
        hash2 = parameter2.hashCode();
        hash3 = parameter3.hashCode();
        hash4 = parameter4.hashCode();
    }

    @Test
    void testToString() {
        String toString = parameter1.toString();
        assertTrue(toString.contains("MethodParameter"));
    }

    @Test
    void testEquals() {
        assertTrue(parameter1.equals(parameter1));
        assertTrue(parameter1.equals(parameter2));
        assertFalse(parameter1.equals("string"));
        assertFalse(parameter1.equals(null));
        assertFalse(parameter1.equals(parameter3));
        assertFalse(parameter2.equals(parameter4));
        assertFalse(parameter4.equals(parameter2));
        assertFalse(parameter3.equals(parameter2));
    }

    @Test
    void testHashCode() {
        assertEquals(hash1, hash2);
        assertNotEquals(hash1, hash3);
        assertNotEquals(hash1, hash4);
    }
}
