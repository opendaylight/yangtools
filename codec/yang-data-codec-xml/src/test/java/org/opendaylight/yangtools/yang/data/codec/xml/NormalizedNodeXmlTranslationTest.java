/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.dom.DOMResult;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class NormalizedNodeXmlTranslationTest extends AbstractXmlTest {
    private static final QNameModule MODULE =
        QNameModule.of("urn:opendaylight:params:xml:ns:yang:controller:test", "2014-03-13");

    private static ContainerNode augmentChoiceHell2() {
        final var container = getNodeIdentifier("container");
        final var augmentChoice1QName = QName.create(container.getNodeType(), "augment-choice1");
        final var augmentChoice2QName = QName.create(augmentChoice1QName, "augment-choice2");
        final var containerQName = QName.create(augmentChoice1QName, "case11-choice-case-container");
        final var leafQName = QName.create(augmentChoice1QName, "case11-choice-case-leaf");

        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(container)
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(new NodeIdentifier(augmentChoice1QName))
                .withChild(ImmutableNodes.newChoiceBuilder()
                    .withNodeIdentifier(new NodeIdentifier(augmentChoice2QName))
                    .withChild(ImmutableNodes.newContainerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(containerQName))
                        .withChild(ImmutableNodes.leafNode(leafQName, "leaf-value"))
                        .build())
                    .build())
                .build())
            .build();
    }

    private static ContainerNode withAttributes() {
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(getNodeIdentifier("container"))
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(getNodeIdentifier("list"))
                .withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(getNodeIdentifier("list").getNodeType(),
                        getNodeIdentifier("uint32InList").getNodeType(), Uint32.valueOf(3)))
                    .withChild(ImmutableNodes.leafNode(getNodeIdentifier("uint32InList"), Uint32.valueOf(3)))
                    .build())
                .build())
            .withChild(ImmutableNodes.leafNode(getNodeIdentifier("boolean"), Boolean.FALSE))
            .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                .withNodeIdentifier(getNodeIdentifier("leafList"))
                .withChild(ImmutableNodes.leafSetEntry(getNodeIdentifier("leafList").getNodeType(), "a"))
                .build())
            .build();
    }

    private static ContainerNode augmentChoiceHell() {
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(getNodeIdentifier("container"))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(getNodeIdentifier("ch2"))
                .withChild(ImmutableNodes.leafNode(getNodeIdentifier("c2Leaf"), "2"))
                .withChild(ImmutableNodes.newChoiceBuilder()
                    .withNodeIdentifier(getNodeIdentifier("c2DeepChoice"))
                    .withChild(ImmutableNodes.leafNode(getNodeIdentifier("c2DeepChoiceCase1Leaf2"), "2"))
                    .build())
                .build())
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(getNodeIdentifier("ch3"))
                .withChild(ImmutableNodes.leafNode(getNodeIdentifier("c3Leaf"), "3"))
                .build())
            .withChild(ImmutableNodes.leafNode(getNodeIdentifier("augLeaf"), "augment"))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(getNodeIdentifier("ch"))
                .withChild(ImmutableNodes.leafNode(getNodeIdentifier("c1Leaf"), "1"))
                .withChild(ImmutableNodes.leafNode(getNodeIdentifier("c1Leaf_AnotherAugment"), "1"))
                .withChild(ImmutableNodes.newChoiceBuilder()
                    .withNodeIdentifier(getNodeIdentifier("deepChoice"))
                    .withChild(ImmutableNodes.leafNode(getNodeIdentifier("deepLeafc1"), "1"))
                    .build())
                .build())
            .build();
    }

    private static NodeIdentifier getNodeIdentifier(final String localName) {
        return new NodeIdentifier(QName.create(MODULE, localName));
    }

    @ParameterizedTest
    @MethodSource("data")
    void testTranslationRepairing(final String yangPath, final String xmlPath, final ContainerNode expectedNode)
            throws Exception {
        testTranslation(TestFactories.REPAIRING_OUTPUT_FACTORY, yangPath, xmlPath, expectedNode);
    }

    @ParameterizedTest
    @MethodSource("data")
    void testTranslation(final String yangPath, final String xmlPath, final ContainerNode expectedNode)
            throws Exception {
        testTranslation(TestFactories.DEFAULT_OUTPUT_FACTORY, yangPath, xmlPath, expectedNode);
    }

    private static void testTranslation(final XMLOutputFactory factory, final String yangPath, final String xmlPath,
            final ContainerNode expectedNode) throws Exception {
        final var schema = YangParserTestUtils.parseYangResource(yangPath);
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream(xmlPath);

        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schema, QName.create(MODULE, "container")));
        xmlParser.parse(reader);

        final var built = result.getResult().data();
        assertNotNull(built);

        if (expectedNode != null) {
            assertEquals(expectedNode, built);
        }

        final var document = UntrustedXML.newDocumentBuilder().newDocument();
        final var domResult = new DOMResult(document);
        final var xmlStreamWriter = factory.createXMLStreamWriter(domResult);
        final var xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter, schema);
        final var normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(built);

        final var doc = loadDocument(xmlPath);

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);

        final String expectedXml = toString(doc.getDocumentElement());
        final String serializedXml = toString(domResult.getNode());

        final Diff diff = new Diff(expectedXml, serializedXml);
        diff.overrideDifferenceListener(new IgnoreTextAndAttributeValuesDifferenceListener());
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());

        // FIXME the comparison cannot be performed, since the qualifiers supplied by XMLUnit do not work correctly in
        // this case
        // We need to implement custom qualifier so that the element ordering does not mess the DIFF
        // dd.overrideElementQualifier(new MultiLevelElementNameAndTextQualifier(100, true));
        // assertTrue(dd.toString(), dd.similar());

        // XMLAssert.assertXMLEqual(diff, true);
    }

    static List<Arguments> data() {
        return List.of(
            Arguments.of("/schema/augment_choice_hell.yang", "/schema/augment_choice_hell_ok.xml", augmentChoiceHell()),
            Arguments.of("/schema/augment_choice_hell.yang", "/schema/augment_choice_hell_ok2.xml", null),
            Arguments.of("/schema/augment_choice_hell.yang", "/schema/augment_choice_hell_ok3.xml",
                augmentChoiceHell2()),
            Arguments.of("/schema/test.yang", "/schema/simple.xml", null),
            Arguments.of("/schema/test.yang", "/schema/simple2.xml", null),
            // TODO check attributes
            Arguments.of("/schema/test.yang", "/schema/simple_xml_with_attributes.xml", withAttributes()));
    }
}
