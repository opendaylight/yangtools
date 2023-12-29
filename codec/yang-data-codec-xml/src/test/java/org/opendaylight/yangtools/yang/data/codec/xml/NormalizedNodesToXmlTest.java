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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class NormalizedNodesToXmlTest extends AbstractXmlTest {
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

    @BeforeAll
    static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResource("/baz.yang");
    }

    @AfterAll
    static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @BeforeEach
    void setup() {
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

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestFactories.class)
    void testNormalizedNodeToXmlSerialization(final String factoryMode, final XMLOutputFactory factory)
            throws Exception {
        final var doc = loadDocument("/baz.xml");
        final var domResult = new DOMResult(UntrustedXML.newDocumentBuilder().newDocument());
        final var xmlStreamWriter = factory.createXMLStreamWriter(domResult);
        final var xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter,
            SCHEMA_CONTEXT);
        final var normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(xmlNormalizedNodeStreamWriter);

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
                .build())
            .withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(myContainer2))
                .withChild(Builders.containerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(innerContainer))
                    .withChild(ImmutableNodes.leafNode(myLeaf2, "value2"))
                    .build())
                .withChild(ImmutableNodes.leafNode(myLeaf3, "value3"))
                .withChild(Builders.choiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(myChoice))
                    .withChild(ImmutableNodes.leafNode(myLeafInCase2, "case2value"))
                    .build())
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
