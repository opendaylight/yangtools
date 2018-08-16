/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

public class YT893Test {
    @Test(expected = SomeModifiersUnresolvedException.class)
    public void testCR() throws Exception {
        StmtTestUtils.parseYangSource("/bugs/YT893/cr.yang");
    }

    @Test
    public void testCRLF() throws Exception {
        assertNotNull(StmtTestUtils.parseYangSource("/bugs/YT893/crlf.yang"));
    }

    @Test
    public void testHTAB() throws Exception {
        assertNotNull(StmtTestUtils.parseYangSource("/bugs/YT893/ht.yang"));
    }

    @Test
    public void testLF() throws Exception {
        assertNotNull(StmtTestUtils.parseYangSource("/bugs/YT893/lf.yang"));
    }
}
