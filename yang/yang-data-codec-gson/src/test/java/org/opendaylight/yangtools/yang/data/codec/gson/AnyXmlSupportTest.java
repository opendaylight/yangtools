/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import javax.xml.transform.dom.DOMSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class AnyXmlSupportTest {

    private static final QName CONT_1 = QName.create("ns:complex:json", "2014-08-11", "cont1");
    private static final QName LF12_ANY = QName.create(CONT_1, "lf12-any");
    private static final QName LF13_ANY = QName.create(CONT_1, "lf13-any");
    private static final QName LF14_ANY = QName.create(CONT_1, "lf14-any");

    private static SchemaContext schemaContext;

    @BeforeClass
    public static void setup() {
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/complexjson/yang");
    }

    @Test
    public void anyXmlNodeWithSimpleValueInContainer() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/anyxml-node-with-simple-value-in-container.json");

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);

        // lf12-any check
        final DOMSource Lf12AnyActualValue = getParsedAnyXmlValue(transformedInput, LF12_ANY);
        final DOMSource Lf12AnyExpectedValue = createAnyXmlSimpleValue("ns:complex:json", "lf12-any", "100.5");
        verifyTransformedAnyXmlNodeValue(Lf12AnyExpectedValue, Lf12AnyActualValue);

        // lf13-any check
        final DOMSource Lf13AnyActualValue = getParsedAnyXmlValue(transformedInput, LF13_ANY);
        final DOMSource Lf13AnyExpectedValue = createAnyXmlSimpleValue("ns:complex:json", "lf13-any", "true");
        verifyTransformedAnyXmlNodeValue(Lf13AnyExpectedValue, Lf13AnyActualValue);

        // lf14-any check
        final DOMSource Lf14AnyActualValue = getParsedAnyXmlValue(transformedInput, LF14_ANY);
        final DOMSource Lf14AnyExpectedValue = createAnyXmlSimpleValue("ns:complex:json", "lf14-any", "null");
        verifyTransformedAnyXmlNodeValue(Lf14AnyExpectedValue, Lf14AnyActualValue);

        // serialization
        final Writer writer = new StringWriter();
        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            JSONCodecFactory.getShared(schemaContext), SchemaPath.ROOT, null,
            JsonWriterFactory.createJsonWriter(writer, 2));
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
        nodeWriter.write(transformedInput);
        nodeWriter.close();
        final String serializationResult = writer.toString();

        final JsonParser parser = new JsonParser();
        final JsonElement expected = parser.parse(inputJson);
        final JsonElement actual = parser.parse(serializationResult);
        assertTrue(expected.equals(actual));
    }

    @Test
    public void anyXmlNodeWithCompositeValueInContainer() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/complexjson/anyxml-node-with-composite-value-in-container.json");

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);

        // lf12-any check
        final DOMSource Lf12AnyActualValue = getParsedAnyXmlValue(transformedInput, LF12_ANY);
        final DOMSource Lf12AnyExpectedValue = createLf12AnyXmlCompositeValue("ns:complex:json", "lf12-any");
        verifyTransformedAnyXmlNodeValue(Lf12AnyExpectedValue, Lf12AnyActualValue);

        // lf13-any check
        final DOMSource Lf13AnyActualValue = getParsedAnyXmlValue(transformedInput, LF13_ANY);
        final DOMSource Lf13AnyExpectedValue = createLf13AnyXmlCompositeValue("ns:complex:json", "lf13-any");
        verifyTransformedAnyXmlNodeValue(Lf13AnyExpectedValue, Lf13AnyActualValue);

        // lf14-any check
        final DOMSource Lf14AnyActualValue = getParsedAnyXmlValue(transformedInput, LF14_ANY);
        final DOMSource Lf14AnyExpectedValue = createLf14AnyXmlCompositeValue("ns:complex:json", "lf14-any");
        verifyTransformedAnyXmlNodeValue(Lf14AnyExpectedValue, Lf14AnyActualValue);

        // serialization
        final Writer writer = new StringWriter();
        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            JSONCodecFactory.getShared(schemaContext), SchemaPath.ROOT, null,
            JsonWriterFactory.createJsonWriter(writer, 2));
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
        nodeWriter.write(transformedInput);
        nodeWriter.close();
        final String serializationResult = writer.toString();

        final JsonParser parser = new JsonParser();
        final JsonElement expected = parser.parse(inputJson);
        final JsonElement actual = parser.parse(serializationResult);
        assertTrue(expected.equals(actual));
    }

    private static DOMSource getParsedAnyXmlValue(final NormalizedNode<?, ?> transformedInput, final QName anyxmlName) {
        assertTrue(transformedInput instanceof ContainerNode);
        final ContainerNode cont1 = (ContainerNode) transformedInput;
        final DataContainerChild<? extends PathArgument, ?> child = cont1.getChild(new NodeIdentifier(anyxmlName))
                .get();
        assertTrue(child instanceof AnyXmlNode);
        final AnyXmlNode anyXmlNode = (AnyXmlNode) child;
        return anyXmlNode.getValue();
    }

    private static void verifyTransformedAnyXmlNodeValue(final DOMSource expectedValue, final DOMSource actualValue) {
        assertTrue(expectedValue.getNode().isEqualNode(actualValue.getNode()));
    }

    private static DOMSource createAnyXmlSimpleValue(final String ns, final String name, final String value) {
        final Document doc = UntrustedXML.newDocumentBuilder().newDocument();
        final Element rootElement = doc.createElementNS(ns, name);
        doc.appendChild(rootElement);
        final Text textNode = doc.createTextNode(value);
        rootElement.appendChild(textNode);
        return new DOMSource(doc.getDocumentElement());
    }

    private static DOMSource createLf12AnyXmlCompositeValue(final String ns, final String name) {
        final Document doc = UntrustedXML.newDocumentBuilder().newDocument();
        final Element rootElement = doc.createElementNS(ns, name);

        final Element arrayElement1 = doc.createElement("array-element");
        final Text arrayElement1Text = doc.createTextNode("true");
        arrayElement1.appendChild(arrayElement1Text);

        final Element arrayElement2 = doc.createElement("array-element");

        final Element arrayElement2Baz = doc.createElement("baz");
        final Element bazArrayElement1 = doc.createElement("array-element");
        final Text bazArrayElement1Text = doc.createTextNode("120");
        bazArrayElement1.appendChild(bazArrayElement1Text);
        final Element bazArrayElement2 = doc.createElement("array-element");
        final Text bazArrayElement2Text = doc.createTextNode("str-val");
        bazArrayElement2.appendChild(bazArrayElement2Text);
        final Element bazArrayElement3 = doc.createElement("array-element");
        final Text bazArrayElement3Text = doc.createTextNode("false");
        bazArrayElement3.appendChild(bazArrayElement3Text);

        arrayElement2Baz.appendChild(bazArrayElement1);
        arrayElement2Baz.appendChild(bazArrayElement2);
        arrayElement2Baz.appendChild(bazArrayElement3);

        arrayElement2.appendChild(arrayElement2Baz);

        final Element arrayElement3 = doc.createElement("array-element");
        final Element arrayElement3Foo = doc.createElement("foo");
        final Text fooText = doc.createTextNode("null");
        arrayElement3Foo.appendChild(fooText);
        arrayElement3.appendChild(arrayElement3Foo);

        rootElement.appendChild(arrayElement1);
        rootElement.appendChild(arrayElement2);
        rootElement.appendChild(arrayElement3);

        doc.appendChild(rootElement);

        return new DOMSource(doc.getDocumentElement());
    }

    private static DOMSource createLf13AnyXmlCompositeValue(final String ns, final String name) {
        final Document doc = UntrustedXML.newDocumentBuilder().newDocument();
        final Element rootElement = doc.createElementNS(ns, name);

        final Element anyXmlArrayA = doc.createElement("anyxml-array-a");

        final Element arrayAElement1 = doc.createElement("array-element");
        final Element arrayAElement1Foo = doc.createElement("foo");
        final Text fooText = doc.createTextNode("true");
        arrayAElement1Foo.appendChild(fooText);
        arrayAElement1.appendChild(arrayAElement1Foo);

        final Element arrayAElement2 = doc.createElement("array-element");
        final Text arrayAElement2Text = doc.createTextNode("10");
        arrayAElement2.appendChild(arrayAElement2Text);

        final Element arrayAElement3 = doc.createElement("array-element");
        final Element arrayAElement3Bar = doc.createElement("bar");
        final Text barText = doc.createTextNode("false");
        arrayAElement3Bar.appendChild(barText);
        arrayAElement3.appendChild(arrayAElement3Bar);

        anyXmlArrayA.appendChild(arrayAElement1);
        anyXmlArrayA.appendChild(arrayAElement2);
        anyXmlArrayA.appendChild(arrayAElement3);

        final Element anyXmlArrayB = doc.createElement("anyxml-array-b");

        final Element arrayBElement1 = doc.createElement("array-element");
        final Text arrayBElement1Text = doc.createTextNode("1");
        arrayBElement1.appendChild(arrayBElement1Text);

        final Element arrayBElement2 = doc.createElement("array-element");
        final Text arrayBElement2Text = doc.createTextNode("2");
        arrayBElement2.appendChild(arrayBElement2Text);

        final Element arrayBElement3 = doc.createElement("array-element");

        final Element arrayBElement3Element1 = doc.createElement("array-element");
        final Text arrayBElement3Element1Text = doc.createTextNode("4");
        arrayBElement3Element1.appendChild(arrayBElement3Element1Text);
        final Element arrayBElement3Element2 = doc.createElement("array-element");
        final Text arrayBElement3Element2Text = doc.createTextNode("5");
        arrayBElement3Element2.appendChild(arrayBElement3Element2Text);

        arrayBElement3.appendChild(arrayBElement3Element1);
        arrayBElement3.appendChild(arrayBElement3Element2);

        final Element arrayBElement4 = doc.createElement("array-element");
        final Text arrayBElement4Text = doc.createTextNode("7");
        arrayBElement4.appendChild(arrayBElement4Text);

        anyXmlArrayB.appendChild(arrayBElement1);
        anyXmlArrayB.appendChild(arrayBElement2);
        anyXmlArrayB.appendChild(arrayBElement3);
        anyXmlArrayB.appendChild(arrayBElement4);

        rootElement.appendChild(anyXmlArrayA);
        rootElement.appendChild(anyXmlArrayB);

        doc.appendChild(rootElement);

        return new DOMSource(doc.getDocumentElement());
    }

    private static DOMSource createLf14AnyXmlCompositeValue(final String ns, final String name) {
        final Document doc = UntrustedXML.newDocumentBuilder().newDocument();
        final Element rootElement = doc.createElementNS(ns, name);

        final Element anyXmlObjectA = doc.createElement("anyxml-object-a");
        final Element dataA1 = doc.createElement("data-a1");
        final Text dataA1Text = doc.createTextNode("10");
        dataA1.appendChild(dataA1Text);
        final Element dataA2 = doc.createElement("data-a2");
        final Text dataA2Text = doc.createTextNode("11");
        dataA2.appendChild(dataA2Text);

        anyXmlObjectA.appendChild(dataA1);
        anyXmlObjectA.appendChild(dataA2);

        final Element anyXmlObjectB = doc.createElement("anyxml-object-b");

        final Element childObjectB1 = doc.createElement("child-object-b1");
        final Element dataB1 = doc.createElement("data-b1");
        final Text dataB1Text = doc.createTextNode("5.5");
        dataB1.appendChild(dataB1Text);
        childObjectB1.appendChild(dataB1);

        final Element childObjectB2 = doc.createElement("child-object-b2");
        final Element dataB2 = doc.createElement("data-b2");
        final Text dataB2Text = doc.createTextNode("b2-val");
        dataB2.appendChild(dataB2Text);
        childObjectB2.appendChild(dataB2);

        anyXmlObjectB.appendChild(childObjectB1);
        anyXmlObjectB.appendChild(childObjectB2);

        rootElement.appendChild(anyXmlObjectA);
        rootElement.appendChild(anyXmlObjectB);

        doc.appendChild(rootElement);

        return new DOMSource(doc.getDocumentElement());
    }
}
