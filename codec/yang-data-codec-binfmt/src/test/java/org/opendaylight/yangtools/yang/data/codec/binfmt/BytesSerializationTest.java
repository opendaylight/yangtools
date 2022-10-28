/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BytesSerializationTest extends AbstractSerializationTest {
    private static final byte[] BINARY_128 = randomBytes(128);
    private static final byte[] BINARY_384 = randomBytes(384);
    private static final byte[] BINARY_65920 = randomBytes(65920);

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Collections.singletonList(
            new Object[] { NormalizedNodeStreamVersion.MAGNESIUM,   96,  97, 225, 482, 66020 });
    }

    @Parameter(1)
    public int emptySize;
    @Parameter(2)
    public int oneSize;
    @Parameter(3)
    public int size128;
    @Parameter(4)
    public int size384;
    @Parameter(5)
    public int size65920;

    @Test
    public void testEmptyBytes() {
        assertEquals(new byte[0], emptySize);
    }

    @Test
    public void testOne() {
        assertEquals(randomBytes(1), oneSize);
    }

    @Test
    public void test128() {
        assertEquals(BINARY_128, size128);
    }

    @Test
    public void test384() {
        assertEquals(BINARY_384, size384);
    }

    @Test
    public void test65920() {
        assertEquals(BINARY_65920, size65920);
    }

    private static byte[] randomBytes(final int size) {
        final byte[] ret = new byte[size];
        ThreadLocalRandom.current().nextBytes(ret);
        return ret;
    }
}
