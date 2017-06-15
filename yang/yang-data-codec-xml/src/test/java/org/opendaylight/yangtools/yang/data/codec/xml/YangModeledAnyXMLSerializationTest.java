/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
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
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class YangModeledAnyXMLSerializationTest extends XMLTestCase {

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

        schemaContext = YangParserTestUtils.parseYangSources("/anyxml-support/serialization");
    }

    @Test
    public void testSerializationOfBaz() throws Exception {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream(
                "/anyxml-support/serialization/baz.xml");
        final Module bazModule = schemaContext.findModuleByName("baz", null);
        final ContainerSchemaNode bazCont = (ContainerSchemaNode) bazModule.getDataChildByName(
                QName.create(bazModule.getQNameModule(), "baz"));
        assertNotNull(bazCont);

        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = inputFactory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();

        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, bazCont);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);

        assertTrue(transformedInput instanceof ContainerNode);
        ContainerNode bazContainer = (ContainerNode) transformedInput;
        assertEquals(bazContainer.getNodeType(), bazQName);

        Optional<DataContainerChild<? extends PathArgument, ?>> bazContainerChild = bazContainer.getChild(
                new NodeIdentifier(myAnyXMLDataBaz));
        assertTrue(bazContainerChild.orNull() instanceof YangModeledAnyXmlNode);
        YangModeledAnyXmlNode yangModeledAnyXmlNode = (YangModeledAnyXmlNode) bazContainerChild.get();

        DataSchemaNode schemaOfAnyXmlData = yangModeledAnyXmlNode.getSchemaOfAnyXmlData();
        SchemaNode myContainer2SchemaNode = SchemaContextUtil.findDataSchemaNode(schemaContext,
                SchemaPath.create(true, bazQName, myContainer2QName));
        assertTrue(myContainer2SchemaNode instanceof ContainerSchemaNode);
        assertEquals(myContainer2SchemaNode, schemaOfAnyXmlData);

        final Document document = UntrustedXML.newDocumentBuilder().newDocument();
        final DOMResult domResult = new DOMResult(document);

        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);

        final XMLStreamWriter xmlStreamWriter = outputFactory.createXMLStreamWriter(domResult);

        final NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter
                .create(xmlStreamWriter, schemaContext);

        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
                xmlNormalizedNodeStreamWriter);

        normalizedNodeWriter.write(transformedInput);

        final Document doc = loadDocument("/anyxml-support/serialization/baz.xml");

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);

        String expectedXml = toString(doc.getDocumentElement());
        String serializedXml = toString(domResult.getNode());

        assertXMLEqual(expectedXml, serializedXml);
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
