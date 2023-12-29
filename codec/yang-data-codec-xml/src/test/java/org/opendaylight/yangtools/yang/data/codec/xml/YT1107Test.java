/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1107Test {
    private static final QName PARENT = QName.create("yt1107", "parent");
    private static final QName ADMIN = QName.create(PARENT, "admin");
    private static final QName NAME = QName.create(PARENT, "name");
    private static final QName USER = QName.create(PARENT, "user");

    @Test
    void testInterleavingLists() throws Exception {
        final var schemaContext = YangParserTestUtils.parseYang("""
            module yt1107 {
              namespace "yt1107";
              prefix "yt1107";

              container parent {
                config true;
                list user {
                  key name;
                  leaf name {
                    type string;
                  }
                }
                list admin {
                  key name;
                  leaf name {
                    type string;
                  }
                }
              }
            }""");
        final var reader = UntrustedXML.createXMLStreamReader(new StringReader("""
            <parent xmlns="yt1107">
                <user>
                    <name>Freud</name>
                </user>
                <admin>
                    <name>John</name>
                </admin>
                <user>
                <name>Bob</name>
                </user>
            </parent>"""));
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, Inference.ofDataTreePath(schemaContext, PARENT));
        xmlParser.parse(reader);

        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(PARENT))
            .withChild(Builders.mapBuilder()
                .withNodeIdentifier(new NodeIdentifier(ADMIN))
                .withChild(Builders.mapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(ADMIN, NAME, "John"))
                    .withChild(ImmutableNodes.leafNode(NAME, "John"))
                    .build())
                .build())
            .withChild(Builders.mapBuilder()
                .withNodeIdentifier(new NodeIdentifier(USER))
                .withChild(Builders.mapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(USER, NAME, "Freud"))
                    .withChild(ImmutableNodes.leafNode(NAME, "Freud"))
                    .build())
                .withChild(Builders.mapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(USER, NAME, "Bob"))
                    .withChild(ImmutableNodes.leafNode(NAME, "Bob"))
                    .build())
                .build())
            .build(), result.getResult().data());
    }
}
