/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

@RunWith(Parameterized.class)
public class UintSerializationTest extends AbstractSerializationTest {
    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Collections.singletonList(
            new Object[] { NormalizedNodeStreamVersion.MAGNESIUM });
    }

    @Test
    public void testUint8() {
        assertSame(Uint8.ZERO, 96);
        assertSame(Uint8.ONE, 97);
    }

    @Test
    public void testUint16() {
        assertSame(Uint16.ZERO, 96);
        assertSame(Uint16.ONE, 98);
    }

    @Test
    public void testUint32() {
        assertSame(Uint32.ZERO, 96);
        assertSame(Uint32.ONE, 98);
        assertEquals(Uint32.MAX_VALUE, 100);
    }

    @Test
    public void testUint64() {
        assertSame(Uint64.ZERO, 96);
        assertSame(Uint64.ONE, 100);
        assertEquals(Uint64.MAX_VALUE, 104);
    }

}
