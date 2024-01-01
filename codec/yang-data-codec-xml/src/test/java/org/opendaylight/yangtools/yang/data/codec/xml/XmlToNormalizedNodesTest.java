/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.xml.sax.SAXException;

class XmlToNormalizedNodesTest {

    private static final QNameModule FOO_MODULE = QNameModule.create(XMLNamespace.of("foo-namespace"));
    private static final QName PARENT_CONTAINER = QName.create(FOO_MODULE, "parent-container");

    private static final QNameModule BAZ_MODULE = QNameModule.create(XMLNamespace.of("baz-namespace"));
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

    private static EffectiveModelContext schemaContext;
    private static Inference outerContainerSchema;
    private static Inference parentContainerSchema;

    @BeforeAll
    static void setup() {
        schemaContext = YangParserTestUtils.parseYangResources(XmlToNormalizedNodesTest.class,
            "/foo.yang", "/baz.yang");
        parentContainerSchema = Inference.ofDataTreePath(schemaContext, PARENT_CONTAINER);
        outerContainerSchema = Inference.ofDataTreePath(schemaContext, OUTER_CONTAINER);
    }

    @AfterAll
    static void cleanup() {
        schemaContext = null;
        parentContainerSchema = null;
        outerContainerSchema = null;
    }

    @Test
    void testComplexXmlParsing() throws IOException, SAXException, URISyntaxException, XMLStreamException,
            ParserConfigurationException {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/baz.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        try (var xmlParser = XmlParserStream.create(streamWriter, outerContainerSchema)) {
            xmlParser.parse(reader);
        }

        final NormalizedNode transformedInput = result.getResult().data();
        assertNotNull(transformedInput);

        final NormalizedNode expectedNormalizedNode = buildOuterContainerNode();
        assertNotNull(expectedNormalizedNode);

        assertEquals(expectedNormalizedNode, transformedInput);
    }

    @Test
    void testSimpleXmlParsing() throws IOException, URISyntaxException, XMLStreamException,
            ParserConfigurationException, SAXException {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/foo.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, parentContainerSchema);
        xmlParser.parse(reader);

        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    void shouldFailOnDuplicateLeaf() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-foo.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, parentContainerSchema);
        final var ex = assertThrows(XMLStreamException.class, () -> xmlParser.parse(reader));
        assertThat(ex.getMessage(), containsString("""
            Duplicate element "decimal64-leaf" in namespace "foo-namespace" with parent \
            "EmptyContainerEffectiveStatement{argument=(foo-namespace)leaf-container}" in XML input"""));
    }

    @Test
    void shouldFailOnDuplicateAnyXml() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-foo-2.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, parentContainerSchema);
        final var ex = assertThrows(XMLStreamException.class, () -> xmlParser.parse(reader));
        assertThat(ex.getMessage(), containsString("""
            Duplicate element "my-anyxml" in namespace "foo-namespace" with parent \
            "EmptyContainerEffectiveStatement{argument=(foo-namespace)anyxml-container}" in XML input"""));
    }

    @Test
    void shouldFailOnDuplicateContainer() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-foo-3.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, parentContainerSchema);
        final var ex = assertThrows(XMLStreamException.class, () -> xmlParser.parse(reader));
        assertThat(ex.getMessage(), containsString("""
            Duplicate element "leaf-container" in namespace "foo-namespace" with parent \
            "EmptyContainerEffectiveStatement{argument=(foo-namespace)parent-container}" in XML input"""));
    }

    @Test
    void shouldFailOnUnterminatedLeafElement() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-baz.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, outerContainerSchema);
        final var ex = assertThrows(XMLStreamException.class, () -> xmlParser.parse(reader));
        assertThat(ex.getMessage(), containsString(" START_ELEMENT "));
    }

    @Test
    void shouldFailOnUnterminatedLeafElement2() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-baz-2.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, outerContainerSchema);
        final var ex = assertThrows(XMLStreamException.class, () -> xmlParser.parse(reader));
        assertThat(ex.getMessage(), containsString("</my-leaf-1>"));
    }

    @Test
    void shouldFailOnUnterminatedContainerElement() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-baz-4.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, outerContainerSchema);
        final var ex = assertThrows(XMLStreamException.class, () -> xmlParser.parse(reader));
        assertThat(ex.getMessage(), containsString("</my-container-1>"));
    }

    @Test
    void shouldFailOnUnknownChildNode() throws XMLStreamException, IOException,
            ParserConfigurationException, SAXException, URISyntaxException {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/invalid-baz-3.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, outerContainerSchema);
        final var ex = assertThrows(XMLStreamException.class, () -> xmlParser.parse(reader));
        assertThat(ex.getMessage(), containsString("""
            Schema for node with name my-container-1 and namespace baz-namespace does not exist in parent \
            EmptyContainerEffectiveStatement{argument=(baz-namespace)my-container-1}"""));
    }

    private static NormalizedNode buildOuterContainerNode() {
        // my-container-1
        MapNode myKeyedListNode = ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(MY_KEYED_LIST))
            .withChild(ImmutableNodes.newMapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(MY_KEYED_LIST, MY_KEY_LEAF, "listkeyvalue1"))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_IN_LIST_1, "listleafvalue1"))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_IN_LIST_2, "listleafvalue2"))
                .build())
            .withChild(ImmutableNodes.newMapEntryBuilder().withNodeIdentifier(
                NodeIdentifierWithPredicates.of(MY_KEYED_LIST, MY_KEY_LEAF, "listkeyvalue2"))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_IN_LIST_1, "listleafvalue12"))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_IN_LIST_2, "listleafvalue22"))
                .build())
            .build();

        LeafNode<?> myLeaf1Node = ImmutableNodes.leafNode(MY_LEAF_1, "value1");

        SystemLeafSetNode<?> myLeafListNode = ImmutableNodes.newSystemLeafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(MY_LEAFLIST))
            .withChild(ImmutableNodes.leafSetEntry(MY_LEAFLIST, "lflvalue1"))
            .withChild(ImmutableNodes.leafSetEntry(MY_LEAFLIST, "lflvalue2"))
            .build();

        ContainerNode myContainer1Node = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(MY_CONTAINER_1))
            .withChild(myKeyedListNode)
            .withChild(myLeaf1Node)
            .withChild(myLeafListNode)
            .build();

        // my-container-2
        ContainerNode innerContainerNode = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(INNER_CONTAINER))
            .withChild(ImmutableNodes.leafNode(MY_LEAF_2, "value2"))
            .build();

        LeafNode<?> myLeaf3Node = ImmutableNodes.leafNode(MY_LEAF_3, "value3");

        ChoiceNode myChoiceNode = ImmutableNodes.newChoiceBuilder()
            .withNodeIdentifier(new NodeIdentifier(MY_CHOICE))
            .withChild(ImmutableNodes.leafNode(MY_LEAF_IN_CASE_2, "case2value"))
            .build();

        ContainerNode myContainer2Node = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(MY_CONTAINER_2))
            .withChild(innerContainerNode)
            .withChild(myLeaf3Node)
            .withChild(myChoiceNode)
            .build();

        // my-container-3
        Map<QName, Object> keys = new HashMap<>();
        keys.put(MY_FIRST_KEY_LEAF, "listkeyvalue1");
        keys.put(MY_SECOND_KEY_LEAF, "listkeyvalue2");

        MapNode myDoublyKeyedListNode = ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(MY_DOUBLY_KEYED_LIST))
                .withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(MY_DOUBLY_KEYED_LIST, keys))
                    .withChild(ImmutableNodes.leafNode(MY_LEAF_IN_LIST_3, "listleafvalue1"))
                    .build())
                .build();

        ContainerNode myContainer3Node = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(MY_CONTAINER_3))
            .withChild(myDoublyKeyedListNode)
            .build();

        ContainerNode outerContainerNode = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(OUTER_CONTAINER))
            .withChild(myContainer1Node)
            .withChild(myContainer2Node)
            .withChild(myContainer3Node)
            .build();

        return outerContainerNode;
    }
}
