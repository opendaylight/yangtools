/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;
import javax.xml.XMLConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1543Test {
    private static final YangInstanceIdentifier IID = YangInstanceIdentifier.builder()
        .node(QName.create("barns", "bar"))
        .nodeWithKey(QName.create("barns", "bar"), QName.create("barns", "key"),
            YangInstanceIdentifier.of(QName.create("bazns", "baz")))
        .build();

    private static EffectiveModelContext MODEL_CONTEXT;

    @BeforeAll
    static void beforeAll() {
        MODEL_CONTEXT = YangParserTestUtils.parseYang("""
            module foo {
              namespace foons;
              prefix fo;

              container foo {
                leaf leaf {
                  type instance-identifier;
                }
              }
            }""", """
            module bar {
              namespace barns;
              prefix br;

              list bar {
                key key;
                leaf key {
                  type instance-identifier;
                }
              }
            }""", """
            module baz {
              namespace bazns;
              prefix bz;

              container baz;
            }""");
    }

    @Test
    void nestedInstanceIdentifierInDocument() throws Exception {
        final var stringWriter = new StringWriter();
        try (var xmlWriter = XMLStreamNormalizedNodeStreamWriter.create(
                TestFactories.DEFAULT_OUTPUT_FACTORY.createXMLStreamWriter(stringWriter), MODEL_CONTEXT, true)) {
            try (var nnWriter = NormalizedNodeWriter.forStreamWriter(xmlWriter)) {
                // Contrived: we have a document for foo's 'foo' container, with 'leaf' pointing to an instance of bar's
                //            'bar' list item, whose key points to baz's 'baz' container.
                nnWriter.write(ImmutableNodes.newContainerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(QName.create("foons", "foo")))
                    .withChild(ImmutableNodes.leafNode(QName.create("foons", "leaf"), IID))
                    .build());
            }
        }

        assertEquals("""
            <foo xmlns="foons"><leaf xmlns:br="barns" xmlns:bz="bazns">/br:bar[br:key='/bz:baz']</leaf></foo>""",
            stringWriter.toString());
    }

    @Test
    void nestedInstanceIdentifierThroughCodec() throws Exception {
        final var stringWriter = new StringWriter();
        final var xmlWriter = TestFactories.DEFAULT_OUTPUT_FACTORY.createXMLStreamWriter(stringWriter);

        xmlWriter.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX, "foo", "foons");
        xmlWriter.writeDefaultNamespace("foons");
        xmlWriter.writeStartElement("leaf");
        XmlCodecFactory.create(MODEL_CONTEXT, true).instanceIdentifierCodec().writeValue(xmlWriter, IID);
        xmlWriter.writeEndElement();
        xmlWriter.writeEndElement();
        xmlWriter.close();

        assertEquals("""
            <foo xmlns="foons"><leaf xmlns:br="barns" xmlns:bz="bazns">/br:bar[br:key='/bz:baz']</leaf></foo>""",
            stringWriter.toString());
    }
}
