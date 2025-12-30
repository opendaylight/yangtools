/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.jupiter.api.Test;

class YT893Test extends AbstractYangTest {
    @Test
    void testCR() {
        assertSourceException(startsWith(
            "'a\rb' is not a valid unique tag on position 2: '\r' is not valid as a character in identifier [at "),
            "/bugs/YT893/cr.yang");
    }

    @Test
    void testCRLF() {
        assertEffectiveModel("/bugs/YT893/crlf.yang");
    }

    @Test
    void testHTAB() {
        assertEffectiveModel("/bugs/YT893/ht.yang");
    }

    @Test
    void testLF() {
        assertEffectiveModel("/bugs/YT893/lf.yang");
    }
}
