/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug7246Test {
    private static final String NS = "my-namespace";

    @Test
    void test() throws Exception {
        final var schemaContext = YangParserTestUtils.parseYang("""
            module rpc-test {
              namespace my-namespace;
              prefix p;
              feature my-name;
              identity my-name;
              extension my-name;

              typedef my-name {
                type string;
              }

              grouping my-name {
                leaf my-name {
                  type my-name;
                }
              }

              rpc my-name {
                input {
                  container my-name {
                    leaf my-name {
                      type my-name;
                    }
                  }
                }
                output {
                  container my-name {
                    leaf my-name {
                      type my-name;
                    }
                  }
                }
              }
            }""");
        final var inputStructure = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(qN("my-name")))
            .withChild(ImmutableNodes.leafNode(new NodeIdentifier(qN("my-name")), "my-value"))
            .build();
        final var jsonOutput = normalizedNodeToJsonStreamTransformation(schemaContext, inputStructure,
            qN("my-name"), qN("input"));

        assertEquals(JsonParser.parseReader(new FileReader(
            new File(getClass().getResource("/bug7246/json/expected-output.json").toURI()), StandardCharsets.UTF_8)),
            JsonParser.parseString(jsonOutput));
    }

    private static QName qN(final String localName) {
        return QName.create(NS, localName);
    }

    private static String normalizedNodeToJsonStreamTransformation(final EffectiveModelContext schemaContext,
            final NormalizedNode inputStructure, final QName... path) throws Exception {
        final var writer = new StringWriter();
        final var jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
                JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext), Absolute.of(path),
                XMLNamespace.of(NS), JsonWriterFactory.createJsonWriter(writer, 2));
        try (var nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream)) {
            nodeWriter.write(inputStructure);
        }
        return writer.toString();
    }
}
