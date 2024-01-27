/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Base64;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMResult;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.xmlunit.builder.DiffBuilder;

class Bug5446Test extends AbstractXmlTest {
    private static final QName ROOT_QNAME = QName.create("foo", "2015-11-05", "root");
    private static final QName IP_ADDRESS_QNAME = QName.create(ROOT_QNAME, "ip-address");

    @Test
    void test() throws Exception {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYang("""
            module foo {
              yang-version 1;
              namespace "foo";
              prefix "foo";

              revision "2015-11-05" {
              }

              typedef ipv4-address-binary {
                type binary {
                  length "4";
                }
              }

              typedef ipv6-address-binary {
                type binary {
                  length "16";
                }
              }

              typedef ip-address-binary {
                type union {
                  type ipv4-address-binary;
                  type ipv6-address-binary;
                }
              }

              container root {
                leaf ip-address {
                  type ip-address-binary;
                }
              }
            }""");
        final var doc = loadDocument("/bug5446/xml/foo.xml");

        final var docNode = createDocNode();

        DataContainerChild root = docNode.getChildByArg(new NodeIdentifier(ROOT_QNAME));
        DataContainerChild child = ((ContainerNode) root).getChildByArg(new NodeIdentifier(IP_ADDRESS_QNAME));
        LeafNode<?> ipAdress = (LeafNode<?>) child;

        Object value = ipAdress.body();
        assertTrue(value instanceof byte[]);
        assertEquals("fwAAAQ==", Base64.getEncoder().encodeToString((byte[]) value));

        DOMResult serializationResult = writeNormalizedNode(docNode, schemaContext);
        assertNotNull(serializationResult);

        final var diff = DiffBuilder.compare(toString(doc.getDocumentElement()))
            .withTest(toString(serializationResult.getNode()))
            .ignoreWhitespace()
            .ignoreComments()
            .checkForIdentical()
            .build();
        assertFalse(diff.hasDifferences(), diff.toString());
    }

    private static ContainerNode createDocNode() {
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(ROOT_QNAME))
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT_QNAME))
                .withChild(ImmutableNodes.leafNode(IP_ADDRESS_QNAME, Base64.getDecoder().decode("fwAAAQ==")))
                .build())
            .build();
    }

    private static DOMResult writeNormalizedNode(final ContainerNode normalized, final EffectiveModelContext context)
            throws IOException, XMLStreamException {
        final var doc = UntrustedXML.newDocumentBuilder().newDocument();
        final var result = new DOMResult(doc);
        final var writer = TestFactories.DEFAULT_OUTPUT_FACTORY.createXMLStreamWriter(result);
        try (var nnWriter = NormalizedNodeWriter.forStreamWriter(
                XMLStreamNormalizedNodeStreamWriter.create(writer, context))) {
            for (var child : normalized.body()) {
                nnWriter.write(child);
            }
            nnWriter.flush();
        } finally {
            writer.close();
        }
        return result;
    }
}
