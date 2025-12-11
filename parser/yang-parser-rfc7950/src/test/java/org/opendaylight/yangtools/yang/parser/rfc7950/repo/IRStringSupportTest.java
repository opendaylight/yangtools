/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

class IRStringSupportTest {
    @Test
    void stringTestUnescape() throws Exception {
        final var schemaContext = StmtTestUtils.parseYangSources(Path.of(IRStringSupportTest.class
            .getResource("/unescape/string-test.yang").toURI()).toFile());
        assertNotNull(schemaContext);
        assertEquals(1, schemaContext.getModules().size());
        final var module = schemaContext.getModules().iterator().next();
        assertEquals(Optional.of("  Unescaping examples: \\,\n,\t  \"string enclosed in double quotes\" end\n"
            + "abc \\\\\\ \\t \\\nnn"), module.getDescription());
    }
}
