/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.spi.DefaultSchemaTreeInference;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;

class AnydataSerializeTest extends AbstractAnydataTest {
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestFactories.class)
    public void testDOMAnydata(final String factoryMode, final XMLOutputFactory factory) throws Exception {
        final var writer = new StringWriter();
        final var xmlStreamWriter = factory.createXMLStreamWriter(writer);

        final var xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter,
            SCHEMA_CONTEXT);
        final var normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(ImmutableNodes.newAnydataBuilder(DOMSourceAnydata.class)
            .withNodeIdentifier(FOO_NODEID)
            .withValue(toDOMSource("<bar xmlns=\"test-anydata\"/>"))
            .build());
        normalizedNodeWriter.flush();

        final String serializedXml = writer.toString();
        assertEquals("<foo xmlns=\"test-anydata\"><bar xmlns=\"test-anydata\"></bar></foo>", serializedXml);
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestFactories.class)
    public void testXmlParseAnydata(final String factoryMode, final XMLOutputFactory factory) throws Exception {
        // deserialization
        final var reader = UntrustedXML.createXMLStreamReader(
            AnydataSerializeTest.class.getResourceAsStream("/test-anydata.xml"));

        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, Inference.ofDataTreePath(SCHEMA_CONTEXT, FOO_QNAME));
        xmlParser.parse(reader);

        final var transformedInput = result.getResult().data();
        assertThat(transformedInput, instanceOf(AnydataNode.class));
        final var anydataNode = (AnydataNode<?>) transformedInput;

        // serialization
        final var writer = new StringWriter();
        final var xmlStreamWriter = factory.createXMLStreamWriter(writer);
        final var xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter,
            SCHEMA_CONTEXT);
        final var normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(transformedInput);
        normalizedNodeWriter.flush();

        final String serializedXml = writer.toString();
        final String deserializeXml = getXmlFromDOMSource(((DOMSourceAnydata) anydataNode.body()).getSource());
        assertFalse(serializedXml.isEmpty());

        // Check if is Serialize Node same as Deserialize Node
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);
        final Diff diff = new Diff(deserializeXml, serializedXml);
        diff.overrideDifferenceListener(new IgnoreTextAndAttributeValuesDifferenceListener());

        XMLAssert.assertXMLEqual(diff, true);
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestFactories.class)
    public void testAnydataLoadFromXML(final String factoryMode, final XMLOutputFactory factory) throws Exception {
        // Load XML file
        final var doc = loadDocument("/test-anydata.xml");
        final var domSource = new DOMSource(doc.getDocumentElement());

        //Load XML from file and write it with xmlParseStream
        final var domResult = new DOMResult(UntrustedXML.newDocumentBuilder().newDocument());
        final var xmlStreamWriter = factory.createXMLStreamWriter(domResult);
        final var streamWriter = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter, SCHEMA_CONTEXT);
        final var reader = new DOMSourceXMLStreamReader(domSource);
        final var xmlParser = XmlParserStream.create(streamWriter, Inference.ofDataTreePath(SCHEMA_CONTEXT, FOO_QNAME));

        xmlParser.parse(reader);
        xmlParser.flush();

        //Set XML comparing
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);

        // Check diff
        final String expectedXml = toString(doc.getDocumentElement());
        final String serializedXml = toString(domResult.getNode());
        final Diff diff = new Diff(expectedXml, serializedXml);
        diff.overrideDifferenceListener(new IgnoreTextAndAttributeValuesDifferenceListener());

        XMLAssert.assertXMLEqual(diff, true);
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestFactories.class)
    public void testAnydataSerialization(final String factoryMode, final XMLOutputFactory factory) throws Exception {
        //Get XML Data.
        final var doc = loadDocument("/test-anydata.xml");
        final var domSource = new DOMSource(doc.getDocumentElement());

        //Create NormalizedNodeResult
        final var normalizedResult = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(normalizedResult);

        //Initialize Reader with XML file
        final var reader = new DOMSourceXMLStreamReader(domSource);
        final var xmlParser = XmlParserStream.create(streamWriter, Inference.ofDataTreePath(SCHEMA_CONTEXT, FOO_QNAME));
        xmlParser.parse(reader);
        xmlParser.flush();

        //Get Result
        final var node = normalizedResult.getResult().data();
        assertThat(node, instanceOf(AnydataNode.class));
        final var anydataResult = (AnydataNode<?>) node;

        //Get Result in formatted String
        assertThat(anydataResult.body(), instanceOf(DOMSourceAnydata.class));
        final String serializedXml = getXmlFromDOMSource(((DOMSourceAnydata)anydataResult.body()).getSource());
        final String expectedXml = toString(doc.getDocumentElement());

        //Looking for difference in Serialized xml and in Loaded XML
        final Diff diff = new Diff(expectedXml, serializedXml);
        diff.overrideDifferenceListener(new IgnoreTextAndAttributeValuesDifferenceListener());

        XMLAssert.assertXMLEqual(diff, true);
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestFactories.class)
    public void testSiblingSerialize(final String factoryMode, final XMLOutputFactory factory) throws Exception {
        final var writer = new StringWriter();
        final var xmlStreamWriter = factory.createXMLStreamWriter(writer);

        final var xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter,
            SCHEMA_CONTEXT);
        final var normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(CONT_NODEID)
            .withChild(ImmutableNodes.newAnydataBuilder(DOMSourceAnydata.class)
                .withNodeIdentifier(CONT_ANY_NODEID)
                .withValue(toDOMSource("<bar xmlns=\"test-anydata\"/>"))
                .build())
            .withChild(CONT_LEAF)
            .build());
        normalizedNodeWriter.flush();

        final String serializedXml = writer.toString();
        assertEquals("<cont xmlns=\"test-anydata\"><cont-any><bar xmlns=\"test-anydata\"></bar></cont-any>"
                + "<cont-leaf>abc</cont-leaf></cont>", serializedXml);
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestFactories.class)
    public void testNormalizedSerialize(final String factoryMode, final XMLOutputFactory factory) throws Exception {
        final var writer = new StringWriter();
        final var xmlStreamWriter = factory.createXMLStreamWriter(writer);

        final var xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter,
            SCHEMA_CONTEXT);
        final var normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(CONT_NODEID)
            .withChild(ImmutableNodes.newAnydataBuilder(NormalizedAnydata.class)
                .withNodeIdentifier(CONT_ANY_NODEID)
                .withValue(NormalizedAnydata.of(
                    DefaultSchemaTreeInference.of(SCHEMA_CONTEXT, Absolute.of(CONT_QNAME)),
                    ImmutableNodes.newContainerBuilder().withNodeIdentifier(CONT_NODEID).build()))
                .build())
            .build());
        normalizedNodeWriter.flush();

        final String serializedXml = writer.toString();
        assertEquals("<cont xmlns=\"test-anydata\"><cont-any><cont/></cont-any></cont>", serializedXml);
    }
}
