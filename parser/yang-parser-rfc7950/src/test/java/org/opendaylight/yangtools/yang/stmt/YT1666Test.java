/*
 * Copyright (c) 2025 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import org.junit.jupiter.api.Test;

class YT1666Test extends AbstractYangTest {
    @Test
    void testNegativeMaxElements() {
        assertSourceExceptionMessage("/bugs/YT1666/foo.yang")
            .startsWith("Invalid max-elements argument \"-1\" at offset 0: '-' is not a valid non-zero-digit [at ");
    }

    @Test
    void testLeadingZeroMaxElements() {
        assertSourceExceptionMessage("/bugs/YT1666/bar.yang")
            .startsWith("Invalid max-elements argument \"01\" at offset 0: '0' is not a valid non-zero-digit [at ");
    }

    @Test
    void testMalformedMaxElements() {
        assertSourceExceptionMessage("/bugs/YT1666/baz.yang")
            .startsWith("Invalid max-elements argument \"1a\" at offset 1: 'a' is not a valid DIGIT [at ");
    }

    @Test
    void testZeroMaxElements() {
        assertSourceExceptionMessage("/bugs/YT1666/xyzzy.yang")
            .startsWith("Invalid max-elements argument \"0\" at offset 0: '0' is not a valid non-zero-digit [at ");
    }
}
