/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.sal.binding.model.api.Type;

public class MethodParameterImplTest {

    MethodParameterImpl parameter1, parameter2, parameter3, parameter4;
    int hash1, hash2, hash3, hash4;

    @Before
    public void Setup() {
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
    public void testToString() {
        String toString = parameter1.toString();
        assertTrue(toString.contains("MethodParameter"));
    }

    @Test
    public void testEquals() {
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
    public void testHashCode() {
        assertEquals(hash1,hash2);
        assertTrue(!(hash1 == hash3));
        assertTrue(!(hash1 == hash4));
    }

}
