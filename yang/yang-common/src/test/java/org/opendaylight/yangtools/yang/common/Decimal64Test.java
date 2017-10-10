/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import org.junit.Test;

public class Decimal64Test {

    @Test(expected = NumberFormatException.class)
    public void testParseEmpty() {
        Decimal64.parse("");
    }

    @Test(expected = NullPointerException.class)
    public void testParseNull() {
        Decimal64.parse(null);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseMinus() {
        Decimal64.parse("-");
    }

    @Test(expected = NumberFormatException.class)
    public void testParsePlus() {
        Decimal64.parse("+");
    }

    @Test(expected = NumberFormatException.class)
    public void testParsePeriod() {
        Decimal64.parse(".");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseTwoPeriods() {
        Decimal64.parse("..");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseTrailingPeriod() {
        Decimal64.parse("0.");
    }

    @Test(expected = NumberFormatException.class)
    public void testParseMultiplePeriods() {
        Decimal64.parse("0.1.");
    }

}
