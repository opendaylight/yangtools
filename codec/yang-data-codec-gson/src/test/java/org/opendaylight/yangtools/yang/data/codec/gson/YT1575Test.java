/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1575Test {
    @Test
    void testMapEntryRoot() throws Exception {
        final var modelContext = YangParserTestUtils.parseYang("""
            module yt1575 {
              namespace yt1575;
              prefix yt1575;

              container testContainer {
                list testList {
                  key "name";
                  leaf name {
                    type string;
                  }
                }
              }
            }""");

        final var testContainer = QName.create("yt1575", "testContainer");
        final var testList = QName.create("yt1575", "testList");
        final var name = QName.create("yt1575", "name");

        final var writer = new StringWriter();
        final var jsonStream = JSONNormalizedNodeStreamWriter.createNestedWriter(
            JSONCodecFactorySupplier.RFC7951.getShared(modelContext),
            SchemaInferenceStack.of(modelContext, Absolute.of(testContainer, testList)).toInference(), null,
            JsonWriterFactory.createJsonWriter(writer, 2));
        try (var nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream)) {
            nodeWriter.write(ImmutableNodes.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(testList, name, "testName"))
                .withChild(ImmutableNodes.leafNode(name, "testName"))
                .build());
        }

        assertEquals("""
            {
              "name": "testName"
            }""", writer.toString());
    }
}
