/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.augmentationBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.choiceBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
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
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class NormalizedNodeXmlTranslationTest {
    private final SchemaContext schema;

    @Parameterized.Parameters()
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "/schema/augment_choice_hell.yang", "/schema/augment_choice_hell_ok.xml", augmentChoiceHell() },
                { "/schema/augment_choice_hell.yang", "/schema/augment_choice_hell_ok2.xml", null },
                { "/schema/augment_choice_hell.yang", "/schema/augment_choice_hell_ok3.xml", augmentChoiceHell2() },
                { "/schema/test.yang", "/schema/simple.xml", null },
                { "/schema/test.yang", "/schema/simple2.xml", null },
                // TODO check attributes
                { "/schema/test.yang", "/schema/simple_xml_with_attributes.xml", withAttributes() }
        });
    }

    private static final String NAMESPACE = "urn:opendaylight:params:xml:ns:yang:controller:test";
    private static final Date revision;
    static {
        try {
            revision = new SimpleDateFormat("yyyy-MM-dd").parse("2014-03-13");
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static ContainerNode augmentChoiceHell2() {
        final YangInstanceIdentifier.NodeIdentifier container = getNodeIdentifier("container");
        final QName augmentChoice1QName = QName.create(container.getNodeType(), "augment-choice1");
        final QName augmentChoice2QName = QName.create(augmentChoice1QName, "augment-choice2");
        final QName containerQName = QName.create(augmentChoice1QName, "case11-choice-case-container");
        final QName leafQName = QName.create(augmentChoice1QName, "case11-choice-case-leaf");

        final YangInstanceIdentifier.AugmentationIdentifier aug1Id = new YangInstanceIdentifier.AugmentationIdentifier(
                Sets.newHashSet(augmentChoice1QName));
        final YangInstanceIdentifier.AugmentationIdentifier aug2Id = new YangInstanceIdentifier.AugmentationIdentifier(
                Sets.newHashSet(augmentChoice2QName));
        final YangInstanceIdentifier.NodeIdentifier augmentChoice1Id = new YangInstanceIdentifier.NodeIdentifier(
                augmentChoice1QName);
        final YangInstanceIdentifier.NodeIdentifier augmentChoice2Id = new YangInstanceIdentifier.NodeIdentifier(
                augmentChoice2QName);
        final YangInstanceIdentifier.NodeIdentifier containerId = new YangInstanceIdentifier.NodeIdentifier(
                containerQName);

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
        final DataContainerNodeBuilder<YangInstanceIdentifier.NodeIdentifier, ContainerNode> b = containerBuilder();
        b.withNodeIdentifier(getNodeIdentifier("container"));

        final CollectionNodeBuilder<MapEntryNode, MapNode> listBuilder = Builders.mapBuilder().withNodeIdentifier(
                getNodeIdentifier("list"));

        final Map<QName, Object> predicates = Maps.newHashMap();
        predicates.put(getNodeIdentifier("uint32InList").getNodeType(), 3L);

        final DataContainerNodeBuilder<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> list1Builder = Builders
                .mapEntryBuilder().withNodeIdentifier(
                        new YangInstanceIdentifier.NodeIdentifierWithPredicates(
                                getNodeIdentifier("list").getNodeType(), predicates));
        final NormalizedNodeBuilder<YangInstanceIdentifier.NodeIdentifier, Object, LeafNode<Object>> uint32InListBuilder = Builders
                .leafBuilder().withNodeIdentifier(getNodeIdentifier("uint32InList"));

        list1Builder.withChild(uint32InListBuilder.withValue(3L).build());

        listBuilder.withChild(list1Builder.build());
        b.withChild(listBuilder.build());

        final NormalizedNodeBuilder<YangInstanceIdentifier.NodeIdentifier, Object, LeafNode<Object>> booleanBuilder = Builders
                .leafBuilder().withNodeIdentifier(getNodeIdentifier("boolean"));
        booleanBuilder.withValue(false);
        b.withChild(booleanBuilder.build());

        final ListNodeBuilder<Object, LeafSetEntryNode<Object>> leafListBuilder = Builders.leafSetBuilder()
                .withNodeIdentifier(getNodeIdentifier("leafList"));

        final NormalizedNodeBuilder<YangInstanceIdentifier.NodeWithValue, Object, LeafSetEntryNode<Object>> leafList1Builder = Builders
                .leafSetEntryBuilder().withNodeIdentifier(
                        new YangInstanceIdentifier.NodeWithValue(getNodeIdentifier("leafList").getNodeType(), "a"));

        leafList1Builder.withValue("a");

        leafListBuilder.withChild(leafList1Builder.build());
        b.withChild(leafListBuilder.build());

        return b.build();
    }

    private static ContainerNode augmentChoiceHell() {

        final DataContainerNodeBuilder<YangInstanceIdentifier.NodeIdentifier, ContainerNode> b = containerBuilder();
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

    private static YangInstanceIdentifier.NodeIdentifier getNodeIdentifier(final String localName) {
        return new YangInstanceIdentifier.NodeIdentifier(QName.create(URI.create(NAMESPACE), revision, localName));
    }

    private static YangInstanceIdentifier.AugmentationIdentifier getAugmentIdentifier(final String... childNames) {
        final Set<QName> qn = Sets.newHashSet();

        for (final String childName : childNames) {
            qn.add(getNodeIdentifier(childName).getNodeType());
        }

        return new YangInstanceIdentifier.AugmentationIdentifier(qn);
    }

    public NormalizedNodeXmlTranslationTest(final String yangPath, final String xmlPath,
                                            final ContainerNode expectedNode) throws ReactorException {
        this.schema = parseTestSchema(yangPath);
        this.xmlPath = xmlPath;
        this.containerNode = (ContainerSchemaNode) getSchemaNode(schema, "test", "container");
        this.expectedNode = expectedNode;
    }

    private final ContainerNode expectedNode;
    private final ContainerSchemaNode containerNode;
    private final String xmlPath;

    private SchemaContext parseTestSchema(final String yangPath) throws ReactorException {
        return YangParserTestUtils.parseYangSources(new YangStatementSourceImpl(yangPath, false));
    }

    @Test
    public void testTranslation() throws Exception {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream(xmlPath);

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schema);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> built = result.getResult();
        assertNotNull(built);

        if (expectedNode != null) {
            org.junit.Assert.assertEquals(expectedNode, built);
        }

        final Document document = XmlDocumentUtils.getDocument();
        final DOMResult domResult = new DOMResult(document);

        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);

        final XMLStreamWriter xmlStreamWriter = outputFactory.createXMLStreamWriter(domResult);

        final NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter
                .create(xmlStreamWriter, schema);

        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter
                (xmlNormalizedNodeStreamWriter);

        normalizedNodeWriter.write(built);

        final Document doc = loadDocument(xmlPath);

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);

        final String expectedXml = toString(doc.getDocumentElement().getElementsByTagName("container").item(0));
        final String serializedXml = toString(domResult.getNode());

        final Diff diff = new Diff(expectedXml, serializedXml);
        diff.overrideDifferenceListener(new IgnoreTextAndAttributeValuesDifferenceListener());
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());

        // FIXME the comparison cannot be performed, since the qualifiers supplied by XMlUnit do not work correctly in
        // this case
        // We need to implement custom qualifier so that the element ordering does not mess the DIFF
        // dd.overrideElementQualifier(new MultiLevelElementNameAndTextQualifier(100, true));
        // assertTrue(dd.toString(), dd.similar());

        //new XMLTestCase() {}.assertXMLEqual(diff, true);
    }

    static final XMLOutputFactory XML_FACTORY;
    static {
        XML_FACTORY = XMLOutputFactory.newFactory();
        XML_FACTORY.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
    }

    private static Document loadDocument(final String xmlPath) throws IOException, SAXException {
        final InputStream resourceAsStream = NormalizedNodeXmlTranslationTest.class.getResourceAsStream(xmlPath);

        final Document currentConfigElement = readXmlToDocument(resourceAsStream);
        Preconditions.checkNotNull(currentConfigElement);
        return currentConfigElement;
    }

    private static final DocumentBuilderFactory BUILDERFACTORY;

    static {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        BUILDERFACTORY = factory;
    }

    private static Document readXmlToDocument(final InputStream xmlContent) throws IOException, SAXException {
        final DocumentBuilder dBuilder;
        try {
            dBuilder = BUILDERFACTORY.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException("Failed to parse XML document", e);
        }
        final Document doc = dBuilder.parse(xmlContent);

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

    private static DataSchemaNode getSchemaNode(final SchemaContext context, final String moduleName,
                                               final String childNodeName) {
        for (Module module : context.getModules()) {
            if (module.getName().equals(moduleName)) {
                DataSchemaNode found = findChildNode(module.getChildNodes(), childNodeName);
                Preconditions.checkState(found != null, "Unable to find %s", childNodeName);
                return found;
            }
        }
        throw new IllegalStateException("Unable to find child node " + childNodeName);
    }

    private static DataSchemaNode findChildNode(final Iterable<DataSchemaNode> children, final String name) {
        List<DataNodeContainer> containers = Lists.newArrayList();

        for (DataSchemaNode dataSchemaNode : children) {
            if (dataSchemaNode.getQName().getLocalName().equals(name)) {
                return dataSchemaNode;
            }
            if (dataSchemaNode instanceof DataNodeContainer) {
                containers.add((DataNodeContainer) dataSchemaNode);
            } else if (dataSchemaNode instanceof ChoiceSchemaNode) {
                containers.addAll(((ChoiceSchemaNode) dataSchemaNode).getCases());
            }
        }

        for (DataNodeContainer container : containers) {
            DataSchemaNode retVal = findChildNode(container.getChildNodes(), name);
            if (retVal != null) {
                return retVal;
            }
        }

        return null;
    }

}
