/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BooleanSerializationTest extends AbstractSerializationTest {
    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
            new Object[] { NormalizedNodeStreamVersion.SODIUM_SR1, 96 },
            new Object[] { NormalizedNodeStreamVersion.MAGNESIUM,  96 });
    }

    @Parameter(1)
    public int size;

    @Test
    public void testTrue() {
        assertEquals(Boolean.TRUE, size);
    }

    @Test
    public void testFalse() {
        assertEquals(Boolean.FALSE, size);
    }
}
