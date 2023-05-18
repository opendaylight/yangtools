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
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class StringSerializationTest extends AbstractSerializationTest {
    private static final String STR_MEDIUM = "a".repeat(32767);
    private static final String STR_HUGE = "©".repeat(16777216);

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Collections.singletonList(
            new Object[] { NormalizedNodeStreamVersion.POTASSIUM, 96, 99, 32_865, 33_554_532 });
    }

    @Parameter(1)
    public int emptySize;
    @Parameter(2)
    public int oneSize;
    @Parameter(3)
    public int mediumSize;
    @Parameter(4)
    public int hugeSize;

    @Test
    public void testEmptySame() {
        assertSame("", emptySize);
    }

    @Test
    public void testOne() {
        assertEquals("a", oneSize);
    }

    @Test
    public void testMedium() {
        assertEquals(STR_MEDIUM, mediumSize);
    }

    @Test
    public void testHuge() {
        assertEquals(STR_HUGE, hugeSize);
    }
}
