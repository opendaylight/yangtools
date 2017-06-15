/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug8675Test {

    private static SchemaContext schemaContext;
    private static Module fooModule;

    @BeforeClass
    public static void setup() throws Exception {
        schemaContext = YangParserTestUtils.parseYangSource("/bug8675/foo.yang");
        fooModule = schemaContext.getModules().iterator().next();
    }

    @Test
    public void testParsingEmptyElements() throws Exception {
        final ContainerSchemaNode topLevelContainer = (ContainerSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-container"));
        assertNotNull(topLevelContainer);

        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream(
                "/bug8675/foo.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelContainer);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testParsingEmptyRootElement() throws Exception {
        final ContainerSchemaNode topLevelContainer = (ContainerSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-container"));
        assertNotNull(topLevelContainer);

        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream(
                "/bug8675/foo-2.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelContainer);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testListAsRootElement() throws Exception {
        final ListSchemaNode topLevelList = (ListSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-list"));
        assertNotNull(topLevelList);

        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-3.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelList);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testAnyXmlAsRootElement() throws Exception {
        final AnyXmlSchemaNode topLevelAnyXml = (AnyXmlSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-anyxml"));
        assertNotNull(topLevelAnyXml);

        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-4.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelAnyXml);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testLeafAsRootElement() throws Exception {
        final LeafSchemaNode topLevelLeaf = (LeafSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-leaf"));

        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-5.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelLeaf);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testLeafListAsRootElement() throws Exception {
        final LeafListSchemaNode topLevelLeafList = (LeafListSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-leaf-list"));

        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-6.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelLeafList);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }
}
