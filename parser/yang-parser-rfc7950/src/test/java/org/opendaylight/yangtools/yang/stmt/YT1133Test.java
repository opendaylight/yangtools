/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

class YT1133Test extends AbstractYangTest {
    @Test
    void testAugmentKeys() {
        final EffectiveModelContext modelContext;

        @SuppressWarnings("checkstyle:regexpSinglelineJava")
        final var origOut = System.out;
        final var baos = new ByteArrayOutputStream();
        try (var out = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            System.setOut(out);
            try {
                modelContext = assertEffectiveModelDir("/bugs/YT1133");
            } finally {
                System.setOut(origOut);
            }
        }

        assertEquals(2, modelContext.getModuleStatements().size());
        assertThat(baos.toString(StandardCharsets.UTF_8)).doesNotContain("Configuration list (bar)values");
    }
}
