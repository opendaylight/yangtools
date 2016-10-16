/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.xsd.regex;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RegularExpressionTest {

    @Test
    public void testEmptyExpression() {
        final RegularExpression regex = RegularExpression.parse("");

        assertEquals("^$", regex.toPattern().toString());
        assertEquals("", regex.toString());
    }

    @Test
    public void testCaretExpression() {
        final RegularExpression regex = RegularExpression.parse("^");

        assertEquals("^\\^$", regex.toPattern().toString());
        assertEquals("^", regex.toString());
    }

    @Test
    public void testDollarExpression() {
        final RegularExpression regex = RegularExpression.parse("$");

        assertEquals("^\\$$", regex.toPattern().toString());
        assertEquals("$", regex.toString());
    }

}
