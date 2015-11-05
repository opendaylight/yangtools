/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.data.codec.gson.retest.TestUtils.loadModules;
import static org.opendaylight.yangtools.yang.data.codec.gson.retest.TestUtils.loadTextFile;

import com.google.common.base.Preconditions;
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
import java.util.Collections;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonWriterFactory;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser.DomToNormalizedNodeParserFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class YangModeledAnyXmlSupportTest {

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

    private static SchemaContext schemaContext;
    private static Document xmlDoc;
    private static ContainerNode data;

    @BeforeClass
    public static void init() throws IOException, URISyntaxException, ReactorException, SAXException {
        schemaContext = loadModules("/yang-modeled-anyxml/yang");
        xmlDoc = loadDocument("/yang-modeled-anyxml/xml/baz.xml");
        data = DomToNormalizedNodeParserFactory
                .getInstance(DomUtils.defaultValueCodecProvider(), schemaContext).getContainerNodeParser()
                .parse(Collections.singletonList(xmlDoc.getDocumentElement()), schemaContext);
    }

    @Test
    public void jsonToNormalizedNodesTest() throws IOException, URISyntaxException, SAXException {
        final String inputJson = loadTextFile("/yang-modeled-anyxml/json/baz.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();

        assertNotNull(transformedInput);
    }

    @Test
    public void normalizedNodesToJsonTest() throws IOException, URISyntaxException, SAXException {
        final DataContainerChild<? extends PathArgument, ?> baz = data.getValue().iterator().next();

        final Writer writer = new StringWriter();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, baz);

        final JsonParser parser = new JsonParser();
        final JsonElement serializedJson = parser.parse(jsonOutput);
        final JsonElement expextedJson = parser.parse(new FileReader(new File(getClass().getResource(
                "/yang-modeled-anyxml/json/baz.json").toURI())));
        assertEquals(expextedJson, serializedJson);
    }

    private String normalizedNodeToJsonStreamTransformation(final Writer writer,
            final NormalizedNode<?, ?> inputStructure) throws IOException {

        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
                JSONCodecFactory.create(schemaContext), SchemaPath.ROOT, null,
                JsonWriterFactory.createJsonWriter(writer, 2));
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
        nodeWriter.write(inputStructure);

        nodeWriter.close();
        return writer.toString();
    }

    private static Document loadDocument(final String xmlPath) throws IOException, SAXException {
        final InputStream resourceAsStream = YangModeledAnyXmlSupportTest.class.getResourceAsStream(xmlPath);

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

}
