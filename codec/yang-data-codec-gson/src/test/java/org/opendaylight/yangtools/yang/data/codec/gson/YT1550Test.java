/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1550Test {
    @Test
    void testMissingKey() throws Exception {
        final var modelContext = YangParserTestUtils.parseYang("""
            module foo {
              namespace foons;
              prefix foo;

              list foo {
                key "one two";
                leaf one {
                  type string {
                    base one;
                  }
                }
                leaf two {
                  type string {
                    base one;
                  }
                }
              }
            }""");

        final var holder = new NormalizedNodeResult();
        final var factory = JSONCodecFactorySupplier.RFC7951.getShared(modelContext);
        try (var writer = ImmutableNormalizedNodeStreamWriter.from(holder)) {
            try (var jsonParser = JsonParserStream.create(writer, factory)) {
                try (var reader = new JsonReader(new StringReader("""
                    {
                      "foo" : {
                        "one" : "one"
                      }
                    }"""))) {

                    final var ex = assertThrows(JsonIOException.class, () -> jsonParser.parse(reader));
                    assertEquals("java.io.IOException: List entry (foons)foo is missing leaf values for [two]",
                        ex.getMessage());
                    final var cause = assertInstanceOf(IOException.class, ex.getCause());
                    assertEquals("List entry (foons)foo is missing leaf values for [two]", cause.getMessage());
                }
            }
        }
    }
}
