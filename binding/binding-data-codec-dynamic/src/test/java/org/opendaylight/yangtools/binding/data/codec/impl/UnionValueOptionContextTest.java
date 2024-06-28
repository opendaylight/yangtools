/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.LowestLevel1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.LowestLevel2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.yangtools.test.union.rev150121.UnionTestType;

public class UnionValueOptionContextTest {
    private static UnionValueOptionContext TEST_UVOC_1;
    private static UnionValueOptionContext TEST_UVOC_2;

    @BeforeClass
    public static void beforeClass() throws Exception {
        TEST_UVOC_1 = new UnionValueOptionContext(UnionTestType.class, LowestLevel1.class,
            UnionTestType.class.getMethod("getLowestLevel1"), BindingCodecContext.NOOP_CODEC);
        TEST_UVOC_2 = new UnionValueOptionContext(UnionTestType.class, LowestLevel2.class,
            UnionTestType.class.getMethod("getLowestLevel2"), BindingCodecContext.NOOP_CODEC);
    }

    @Test
    public void hashCodeTest() throws Exception {
        final UnionValueOptionContext test_uvoc = new UnionValueOptionContext(UnionTestType.class, LowestLevel1.class,
            UnionTestType.class.getMethod("getLowestLevel1"), BindingCodecContext.NOOP_CODEC);

        assertEquals("HashCode", test_uvoc.hashCode(), TEST_UVOC_1.hashCode());
        assertNotEquals("HashCode", TEST_UVOC_1.hashCode(), TEST_UVOC_2.hashCode());
    }

    @Test
    public void equalsTest() throws Exception {
        final UnionValueOptionContext test_uvoc = new UnionValueOptionContext(UnionTestType.class, LowestLevel1.class,
            UnionTestType.class.getMethod("getLowestLevel1"), BindingCodecContext.NOOP_CODEC);

        assertEquals(TEST_UVOC_1, test_uvoc);
        assertNotEquals(TEST_UVOC_1, TEST_UVOC_2);
    }
}
