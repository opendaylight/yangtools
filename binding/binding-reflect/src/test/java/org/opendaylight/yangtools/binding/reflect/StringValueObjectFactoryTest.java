/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.reflect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

// public for TestClass visibility
public class StringValueObjectFactoryTest {
    @Test
    void createTest() {
        final var stringValueObjectFactory = StringValueObjectFactory.create(TestClass.class, "testTemplate");
        assertNotNull(stringValueObjectFactory);
        assertEquals("testTemplate", stringValueObjectFactory.getTemplate().toString());
    }

    @Test
    void newInstanceTest() {
        final var instance = StringValueObjectFactory.create(TestClass.class, "testTemplate");

        assertEquals("instanceTest", instance.newInstance("instanceTest").toString());
    }

    @Test
    void createTestNoConstructor() throws Exception {
        final var iae = assertThrows(IllegalArgumentException.class,
            () -> StringValueObjectFactory.create(Object.class, ""));
        assertEquals("class java.lang.Object does not have a String constructor", iae.getMessage());
    }

    @Test
    void createTestNoField() throws Exception {
        final var iae = assertThrows(IllegalArgumentException.class,
            () -> StringValueObjectFactory.create(String.class, ""));
        assertEquals("class java.lang.String nor its superclasses define required internal field _value",
            iae.getMessage());
    }

    public static final class TestClass {
        @SuppressWarnings("checkstyle:memberName")
        private final String _value;

        public TestClass(final TestClass parent) {
            _value = parent._value;
        }

        public TestClass(final String value) {
            _value = value;
        }

        @Override
        public String toString() {
            return _value;
        }
    }
}
