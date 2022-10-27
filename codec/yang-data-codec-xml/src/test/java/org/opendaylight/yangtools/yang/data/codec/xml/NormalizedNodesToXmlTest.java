/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class NormalizedNodesToXmlTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return TestFactories.junitParameters();
    }

    private QNameModule bazModule;

    private QName outerContainer;

    private QName myContainer1;
    private QName myKeyedList;
    private QName myKeyLeaf;
    private QName myLeafInList1;
    private QName myLeafInList2;
    private QName myLeaf1;
    private QName myLeafList;

    private QName myContainer2;
    private QName innerContainer;
    private QName myLeaf2;
    private QName myLeaf3;
    private QName myChoice;
    private QName myLeafInCase2;

    private QName myContainer3;
    private QName myDoublyKeyedList;
    private QName myFirstKeyLeaf;
    private QName mySecondKeyLeaf;
    private QName myLeafInList3;

    private static EffectiveModelContext SCHEMA_CONTEXT;

    private final XMLOutputFactory factory;

    public NormalizedNodesToXmlTest(final String factoryMode, final XMLOutputFactory factory) {
        this.factory = factory;
    }

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResource("/baz.yang");
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @Before
    public void setup() {
        bazModule = QNameModule.create(XMLNamespace.of("baz-namespace"));

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
    }

    @Test
    public void testNormalizedNodeToXmlSerialization() throws XMLStreamException, IOException, SAXException {
        final Document doc = loadDocument("/baz.xml");

        final DOMResult domResult = new DOMResult(UntrustedXML.newDocumentBuilder().newDocument());

        final XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(domResult);

        final NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(
                xmlStreamWriter, SCHEMA_CONTEXT);

        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
                xmlNormalizedNodeStreamWriter);

        normalizedNodeWriter.write(buildOuterContainerNode());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);

        final String expectedXml = toString(doc.getDocumentElement());
        final String serializedXml = toString(domResult.getNode());
        final Diff diff = new Diff(expectedXml, serializedXml);

        final DifferenceListener differenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        diff.overrideDifferenceListener(differenceListener);

        XMLAssert.assertXMLEqual(diff, true);
    }

    private NormalizedNode buildOuterContainerNode() {
        // my-container-1
        MapNode myKeyedListNode = Builders.mapBuilder()
            .withNodeIdentifier(new NodeIdentifier(myKeyedList))
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(myKeyedList, myKeyLeaf, "listkeyvalue1"))
                .withChild(ImmutableNodes.leafNode(myLeafInList1, "listleafvalue1"))
                .withChild(ImmutableNodes.leafNode(myLeafInList2, "listleafvalue2"))
                .build())
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(myKeyedList, myKeyLeaf, "listkeyvalue2"))
                .withChild(ImmutableNodes.leafNode(myLeafInList1, "listleafvalue12"))
                .withChild(ImmutableNodes.leafNode(myLeafInList2, "listleafvalue22"))
                .build())
            .build();

        LeafNode<?> myLeaf1Node = ImmutableNodes.leafNode(myLeaf1, "value1");

        SystemLeafSetNode<?> myLeafListNode = Builders.leafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(myLeafList))
            .withChild(Builders.leafSetEntryBuilder()
                .withNodeIdentifier(new NodeWithValue<>(myLeafList, "lflvalue1"))
                .withValue("lflvalue1")
                .build())
            .withChild(Builders.leafSetEntryBuilder()
                .withNodeIdentifier(new NodeWithValue<>(myLeafList, "lflvalue2"))
                .withValue("lflvalue2")
                .build())
            .build();

        ContainerNode myContainer1Node = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(myContainer1))
            .withChild(myKeyedListNode)
            .withChild(myLeaf1Node)
            .withChild(myLeafListNode)
            .build();

        // my-container-2
        ContainerNode innerContainerNode = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(innerContainer))
            .withChild(ImmutableNodes.leafNode(myLeaf2, "value2"))
            .build();

        LeafNode<?> myLeaf3Node = ImmutableNodes.leafNode(myLeaf3, "value3");

        ChoiceNode myChoiceNode = Builders.choiceBuilder()
            .withNodeIdentifier(new NodeIdentifier(myChoice))
            .withChild(ImmutableNodes.leafNode(myLeafInCase2, "case2value"))
            .build();

        ContainerNode myContainer2Node = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(myContainer2))
            .withChild(innerContainerNode)
            .withChild(myLeaf3Node)
            .withChild(myChoiceNode)
            .build();

        // my-container-3
        Map<QName, Object> keys = new HashMap<>();
        keys.put(myFirstKeyLeaf, "listkeyvalue1");
        keys.put(mySecondKeyLeaf, "listkeyvalue2");

        MapNode myDoublyKeyedListNode = Builders.mapBuilder()
            .withNodeIdentifier(new NodeIdentifier(myDoublyKeyedList))
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(myDoublyKeyedList, keys))
                .withChild(Builders.leafBuilder()
                    .withNodeIdentifier(new NodeIdentifier(myLeafInList3))
                    .withValue("listleafvalue1")
                    .build())
                .build())
            .build();

        ContainerNode myContainer3Node = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(myContainer3))
            .withChild(myDoublyKeyedListNode).build();


        ContainerNode outerContainerNode = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(outerContainer))
            .withChild(myContainer1Node)
            .withChild(myContainer2Node)
            .withChild(myContainer3Node)
            .build();

        return outerContainerNode;
    }

    private static Document loadDocument(final String xmlPath) throws IOException, SAXException {
        final InputStream resourceAsStream = NormalizedNodesToXmlTest.class.getResourceAsStream(xmlPath);
        return requireNonNull(readXmlToDocument(resourceAsStream));
    }

    private static Document readXmlToDocument(final InputStream xmlContent) throws IOException, SAXException {
        final Document doc = UntrustedXML.newDocumentBuilder().parse(xmlContent);
        doc.getDocumentElement().normalize();
        return doc;
    }

    private static String toString(final Node xml) {
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            final StreamResult result = new StreamResult(new StringWriter());
            final DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (IllegalArgumentException | TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Unable to serialize xml element " + xml, e);
        }
    }
}
