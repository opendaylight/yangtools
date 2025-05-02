/*
 * Copyright (c) 2025 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class YT1665Test extends AbstractYangTest {
    @Test
    void testNegativeMinElements() {
        assertSourceException(startsWith("Invalid min-elements argument \"-1\" [at "), "/bugs/YT1665/foo.yang");
    }

    @Test
    void testLeadingZeroMinElements() {
        assertSourceException(startsWith("Invalid min-elements argument \"01\" [at "), "/bugs/YT1665/bar.yang");
    }

    @Test
    void testMalformedMinElements() {
        assertSourceException(startsWith("Invalid min-elements argument \"1a\" [at "), "/bugs/YT1665/baz.yang");
    }

    @Test
    void testOutOfRangeMinElements() {
        final var ex = assertSourceException(startsWith("Invalid min-elements argument \"123456789012345\" [at "),
            "/bugs/YT1665/xyzzy.yang");
        final var cause = assertInstanceOf(NumberFormatException.class, ex.getCause());
        assertEquals("For input string: \"123456789012345\"", cause.getMessage());
    }
}
