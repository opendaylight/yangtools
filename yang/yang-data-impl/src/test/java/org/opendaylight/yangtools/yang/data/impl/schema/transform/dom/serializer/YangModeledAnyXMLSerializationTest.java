/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import javax.xml.stream.XMLOutputFactory;
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
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XMLStreamNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser.DomToNormalizedNodeParserFactory;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class YangModeledAnyXMLSerializationTest extends XMLTestCase {
    private static final XMLOutputFactory XML_FACTORY;

    static {
        XML_FACTORY = XMLOutputFactory.newFactory();
        XML_FACTORY.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.FALSE);
    }

    private final QNameModule bazModuleQName;
    private final QName myAnyXMLDataBaz;
    private final QName bazQName;
    private final QName myContainer2QName;
    private final SchemaContext schemaContext;

    public YangModeledAnyXMLSerializationTest() throws Exception {
        bazModuleQName = QNameModule.create(new URI("baz"), SimpleDateFormatUtil.getRevisionFormat()
                .parse("1970-01-01"));
        bazQName = QName.create(bazModuleQName, "baz");
        myContainer2QName = QName.create(bazModuleQName, "my-container-2");
        myAnyXMLDataBaz = QName.create(bazModuleQName, "my-anyxml-data");

        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/anyxml-support/serialization");
    }

    @Test
    public void testSerializationOfBaz() throws Exception {
        final Document doc = loadDocument("/anyxml-support/serialization/baz.xml");

        final ContainerNode output = DomToNormalizedNodeParserFactory
                .getInstance(DomUtils.defaultValueCodecProvider(), schemaContext).getContainerNodeParser()
                .parse(Collections.singletonList(doc.getDocumentElement()), schemaContext);

        assertNotNull(output);
        Optional<DataContainerChild<? extends PathArgument, ?>> child = output.getChild(new NodeIdentifier(bazQName));
        assertTrue(child.orNull() instanceof ContainerNode);
        ContainerNode baz = (ContainerNode) child.get();

        Optional<DataContainerChild<? extends PathArgument, ?>> bazChild = baz.getChild(new NodeIdentifier(
                myAnyXMLDataBaz));
        assertTrue(bazChild.orNull() instanceof YangModeledAnyXmlNode);
        YangModeledAnyXmlNode yangModeledAnyXmlNode = (YangModeledAnyXmlNode) bazChild.get();

        DataSchemaNode schemaOfAnyXmlData = yangModeledAnyXmlNode.getSchemaOfAnyXmlData();
        SchemaNode myContainer2SchemaNode = SchemaContextUtil.findDataSchemaNode(schemaContext,
                SchemaPath.create(true, bazQName, myContainer2QName));
        assertTrue(myContainer2SchemaNode instanceof ContainerSchemaNode);
        assertEquals(myContainer2SchemaNode, schemaOfAnyXmlData);

        DOMResult serializationResult = writeNormalizedNode(output, schemaContext);
        assertNotNull(serializationResult);

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);

        String expectedXMLString = toString(doc.getDocumentElement().getElementsByTagName("baz").item(0));
        String serializationResultXMLString = toString(serializationResult.getNode());

        assertXMLEqual(expectedXMLString, serializationResultXMLString);
    }

    private static DOMResult writeNormalizedNode(final ContainerNode normalized, final SchemaContext context)
            throws IOException, XMLStreamException {
        final Document doc = XmlDocumentUtils.getDocument();
        final DOMResult result = new DOMResult(doc);
        NormalizedNodeWriter normalizedNodeWriter = null;
        NormalizedNodeStreamWriter normalizedNodeStreamWriter = null;
        XMLStreamWriter writer = null;
        try {
            writer = XML_FACTORY.createXMLStreamWriter(result);
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
        final InputStream resourceAsStream = YangModeledAnyXMLSerializationTest.class.getResourceAsStream(xmlPath);
        final Document currentConfigElement = readXmlToDocument(resourceAsStream);
        Preconditions.checkNotNull(currentConfigElement);
        return currentConfigElement;
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
