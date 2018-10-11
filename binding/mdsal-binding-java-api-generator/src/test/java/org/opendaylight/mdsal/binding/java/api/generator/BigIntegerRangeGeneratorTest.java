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

public class BigIntegerRangeGeneratorTest {
    @Test
    public void basicTest() throws Exception {
        BigIntegerRangeGenerator generator = new BigIntegerRangeGenerator();
        assertEquals(BigInteger.valueOf(1L), generator.convert(1L));
        assertNotNull(generator.format(BigInteger.ONE));
        assertNotNull(generator.format(BigInteger.TEN));
        assertNotNull(generator.format(BigInteger.valueOf(2L)));
    }
}