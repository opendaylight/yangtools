/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;

public class BindingMappingTest {

    @Test
    public void basicTest() {
        assertTrue(BindingMapping.getRootPackageName(QName.create(QNameModule.create(URI.create("test:URI"),
                Revision.of("2017-10-26")), "test")).contains("test.uri"));
        assertNull(BindingMapping.normalizePackageName(null));
        assertTrue(BindingMapping.normalizePackageName("1testpublic").contains("_1testpublic"));
        assertTrue(BindingMapping.getMethodName(QName.create("testNS", "testLocalName")).equals("testLocalName"));
        assertTrue(BindingMapping.getMethodName("TestYangIdentifier").equals("testYangIdentifier"));
        assertTrue(BindingMapping.getClassName(QName.create("testNS", "testClass")).equals("TestClass"));
        assertTrue(BindingMapping.getClassName("testClass").equals("TestClass"));
        assertTrue(BindingMapping.getGetterSuffix(QName.create("test", "test")).equals("Test"));
        assertTrue(BindingMapping.getGetterSuffix(QName.create("test", "class")).equals("XmlClass"));
        assertTrue(BindingMapping.getPropertyName("Test").equals("test"));
        assertTrue(BindingMapping.getPropertyName("test").equals("test"));
        assertTrue(BindingMapping.getPropertyName("Class").equals("xmlClass"));
        assertEquals("_5", BindingMapping.getPropertyName("5"));
        assertEquals("", BindingMapping.getPropertyName(""));
        assertEquals("", BindingMapping.getClassName(""));
    }

    @Test(expected = UnsupportedOperationException.class)
    @SuppressWarnings({ "checkstyle:illegalThrows", "checkstyle:avoidHidingCauseException" })
    public void privateConstructTest() throws Throwable {
        final Constructor<BindingMapping> bindingMappingConstructor = BindingMapping.class.getDeclaredConstructor();
        assertFalse(bindingMappingConstructor.isAccessible());

        bindingMappingConstructor.setAccessible(true);
        try {
            bindingMappingConstructor.newInstance();
            fail("Expected exception for calling private constructor");
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
