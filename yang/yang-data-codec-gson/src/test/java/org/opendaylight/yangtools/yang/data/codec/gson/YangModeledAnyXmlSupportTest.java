/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.codec.xml.XmlParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.xml.sax.SAXException;

public class YangModeledAnyXmlSupportTest {

    private static SchemaContext schemaContext;
    private static ContainerNode data;

    @BeforeClass
    public static void init() throws IOException, URISyntaxException, ReactorException, SAXException,
            XMLStreamException, ParserConfigurationException {
        schemaContext = YangParserTestUtils.parseYangSources("/yang-modeled-anyxml/yang");
        final Module bazModule = schemaContext.findModuleByName("baz", null);
        final ContainerSchemaNode bazCont = (ContainerSchemaNode) bazModule.getDataChildByName(
                QName.create(bazModule.getQNameModule(), "baz"));
        assertNotNull(bazCont);

        final InputStream resourceAsStream = YangModeledAnyXmlSupportTest.class.getResourceAsStream(
                "/yang-modeled-anyxml/xml/baz.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();

        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, bazCont);
        xmlParser.parse(reader);

        assertNotNull(result.getResult());
        assertTrue(result.getResult() instanceof ContainerNode);
        data = (ContainerNode) result.getResult();
    }

    @Test
    public void jsonToNormalizedNodesTest() throws IOException, URISyntaxException, SAXException {
        final String inputJson = loadTextFile("/yang-modeled-anyxml/json/baz.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();

        assertEquals(data, transformedInput);
    }

    @Test
    public void normalizedNodesToJsonTest() throws IOException, URISyntaxException, SAXException {
        final DataContainerChild<? extends PathArgument, ?> baz = data;

        final Writer writer = new StringWriter();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, baz);

        final JsonParser parser = new JsonParser();
        final JsonElement serializedJson = parser.parse(jsonOutput);
        final JsonElement expextedJson = parser.parse(new FileReader(new File(getClass().getResource(
                "/yang-modeled-anyxml/json/baz.json").toURI())));

        assertEquals(expextedJson, serializedJson);
    }

    private static String normalizedNodeToJsonStreamTransformation(final Writer writer,
            final NormalizedNode<?, ?> inputStructure) throws IOException {

        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
                JSONCodecFactory.getShared(schemaContext), SchemaPath.ROOT, null,
                JsonWriterFactory.createJsonWriter(writer, 2));
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
        nodeWriter.write(inputStructure);

        nodeWriter.close();
        return writer.toString();
    }
}