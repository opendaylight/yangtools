/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.WildcardType;

public class TypesTest {

    @Test
    public void testVoidType() {
        final ConcreteType voidType = Types.voidType();
        assertEquals("Void", voidType.getName());
    }

    @Test
    public void testPrimitiveType() {
        final Type primitiveType = Types.primitiveType("newType", null);
        assertEquals("newType", primitiveType.getName());
    }

    @Test
    public void testMapTypeFor() {
        final ParameterizedType mapType = Types.mapTypeFor(null, null);
        assertEquals("Map", mapType.getName());
    }

    @Test
    public void testSetTypeFor() {
        final ParameterizedType setType = Types.setTypeFor(null);
        assertEquals("Set", setType.getName());
    }

    @Test
    public void testListTypeFor() {
        final ParameterizedType listType = Types.listTypeFor(null);
        assertEquals("List", listType.getName());
    }

    @Test
    public void testWildcardTypeFor() {
        final WildcardType wildcardType = Types.wildcardTypeFor("org.opendaylight.yangtools.test", "WildcardTypeTest");
        assertEquals("WildcardTypeTest", wildcardType.getName());
    }

    @Test
    public void testAugmentableTypeFor() {
        ParameterizedType augmentableType = Types.augmentableTypeFor(null);
        assertEquals("Augmentable", augmentableType.getName());
    }

    @Test
    public void augmentationTypeFor() {
        ParameterizedType augmentationType = Types.augmentationTypeFor(null);
        assertEquals("Augmentation", augmentationType.getName());
    }
}
