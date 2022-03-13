/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;

public class Decimal64RangeGeneratorTest {
    @Test
    @Deprecated
    public void convertTest() {
        Decimal64RangeGenerator generator = new Decimal64RangeGenerator();
        Decimal64 one = Decimal64.valueOf(1, 1);
        assertEquals(one, generator.convert(1L));
        assertEquals(one, generator.convert(new BigInteger("1")));
        assertEquals(one, generator.convert(Byte.valueOf("1")));
        assertEquals(one, generator.convert(Short.valueOf("1")));
        assertEquals(one, generator.convert(Integer.valueOf("1")));
        assertNotNull(generator.format(Decimal64.valueOf("10")));
    }
}
