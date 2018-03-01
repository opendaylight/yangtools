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

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;

public class BigDecimalRangeGeneratorTest {

    @Test
    @Deprecated
    public void convertTest() {
        BigDecimalRangeGenerator generator = new BigDecimalRangeGenerator();
        assertEquals(BigDecimal.valueOf(1L), generator.convert(1L));
        assertEquals(BigDecimal.valueOf(1), generator.convert(new BigInteger("1")));
        assertEquals(BigDecimal.valueOf(1), generator.convert(new Byte("1")));
        assertEquals(BigDecimal.valueOf(1), generator.convert(new Short("1")));
        assertEquals(BigDecimal.valueOf(1), generator.convert(new Integer("1")));
        assertNotNull(generator.format(BigDecimal.TEN));
    }
}