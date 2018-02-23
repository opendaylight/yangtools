/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.xml.sax.SAXException;

public class XmlToNormalizedNodesTest {

    private static final QNameModule FOO_MODULE = QNameModule.create(URI.create("foo-namespace"));
    private static final QName PARENT_CONTAINER = QName.create(FOO_MODULE, "parent-container");

    private static final QNameModule BAZ_MODULE = QNameModule.create(URI.create("baz-namespace"));
    private static final QName OUTER_CONTAINER = QName.create(BAZ_MODULE, "outer-container");

    private static final QName MY_CONTAINER_1 = QName.create(BAZ_MODULE, "my-container-1");
    private static final QName MY_KEYED_LIST = QName.create(BAZ_MODULE, "my-keyed-list");
    private static final QName MY_KEY_LEAF = QName.create(BAZ_MODULE, "my-key-leaf");
    private static final QName MY_LEAF_IN_LIST_1 = QName.create(BAZ_MODULE, "my-leaf-in-list-1");
    private static final QName MY_LEAF_IN_LIST_2 = QName.create(BAZ_MODULE, "my-leaf-in-list-2");
    private static final QName MY_LEAF_1 = QName.create(BAZ_MODULE, "my-leaf-1");
    private static final QName MY_LEAFLIST = QName.create(BAZ_MODULE, "my-leaf-list");

    private static final QName MY_CONTAINER_2 = QName.create(BAZ_MODULE, "my-container-2");
    private static final QName INNER_CONTAINER = QName.create(BAZ_MODULE, "inner-container");
    private static final QName MY_LEAF_2 = QName.create(BAZ_MODULE, "my-leaf-2");
    private static final QName MY_LEAF_3 = QName.create(BAZ_MODULE, "my-leaf-3");
    private static final QName MY_CHOICE = QName.create(BAZ_MODULE, "my-choice");
    private static final QName MY_LEAF_IN_CASE_2 = QName.create(BAZ_MODULE, "my-leaf-in-case-2");

    private static final QName MY_CONTAINER_3 = QName.create(BAZ_MODULE, "my-container-3");
    private static final QName MY_DOUBLY_KEYED_LIST = QName.create(BAZ_MODULE, "my-doubly-keyed-list");
    private static final QName MY_FIRST_KEY_LEAF = QName.create(BAZ_MODULE, "my-first-key-leaf");
    private static final QName MY_SECOND_KEY_LEAF = QName.create(BAZ_MODULE, "my-second-key-leaf");
    private static final QName MY_LEAF_IN_LIST_3 = QName.create(BAZ_MODULE, "my-leaf-in-list-3");

    private static SchemaContext schemaContext;
    private static ContainerSchemaNode outerContainerSchema;
    private static ContainerSchemaNode parentContainerSchema;

    @BeforeClass
    public static void setup() {
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/");
        parentContainerSchema = (ContainerSchemaNode) SchemaContextUtil.findNodeInSchemaContext(schemaContext,
                ImmutableList.of(PARENT_CONTAINER));
        outerContainerSchema = (ContainerSchemaNode) SchemaContextUtil.findNodeInSchemaContext(schemaContext,
                ImmutableList.of(OUTER_CONTAINER));
    }

    @AfterClass
    public static void cleanup() {
        schemaContext = null;
        parentContainerSchema = null;
        outerContainerSchema = null;
    }

    @Test
    public void testComplexXmlParsing() throws IOException, SAXException, URISyntaxException, XMLStreamException,
            ParserConfigurationException {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/baz.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, outerContainerSchema);
        xmlParser.parse(reader);

        xmlParser.flush();
        xmlParser.close();

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);

        final NormalizedNode<?, ?> expectedNormalizedNode = buildOuterContainerNode();
        assertNotNull(expectedNormalizedNode);

        assertEquals(expectedNormalizedNode, transformedInput);
    }

    @Test
    public void testSimpleXmlParsing() throws IOException, URISyntaxException, XMLStreamException,
            ParserConfigurationException, SAXException {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/foo.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, parentContainerSchema);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void shouldFailOnDuplicateLeaf() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-foo.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, parentContainerSchema);
        try {
            xmlParser.parse(reader);
            fail("IllegalStateException should have been thrown because of duplicate leaf.");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Duplicate element \"decimal64-leaf\" in XML input"));
        }

    }

    @Test
    public void shouldFailOnDuplicateAnyXml() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-foo-2.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, parentContainerSchema);
        try {
            xmlParser.parse(reader);
            fail("IllegalStateException should have been thrown because of duplicate anyxml");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Duplicate element \"my-anyxml\" in XML input"));
        }
    }

    @Test
    public void shouldFailOnDuplicateContainer() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-foo-3.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, parentContainerSchema);
        try {
            xmlParser.parse(reader);
            fail("IllegalStateException should have been thrown because of duplicate container");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Duplicate element \"leaf-container\" in XML input"));
        }
    }

    @Test
    public void shouldFailOnUnterminatedLeafElement() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-baz.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, outerContainerSchema);
        try {
            xmlParser.parse(reader);
            fail("XMLStreamException should have been thrown because of unterminated leaf element.");
        } catch (XMLStreamException ex) {
            assertTrue(ex.getMessage().contains("elementGetText() function expects text only elment but "
                        + "START_ELEMENT was encountered."));
        }
    }

    @Test
    public void shouldFailOnUnterminatedLeafElement2() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-baz-2.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, outerContainerSchema);
        try {
            xmlParser.parse(reader);
            fail("XMLStreamException should have been thrown because of unterminated leaf element.");
        } catch (XMLStreamException ex) {
            assertTrue(ex.getMessage().contains("The element type \"my-leaf-1\" must be terminated by the matching "
                        + "end-tag \"</my-leaf-1>\"."));
        }
    }

    @Test
    public void shouldFailOnUnterminatedContainerElement() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-baz-4.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, outerContainerSchema);
        try {
            xmlParser.parse(reader);
            fail("XMLStreamException should have been thrown because of unterminated container element.");
        } catch (XMLStreamException ex) {
            assertTrue(ex.getMessage().contains("The element type \"my-container-1\" must be terminated by the "
                        + "matching end-tag \"</my-container-1>\"."));
        }
    }

    @Test
    public void shouldFailOnUnknownChildNode() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-baz-3.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, outerContainerSchema);
        try {
            xmlParser.parse(reader);
            fail("IllegalStateException should have been thrown because of an unknown child node.");
        } catch (IllegalStateException ex) {
            assertEquals("Schema for node with name my-container-1 and namespace baz-namespace does not exist at "
                    + "AbsoluteSchemaPath{path=[(baz-namespace)outer-container, (baz-namespace)my-container-1]}",
                    ex.getMessage());
        }
    }

    private static NormalizedNode<?, ?> buildOuterContainerNode() {
        // my-container-1
        MapNode myKeyedListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(MY_KEYED_LIST))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(MY_KEYED_LIST, MY_KEY_LEAF, "listkeyvalue1"))
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(MY_LEAF_IN_LIST_1))
                                .withValue("listleafvalue1").build())
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(MY_LEAF_IN_LIST_2))
                                .withValue("listleafvalue2").build()).build())
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(MY_KEYED_LIST, MY_KEY_LEAF, "listkeyvalue2"))
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(MY_LEAF_IN_LIST_1))
                                .withValue("listleafvalue12").build())
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(MY_LEAF_IN_LIST_2))
                                .withValue("listleafvalue22").build()).build()).build();

        LeafNode<?> myLeaf1Node = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(MY_LEAF_1))
                .withValue("value1").build();

        LeafSetNode<?> myLeafListNode = Builders.leafSetBuilder().withNodeIdentifier(new NodeIdentifier(MY_LEAFLIST))
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(MY_LEAFLIST, "lflvalue1")).withValue("lflvalue1").build())
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(MY_LEAFLIST, "lflvalue2")).withValue("lflvalue2").build()).build();

        ContainerNode myContainer1Node = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(MY_CONTAINER_1))
                .withChild(myKeyedListNode)
                .withChild(myLeaf1Node)
                .withChild(myLeafListNode).build();

        // my-container-2
        ContainerNode innerContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(INNER_CONTAINER))
                .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(MY_LEAF_2))
                        .withValue("value2").build()).build();

        LeafNode<?> myLeaf3Node = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(MY_LEAF_3))
                .withValue("value3").build();

        ChoiceNode myChoiceNode = Builders.choiceBuilder().withNodeIdentifier(new NodeIdentifier(MY_CHOICE))
                .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(MY_LEAF_IN_CASE_2))
                        .withValue("case2value").build()).build();

        ContainerNode myContainer2Node = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(MY_CONTAINER_2))
                .withChild(innerContainerNode)
                .withChild(myLeaf3Node)
                .withChild(myChoiceNode).build();

        // my-container-3
        Map<QName, Object> keys = new HashMap<>();
        keys.put(MY_FIRST_KEY_LEAF, "listkeyvalue1");
        keys.put(MY_SECOND_KEY_LEAF, "listkeyvalue2");

        MapNode myDoublyKeyedListNode = Builders.mapBuilder()
                .withNodeIdentifier(new NodeIdentifier(MY_DOUBLY_KEYED_LIST))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(MY_DOUBLY_KEYED_LIST, keys))
                        .withChild(Builders.leafBuilder().withNodeIdentifier(
                                new NodeIdentifier(MY_LEAF_IN_LIST_3)).withValue("listleafvalue1").build()).build())
                .build();

        AugmentationNode myDoublyKeyedListAugNode = Builders.augmentationBuilder().withNodeIdentifier(
                new AugmentationIdentifier(Collections.singleton(MY_DOUBLY_KEYED_LIST)))
                .withChild(myDoublyKeyedListNode).build();

        ContainerNode myContainer3Node = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(MY_CONTAINER_3))
                .withChild(myDoublyKeyedListAugNode).build();

        AugmentationNode myContainer3AugNode = Builders.augmentationBuilder().withNodeIdentifier(
                new AugmentationIdentifier(Collections.singleton(MY_CONTAINER_3)))
                .withChild(myContainer3Node).build();

        ContainerNode outerContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(OUTER_CONTAINER))
                .withChild(myContainer1Node)
                .withChild(myContainer2Node)
                .withChild(myContainer3AugNode).build();

        return outerContainerNode;
    }
}
