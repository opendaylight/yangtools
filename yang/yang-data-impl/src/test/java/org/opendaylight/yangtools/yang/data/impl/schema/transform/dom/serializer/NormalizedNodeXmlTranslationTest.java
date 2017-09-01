/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.augmentationBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.choiceBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XMLStreamNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedDataBuilderTest;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser.DomToNormalizedNodeParserFactory;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class NormalizedNodeXmlTranslationTest {
    private static final Logger LOG = LoggerFactory.getLogger(NormalizedNodeXmlTranslationTest.class);
    private final SchemaContext schema;

    @Parameterized.Parameters()
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "augment_choice_hell.yang", "augment_choice_hell_ok.xml", augmentChoiceHell() },
                { "augment_choice_hell.yang", "augment_choice_hell_ok2.xml", null },
                { "augment_choice_hell.yang", "augment_choice_hell_ok3.xml", augmentChoiceHell2() },
                { "test.yang", "simple.xml", null }, { "test.yang", "simple2.xml", null },
                // TODO check attributes
                { "test.yang", "simple_xml_with_attributes.xml", withAttributes() }
        });
    }

    public static final String NAMESPACE = "urn:opendaylight:params:xml:ns:yang:controller:test";
    private static final Date REVISION;
    static {
        try {
            REVISION = SimpleDateFormatUtil.getRevisionFormat().parse("2014-03-13");
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static ContainerNode augmentChoiceHell2() {
        final NodeIdentifier container = getNodeIdentifier("container");
        QName augmentChoice1QName = QName.create(container.getNodeType(), "augment-choice1");
        QName augmentChoice2QName = QName.create(augmentChoice1QName, "augment-choice2");
        final QName containerQName = QName.create(augmentChoice1QName, "case11-choice-case-container");
        final QName leafQName = QName.create(augmentChoice1QName, "case11-choice-case-leaf");

        final AugmentationIdentifier aug1Id = new AugmentationIdentifier(ImmutableSet.of(augmentChoice1QName));
        final AugmentationIdentifier aug2Id = new AugmentationIdentifier(ImmutableSet.of(augmentChoice2QName));
        final NodeIdentifier augmentChoice1Id = new NodeIdentifier(augmentChoice1QName);
        final NodeIdentifier augmentChoice2Id = new NodeIdentifier(augmentChoice2QName);
        final NodeIdentifier containerId = new NodeIdentifier(containerQName);

        return containerBuilder().withNodeIdentifier(container)
                .withChild(augmentationBuilder().withNodeIdentifier(aug1Id)
                        .withChild(choiceBuilder().withNodeIdentifier(augmentChoice1Id)
                                .withChild(augmentationBuilder().withNodeIdentifier(aug2Id)
                                        .withChild(choiceBuilder().withNodeIdentifier(augmentChoice2Id)
                                                .withChild(containerBuilder().withNodeIdentifier(containerId)
                                                        .withChild(leafNode(leafQName, "leaf-value"))
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build()).build();
    }

    private static ContainerNode withAttributes() {
        final DataContainerNodeBuilder<NodeIdentifier, ContainerNode> b = containerBuilder();
        b.withNodeIdentifier(getNodeIdentifier("container"));

        final CollectionNodeBuilder<MapEntryNode, MapNode> listBuilder = Builders.mapBuilder().withNodeIdentifier(
                getNodeIdentifier("list"));

        final Map<QName, Object> predicates = new HashMap<>();
        predicates.put(getNodeIdentifier("uint32InList").getNodeType(), 3L);

        final DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> list1Builder = Builders
                .mapEntryBuilder().withNodeIdentifier(new NodeIdentifierWithPredicates(
                    getNodeIdentifier("list").getNodeType(), predicates));
        final NormalizedNodeBuilder<NodeIdentifier, Object, LeafNode<Object>> uint32InListBuilder = Builders
                .leafBuilder().withNodeIdentifier(getNodeIdentifier("uint32InList"));

        list1Builder.withChild(uint32InListBuilder.withValue(3L).build());

        listBuilder.withChild(list1Builder.build());
        b.withChild(listBuilder.build());

        final NormalizedNodeBuilder<NodeIdentifier, Object, LeafNode<Object>> booleanBuilder = Builders
                .leafBuilder().withNodeIdentifier(getNodeIdentifier("boolean"));
        booleanBuilder.withValue(Boolean.FALSE);
        b.withChild(booleanBuilder.build());

        final ListNodeBuilder<Object, LeafSetEntryNode<Object>> leafListBuilder = Builders.leafSetBuilder()
                .withNodeIdentifier(getNodeIdentifier("leafList"));

        final NormalizedNodeBuilder<NodeWithValue, Object, LeafSetEntryNode<Object>> leafList1Builder = Builders
                .leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(getNodeIdentifier("leafList").getNodeType(), "a"));

        leafList1Builder.withValue("a");

        leafListBuilder.withChild(leafList1Builder.build());
        b.withChild(leafListBuilder.build());

        return b.build();
    }

    private static ContainerNode augmentChoiceHell() {

        final DataContainerNodeBuilder<NodeIdentifier, ContainerNode> b = containerBuilder();
        b.withNodeIdentifier(getNodeIdentifier("container"));

        b.withChild(choiceBuilder()
                .withNodeIdentifier(getNodeIdentifier("ch2"))
                .withChild(
                        Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("c2Leaf")).withValue("2").build())
                .withChild(
                        choiceBuilder()
                                .withNodeIdentifier(getNodeIdentifier("c2DeepChoice"))
                                .withChild(
                                        Builders.leafBuilder()
                                                .withNodeIdentifier(getNodeIdentifier("c2DeepChoiceCase1Leaf2"))
                                                .withValue("2").build()).build()).build());

        b.withChild(choiceBuilder()
                .withNodeIdentifier(getNodeIdentifier("ch3"))
                .withChild(
                        Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("c3Leaf")).withValue("3").build())
                .build());

        b.withChild(augmentationBuilder()
                .withNodeIdentifier(getAugmentIdentifier("augLeaf"))
                .withChild(
                        Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("augLeaf")).withValue("augment")
                                .build()).build());

        b.withChild(augmentationBuilder()
                .withNodeIdentifier(getAugmentIdentifier("ch"))
                .withChild(
                        choiceBuilder()
                                .withNodeIdentifier(getNodeIdentifier("ch"))
                                .withChild(
                                        Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("c1Leaf"))
                                                .withValue("1").build())
                                .withChild(
                                        augmentationBuilder()
                                                .withNodeIdentifier(
                                                        getAugmentIdentifier("c1Leaf_AnotherAugment", "deepChoice"))
                                                .withChild(
                                                        Builders.leafBuilder()
                                                                .withNodeIdentifier(
                                                                        getNodeIdentifier("c1Leaf_AnotherAugment"))
                                                                .withValue("1").build())
                                                .withChild(
                                                        choiceBuilder()
                                                                .withNodeIdentifier(getNodeIdentifier("deepChoice"))
                                                                .withChild(
                                                                        Builders.leafBuilder()
                                                                                .withNodeIdentifier(
                                                                                        getNodeIdentifier("deepLeafc1"))
                                                                                .withValue("1").build()).build())
                                                .build()).build()).build());

        return b.build();
    }

    private static NodeIdentifier getNodeIdentifier(final String localName) {
        return new NodeIdentifier(QName.create(URI.create(NAMESPACE), REVISION, localName));
    }

    public static AugmentationIdentifier getAugmentIdentifier(final String... childNames) {
        final Set<QName> qn = new HashSet<>();

        for (final String childName : childNames) {
            qn.add(getNodeIdentifier(childName).getNodeType());
        }

        return new AugmentationIdentifier(qn);
    }

    public NormalizedNodeXmlTranslationTest(final String yangPath, final String xmlPath,
            final ContainerNode expectedNode) {
        schema = YangParserTestUtils.parseYangResources(NormalizedDataBuilderTest.class, yangPath);
        this.xmlPath = xmlPath;
        this.containerNode = (ContainerSchemaNode) NormalizedDataBuilderTest.getSchemaNode(schema, "test", "container");
        this.expectedNode = expectedNode;
    }

    private final ContainerNode expectedNode;
    private final ContainerSchemaNode containerNode;
    private final String xmlPath;

    @Test
    public void testTranslation() throws Exception {
        final Document doc = loadDocument(xmlPath);

        final ContainerNode built = DomToNormalizedNodeParserFactory
                .getInstance(DomUtils.defaultValueCodecProvider(), schema).getContainerNodeParser()
                .parse(Collections.singletonList(doc.getDocumentElement()), containerNode);

        if (expectedNode != null) {
            org.junit.Assert.assertEquals(expectedNode, built);
        }

        System.err.println(built);
        LOG.info("{}", built);

        final Element elementNS = XmlDocumentUtils.getDocument().createElementNS(
                containerNode.getQName().getNamespace().toString(), containerNode.getQName().getLocalName());
        writeNormalizedNode(built, new DOMResult(elementNS), SchemaPath.create(true), schema);

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);

        System.err.println(toString(doc.getDocumentElement()));
        System.err.println(toString(elementNS));

        final Diff diff = new Diff(XMLUnit.buildControlDocument(toString(doc.getDocumentElement())),
                XMLUnit.buildTestDocument(toString(elementNS)));

        // FIXME the comparison cannot be performed, since the qualifiers supplied by XMlUnit do not work correctly in
        // this case
        // We need to implement custom qualifier so that the element ordering does not mess the DIFF
        // dd.overrideElementQualifier(new MultiLevelElementNameAndTextQualifier(100, true));
        // assertTrue(dd.toString(), dd.similar());
    }

    static final XMLOutputFactory XML_FACTORY;
    static {
        XML_FACTORY = XMLOutputFactory.newFactory();
        XML_FACTORY.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.FALSE);
    }

    private static void writeNormalizedNode(final NormalizedNode<?, ?> normalized, final DOMResult result,
            final SchemaPath schemaPath, final SchemaContext context) throws IOException, XMLStreamException {
        NormalizedNodeWriter normalizedNodeWriter = null;
        NormalizedNodeStreamWriter normalizedNodeStreamWriter = null;
        XMLStreamWriter writer = null;
        try {
            writer = XML_FACTORY.createXMLStreamWriter(result);
            normalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(writer, context, schemaPath);
            normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(normalizedNodeStreamWriter);

            normalizedNodeWriter.write(normalized);

            normalizedNodeWriter.flush();
        } finally {
            if (normalizedNodeWriter != null) {
                normalizedNodeWriter.close();
            }
            if (normalizedNodeStreamWriter != null) {
                normalizedNodeStreamWriter.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static Document loadDocument(final String xmlPath) throws IOException, SAXException {
        final InputStream resourceAsStream = NormalizedDataBuilderTest.class.getResourceAsStream(xmlPath);

        final Document currentConfigElement = readXmlToDocument(resourceAsStream);
        return Preconditions.checkNotNull(currentConfigElement);
    }

    private static Document readXmlToDocument(final InputStream xmlContent) throws IOException, SAXException {
        final Document doc = UntrustedXML.newDocumentBuilder().parse(xmlContent);
        doc.getDocumentElement().normalize();
        return doc;
    }

    public static String toString(final Element xml) {
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            final StreamResult result = new StreamResult(new StringWriter());
            final DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (IllegalArgumentException | TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Unable to serialize xml element " + xml, e);
        }
    }
}
