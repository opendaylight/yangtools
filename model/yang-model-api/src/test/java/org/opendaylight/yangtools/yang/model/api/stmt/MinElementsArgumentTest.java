/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.math.BigInteger;
import java.text.ParseException;
import org.junit.jupiter.api.Test;

class MinElementsArgumentTest {
    @Test
    void emptyThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MinElementsArgument.parse(""));
        assertEquals("empty min-value-arg", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void negativeThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MinElementsArgument.parse("-1"));
        assertEquals("'-' is not a valid non-zero-digit", ex.getMessage());
        assertEquals(0, ex.getErrorOffset());
    }

    @Test
    void badThrows() {
        final var ex = assertThrowsExactly(ParseException.class, () -> MinElementsArgument.parse("1a"));
        assertEquals("'a' is not a valid DIGIT", ex.getMessage());
        assertEquals(1, ex.getErrorOffset());
    }

    @Test
    void intParses() throws Exception {
        final var arg = assertInstanceOf(MinElementsArgument32.class, MinElementsArgument.parse("2147483647"));
        assertEquals("2147483647", arg.toString());
        assertEquals(2147483646, arg.lowerInt());
        assertEquals(2147483646L, arg.lowerLong());
        assertEquals(BigInteger.valueOf(2147483646), arg.lowerBig());
    }

    @Test
    void longParses() throws Exception {
        final var arg = assertInstanceOf(MinElementsArgument64.class, MinElementsArgument.parse("9223372036854775807"));
        assertEquals("9223372036854775807", arg.toString());
        assertEquals(2147483647, arg.lowerInt());
        assertEquals(9223372036854775806L, arg.lowerLong());
        assertEquals(BigInteger.valueOf(9223372036854775806L), arg.lowerBig());
    }

    @Test
    void bigParses() throws Exception {
        final var arg = assertInstanceOf(MinElementsArgumentBig.class,
            MinElementsArgument.parse("92233720368547758070"));
        assertEquals("92233720368547758070", arg.toString());
        assertEquals(Integer.MAX_VALUE, arg.lowerInt());
        assertEquals(Long.MAX_VALUE, arg.lowerLong());
        assertEquals(new BigInteger("92233720368547758069"), arg.lowerBig());
    }
}
