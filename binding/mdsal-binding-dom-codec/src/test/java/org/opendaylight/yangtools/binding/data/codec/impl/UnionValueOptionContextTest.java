/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;

public class UnionValueOptionContextTest {
    private static UnionValueOptionContext TEST_UVOC_1;
    private static UnionValueOptionContext TEST_UVOC_2;

    @Before
    public void setUp() throws Exception {
        final Method methodFoo1 = TestDataObject1.class.getMethod("foo");
        final Method methodFoo2 = TestDataObject2.class.getMethod("foo");
        TEST_UVOC_1 = new UnionValueOptionContext(TestUnion.class, TestDataObject1.class, methodFoo1,
            ValueTypeCodec.EMPTY_CODEC);
        TEST_UVOC_2 = new UnionValueOptionContext(TestUnion.class, TestDataObject2.class, methodFoo2,
            ValueTypeCodec.EMPTY_CODEC);
    }

    @Test
    public void hashCodeTest() throws Exception {
        final Method methodFoo1 = TestDataObject1.class.getMethod("foo");
        final UnionValueOptionContext test_uvoc = new UnionValueOptionContext(TestUnion.class, TestDataObject1.class,
            methodFoo1, ValueTypeCodec.EMPTY_CODEC);

        assertEquals("HashCode", test_uvoc.hashCode(), TEST_UVOC_1.hashCode());
        assertNotEquals("HashCode", TEST_UVOC_1.hashCode(), TEST_UVOC_2.hashCode());
    }

    @Test
    public void equalsTest() throws Exception {
        final Method methodFoo1 = TestDataObject1.class.getMethod("foo");
        final UnionValueOptionContext test_uvoc = new UnionValueOptionContext(TestUnion.class, TestDataObject1.class,
            methodFoo1, ValueTypeCodec.EMPTY_CODEC);

        assertTrue("Equals", TEST_UVOC_1.equals(test_uvoc));
        assertFalse("Not equals", TEST_UVOC_1.equals(TEST_UVOC_2));
    }

    protected static final class TestDataObject1 {
        public void foo() {}
    }

    protected static final class TestDataObject2 {
        public void foo() {}
    }

    public static final class TestUnion {
        public TestUnion(final TestDataObject1 arg) { }
        public TestUnion(final TestDataObject2 arg) { }
    }

}
