/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer.retest;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.RetestUtils;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XMLStreamNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser.DomToNormalizedNodeParserFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class AnyXMLSerializationSupportTest extends XMLTestCase {
    private static final XMLOutputFactory XML_FACTORY;
    private static final DocumentBuilderFactory BUILDERFACTORY;

    static {
        XML_FACTORY = XMLOutputFactory.newFactory();
        XML_FACTORY.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);

        BUILDERFACTORY = DocumentBuilderFactory.newInstance();
        BUILDERFACTORY.setNamespaceAware(true);
        BUILDERFACTORY.setCoalescing(true);
        BUILDERFACTORY.setIgnoringElementContentWhitespace(true);
        BUILDERFACTORY.setIgnoringComments(true);
    }

    private QNameModule fooModuleQName;
    private QNameModule barModuleQName;
    private QName myContainer1;
    private QName myContainer2;
    private QName innerContainer;
    private QName myLeaf3;
    private QName myLeaf2;
    private QName myLeaf1;
    private QName myAnyXMLDataBar;
    private QName myAnyXMLDataFoo;
    private SchemaContext schemaContext;

    public AnyXMLSerializationSupportTest() throws Exception {
        barModuleQName = QNameModule.create(new URI("bar"), SimpleDateFormatUtil.getRevisionFormat()
                .parse("1970-01-01"));
        myContainer1 = QName.create(barModuleQName, "my-container-1");
        myLeaf1 = QName.create(barModuleQName, "my-leaf-1");
        myAnyXMLDataBar = QName.create(barModuleQName, "my-anyxml-data");

        fooModuleQName = QNameModule.create(new URI("foo"), SimpleDateFormatUtil.getRevisionFormat()
                .parse("1970-01-01"));
        myContainer2 = QName.create(fooModuleQName, "my-container-2");
        innerContainer = QName.create(fooModuleQName, "inner-container");
        myLeaf3 = QName.create(fooModuleQName, "my-leaf-3");
        myLeaf2 = QName.create(fooModuleQName, "my-leaf-2");
        myAnyXMLDataFoo = QName.create(fooModuleQName, "my-anyxml-data");
        schemaContext = RetestUtils.parseYangSources(new File(getClass().getResource("/anyxml-support/serialization/baz.yang").toURI()));
    }

    @Test
    public void testSerializationOfBaz() throws Exception {
        final Document doc = loadDocument("/anyxml-support/serialization/baz.xml");

        final ContainerNode output = DomToNormalizedNodeParserFactory
                .getInstance(DomUtils.defaultValueCodecProvider(), schemaContext).getContainerNodeParser()
                .parse(Collections.singletonList(doc.getDocumentElement()), schemaContext);

        assertNotNull(output);

        DOMResult serializationResult = writeNormalizedNode(output, schemaContext);
        assertNotNull(serializationResult);

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);

        String expectedXMLString = toString(doc.getDocumentElement().getElementsByTagName("baz").item(0));
        String serializationResultXMLString = toString(serializationResult.getNode());

        assertXMLEqual(expectedXMLString,serializationResultXMLString);
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

            for(NormalizedNode<?, ?> child : normalized.getValue()) {
                normalizedNodeWriter.write(child);
            }

            normalizedNodeWriter.flush();
        } finally {
            if(normalizedNodeWriter != null) {
                normalizedNodeWriter.close();
            }
            if(normalizedNodeStreamWriter != null) {
                normalizedNodeStreamWriter.close();
            }
            if(writer != null) {
                writer.close();
            }
        }

        return result;
    }

    private static Document loadDocument(final String xmlPath) throws IOException, SAXException {
        final InputStream resourceAsStream = AnyXMLSerializationSupportTest.class.getResourceAsStream(xmlPath);
        final Document currentConfigElement = readXmlToDocument(resourceAsStream);
        Preconditions.checkNotNull(currentConfigElement);
        return currentConfigElement;
    }

    private static Document readXmlToDocument(final InputStream xmlContent) throws IOException, SAXException {
        final DocumentBuilder dBuilder;
        try {
            dBuilder = BUILDERFACTORY.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException("Failed to parse XML document", e);
        }
        final Document doc = dBuilder.parse(xmlContent);

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
