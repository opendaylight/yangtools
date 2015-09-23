/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.activation.UnsupportedDataTypeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XmlDocumentUtilsTest {

    private static final DocumentBuilderFactory BUILDERFACTORY;

    static {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        BUILDERFACTORY = factory;
    }

    public static final String XML_CONTENT = "<input xmlns=\"urn:opendaylight:controller:rpc:test\">\n" +
            "<a>value</a>\n" +
            "<ref xmlns:ltha=\"urn:opendaylight:controller:rpc:test\">/ltha:cont/ltha:l[ltha:id='id']</ref>\n" +
            "</input>";

    public static final String RPC_REPLY = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"m-1\">\n" +
            " <ok/>\n" +
            "</rpc-reply>";

    private SchemaContext schema;
    private RpcDefinition testRpc;

    @Before
    public void setUp() throws Exception {
        final ByteSource byteSource = new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return XmlDocumentUtilsTest.this.getClass().getResourceAsStream("rpc-test.yang");
            }
        };
        schema = new YangParserImpl().parseSources(Lists.newArrayList(byteSource));
        final Module rpcTestModule = schema.getModules().iterator().next();
        testRpc = rpcTestModule.getRpcs().iterator().next();
    }

    @Test
    public void testRpcInputTransform() throws Exception {

        final Document inputDocument = readXmlToDocument(XML_CONTENT);
        final Element input = inputDocument.getDocumentElement();

        final CompositeNode node = inputXmlToCompositeNode(input);
        final SimpleNode<?> refParsed = node.getSimpleNodesByName("ref").iterator().next();
        assertEquals(YangInstanceIdentifier.class, refParsed.getValue().getClass());
        final Document serializedDocument = inputCompositeNodeToXml(node);

        XMLUnit.compareXML(inputDocument, serializedDocument);
    }

    @Test
    public void testRpcReplyToDom() throws Exception {
        final Document reply = readXmlToDocument(RPC_REPLY);
        final CompositeNode domNodes = XmlDocumentUtils.rpcReplyToDomNodes(reply, QName.create("urn:opendaylight:controller:rpc:test", "2014-07-28", "test"), schema);
        assertEquals(1, domNodes.getValue().size());
        final Node<?> outputNode = domNodes.getValue().get(0);
        assertTrue(outputNode instanceof CompositeNode);
        assertEquals(1, ((CompositeNode) outputNode).getValue().size());
        final Node<?> okNode = ((CompositeNode) outputNode).getValue().get(0);
        assertEquals("ok", okNode.getNodeType().getLocalName());
    }

    public static Document readXmlToDocument(final String xmlContent) throws SAXException, IOException {
        return readXmlToDocument(new ByteArrayInputStream(xmlContent.getBytes(Charsets.UTF_8)));
    }

    public static Document readXmlToDocument(final InputStream xmlContent) throws SAXException, IOException {
        final DocumentBuilder dBuilder;
        try {
            dBuilder = BUILDERFACTORY.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new IllegalStateException("Failed to parse XML document", e);
        }
        final Document doc = dBuilder.parse(xmlContent);

        doc.getDocumentElement().normalize();
        return doc;
    }

    public Document inputCompositeNodeToXml(final CompositeNode cNode)
            throws UnsupportedDataTypeException {
        return XmlDocumentUtils.toDocument(cNode, testRpc.getInput(), XmlDocumentUtils.defaultValueCodecProvider(), schema);
    }

    public CompositeNode inputXmlToCompositeNode(final Element e) {
        return (CompositeNode) XmlDocumentUtils.toDomNode(e, Optional.<DataSchemaNode>of(testRpc.getInput()),
                Optional.of(XmlDocumentUtils.defaultValueCodecProvider()), Optional.of(schema));
    }
}
