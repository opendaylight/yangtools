/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.dom.DOMResult;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

public class SchemalessXMLStreamNormalizedNodeStreamWriterTest extends AbstractXmlTest {
    private QNameModule foobarModule;

    private QName outerContainer;

    private QName myContainer1;
    private QName myKeyedList;
    private QName myKeyLeaf;
    private QName myLeafInList1;
    private QName myLeafInList2;
    private QName myOrderedList;
    private QName myKeyLeafInOrderedList;
    private QName myLeafInOrderedList1;
    private QName myLeafInOrderedList2;
    private QName myUnkeyedList;
    private QName myLeafInUnkeyedList;
    private QName myLeaf1;
    private QName myLeafList;
    private QName myOrderedLeafList;

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

    @BeforeEach
    void setup() {
        foobarModule = QNameModule.create(XMLNamespace.of("foobar-namespace"), Revision.of("2016-09-19"));

        outerContainer = QName.create(foobarModule, "outer-container");

        myContainer1 = QName.create(foobarModule, "my-container-1");
        myKeyedList = QName.create(foobarModule, "my-keyed-list");
        myKeyLeaf = QName.create(foobarModule, "my-key-leaf");
        myLeafInList1 = QName.create(foobarModule, "my-leaf-in-list-1");
        myLeafInList2 = QName.create(foobarModule, "my-leaf-in-list-2");
        myOrderedList = QName.create(foobarModule, "my-ordered-list");
        myKeyLeafInOrderedList = QName.create(foobarModule, "my-key-leaf-in-ordered-list");
        myLeafInOrderedList1 = QName.create(foobarModule, "my-leaf-in-ordered-list-1");
        myLeafInOrderedList2 = QName.create(foobarModule, "my-leaf-in-ordered-list-2");
        myUnkeyedList = QName.create(foobarModule, "my-unkeyed-list");
        myLeafInUnkeyedList = QName.create(foobarModule, "my-leaf-in-unkeyed-list");
        myLeaf1 = QName.create(foobarModule, "my-leaf-1");
        myLeafList = QName.create(foobarModule, "my-leaf-list");
        myOrderedLeafList = QName.create(foobarModule, "my-ordered-leaf-list");

        myContainer2 = QName.create(foobarModule, "my-container-2");
        innerContainer = QName.create(foobarModule, "inner-container");
        myLeaf2 = QName.create(foobarModule, "my-leaf-2");
        myLeaf3 = QName.create(foobarModule, "my-leaf-3");
        myChoice = QName.create(foobarModule, "my-choice");
        myLeafInCase2 = QName.create(foobarModule, "my-leaf-in-case-2");

        myContainer3 = QName.create(foobarModule, "my-container-3");
        myDoublyKeyedList = QName.create(foobarModule, "my-doubly-keyed-list");
        myFirstKeyLeaf = QName.create(foobarModule, "my-first-key-leaf");
        mySecondKeyLeaf = QName.create(foobarModule, "my-second-key-leaf");
        myLeafInList3 = QName.create(foobarModule, "my-leaf-in-list-3");
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestFactories.class)
    void testWrite(final String factoryMode, final XMLOutputFactory factory) throws Exception {
        final var doc = loadDocument("/foobar.xml");
        final var domResult = new DOMResult(UntrustedXML.newDocumentBuilder().newDocument());
        final var xmlStreamWriter = factory.createXMLStreamWriter(domResult);
        final var schemalessXmlNormalizedNodeStreamWriter =
                XMLStreamNormalizedNodeStreamWriter.createSchemaless(xmlStreamWriter);
        final var normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(schemalessXmlNormalizedNodeStreamWriter);

        normalizedNodeWriter.write(buildOuterContainerNode());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);

        final String expectedXml = toString(doc.getDocumentElement());
        final String serializedXml = toString(domResult.getNode());
        final Diff diff = new Diff(expectedXml, serializedXml);
        diff.overrideDifferenceListener(new IgnoreTextAndAttributeValuesDifferenceListener());

        XMLAssert.assertXMLEqual(diff, true);
    }

    private ContainerNode buildOuterContainerNode() {
        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(outerContainer))
            .withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(myContainer1))
                .withChild(Builders.mapBuilder()
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
                    .build())
                .withChild(Builders.orderedMapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(myOrderedList))
                    .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(
                            NodeIdentifierWithPredicates.of(myOrderedList, myKeyLeafInOrderedList, "olistkeyvalue1"))
                        .withChild(ImmutableNodes.leafNode(myLeafInOrderedList1, "olistleafvalue1"))
                        .withChild(ImmutableNodes.leafNode(myLeafInOrderedList2, "olistleafvalue2"))
                        .build())
                    .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(
                            NodeIdentifierWithPredicates.of(myOrderedList, myKeyLeafInOrderedList, "olistkeyvalue2"))
                        .withChild(ImmutableNodes.leafNode(myLeafInOrderedList1, "olistleafvalue12"))
                        .withChild(ImmutableNodes.leafNode(myLeafInOrderedList2, "olistleafvalue22"))
                        .build())
                    .build())
                .withChild(Builders.unkeyedListBuilder()
                    .withNodeIdentifier(new NodeIdentifier(myUnkeyedList))
                    .withChild(Builders.unkeyedListEntryBuilder()
                        .withNodeIdentifier(new NodeIdentifier(myUnkeyedList))
                        .withChild(ImmutableNodes.leafNode(myLeafInUnkeyedList, "foo"))
                        .build())
                    .build())
                .withChild(ImmutableNodes.leafNode(myLeaf1, "value1"))
                .withChild(Builders.leafSetBuilder()
                    .withNodeIdentifier(new NodeIdentifier(myLeafList))
                    .withChild(Builders.leafSetEntryBuilder()
                        .withNodeIdentifier(new NodeWithValue<>(myLeafList, "lflvalue1"))
                        .withValue("lflvalue1")
                        .build())
                    .withChild(Builders.leafSetEntryBuilder()
                        .withNodeIdentifier(new NodeWithValue<>(myLeafList, "lflvalue2"))
                        .withValue("lflvalue2")
                        .build())
                    .build())
                .withChild(Builders.orderedLeafSetBuilder()
                    .withNodeIdentifier(new NodeIdentifier(myOrderedLeafList))
                    .withChild(Builders.leafSetEntryBuilder()
                        .withNodeIdentifier(new NodeWithValue<>(myOrderedLeafList, "olflvalue1"))
                        .withValue("olflvalue1")
                        .build())
                    .withChild(Builders.leafSetEntryBuilder()
                        .withNodeIdentifier(new NodeWithValue<>(myOrderedLeafList, "olflvalue2"))
                        .withValue("olflvalue2")
                        .build())
                    .build())
                .build())
            .withChild(Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer2))
                .withChild(Builders.containerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(innerContainer))
                    .withChild(ImmutableNodes.leafNode(myLeaf2, "value2"))
                    .build())
                .withChild(ImmutableNodes.leafNode(myLeaf3, "value3"))
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(myChoice))
                    .withChild(ImmutableNodes.leafNode(myLeafInCase2, "case2value"))
                    .build())
//                .withChild(Builders.anyXmlBuilder()
//                    .withNodeIdentifier(new NodeIdentifier(myAnyxml))
//                    .withValue(anyxmlDomSource)
//                    .build())
                .build())
            .withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(myContainer3))
                .withChild(Builders.mapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(myDoublyKeyedList))
                    .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(myDoublyKeyedList,
                            Map.of(myFirstKeyLeaf, "listkeyvalue1", mySecondKeyLeaf, "listkeyvalue2")))
                        .withChild(ImmutableNodes.leafNode(myLeafInList3, "listleafvalue1"))
                        .build())
                    .build())
                .build())
            .build();
    }
}
