/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNormalizationException;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.xml.sax.SAXException;

public class AnydataNormalizeToContainerTest extends AbstractAnydataTest {

    @Test
    public void testAnydataNormalizeToContainer()
            throws XMLStreamException, SAXException, IOException, URISyntaxException, AnydataNormalizationException {
        //Create Data Scheme from yang file
        SchemaPath anydataPath = SchemaPath.create(true, FOO_QNAME);
        final SchemaNode fooSchemaNode = SchemaContextUtil.findDataSchemaNode(SCHEMA_CONTEXT, anydataPath);
        assertTrue(fooSchemaNode instanceof AnydataSchemaNode);
        final AnydataSchemaNode anyDataSchemaNode = (AnydataSchemaNode) fooSchemaNode;

        SchemaPath containerPath = SchemaPath.create(true, CONT_QNAME);
        final SchemaNode barSchemaNode = SchemaContextUtil.findDataSchemaNode(SCHEMA_CONTEXT, containerPath);
        assertTrue(barSchemaNode instanceof ContainerSchemaNode);
        final ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) barSchemaNode;

        // deserialization
        final XMLStreamReader reader
                = UntrustedXML.createXMLStreamReader(toInputStream("<foo xmlns=\"test-anydata\">"
                                                                  +     "<bar xmlns=\"test-anydata\">"
                                                                  +         "<cont-leaf>somedata</cont-leaf>"
                                                                  +     "</bar>"
                                                                  + "</foo>"));

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, SCHEMA_CONTEXT, anyDataSchemaNode);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
        assertTrue(transformedInput instanceof AnydataNode);
        AnydataNode<?> anydataNode = (AnydataNode<?>) transformedInput;

        //Normalize anydata content to specific container element
        DOMSourceAnydata domSourceAnydata = (DOMSourceAnydata) anydataNode.getValue();
        NormalizedAnydata normalizedAnydata = domSourceAnydata.normalizeTo(SCHEMA_CONTEXT, containerSchemaNode);
        assertNotNull(normalizedAnydata);
    }

    @Test
    public void testAnydataNormalizeToContainerIfEmpty()
            throws XMLStreamException, SAXException, IOException, URISyntaxException, AnydataNormalizationException {
        //Create Data Scheme from yang file
        SchemaPath anydataPath = SchemaPath.create(true, FOO_QNAME);
        final SchemaNode fooSchemaNode = SchemaContextUtil.findDataSchemaNode(SCHEMA_CONTEXT, anydataPath);
        assertTrue(fooSchemaNode instanceof AnyDataSchemaNode);
        final AnyDataSchemaNode anyDataSchemaNode = (AnyDataSchemaNode) fooSchemaNode;

        SchemaPath containerPath = SchemaPath.create(true, CONT_QNAME);
        final SchemaNode barSchemaNode = SchemaContextUtil.findDataSchemaNode(SCHEMA_CONTEXT, containerPath);
        assertTrue(barSchemaNode instanceof ContainerSchemaNode);
        final ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) barSchemaNode;

        // deserialization of empty anyDataNode
        final XMLStreamReader reader
                = UntrustedXML.createXMLStreamReader(toInputStream("<foo xmlns=\"test-anydata\">"
                + "</foo>"));

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, SCHEMA_CONTEXT, anyDataSchemaNode);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
        assertTrue(transformedInput instanceof AnydataNode);
        AnydataNode<?> anydataNode = (AnydataNode<?>) transformedInput;

        //Normalize anydata content to specific container element
        DOMSourceAnydata domSourceAnydata = (DOMSourceAnydata) anydataNode.getValue();
        NormalizedAnydata normalizedAnydata = domSourceAnydata.normalizeTo(SCHEMA_CONTEXT, containerSchemaNode);
        assertNotNull(normalizedAnydata);
    }
}
