/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

class YT1133Test extends AbstractYangTest {
    @Test
    @SuppressWarnings("checkstyle:regexpSinglelineJava")
    void testAugmentKeys() throws Exception {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final EffectiveModelContext ctx;

        try (PrintStream out = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(out);
            ctx = assertEffectiveModelDir("/bugs/YT1133");
        } finally {
            System.setOut(stdout);
        }

        assertEquals(2, ctx.getModules().size());
        final String log = output.toString();
        assertThat(log, not(containsString("Configuration list (bar)values")));
    }
}
