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
import javax.xml.stream.XMLStreamReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug8675Test {

    private static EffectiveModelContext schemaContext;
    private static Module fooModule;

    @BeforeClass
    public static void setup() {
        schemaContext = YangParserTestUtils.parseYangResource("/bug8675/foo.yang");
        fooModule = schemaContext.getModules().iterator().next();
    }

    @AfterClass
    public static void cleanup() {
        fooModule = null;
        schemaContext = null;
    }

    @Test
    public void testParsingEmptyElements() throws Exception {
        final ContainerSchemaNode topLevelContainer = (ContainerSchemaNode) fooModule.findDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-container")).get();

        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream(
                "/bug8675/foo.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelContainer);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testParsingEmptyRootElement() throws Exception {
        final ContainerSchemaNode topLevelContainer = (ContainerSchemaNode) fooModule.findDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-container")).get();

        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream(
                "/bug8675/foo-2.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelContainer);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testListAsRootElement() throws Exception {
        final ListSchemaNode topLevelList = (ListSchemaNode) fooModule.findDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-list")).get();

        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-3.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelList);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testAnyXmlAsRootElement() throws Exception {
        final AnyxmlSchemaNode topLevelAnyXml = (AnyxmlSchemaNode) fooModule.findDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-anyxml")).get();

        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-4.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelAnyXml);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testLeafAsRootElement() throws Exception {
        final LeafSchemaNode topLevelLeaf = (LeafSchemaNode) fooModule.findDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-leaf")).get();

        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-5.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelLeaf);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testLeafListAsRootElement() throws Exception {
        final LeafListSchemaNode topLevelLeafList = (LeafListSchemaNode) fooModule.findDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-leaf-list")).get();

        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-6.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelLeafList);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }
}
