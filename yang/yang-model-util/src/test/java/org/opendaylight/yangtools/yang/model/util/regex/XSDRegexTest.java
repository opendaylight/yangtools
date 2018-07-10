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
    public void testSimple() {
        assertJavaPattern("^a$", "a");
    }

    private static void assertJavaPattern(final String expected, final String xsd) {
        assertEquals(expected, XSDRegex.parse(xsd).toJavaPattern().toString());
    }
}
