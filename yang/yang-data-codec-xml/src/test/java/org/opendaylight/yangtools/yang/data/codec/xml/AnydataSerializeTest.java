/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Collection;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnydataNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.util.ImmutableNormalizedAnydata;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class AnydataSerializeTest extends AbstractAnydataTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return TestFactories.junitParameters();
    }

    private final XMLOutputFactory factory;

    public AnydataSerializeTest(final String factoryMode, final XMLOutputFactory factory) {
        this.factory = factory;
    }

    @Test
    public void testDOMAnydata() throws XMLStreamException, IOException {
        final StringWriter writer = new StringWriter();
        final XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(writer);

        final NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(
            xmlStreamWriter, SCHEMA_CONTEXT);
        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
            xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(ImmutableAnydataNodeBuilder.create(DOMSourceAnydata.class)
            .withNodeIdentifier(FOO_NODEID).withValue(toDOMSource("<bar xmlns=\"test-anydata\"/>")).build());
        normalizedNodeWriter.flush();

        final String serializedXml = writer.toString();
        assertEquals("<foo xmlns=\"test-anydata\"><bar xmlns=\"test-anydata\"></bar></foo>", serializedXml);
    }

    @Test
    public void testXmlParseAnydata()
            throws IOException, SAXException, XMLStreamException, URISyntaxException, TransformerException {

        //Create Data Scheme from yang file
        SchemaPath anydataPath = SchemaPath.create(true, FOO_QNAME);
        final SchemaNode dataSchemaNode = SchemaContextUtil.findDataSchemaNode(SCHEMA_CONTEXT, anydataPath);
        assertTrue(dataSchemaNode instanceof AnydataSchemaNode);
        final AnydataSchemaNode anyDataSchemaNode = (AnydataSchemaNode) dataSchemaNode;

        // deserialization
        final XMLStreamReader reader
                = UntrustedXML.createXMLStreamReader(loadResourcesAsInputStream("/test-anydata.xml"));

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, SCHEMA_CONTEXT, anyDataSchemaNode);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
        assertTrue(transformedInput instanceof AnydataNode);
        AnydataNode<?> anydataNode = (AnydataNode<?>) transformedInput;

        // serialization
        final StringWriter writer = new StringWriter();
        final XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(writer);
        final NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(
                xmlStreamWriter, SCHEMA_CONTEXT);
        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
                xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(transformedInput);
        normalizedNodeWriter.flush();

        final String serializedXml = writer.toString();
        final String deserializeXml = getXmlFromDOMSource(((DOMSourceAnydata) anydataNode.getValue()).getSource());
        assertFalse(serializedXml.isEmpty());

        // Check if is Serialize Node same as Deserialize Node
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);
        final Diff diff = new Diff(deserializeXml, serializedXml);
        final DifferenceListener differenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        diff.overrideDifferenceListener(differenceListener);

        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    public void testAnydataLoadFromXML() throws IOException, SAXException, XMLStreamException, URISyntaxException {
        // Load XML file
        Document doc = loadXmlDocument("/test-anydata.xml");
        final DOMSource domSource = new DOMSource(doc.getDocumentElement());

        //Load XML from file and write it with xmlParseStream
        final DOMResult domResult = new DOMResult(UntrustedXML.newDocumentBuilder().newDocument());
        final XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(domResult);
        final AnydataSchemaNode anyDataSchemaNode = (AnydataSchemaNode) SchemaContextUtil.findDataSchemaNode(
                SCHEMA_CONTEXT, SchemaPath.create(true, FOO_QNAME));
        final NormalizedNodeStreamWriter streamWriter = XMLStreamNormalizedNodeStreamWriter.create(
                xmlStreamWriter, SCHEMA_CONTEXT);
        final XMLStreamReader reader = new DOMSourceXMLStreamReader(domSource);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, SCHEMA_CONTEXT, anyDataSchemaNode);

        xmlParser.parse(reader);
        xmlParser.flush();

        //Set XML comparing
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);

        // Check diff
        final String expectedXml = toString(doc.getDocumentElement());
        final String serializedXml = toString(domResult.getNode());
        final Diff diff = new Diff(expectedXml, serializedXml);
        final DifferenceListener differenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        diff.overrideDifferenceListener(differenceListener);

        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    public void testAnydataSerialization()
            throws IOException, SAXException, XMLStreamException, URISyntaxException, TransformerException {
        //Get XML Data.
        Document doc = loadXmlDocument("/test-anydata.xml");
        final DOMSource domSource = new DOMSource(doc.getDocumentElement());

        //Get specific attribute from Yang file.
        final AnydataSchemaNode contWithAttr = (AnydataSchemaNode) SchemaContextUtil.findDataSchemaNode(
                SCHEMA_CONTEXT, SchemaPath.create(true, FOO_QNAME));

        //Create NormalizedNodeResult
        NormalizedNodeResult normalizedResult = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(normalizedResult);

        //Initialize Reader with XML file
        final XMLStreamReader reader = new DOMSourceXMLStreamReader(domSource);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, SCHEMA_CONTEXT, contWithAttr);
        xmlParser.parse(reader);
        xmlParser.flush();

        //Get Result
        final NormalizedNode<?, ?> node = normalizedResult.getResult();
        assertTrue(node instanceof AnydataNode);
        final AnydataNode<?> anydataResult = (AnydataNode<?>) node;

        //Get Result in formatted String
        assertTrue(anydataResult.getValue() instanceof DOMSourceAnydata);
        final String serializedXml = getXmlFromDOMSource(((DOMSourceAnydata)anydataResult.getValue()).getSource());
        final String expectedXml = toString(doc.getDocumentElement());

        //Looking for difference in Serialized xml and in Loaded XML
        final Diff diff = new Diff(expectedXml, serializedXml);
        final DifferenceListener differenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        diff.overrideDifferenceListener(differenceListener);

        XMLAssert.assertXMLEqual(diff, true);
    }

    @Test
    public void testSiblingSerialize() throws IOException, XMLStreamException {
        final StringWriter writer = new StringWriter();
        final XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(writer);

        final NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(
            xmlStreamWriter, SCHEMA_CONTEXT);
        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
            xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(ImmutableContainerNodeBuilder.create().withNodeIdentifier(CONT_NODEID)
            .withChild(ImmutableAnydataNodeBuilder.create(DOMSourceAnydata.class).withNodeIdentifier(CONT_ANY_NODEID)
                .withValue(toDOMSource("<bar xmlns=\"test-anydata\"/>")).build())
            .withChild(CONT_LEAF)
            .build());
        normalizedNodeWriter.flush();

        final String serializedXml = writer.toString();
        assertEquals("<cont xmlns=\"test-anydata\"><cont-any><bar xmlns=\"test-anydata\"></bar></cont-any>"
                + "<cont-leaf>abc</cont-leaf></cont>", serializedXml);
    }

    @Test
    public void testNormalizedSerialize() throws IOException, XMLStreamException {
        final StringWriter writer = new StringWriter();
        final XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(writer);

        final NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(
            xmlStreamWriter, SCHEMA_CONTEXT);
        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
            xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(ImmutableContainerNodeBuilder.create().withNodeIdentifier(CONT_NODEID)
            .withChild(ImmutableAnydataNodeBuilder.create(NormalizedAnydata.class).withNodeIdentifier(CONT_ANY_NODEID)
                .withValue(new ImmutableNormalizedAnydata(SCHEMA_CONTEXT,
                    SCHEMA_CONTEXT.findDataChildByName(CONT_QNAME).get(), ImmutableContainerNodeBuilder.create()
                    .withNodeIdentifier(CONT_NODEID).build())).build())
            .build());
        normalizedNodeWriter.flush();

        final String serializedXml = writer.toString();
        assertEquals("<cont xmlns=\"test-anydata\"><cont-any><cont/></cont-any></cont>", serializedXml);
    }
}
