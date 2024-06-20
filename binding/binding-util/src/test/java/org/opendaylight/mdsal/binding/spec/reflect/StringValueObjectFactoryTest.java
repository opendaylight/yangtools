/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spec.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class StringValueObjectFactoryTest {

    @Test
    public void createTest() throws Exception {
        final StringValueObjectFactory<?> stringValueObjectFactory =
                StringValueObjectFactory.create(TestClass.class, "testTemplate");
        assertNotNull(stringValueObjectFactory);
        assertEquals("testTemplate", stringValueObjectFactory.getTemplate().toString());
    }

    @Test
    public void newInstanceTest() throws Exception {
        final StringValueObjectFactory<?> instance = StringValueObjectFactory.create(TestClass.class, "testTemplate");

        assertEquals("instanceTest", instance.newInstance("instanceTest").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTestNoConstructor() throws Exception {
        StringValueObjectFactory.create(Object.class, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTestNoField() throws Exception {
        StringValueObjectFactory.create(String.class, "");
    }

    public static final class TestClass {

        @SuppressWarnings("checkstyle:memberName")
        private final String _value;

        public TestClass(final TestClass parent) {
            this._value = parent._value;
        }

        public TestClass(final String value) {
            this._value = value;
        }

        @Override
        public String toString() {
            return this._value;
        }
    }
}
