/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.regex;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class XSDRegexTest {
    @Test
    public void testEmpty() {
        assertJavaPattern("^$", "");
    }

    @Test
    public void testSimple() {
        assertJavaPattern("^a$", "a");
    }

    @Test
    public void testSimpleAlternatives() {
        assertJavaPattern("^(?:a|b)$", "a|b");
        assertJavaPattern("^(?:a|b|c)$", "a|b|c");
    }

    @Test
    public void testQuantified() {
        assertJavaPattern("^a{1}$", "a{1}");
        assertJavaPattern("^a{1,}$", "a{1,}");
        assertJavaPattern("^a{1,2}$", "a{1,2}");
        assertJavaPattern("^a?$", "a?");
        assertJavaPattern("^a+$", "a+");
        assertJavaPattern("^a*$", "a*");
    }

    @Test
    public void testCharClass() {
        assertJavaPattern("^[ab]$", "[ab]");
    }

    private static void assertJavaPattern(final String expected, final String xsd) {
        assertEquals(expected, XSDRegex.parse(xsd).toJavaPattern().toString());
    }
}
