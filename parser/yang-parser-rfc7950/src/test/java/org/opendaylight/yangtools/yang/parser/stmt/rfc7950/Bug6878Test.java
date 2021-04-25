/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6878Test {

    @Test
    public void testParsingXPathWithYang11Functions() throws Exception {
        final String testLog = parseAndcaptureLog("/rfc7950/bug6878/foo.yang");
        assertFalse(testLog.contains("Could not find function: "));
    }

    @Test
    public void shouldLogInvalidYang10XPath() throws Exception {
        final String testLog = parseAndcaptureLog("/rfc7950/bug6878/foo10-invalid.yang");
        assertThat(testLog, containsString("RFC7950 features required in RFC6020 context to parse expression "));
    }

    @Test
    public void shouldLogInvalidYang10XPath2() throws Exception {
        final String testLog = parseAndcaptureLog("/rfc7950/bug6878/foo10-invalid-2.yang");
        assertThat(testLog, containsString("RFC7950 features required in RFC6020 context to parse expression "));
    }

    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    private static String parseAndcaptureLog(final String yangFile) throws Exception {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (PrintStream out = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(out);
            StmtTestUtils.parseYangSource(yangFile);
        } finally {
            System.setOut(stdout);
        }

        return output.toString();
    }
}
