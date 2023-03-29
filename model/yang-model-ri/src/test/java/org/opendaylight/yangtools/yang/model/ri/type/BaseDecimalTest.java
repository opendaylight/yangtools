/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;

class BaseDecimalTest {
    @Test
    void testImplicitRanges() {
        assertEquals(
            Range.closed(Decimal64.valueOf("-922337203685477580.8"), Decimal64.valueOf("922337203685477580.7")),
            Iterables.getOnlyElement(BaseDecimalType.constraintsForDigits(1).getAllowedRanges().asRanges()));

        assertEquals(Range.closed(
            Decimal64.valueOf("-9.223372036854775808"), Decimal64.valueOf("9.223372036854775807")),
            Iterables.getOnlyElement(BaseDecimalType.constraintsForDigits(18).getAllowedRanges().asRanges()));
    }
}
