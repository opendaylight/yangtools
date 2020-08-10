/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AbstractCharTokenTest {
    @Test
    public void testThreeOneValues() {
        assertValue31(0, 0, 0);
        assertValue31(255, 255, 0xFFFF);
        assertValue31(65535, 255, 0xFFFFFF);
        assertValue31(131071, 128, 0x1FFFF80);
        assertValue31(16777215, 255, 0xFFFFFFFF);
    }

    private static void assertValue31(final int line, final int charPositionInLine, final int expValue) {
        final int value = AbstractCharToken.value31(line, charPositionInLine);
        assertEquals(expValue, value);
        assertEquals(line, AbstractCharToken.getLine(value));
        assertEquals(charPositionInLine, AbstractCharToken.getCharPositionInLine(value));
    }
}
