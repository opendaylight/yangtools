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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration;
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration.Pair;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class EnumerationBuilderImplTest {

    @Rule
    public ExpectedException expException = ExpectedException.none();

    @Test
    public void testCreateNewEnumerationBuilderImpl() {
        final EnumerationBuilderImpl enumBuilderImpl = new EnumerationBuilderImpl("org.opendaylight.yangtools.test", "TestEnum");
        assertNotNull(enumBuilderImpl);
    }

    @Test
    public void testSetterMethods() {
        final EnumerationBuilderImpl enumBuilderImpl = new EnumerationBuilderImpl("org.opendaylight.yangtools.test", "TestEnum");
        enumBuilderImpl.setDescription("default text");
        enumBuilderImpl.setModuleName("test-module-name");
        enumBuilderImpl.setReference("http://tools.ietf.org/html/rfc6020");
        enumBuilderImpl.setSchemaPath(SchemaPath.ROOT.getPathFromRoot());

        Enumeration enumeration = enumBuilderImpl.toInstance(null);
        assertEquals("default text", enumeration.getDescription());
        assertEquals("test-module-name", enumeration.getModuleName());
        assertEquals("http://tools.ietf.org/html/rfc6020", enumeration.getReference());
        assertEquals(SchemaPath.ROOT.getPathFromRoot(), enumeration.getSchemaPath());
    }

    @Test
    public void testAddAnnotation() {
        final EnumerationBuilderImpl enumBuilderImpl = new EnumerationBuilderImpl("org.opendaylight.yangtools.test", "TestEnum");
        enumBuilderImpl.addAnnotation("javax.management", "MXBean");
        final Enumeration enumeration = enumBuilderImpl.toInstance(null);
        assertFalse(enumeration.getAnnotations().isEmpty());
    }

    @Test
    public void testAddValue() {
        final EnumerationBuilderImpl enumBuilderImpl = new EnumerationBuilderImpl("org.opendaylight.yangtools.test", "TestEnum");
        enumBuilderImpl.addValue("Test", 0, "test control value");
        final Enumeration enumeration = enumBuilderImpl.toInstance(null);
        assertFalse(enumeration.getValues().isEmpty());
    }

    @Test
    public void testHashCode() {
        final EnumerationBuilderImpl enumBuilderImpl = new EnumerationBuilderImpl("org.opendaylight.yangtools.test", "TestEnum");
        final EnumerationBuilderImpl enumBuilderImpl2 = new EnumerationBuilderImpl("org.opendaylight.yangtools.test", "TestEnum2");
        assertNotEquals(enumBuilderImpl.hashCode(), enumBuilderImpl2.hashCode());
    }

    @Test
    public void testEquals() {
        final EnumerationBuilderImpl enumBuilderImpl = new EnumerationBuilderImpl("org.opendaylight.yangtools.test", "TestEnum");
        final EnumerationBuilderImpl enumBuilderImpl2 = new EnumerationBuilderImpl("org.opendaylight.yangtools.test", "TestEnum");
        final EnumerationBuilderImpl enumBuilderImpl3 = new EnumerationBuilderImpl("", "TestEnum");
        final EnumerationBuilderImpl enumBuilderImpl4 = new EnumerationBuilderImpl("org.opendaylight.yangtools.test", "");
        final EnumerationBuilderImpl enumBuilderImpl5 = enumBuilderImpl4;

        assertTrue(enumBuilderImpl.equals(enumBuilderImpl2));
        assertFalse(enumBuilderImpl.equals(enumBuilderImpl3));
        assertFalse(enumBuilderImpl.equals(enumBuilderImpl4));
        assertFalse(enumBuilderImpl.equals(null));
        assertFalse(enumBuilderImpl.equals("test"));
        assertTrue(enumBuilderImpl5.equals(enumBuilderImpl4));
    }

    @Test
    public void testToString() {
        final EnumerationBuilderImpl enumBuilderImpl = new EnumerationBuilderImpl("org.opendaylight.yangtools.test", "TestEnum");
        assertEquals("EnumerationBuilderImpl [packageName=org.opendaylight.yangtools.test, name=TestEnum, values=[]]", enumBuilderImpl.toString());
    }

    @Test
    public void testEnumerationImplMethods() {
        final EnumerationBuilderImpl enumBuilderImpl = new EnumerationBuilderImpl("org.opendaylight.yangtools.test", "TestEnum");
        final Enumeration enumeration = enumBuilderImpl.toInstance(null);
        final EnumerationBuilderImpl enumBuilderImpl2 = new EnumerationBuilderImpl("org.opendaylight.yangtools.test", "TestEnum2");
        final Enumeration enumeration2 = enumBuilderImpl2.toInstance(null);
        final EnumerationBuilderImpl enumBuilderImpl3 = new EnumerationBuilderImpl("", "TestEnum2");
        final Enumeration enumeration3 = enumBuilderImpl3.toInstance(null);
        final Enumeration enumeration4 = enumeration3;

        assertNull(enumeration.getParentType());
        assertNotNull(enumeration.getPackageName());
        assertNotNull(enumeration.getName());
        assertNotNull(enumeration.getFullyQualifiedName());
        assertNotNull(enumeration.toFormattedString());
        assertNotEquals(enumeration.hashCode(), enumeration2.hashCode());
        assertNull(enumeration.getComment());
        assertFalse(enumeration.isAbstract());
        assertTrue(enumeration.getImplements().isEmpty());
        assertTrue(enumeration.getEnclosedTypes().isEmpty());
        assertTrue(enumeration.getEnumerations().isEmpty());
        assertTrue(enumeration.getConstantDefinitions().isEmpty());
        assertTrue(enumeration.getMethodDefinitions().isEmpty());
        assertTrue(enumeration.getProperties().isEmpty());
        assertNotNull(enumeration.toString());

        assertFalse(enumeration.equals(enumeration2));
        assertFalse(enumeration.equals(null));
        assertFalse(enumeration.equals("test"));
        assertFalse(enumeration2.equals(enumeration3));
        assertTrue(enumeration3.equals(enumeration4));
    }

    @Test
    public void testEnumPairImplMethods() {
        final EnumerationBuilderImpl enumBuilderImpl = new EnumerationBuilderImpl("org.opendaylight.yangtools.test", "TestEnum");
        enumBuilderImpl.addValue("test", 0, "test value");
        enumBuilderImpl.addValue("test1", 1, "test value 1");
        enumBuilderImpl.addValue(null, null, null);
        enumBuilderImpl.addValue("test", 0, null);
        enumBuilderImpl.addValue("test", 0, "test value");
        enumBuilderImpl.addValue("test", 1, "test value2");
        enumBuilderImpl.addValue("test", null, "test value2");

        final Enumeration enumeration = enumBuilderImpl.toInstance(null);
        final Pair firstEnumPair = enumeration.getValues().get(0);
        final Pair secondEnumPair = enumeration.getValues().get(1);
        final Pair thirdEnumPair = enumeration.getValues().get(2);
        final Pair forthEnumPair = thirdEnumPair;
        final Pair fifthEnumPair = enumeration.getValues().get(3);
        final Pair sixthEnumPair = enumeration.getValues().get(4);
        final Pair seventhEnumPair = enumeration.getValues().get(5);
        final Pair eightEnumPair = enumeration.getValues().get(6);

        assertNotNull(firstEnumPair.getDescription());
        assertNotNull(firstEnumPair.getName());
        assertNull(firstEnumPair.getReference());
        assertNull(firstEnumPair.getStatus());
        assertEquals(0, firstEnumPair.getValue().intValue());

        assertFalse(firstEnumPair.equals(secondEnumPair));
        assertFalse(firstEnumPair.equals(thirdEnumPair));
        assertTrue(thirdEnumPair.equals(forthEnumPair));
        assertFalse(thirdEnumPair.equals(firstEnumPair));
        assertTrue(forthEnumPair.equals(thirdEnumPair));
        assertFalse(firstEnumPair.equals("test"));
        assertFalse(firstEnumPair.equals(null));
        assertTrue(firstEnumPair.equals(fifthEnumPair));
        assertTrue(fifthEnumPair.equals(sixthEnumPair));
        assertFalse(sixthEnumPair.equals(seventhEnumPair));
        assertFalse(eightEnumPair.equals(firstEnumPair));

        assertNotEquals(firstEnumPair.hashCode(), secondEnumPair.hashCode());
        assertNotNull(firstEnumPair.toString());
    }
}
