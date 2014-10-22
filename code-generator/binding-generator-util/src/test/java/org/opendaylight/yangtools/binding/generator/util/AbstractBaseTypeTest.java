/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AbstractBaseTypeTest {

    @Rule
    public ExpectedException expException = ExpectedException.none();

    @Test
    public void testgetFullyQualifiedName() {
        AbstractBaseType baseType = new AbstractBaseType("", "");
        assertTrue(baseType.getFullyQualifiedName().isEmpty());
    }

    @Test
    public void testCreateAbstractBaseTypeWithNullPackagename() {
        expException.expect(IllegalArgumentException.class);
        expException.expectMessage("Package Name for Generated Type cannot be null!");
        AbstractBaseType baseTypeNullpackagename = new AbstractBaseType(null, "Test");
    }

    @Test
    public void testCreateAbstractBaseTypeWithNullTypeName() {
        expException.expect(IllegalArgumentException.class);
        expException.expectMessage("Name of Generated Type cannot be null!");
        AbstractBaseType baseTypeNullTypeName = new AbstractBaseType("org.opendaylight.yangtools.test", null);
    }

    @Test
    public void testHashCode() {
        AbstractBaseType baseType1 = new AbstractBaseType("org.opendaylight.yangtools.test", "Test");
        AbstractBaseType baseType2 = new AbstractBaseType("org.opendaylight.yangtools.test", "Test2");
        assertNotEquals(baseType1.hashCode(), baseType2.hashCode());
    }

    @Test
    public void testToString() {
        AbstractBaseType baseType = new AbstractBaseType("org.opendaylight.yangtools.test", "Test");
        assertTrue(baseType.toString().contains("org.opendaylight.yangtools.test.Test"));
        baseType = new AbstractBaseType("", "Test");
        assertTrue(baseType.toString().contains("Test"));
    }

    @Test
    public void testEquals() {
        AbstractBaseType baseType1 = new AbstractBaseType("org.opendaylight.yangtools.test", "Test");
        AbstractBaseType baseType2 = new AbstractBaseType("org.opendaylight.yangtools.test", "Test2");
        AbstractBaseType baseType3 = null;
        AbstractBaseType baseType4 = new AbstractBaseType("org.opendaylight.yangtools.test", "Test");
        AbstractBaseType baseType5 = new AbstractBaseType("org.opendaylight.yangtools.test1", "Test");

        assertFalse(baseType1.equals(baseType2));
        assertFalse(baseType1.equals(baseType3));
        assertTrue(baseType1.equals(baseType4));
        assertFalse(baseType1.equals(baseType5));
        assertFalse(baseType1.equals(null));
    }
}
