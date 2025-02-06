/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6878Test extends AbstractYangTest {
    @Test
    void testParsingXPathWithYang11Functions() {
        final var testLog = parseAndcaptureLog("/rfc7950/bug6878/foo.yang");
        assertThat(testLog).doesNotContain("Could not find function: ");
    }

    @Test
    void shouldLogInvalidYang10XPath() {
        final var testLog = parseAndcaptureLog("/rfc7950/bug6878/foo10-invalid.yang");
        assertThat(testLog).contains("RFC7950 features required in RFC6020 context to parse expression ");
    }

    @Test
    void shouldLogInvalidYang10XPath2() {
        final var testLog = parseAndcaptureLog("/rfc7950/bug6878/foo10-invalid-2.yang");
        assertThat(testLog).contains("RFC7950 features required in RFC6020 context to parse expression ");
    }

    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    private static String parseAndcaptureLog(final String yangFile) {
        final var stdout = System.out;
        final var output = new ByteArrayOutputStream();

        try (var out = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(out);
            assertEffectiveModel(yangFile);
        } finally {
            System.setOut(stdout);
        }

        return output.toString();
    }
}
