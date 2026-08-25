/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug6856Test {
    @Test
    void testImplicitInputAndOutputInRpc() throws Exception {
        final var modelContext = YangParserTestUtils.parseYang("""
            module foo {
              namespace foo;
              prefix foo;
              revision 2017-02-28;

              rpc foo-rpc {}
            }""");
        final var fooModule = modelContext.findModule("foo", Revision.of("2017-02-28")).orElseThrow();
        assertNotNull(fooModule);

        final var byteArrayOutputStream = new ByteArrayOutputStream();
        final var bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        YinExportUtils.writeModuleAsYinText(fooModule.asEffectiveStatement(), bufferedOutputStream);
        assertThat(byteArrayOutputStream.toString(StandardCharsets.UTF_8))
            .isNotEmpty()
            .doesNotContain("<input>", "<output>");
    }

    @Test
    void testExplicitInputAndOutputInRpc() throws Exception {
        final var modelContext = YangParserTestUtils.parseYang("""
            module bar {
              namespace bar;
              prefix bar;
              revision 2017-02-28;

              rpc bar-rpc {
                input {
                  leaf input-leaf {
                    type string;
                  }
                }
                output {
                  leaf output-leaf {
                    type string;
                  }
                }
              }
            }""");
        final var barModule = modelContext.findModule("bar", Revision.of("2017-02-28")).orElseThrow();
        assertNotNull(barModule);

        final var byteArrayOutputStream = new ByteArrayOutputStream();
        final var bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        YinExportUtils.writeModuleAsYinText(barModule.asEffectiveStatement(), bufferedOutputStream);
        assertThat(byteArrayOutputStream.toString(StandardCharsets.UTF_8))
            .isNotEmpty()
            .contains("<input>", "<output>");
    }
}
