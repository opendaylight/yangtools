/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
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
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@RunWith(Parameterized.class)
public class NormalizedNodeXmlTranslationTest {
    private static final Logger logger = LoggerFactory.getLogger(NormalizedNodeXmlTranslationTest.class);

    @Parameterized.Parameters()
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"augment_choice_hell.yang", "augment_choice_hell_ok.xml", augmentChoiceHell()},
                {"augment_choice_hell.yang", "augment_choice_hell_ok2.xml", null},
                {"test.yang", "simple.xml", null},
                {"test.yang", "simple2.xml", null},
                // TODO check attributes
                {"test.yang", "simple_xml_with_attributes.xml", withAttributes()}
        });
    }


    public static final String NAMESPACE = "urn:opendaylight:params:xml:ns:yang:controller:test";
    private static Date revision;
    static {
        try {
            revision = new SimpleDateFormat("yyyy-MM-dd").parse("2014-03-13");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static ContainerNode withAttributes() {
        DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> b = Builders.containerBuilder();
        b.withNodeIdentifier(getNodeIdentifier("container"));

        CollectionNodeBuilder<MapEntryNode, MapNode> listBuilder = Builders.mapBuilder().withNodeIdentifier(
                getNodeIdentifier("list"));

        Map<QName, Object> predicates = Maps.newHashMap();
        predicates.put(getNodeIdentifier("uint32InList").getNodeType(), 3L);

        DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> list1Builder = Builders
                .mapEntryBuilder().withNodeIdentifier(
                        new InstanceIdentifier.NodeIdentifierWithPredicates(getNodeIdentifier("list").getNodeType(),
                                predicates));
        NormalizedNodeBuilder<InstanceIdentifier.NodeIdentifier,Object,LeafNode<Object>> uint32InListBuilder
                = Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("uint32InList"));

        list1Builder.withChild(uint32InListBuilder.withValue(3L).build());

        listBuilder.withChild(list1Builder.build());
        b.withChild(listBuilder.build());

        NormalizedNodeBuilder<InstanceIdentifier.NodeIdentifier, Object, LeafNode<Object>> booleanBuilder
                = Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("boolean"));
        booleanBuilder.withValue(false);
        b.withChild(booleanBuilder.build());

        ListNodeBuilder<Object, LeafSetEntryNode<Object>> leafListBuilder
                = Builders.leafSetBuilder().withNodeIdentifier(getNodeIdentifier("leafList"));

        NormalizedNodeBuilder<InstanceIdentifier.NodeWithValue, Object, LeafSetEntryNode<Object>> leafList1Builder
                = Builders.leafSetEntryBuilder().withNodeIdentifier(new InstanceIdentifier.NodeWithValue(getNodeIdentifier("leafList").getNodeType(), "a"));

        leafList1Builder.withValue("a");

        leafListBuilder.withChild(leafList1Builder.build());
        b.withChild(leafListBuilder.build());

        return b.build();
    }

    private static ContainerNode augmentChoiceHell() {

        DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> b = Builders.containerBuilder();
        b.withNodeIdentifier(getNodeIdentifier("container"));

        b.withChild(
                Builders.choiceBuilder().withNodeIdentifier(getNodeIdentifier("ch2"))
                        .withChild(Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("c2Leaf")).withValue("2").build())
                        .withChild(
                                Builders.choiceBuilder().withNodeIdentifier(getNodeIdentifier("c2DeepChoice"))
                                        .withChild(Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("c2DeepChoiceCase1Leaf2")).withValue("2").build())
                                        .build()
                        )
                        .build()
        );

        b.withChild(
                Builders.choiceBuilder().withNodeIdentifier(getNodeIdentifier("ch3")).withChild(
                        Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("c3Leaf")).withValue("3").build())
                .build());

        b.withChild(
                Builders.augmentationBuilder().withNodeIdentifier(getAugmentIdentifier("augLeaf")).withChild(
                        Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("augLeaf")).withValue("augment").build())
                .build());

        b.withChild(
                Builders.augmentationBuilder().withNodeIdentifier(getAugmentIdentifier("ch")).withChild(
                        Builders.choiceBuilder().withNodeIdentifier(getNodeIdentifier("ch"))
                                .withChild(
                                        Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("c1Leaf")).withValue("1").build())
                                .withChild(
                                        Builders.augmentationBuilder().withNodeIdentifier(getAugmentIdentifier("c1Leaf_AnotherAugment", "deepChoice"))
                                                .withChild(
                                                        Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("c1Leaf_AnotherAugment")).withValue("1").build())
                                                .withChild(
                                                        Builders.choiceBuilder().withNodeIdentifier(getNodeIdentifier("deepChoice"))
                                                                .withChild(
                                                                        Builders.leafBuilder().withNodeIdentifier(getNodeIdentifier("deepLeafc1")).withValue("1").build()
                                                                ).build()
                                                ).build()
                                ).build()
        ).build());

        return b.build();
    }

    private static InstanceIdentifier.NodeIdentifier getNodeIdentifier(String localName) {
        return new InstanceIdentifier.NodeIdentifier(new QName(URI.create(NAMESPACE), revision, localName));
    }

    public static InstanceIdentifier.AugmentationIdentifier getAugmentIdentifier(String... childNames) {
        Set<QName> qn = Sets.newHashSet();

        for (String childName : childNames) {
            qn.add(getNodeIdentifier(childName).getNodeType());
        }

        return new InstanceIdentifier.AugmentationIdentifier(qn);
    }

    public NormalizedNodeXmlTranslationTest(String yangPath, String xmlPath, ContainerNode expectedNode) {
        SchemaContext schema = parseTestSchema(yangPath);
        this.xmlPath = xmlPath;
        this.containerNode = (ContainerSchemaNode) NormalizedDataBuilderTest.getSchemaNode(schema, "test", "container");
        this.expectedNode = expectedNode;
    }

    private final ContainerNode expectedNode;
    private final ContainerSchemaNode containerNode;
    private final String xmlPath;


    SchemaContext parseTestSchema(String... yangPath) {
        YangParserImpl yangParserImpl = new YangParserImpl();
        Set<Module> modules = yangParserImpl.parseYangModelsFromStreams(getTestYangs(yangPath));
        return yangParserImpl.resolveSchemaContext(modules);
    }

    List<InputStream> getTestYangs(String... yangPaths) {

        return Lists.newArrayList(Collections2.transform(Lists.newArrayList(yangPaths),
                new Function<String, InputStream>() {
                    @Override
                    public InputStream apply(String input) {
                        InputStream resourceAsStream = NormalizedDataBuilderTest.class.getResourceAsStream(input);
                        Preconditions.checkNotNull(resourceAsStream, "File %s was null", resourceAsStream);
                        return resourceAsStream;
                    }
                }));
    }

    @Test
    public void testTranslation() throws Exception {
        Document doc = loadDocument(xmlPath);

        ContainerNode built =
                DomToNormalizedNodeParserFactory.getInstance(DomUtils.defaultValueCodecProvider()).getContainerNodeParser().parse(
                Collections.singletonList(doc.getDocumentElement()), containerNode);

        if (expectedNode != null)
            junit.framework.Assert.assertEquals(expectedNode, built);

        logger.info("{}", built);

        Iterable<Element> els = DomFromNormalizedNodeSerializerFactory.getInstance(XmlDocumentUtils.getDocument(), DomUtils.defaultValueCodecProvider())
                .getContainerNodeSerializer().serialize(containerNode, built);

        Element el = els.iterator().next();

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);

        System.err.println(toString(doc.getDocumentElement()));
        System.err.println(toString(el));

        boolean diff = new Diff(XMLUnit.buildControlDocument(toString(doc.getDocumentElement())), XMLUnit.buildTestDocument(toString(el))).similar();
    }

    private Document loadDocument(String xmlPath) throws Exception {
        InputStream resourceAsStream = NormalizedDataBuilderTest.class.getResourceAsStream(xmlPath);

        Document currentConfigElement = readXmlToDocument(resourceAsStream);
        Preconditions.checkNotNull(currentConfigElement);
        return currentConfigElement;
    }

    private static final DocumentBuilderFactory BUILDERFACTORY;

    static {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        BUILDERFACTORY = factory;
    }

    private Document readXmlToDocument(InputStream xmlContent) throws IOException, SAXException {
        DocumentBuilder dBuilder;
        try {
            dBuilder = BUILDERFACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to parse XML document", e);
        }
        Document doc = dBuilder.parse(xmlContent);

        doc.getDocumentElement().normalize();
        return doc;
    }

    public static String toString(Element xml) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (IllegalArgumentException | TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Unable to serialize xml element " + xml, e);
        }
    }
}
