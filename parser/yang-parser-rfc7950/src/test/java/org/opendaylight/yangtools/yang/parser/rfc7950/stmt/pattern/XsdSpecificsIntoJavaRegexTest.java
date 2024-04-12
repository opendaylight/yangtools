/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class XsdSpecificsIntoJavaRegexTest {

    @Test
    void characterSubtractionGroupTest() {
        final var javaRegexFromXSD = RegexUtils.getJavaRegexFromXSD("[a-z-[c]]");
        assertEquals("^(?:[a-z&&[^c]])$", javaRegexFromXSD);
        final var pattern = Pattern.compile(javaRegexFromXSD);
        assertTrue(pattern.matcher("a").find());
        assertFalse(pattern.matcher("c").find());
    }

    @ParameterizedTest
    @ValueSource(chars = {'c', 'C', 'd', 'D', 'i', 'I', 's', 'S', 'w', 'W'})
    void multiCharEscapeTest(final char ch) {
        final var javaRegexFromXSD = RegexUtils.getJavaRegexFromXSD("\\" + ch);
        assertNotNull(Pattern.compile(javaRegexFromXSD));
    }
}
