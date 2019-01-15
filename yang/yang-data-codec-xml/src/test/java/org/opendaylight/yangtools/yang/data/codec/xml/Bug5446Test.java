/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import com.google.common.io.BaseEncoding;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Bug5446Test extends XMLTestCase {
    private static final QNameModule FOO_MODULE = QNameModule.create(URI.create("foo"), Revision.of("2015-11-05"));
    private static final QName ROOT_QNAME = QName.create(FOO_MODULE, "root");
    private static final QName IP_ADDRESS_QNAME = QName.create(FOO_MODULE, "ip-address");

    @Test
    public void test() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/bug5446/yang/foo.yang");
        final Document doc = loadDocument("/bug5446/xml/foo.xml");

        final ContainerNode docNode = createDocNode();

        Optional<DataContainerChild<? extends PathArgument, ?>> root = docNode.getChild(new NodeIdentifier(ROOT_QNAME));
        assertTrue(root.orElse(null) instanceof ContainerNode);

        Optional<DataContainerChild<? extends PathArgument, ?>> child = ((ContainerNode) root.orElse(null))
                .getChild(new NodeIdentifier(IP_ADDRESS_QNAME));
        assertTrue(child.orElse(null) instanceof LeafNode);
        LeafNode<?> ipAdress = (LeafNode<?>) child.get();

        Object value = ipAdress.getValue();
        assertTrue(value instanceof byte[]);
        assertEquals("fwAAAQ==", BaseEncoding.base64().encode((byte[]) value));

        DOMResult serializationResult = writeNormalizedNode(docNode, schemaContext);
        assertNotNull(serializationResult);

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);

        String expectedXMLString = toString(doc.getDocumentElement());
        String serializationResultXMLString = toString(serializationResult.getNode());

        assertXMLEqual(expectedXMLString, serializationResultXMLString);
    }

    private static ContainerNode createDocNode() {
        LeafNode<byte[]> ipAddress = ImmutableNodes.leafNode(IP_ADDRESS_QNAME, BaseEncoding.base64()
                .decode("fwAAAQ=="));
        ContainerNode root = ImmutableContainerNodeBuilder.create().withNodeIdentifier(new NodeIdentifier(ROOT_QNAME))
                .withChild(ipAddress).build();
        return ImmutableContainerNodeBuilder.create().withNodeIdentifier(new NodeIdentifier(ROOT_QNAME)).withChild(root)
                .build();
    }

    private static DOMResult writeNormalizedNode(final ContainerNode normalized, final SchemaContext context)
            throws IOException, XMLStreamException {
        final Document doc = UntrustedXML.newDocumentBuilder().newDocument();
        final DOMResult result = new DOMResult(doc);
        NormalizedNodeWriter normalizedNodeWriter = null;
        NormalizedNodeStreamWriter normalizedNodeStreamWriter = null;
        XMLStreamWriter writer = null;
        try {
            writer = TestFactories.DEFAULT_OUTPUT_FACTORY.createXMLStreamWriter(result);
            normalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(writer, context);
            normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(normalizedNodeStreamWriter);

            for (NormalizedNode<?, ?> child : normalized.getValue()) {
                normalizedNodeWriter.write(child);
            }

            normalizedNodeWriter.flush();
        } finally {
            if (normalizedNodeWriter != null) {
                normalizedNodeWriter.close();
            }
            if (normalizedNodeStreamWriter != null) {
                normalizedNodeStreamWriter.close();
            }
            if (writer != null) {
                writer.close();
            }
        }

        return result;
    }

    private static Document loadDocument(final String xmlPath) throws IOException, SAXException {
        final InputStream resourceAsStream = Bug5446Test.class.getResourceAsStream(xmlPath);
        return requireNonNull(readXmlToDocument(resourceAsStream));
    }

    private static Document readXmlToDocument(final InputStream xmlContent) throws IOException, SAXException {
        final Document doc = UntrustedXML.newDocumentBuilder().parse(xmlContent);
        doc.getDocumentElement().normalize();
        return doc;
    }

    private static String toString(final Node xml) {
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            final StreamResult result = new StreamResult(new StringWriter());
            final DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (IllegalArgumentException | TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Unable to serialize xml element " + xml, e);
        }
    }
}
