/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.Test;

public class YT893Test extends AbstractYangTest {
    @Test
    public void testCR() {
        assertSourceException(startsWith("Failed to parse node"), "/bugs/YT893/cr.yang");
    }

    @Test
    public void testCRLF() {
        assertEffectiveModel("/bugs/YT893/crlf.yang");
    }

    @Test
    public void testHTAB() {
        assertEffectiveModel("/bugs/YT893/ht.yang");
    }

    @Test
    public void testLF() {
        assertEffectiveModel("/bugs/YT893/lf.yang");
    }
}
