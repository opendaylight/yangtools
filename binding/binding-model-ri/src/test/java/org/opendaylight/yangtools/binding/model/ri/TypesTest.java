/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.WildcardType;

public class TypesTest {

    @Test
    public void testVoidType() {
        final ConcreteType voidType = Types.voidType();
        assertEquals("Void", voidType.getName());
        assertNotNull(voidType);
    }

    @Test
    public void testPrimitiveType() {
        final Type primitiveType = Types.typeForClass(String[].class);
        assertEquals("String[]", primitiveType.getName());
    }

    @Test
    public void testMapTypeFor() {
        final ParameterizedType mapType = Types.mapTypeFor(Types.objectType(), Types.objectType());
        assertEquals("Map", mapType.getName());
    }

    @Test(expected = NullPointerException.class)
    public void testMapTypeForNull() {
        Types.mapTypeFor(null, null);
    }

    @Test
    public void testSetTypeFor() {
        final ParameterizedType setType = Types.setTypeFor(Types.objectType());
        assertEquals("Set", setType.getName());
    }

    @Test(expected = NullPointerException.class)
    public void testSetTypeForNull() {
        Types.setTypeFor(null);
    }

    @Test
    public void testListTypeFor() {
        final ParameterizedType listType = Types.listTypeFor(Types.objectType());
        assertEquals("List", listType.getName());
    }

    @Test(expected = NullPointerException.class)
    public void testListTypeForNull() {
        Types.listTypeFor(null);
    }

    @Test
    public void testWildcardTypeFor() {
        final WildcardType wildcardType = Types.wildcardTypeFor(JavaTypeName.create("org.opendaylight.yangtools.test",
            "WildcardTypeTest"));
        assertEquals("WildcardTypeTest", wildcardType.getName());
    }
}
