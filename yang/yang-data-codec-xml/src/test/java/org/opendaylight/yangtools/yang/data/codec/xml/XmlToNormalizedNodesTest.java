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
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.xml.sax.SAXException;

public class XmlToNormalizedNodesTest {

    private static SchemaContext schemaContext;
    private static ContainerSchemaNode outerContainerSchema;
    private static ContainerSchemaNode parentContainerSchema;

    private static QNameModule fooModule;
    private static QName parentContainer;

    private static QNameModule bazModule;
    private static QName outerContainer;

    private static QName myContainer1;
    private static QName myKeyedList;
    private static QName myKeyLeaf;
    private static QName myLeafInList1;
    private static QName myLeafInList2;
    private static QName myLeaf1;
    private static QName myLeafList;

    private static QName myContainer2;
    private static QName innerContainer;
    private static QName myLeaf2;
    private static QName myLeaf3;
    private static QName myChoice;
    private static QName myLeafInCase2;

    private static QName myContainer3;
    private static QName myDoublyKeyedList;
    private static QName myFirstKeyLeaf;
    private static QName mySecondKeyLeaf;
    private static QName myLeafInList3;

    @BeforeClass
    public static void setup() throws Exception {
        fooModule = QNameModule.create(new URI("foo-namespace"), SimpleDateFormatUtil.getRevisionFormat().parse(
                "1970-01-01"));
        parentContainer = QName.create(fooModule, "parent-container");

        bazModule = QNameModule.create(new URI("baz-namespace"), SimpleDateFormatUtil.getRevisionFormat().parse(
                    "1970-01-01"));
        outerContainer = QName.create(bazModule, "outer-container");

        myContainer1 = QName.create(bazModule, "my-container-1");
        myKeyedList = QName.create(bazModule, "my-keyed-list");
        myKeyLeaf = QName.create(bazModule, "my-key-leaf");
        myLeafInList1 = QName.create(bazModule, "my-leaf-in-list-1");
        myLeafInList2 = QName.create(bazModule, "my-leaf-in-list-2");
        myLeaf1 = QName.create(bazModule, "my-leaf-1");
        myLeafList = QName.create(bazModule, "my-leaf-list");

        myContainer2 = QName.create(bazModule, "my-container-2");
        innerContainer = QName.create(bazModule, "inner-container");
        myLeaf2 = QName.create(bazModule, "my-leaf-2");
        myLeaf3 = QName.create(bazModule, "my-leaf-3");
        myChoice = QName.create(bazModule, "my-choice");
        myLeafInCase2 = QName.create(bazModule, "my-leaf-in-case-2");

        myContainer3 = QName.create(bazModule, "my-container-3");
        myDoublyKeyedList = QName.create(bazModule, "my-doubly-keyed-list");
        myFirstKeyLeaf = QName.create(bazModule, "my-first-key-leaf");
        mySecondKeyLeaf = QName.create(bazModule, "my-second-key-leaf");
        myLeafInList3 = QName.create(bazModule, "my-leaf-in-list-3");

        schemaContext = YangParserTestUtils.parseYangSources("/");
        parentContainerSchema = (ContainerSchemaNode) SchemaContextUtil.findNodeInSchemaContext(schemaContext,
                ImmutableList.of(parentContainer));
        outerContainerSchema = (ContainerSchemaNode) SchemaContextUtil.findNodeInSchemaContext(schemaContext,
                ImmutableList.of(outerContainer));
    }

    @Test
    public void testComplexXmlParsing() throws IOException, URISyntaxException, ReactorException, XMLStreamException,
            ParserConfigurationException, SAXException {
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
    public void testSimpleXmlParsing() throws IOException, URISyntaxException, ReactorException, XMLStreamException,
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
    public void shouldFailOnDuplicateLeaf() throws ReactorException, XMLStreamException, IOException,
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
    public void shouldFailOnDuplicateAnyXml() throws ReactorException, XMLStreamException, IOException,
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
    public void shouldFailOnDuplicateContainer() throws ReactorException, XMLStreamException, IOException,
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
    public void shouldFailOnUnterminatedLeafElement() throws ReactorException, XMLStreamException, IOException,
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
    public void shouldFailOnUnterminatedLeafElement2() throws ReactorException, XMLStreamException, IOException,
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
    public void shouldFailOnUnterminatedContainerElement() throws ReactorException, XMLStreamException, IOException,
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
    public void shouldFailOnUnexistingContainerElement() throws ReactorException, XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        SchemaContext schemaContext = YangParserTestUtils.parseYangSource("/baz.yang");

        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-baz-3.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, outerContainerSchema);
        try {
            xmlParser.parse(reader);
            fail("IllegalStateException should have been thrown because of an unexisting container element.");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Schema for node with name my-container-1 and namespace baz-namespace"
                        + " doesn't exist."));
        }
    }

    private NormalizedNode<?, ?> buildOuterContainerNode() {
        // my-container-1
        MapNode myKeyedListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(myKeyedList))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myKeyedList, myKeyLeaf, "listkeyvalue1"))
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInList1))
                                .withValue("listleafvalue1").build())
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInList2))
                                .withValue("listleafvalue2").build()).build())
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myKeyedList, myKeyLeaf, "listkeyvalue2"))
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInList1))
                                .withValue("listleafvalue12").build())
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInList2))
                                .withValue("listleafvalue22").build()).build()).build();

        LeafNode<?> myLeaf1Node = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeaf1))
                .withValue("value1").build();

        LeafSetNode<?> myLeafListNode = Builders.leafSetBuilder().withNodeIdentifier(new NodeIdentifier(myLeafList))
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(myLeafList, "lflvalue1")).withValue("lflvalue1").build())
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(myLeafList, "lflvalue2")).withValue("lflvalue2").build()).build();

        ContainerNode myContainer1Node = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer1))
                .withChild(myKeyedListNode)
                .withChild(myLeaf1Node)
                .withChild(myLeafListNode).build();

        // my-container-2
        ContainerNode innerContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(innerContainer))
                .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeaf2))
                        .withValue("value2").build()).build();

        LeafNode<?> myLeaf3Node = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeaf3))
                .withValue("value3").build();

        ChoiceNode myChoiceNode = Builders.choiceBuilder().withNodeIdentifier(new NodeIdentifier(myChoice))
                .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInCase2))
                        .withValue("case2value").build()).build();

        ContainerNode myContainer2Node = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer2))
                .withChild(innerContainerNode)
                .withChild(myLeaf3Node)
                .withChild(myChoiceNode).build();

        // my-container-3
        Map<QName, Object> keys = new HashMap<>();
        keys.put(myFirstKeyLeaf, "listkeyvalue1");
        keys.put(mySecondKeyLeaf, "listkeyvalue2");

        MapNode myDoublyKeyedListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(myDoublyKeyedList))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myDoublyKeyedList, keys))
                        .withChild(Builders.leafBuilder().withNodeIdentifier(
                                new NodeIdentifier(myLeafInList3)).withValue("listleafvalue1").build()).build())
                .build();

        AugmentationNode myDoublyKeyedListAugNode = Builders.augmentationBuilder().withNodeIdentifier(
                new AugmentationIdentifier(Sets.newHashSet(myDoublyKeyedList)))
                .withChild(myDoublyKeyedListNode).build();

        ContainerNode myContainer3Node = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer3))
                .withChild(myDoublyKeyedListAugNode).build();

        AugmentationNode myContainer3AugNode = Builders.augmentationBuilder().withNodeIdentifier(
                new AugmentationIdentifier(Sets.newHashSet(myContainer3)))
                .withChild(myContainer3Node).build();

        ContainerNode outerContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(outerContainer))
                .withChild(myContainer1Node)
                .withChild(myContainer2Node)
                .withChild(myContainer3AugNode).build();

        return outerContainerNode;
    }
}
