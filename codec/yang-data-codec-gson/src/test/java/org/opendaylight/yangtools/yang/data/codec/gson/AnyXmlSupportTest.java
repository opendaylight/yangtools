/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadXmlToNormalizedNodes;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.normalizedNodesToJsonString;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.dom.DOMSource;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

class AnyXmlSupportTest extends AbstractComplexJsonTest {
    private static final QName LF12_ANY = QName.create(CONT_1, "lf12-any");
    private static final QName LF13_ANY = QName.create(CONT_1, "lf13-any");
    private static final QName LF14_ANY = QName.create(CONT_1, "lf14-any");

    @Test
    void anyXmlNodeWithSimpleValueInContainer() throws IOException, URISyntaxException {
        final var inputJson = loadTextFile("/complexjson/anyxml-node-with-simple-value-in-container.json");

        // deserialization
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);

        // lf12-any check
        final var lf12AnyActualValue = getParsedAnyXmlValue(transformedInput, LF12_ANY);
        final var lf12AnyExpectedValue = createAnyXmlSimpleValue("ns:complex:json", "lf12-any", "100.5");
        verifyTransformedAnyXmlNodeValue(lf12AnyExpectedValue, lf12AnyActualValue);

        // lf13-any check
        final var lf13AnyActualValue = getParsedAnyXmlValue(transformedInput, LF13_ANY);
        final var lf13AnyExpectedValue = createAnyXmlSimpleValue("ns:complex:json", "lf13-any", "true");
        verifyTransformedAnyXmlNodeValue(lf13AnyExpectedValue, lf13AnyActualValue);

        // lf14-any check
        final var lf14AnyActualValue = getParsedAnyXmlValue(transformedInput, LF14_ANY);
        final var lf14AnyExpectedValue = createAnyXmlSimpleValue("ns:complex:json", "lf14-any", "null");
        verifyTransformedAnyXmlNodeValue(lf14AnyExpectedValue, lf14AnyActualValue);

        final var serializationResult = normalizedNodesToJsonString(transformedInput, schemaContext);

        assertEquals(JsonParser.parseString(inputJson), JsonParser.parseString(serializationResult));
    }

    @Test
    void anyXmlNodeWithCompositeValueInContainer() throws IOException, URISyntaxException {
        final var inputJson = loadTextFile("/complexjson/anyxml-node-with-composite-value-in-container.json");

        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);

        // lf12-any check
        final var lf12AnyActualValue = getParsedAnyXmlValue(transformedInput, LF12_ANY);
        final var lf12AnyExpectedValue = createLf12AnyXmlCompositeValue("ns:complex:json", "lf12-any");
        verifyTransformedAnyXmlNodeValue(lf12AnyExpectedValue, lf12AnyActualValue);
        // lf13-any check
        final var lf13AnyActualValue = getParsedAnyXmlValue(transformedInput, LF13_ANY);
        final var lf13AnyExpectedValue = createLf13AnyXmlCompositeValue("ns:complex:json", "lf13-any");
        verifyTransformedAnyXmlNodeValue(lf13AnyExpectedValue, lf13AnyActualValue);

        // lf14-any check
        final var lf14AnyActualValue = getParsedAnyXmlValue(transformedInput, LF14_ANY);
        final var lf14AnyExpectedValue = createLf14AnyXmlCompositeValue("ns:complex:json", "lf14-any");
        verifyTransformedAnyXmlNodeValue(lf14AnyExpectedValue, lf14AnyActualValue);

        // serialization
        final var serializationResult = normalizedNodesToJsonString(transformedInput, schemaContext);

        assertEquals(JsonParser.parseString(loadTextFile("/bug8927/json/composite.json")),
            JsonParser.parseString(serializationResult));
    }

    @Test
    void bug8927TestComplexArrayWithOthers() throws Exception {
        executebug8927Test("/bug8927/xml/complex_array_with_other_elements.xml",
            "/bug8927/json/complex_array_with_other_elements.json");
    }

    @Test
    void bug8927TestComplexArray() throws Exception {
        executebug8927Test("/bug8927/xml/complex_array.xml", "/bug8927/json/complex_array.json");
    }

    @Test
    void bug8927TestScalarArrayWithOthers() throws Exception {
        executebug8927Test("/bug8927/xml/scalar_array_with_other_elements.xml",
            "/bug8927/json/scalar_array_with_other_elements.json");
    }

    @Test
    void bug8927TestScalarArray() throws Exception {
        executebug8927Test("/bug8927/xml/scalar_array.xml", "/bug8927/json/scalar_array.json");
    }

    @Test
    void yt1302TestFlatArray() throws Exception {
        executeYt1302Test("/yt1302/xml/flat-array.xml", "/yt1302/json/flat-array.json");
    }

    @Test
    void yt1302TestArrayOfArray() throws Exception {
        executeYt1302Test("/yt1302/xml/array-of-array.xml", "/yt1302/json/array-of-array.json");
    }

    private void executebug8927Test(final String inputXmlFile, final String expectedJsonFile) throws Exception {
        verifyExpectedJson(inputXmlFile, expectedJsonFile, "bug8927.test", "2017-01-01", "foo");
    }

    private void executeYt1302Test(final String inputXmlFile, final String expectedJsonFile) throws Exception {
        verifyExpectedJson(inputXmlFile, expectedJsonFile, "yt1302.test", "2022-10-11", "data");
    }

    private void verifyExpectedJson(final String inputXmlFile, final String expectedJsonFile,
            final String namespace, final String revision, final String localName) throws Exception {
        final var resourceAsStream = AnyXmlSupportTest.class.getResourceAsStream(inputXmlFile);
        final var result = new NormalizationResultHolder();
        loadXmlToNormalizedNodes(resourceAsStream, result, schemaContext);

        final var data = assertInstanceOf(ContainerNode.class, result.getResult().data())
                .childByArg(new NodeIdentifier(QName.create(namespace, revision, localName)));
        assertNotNull(data);
        final var jsonOutput = normalizedNodesToJsonString(data, schemaContext);
        assertEquals(JsonParser.parseReader(new FileReader(
                        new File(getClass().getResource(expectedJsonFile).toURI()), StandardCharsets.UTF_8)),
                JsonParser.parseString(jsonOutput));
    }

    private static DOMSource getParsedAnyXmlValue(final NormalizedNode transformedInput, final QName anyxmlName) {
        final var cont1 = assertInstanceOf(ContainerNode.class, transformedInput);
        return assertInstanceOf(DOMSourceAnyxmlNode.class, cont1.childByArg(new NodeIdentifier(anyxmlName)))
            .body();
    }

    private static void verifyTransformedAnyXmlNodeValue(final DOMSource expectedValue, final DOMSource actualValue) {
        assertTrue(expectedValue.getNode().isEqualNode(actualValue.getNode()));
    }

    private static DOMSource createAnyXmlSimpleValue(final String ns, final String name, final String value) {
        final var doc = UntrustedXML.newDocumentBuilder().newDocument();
        final var rootElement = doc.createElementNS(ns, name);
        doc.appendChild(rootElement);
        final var textNode = doc.createTextNode(value);
        rootElement.appendChild(textNode);
        return new DOMSource(doc.getDocumentElement());
    }

    private static DOMSource createLf12AnyXmlCompositeValue(final String ns, final String name) {
        final var doc = UntrustedXML.newDocumentBuilder().newDocument();
        final var rootElement = doc.createElementNS(ns, name);

        final var arrayElement1 = doc.createElement("array-element");
        final var arrayElement1Text = doc.createTextNode("true");
        arrayElement1.appendChild(arrayElement1Text);

        final var arrayElement2 = doc.createElement("array-element");

        final var arrayElement2Baz = doc.createElement("baz");
        final var bazArrayElement1 = doc.createElement("array-element");
        final var bazArrayElement1Text = doc.createTextNode("120");
        bazArrayElement1.appendChild(bazArrayElement1Text);
        final var bazArrayElement2 = doc.createElement("array-element");
        final var bazArrayElement2Text = doc.createTextNode("str-val");
        bazArrayElement2.appendChild(bazArrayElement2Text);
        final var bazArrayElement3 = doc.createElement("array-element");
        final var bazArrayElement3Text = doc.createTextNode("false");
        bazArrayElement3.appendChild(bazArrayElement3Text);

        arrayElement2Baz.appendChild(bazArrayElement1);
        arrayElement2Baz.appendChild(bazArrayElement2);
        arrayElement2Baz.appendChild(bazArrayElement3);

        arrayElement2.appendChild(arrayElement2Baz);

        final var arrayElement3 = doc.createElement("array-element");
        final var arrayElement3Foo = doc.createElement("foo");
        final var fooText = doc.createTextNode("null");
        arrayElement3Foo.appendChild(fooText);
        arrayElement3.appendChild(arrayElement3Foo);

        rootElement.appendChild(arrayElement1);
        rootElement.appendChild(arrayElement2);
        rootElement.appendChild(arrayElement3);

        doc.appendChild(rootElement);

        return new DOMSource(doc.getDocumentElement());
    }

    private static DOMSource createLf13AnyXmlCompositeValue(final String ns, final String name) {
        final var doc = UntrustedXML.newDocumentBuilder().newDocument();
        final var rootElement = doc.createElementNS(ns, name);

        final var anyXmlArrayA = doc.createElement("anyxml-array-a");

        final var arrayAElement1 = doc.createElement("array-element");
        final var arrayAElement1Foo = doc.createElement("foo");
        final var fooText = doc.createTextNode("true");
        arrayAElement1Foo.appendChild(fooText);
        arrayAElement1.appendChild(arrayAElement1Foo);

        final var arrayAElement2 = doc.createElement("array-element");
        final var arrayAElement2Text = doc.createTextNode("10");
        arrayAElement2.appendChild(arrayAElement2Text);

        final var arrayAElement3 = doc.createElement("array-element");
        final var arrayAElement3Bar = doc.createElement("bar");
        final var barText = doc.createTextNode("false");
        arrayAElement3Bar.appendChild(barText);
        arrayAElement3.appendChild(arrayAElement3Bar);

        anyXmlArrayA.appendChild(arrayAElement1);
        anyXmlArrayA.appendChild(arrayAElement2);
        anyXmlArrayA.appendChild(arrayAElement3);

        final var anyXmlArrayB = doc.createElement("anyxml-array-b");

        final var arrayBElement1 = doc.createElement("array-element");
        final Text arrayBElement1Text = doc.createTextNode("1");
        arrayBElement1.appendChild(arrayBElement1Text);

        final var arrayBElement2 = doc.createElement("array-element");
        final Text arrayBElement2Text = doc.createTextNode("2");
        arrayBElement2.appendChild(arrayBElement2Text);

        final var arrayBElement3 = doc.createElement("array-element");

        final var arrayBElement3Element1 = doc.createElement("array-element");
        final var arrayBElement3Element1Text = doc.createTextNode("4");
        arrayBElement3Element1.appendChild(arrayBElement3Element1Text);
        final Element arrayBElement3Element2 = doc.createElement("array-element");
        final var arrayBElement3Element2Text = doc.createTextNode("5");
        arrayBElement3Element2.appendChild(arrayBElement3Element2Text);

        arrayBElement3.appendChild(arrayBElement3Element1);
        arrayBElement3.appendChild(arrayBElement3Element2);

        final var arrayBElement4 = doc.createElement("array-element");
        final var arrayBElement4Text = doc.createTextNode("7");
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
        final var doc = UntrustedXML.newDocumentBuilder().newDocument();
        final var rootElement = doc.createElementNS(ns, name);

        final var anyXmlObjectA = doc.createElement("anyxml-object-a");
        final var dataA1 = doc.createElement("data-a1");
        final Text dataA1Text = doc.createTextNode("10");
        dataA1.appendChild(dataA1Text);
        final var dataA2 = doc.createElement("data-a2");
        final var dataA2Text = doc.createTextNode("11");
        dataA2.appendChild(dataA2Text);

        anyXmlObjectA.appendChild(dataA1);
        anyXmlObjectA.appendChild(dataA2);

        final var anyXmlObjectB = doc.createElement("anyxml-object-b");

        final var childObjectB1 = doc.createElement("child-object-b1");
        final var dataB1 = doc.createElement("data-b1");
        final var dataB1Text = doc.createTextNode("5.5");
        dataB1.appendChild(dataB1Text);
        childObjectB1.appendChild(dataB1);

        final var childObjectB2 = doc.createElement("child-object-b2");
        final var dataB2 = doc.createElement("data-b2");
        final var dataB2Text = doc.createTextNode("b2-val");
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
