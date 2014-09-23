/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.WildcardType;

public class TypesTest {

    @Test
    public void testVoidType() {
        ConcreteType voidType = Types.voidType();
        assertNotNull(voidType);
    }

    @Test
    public void testPrimitiveType() {
        Type primitiveType = Types.primitiveType("org.opendaylight.yangtools.test", null);
        assertNotNull(primitiveType);
    }

    @Test
    public void testMapTypeFor() {
        ParameterizedType mapType = Types.mapTypeFor(null, null);
        assertNotNull(mapType);
    }

    @Test
    public void testSetTypeFor() {
        ParameterizedType setType = Types.setTypeFor(null);
        assertNotNull(setType);
    }

    @Test
    public void testListTypeFor() {
        ParameterizedType listType = Types.listTypeFor(null);
        assertNotNull(listType);
    }

    @Test
    public void testWildcardTypeFor() {
        WildcardType wildcardType = Types.wildcardTypeFor("org.opendaylight.yangtools.test", "WildcardTypeTest");
        assertNotNull(wildcardType);
    }

    @Test
    public void testAugmentableTypeFor() {
        ParameterizedType augmentableType = Types.augmentableTypeFor(null);
        assertNotNull(augmentableType);
    }

    @Test
    public void augmentationTypeFor() {
        ParameterizedType augmentationType = Types.augmentationTypeFor(null);
        assertNotNull(augmentationType);
    }
}
