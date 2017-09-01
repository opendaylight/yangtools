/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
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
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Bug8745Test {

    @Test
    public void testParsingAttributes() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/bug8745/foo.yang");
        final QName contWithAttributes = QName.create("foo", "1970-01-01", "cont-with-attributes");
        final ContainerSchemaNode contWithAttr = (ContainerSchemaNode) SchemaContextUtil.findDataSchemaNode(
                schemaContext, SchemaPath.create(true, contWithAttributes));

        final Document doc = loadDocument("/bug8745/foo.xml");
        final DOMSource domSource = new DOMSource(doc.getDocumentElement());

        final DOMResult domResult = new DOMResult(UntrustedXML.newDocumentBuilder().newDocument());

        final XMLOutputFactory outputfactory = XMLOutputFactory.newInstance();
        outputfactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);

        final XMLStreamWriter xmlStreamWriter = outputfactory.createXMLStreamWriter(domResult);

        final NormalizedNodeStreamWriter streamWriter = XMLStreamNormalizedNodeStreamWriter.create(
                xmlStreamWriter, schemaContext);

        final InputStream resourceAsStream = Bug8745Test.class.getResourceAsStream(
                "/bug8745/foo.xml");
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
//        final XMLStreamReader reader = inputFactory.createXMLStreamReader(resourceAsStream);
        final XMLStreamReader reader = new DOMSourceXMLStreamReader(domSource);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, contWithAttr);
        xmlParser.parse(reader);

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);

        final String expectedXml = toString(doc.getDocumentElement());
        final String serializedXml = toString(domResult.getNode());
        final Diff diff = new Diff(expectedXml, serializedXml);

        XMLAssert.assertXMLEqual(diff, true);
    }

    private static Document loadDocument(final String xmlPath) throws IOException, SAXException {
        final InputStream resourceAsStream = NormalizedNodesToXmlTest.class.getResourceAsStream(xmlPath);
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
