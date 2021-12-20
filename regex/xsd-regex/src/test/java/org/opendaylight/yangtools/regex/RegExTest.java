/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import org.junit.Test;

public class RegExTest {
    @Test
    public void testEmpty() throws ParseException {
        final var regex = RegEx.of("");
        assertTrue(regex.match(""));
        assertFalse(regex.match("a"));
        assertFalse(regex.match("b"));
    }

    @Test
    public void testSingleChar() throws ParseException {
        final var regex = RegEx.of("a");
        assertFalse(regex.match(""));
        assertTrue(regex.match("a"));
        assertFalse(regex.match("b"));
    }

    @Test
    public void testOptionalChar() throws ParseException {
        final var regex = RegEx.of("ab?");
        assertFalse(regex.match(""));
        assertTrue(regex.match("a"));
        assertTrue(regex.match("ab"));
        assertFalse(regex.match("b"));
    }

    @Test
    public void testEither() throws ParseException {
        final var regex = RegEx.of("a|b");
        assertFalse(regex.match(""));
        assertTrue(regex.match("a"));
        assertTrue(regex.match("b"));
        assertFalse(regex.match("ab"));
        assertFalse(regex.match("ba"));
    }

    @Test
    public void testNested() throws ParseException {
        final var regex = RegEx.of("(a)+");
        assertFalse(regex.match(""));
        assertTrue(regex.match("a"));
        assertTrue(regex.match("aa"));
        assertFalse(regex.match("b"));
    }

    @Test
    public void testExact() throws ParseException {
        final var regex = RegEx.of("a{2}");
        assertFalse(regex.match(""));
        assertFalse(regex.match("a"));
        assertTrue(regex.match("aa"));
        assertFalse(regex.match("aaa"));
    }

}
