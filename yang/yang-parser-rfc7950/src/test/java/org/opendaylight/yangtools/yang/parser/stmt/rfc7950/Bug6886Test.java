/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6886Test {

    @Test
    public void yang11UnquotedStrTest() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6886/yang11/foo.yang");
            fail("Test should fail due to invalid yang");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage()
                    .startsWith("YANG 1.1: unquoted string (illegalchars\"test1) contains illegal characters"));
        }
    }

    @Test
    public void yang11UnquotedStrTest2() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6886/yang11/foo2.yang");
            fail("Test should fail due to invalid yang");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage()
                    .startsWith("YANG 1.1: unquoted string (illegalchars'test2) contains illegal characters"));
        }
    }

    @Test
    public void yang11DoubleQuotedStrTest() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6886/yang11/foo3.yang");
            fail("Test should fail due to invalid yang");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith("YANG 1.1: illegal double quoted string "
                    + "(i\\\\\\\\l\\nl\\te\\\"\\galcharstest1). In double quoted string the backslash must be followed "
                    + "by one of the following character [n,t,\",\\], but was 'g'."));
        }
    }

    @Test
    public void yang10UnquotedStrTest() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6886/yang10/foo.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void yang10UnquotedStrTest2() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6886/yang10/foo2.yang");
        assertNotNull(schemaContext);
    }

    @Test
    public void yang10DoubleQuotedStrTest() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6886/yang10/foo3.yang");
        assertNotNull(schemaContext);
    }
}
