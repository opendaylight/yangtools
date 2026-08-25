/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug5531Test {
    @Test
    void test() throws Exception {
        final var schema = YangParserTestUtils.parseYang("""
            module foo {
              namespace "foo";
              prefix foo;

              revision 2015-01-01 {
                description "test";
              }
            }""");

        assertNotNull(schema);
        assertNotNull(schema.getModules());
        assertEquals(1, schema.getModules().size());

        final var byteArrayOutputStream = new ByteArrayOutputStream();
        final var bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

        // write small module of size less than 8kB
        for (var module : schema.getModuleStatements().values()) {
            YinExportUtils.writeModuleAsYinText(module, bufferedOutputStream);
        }

        // if all changes were flushed then following conditions are satisfied
        assertThat(byteArrayOutputStream.toString(StandardCharsets.UTF_8))
            .isNotEmpty()
            .contains("<module")
            .contains("</module>");
    }
}
