/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Bits;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug4501Test {

    private static EffectiveModelContext schemaContext;

    @BeforeAll
    static void initialization() {
        schemaContext = YangParserTestUtils.parseYang("""
            module foo {
              namespace "foo";
              prefix foo;
              yang-version 1;

              list hop {
                leaf address {
                  type string;
                }
                leaf lrs-bits {
                  type bits {
                    bit lookup {
                      description "Lookup bit.";
                    }
                    bit rloc-probe {
                      description "RLOC-probe bit.";
                    }
                    bit strict {
                      description "Strict bit.";
                    }
                  }
                  description "Flag bits per hop.";
                }
              }
            }""");
    }

    @AfterAll
    static void cleanup() {
        schemaContext = null;
    }

    @Test
    void testCorrectInput() throws IOException, URISyntaxException {
        final var inputJson = loadTextFile("/bug-4501/json/foo-correct.json");
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));

        final var hop = assertInstanceOf(UnkeyedListNode.class, result.getResult().data());
        final var lrsBits = hop.childAt(0).getChildByArg(
                NodeIdentifier.create(QName.create("foo", "lrs-bits")));

        final Bits expected = Bits.of(Map.of("lookup", 0, "rloc-probe", 1, "strict", 2), new byte[]{7});
        assertEquals(expected, lrsBits.body());
    }

    @Test
    void testIncorrectInput() throws IOException, URISyntaxException {
        final var inputJson = loadTextFile("/bug-4501/json/foo-incorrect.json");
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));

        final var reader = new JsonReader(new StringReader(inputJson));
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> jsonParser.parse(reader));
        assertEquals("Node '(foo)lrs-bits' has already set its value to 'lookup'", ex.getMessage());
    }
}