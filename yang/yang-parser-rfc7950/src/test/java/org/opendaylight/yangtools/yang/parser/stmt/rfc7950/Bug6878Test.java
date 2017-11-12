/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6878Test {

    @Test
    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    public void testParsingXPathWithYang11Functions() throws Exception {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final String testLog;

        System.setOut(new PrintStream(output, true, "UTF-8"));

        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6878/foo.yang");
        assertNotNull(schemaContext);

        testLog = output.toString();
        assertFalse(testLog.contains("Could not find function: "));
        System.setOut(stdout);
    }

    @Test
    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    public void shouldLogInvalidYang10XPath() throws Exception {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final String testLog;

        System.setOut(new PrintStream(output, true, "UTF-8"));

        StmtTestUtils.parseYangSource("/rfc7950/bug6878/foo10-invalid.yang");

        testLog = output.toString();
        assertTrue(testLog.contains("Could not find function: re-match"));
        System.setOut(stdout);
    }

    @Test
    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    public void shouldLogInvalidYang10XPath2() throws Exception {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final String testLog;

        System.setOut(new PrintStream(output, true, "UTF-8"));

        StmtTestUtils.parseYangSource("/rfc7950/bug6878/foo10-invalid-2.yang");

        testLog = output.toString();
        assertTrue(testLog.contains("Could not find function: deref"));
        System.setOut(stdout);
    }
}
