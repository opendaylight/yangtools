/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class YangUnescapingTest extends AbstractYangTest {
    @Test
    void stringTestUnescape() throws Exception {
        final var modelContext = assertEffectiveModel("/unescape/string-test.yang");
        assertEquals(1, modelContext.getModules().size());
        final var module = modelContext.getModules().iterator().next();
        assertEquals(Optional.of(
            "  Unescaping examples: \\,\n,\t  \"string enclosed in double quotes\" end\nabc \\\\\\ \\t \\\nnn"),
            module.getDescription());
    }
}
