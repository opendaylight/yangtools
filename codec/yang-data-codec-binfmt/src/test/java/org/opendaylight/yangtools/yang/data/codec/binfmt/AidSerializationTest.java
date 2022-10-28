/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;

@RunWith(Parameterized.class)
public class AidSerializationTest extends AbstractSerializationTest {
    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Collections.singletonList(
            new Object[] { NormalizedNodeStreamVersion.MAGNESIUM, 4, 94, 332, 2376, 716_618, 912_975 });
    }

    @Parameter(1)
    public int emptySize;
    @Parameter(2)
    public int oneSize;
    @Parameter(3)
    public int size29;
    @Parameter(4)
    public int size256;
    @Parameter(5)
    public int size65536;
    @Parameter(6)
    public int twiceSize65536;

    @Test
    public void testEmptyIdentifier() {
        assertSame(fillIdentifier(0), emptySize);
    }

    @Test
    public void testOneIdentifier() {
        assertEquals(fillIdentifier(1), oneSize);
    }

    @Test
    public void test29() {
        assertEquals(fillIdentifier(29), size29);
    }

    @Test
    public void test256() {
        assertEquals(fillIdentifier(256), size256);
    }

    @Test
    public void test65536() {
        final AugmentationIdentifier id = fillIdentifier(65536);
        assertEquals(id, size65536);
        assertEqualsTwice(id, twiceSize65536);
    }

    private static AugmentationIdentifier fillIdentifier(final int size) {
        final AugmentationIdentifier ret = AugmentationIdentifier.create(ImmutableSet.copyOf(generateQNames(size)));
        Assert.assertEquals(size, ret.getPossibleChildNames().size());
        return ret;
    }
}
