/*
 * Copyright (c) 2016 Intel Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug6112Test {
    private static EffectiveModelContext schemaContext;

    @BeforeAll
    static void initialization() {
        schemaContext = YangParserTestUtils.parseYang("""
            module union-with-identityref {
              yang-version 1;
              namespace "union:identityref:test";
              prefix "unionidentityreftest";
              description "test union with identityref";
              revision "2016-07-12";

              identity ident-base;

              identity ident-one {
                base ident-base;
              }

              typedef union-type {
                type union {
                  type uint8;
                  type identityref {
                    base ident-base;
                  }
                }
              }

              container root {
                leaf leaf-value {
                  type union-type;
                }
              }
            }""");
    }

    @AfterAll
    static void cleanup() {
        schemaContext = null;
    }

    private static NormalizedNode readJson(final String jsonPath) throws IOException, URISyntaxException {
        final var inputJson = loadTextFile(jsonPath);

        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        return result.getResult().data();
    }

    @Test
    void testUnionIdentityrefInput() throws IOException, URISyntaxException {
        final var transformedInput = readJson("/bug-6112/json/data-identityref.json");
        final var root = assertInstanceOf(ContainerNode.class, transformedInput);
        final var leafValue = root.childByArg(NodeIdentifier.create(
            QName.create("union:identityref:test", "2016-07-12", "leaf-value")));

        assertNotNull(leafValue);
        assertEquals(QName.create("union:identityref:test", "2016-07-12", "ident-one"),
            assertInstanceOf(QName.class, leafValue.body()));
    }

    @Test
    void testUnionUint8Input() throws IOException, URISyntaxException {
        final var transformedInput = readJson("/bug-6112/json/data-uint8.json");
        final var root = assertInstanceOf(ContainerNode.class, transformedInput);
        final var leafValue = root.childByArg(NodeIdentifier.create(
            QName.create("union:identityref:test", "2016-07-12", "leaf-value")));

        assertNotNull(leafValue);
        assertEquals(Uint8.valueOf(1), leafValue.body());
    }
}
