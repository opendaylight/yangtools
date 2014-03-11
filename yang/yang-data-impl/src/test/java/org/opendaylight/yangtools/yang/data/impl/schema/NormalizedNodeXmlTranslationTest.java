/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.parser.ContainerNodeDomParser;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.serializer.ContainerNodeDomSerializer;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RunWith(Parameterized.class)
public class NormalizedNodeXmlTranslationTest {


    @Parameterized.Parameters()
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"augment_choice_hell.yang", "augment_choice_hell_ok.xml", augmentChoiceHell()},
                {"augment_choice_hell.yang", "augment_choice_hell_ok2.xml", null},
                {"test.yang", "simple.xml", null},
                {"test.yang", "simple2.xml", null}
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

        return new InstanceIdentifier.AugmentationIdentifier(null, qn);
    }

    public NormalizedNodeXmlTranslationTest(String yangPath, String xmlPath, ContainerNode expectedNode) {
        this.schema = parseTestSchema(yangPath);
        this.xmlPath = xmlPath;
        this.containerNode = (ContainerSchemaNode) NormalizedDataBuilderTest.getSchemaNode(schema, "test", "container");
        this.expectedNode = expectedNode;
    }

    private final ContainerNode expectedNode;
    private final ContainerSchemaNode containerNode;
    private final SchemaContext schema;
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
                        InputStream resourceAsStream = getClass().getResourceAsStream(input);
                        Preconditions.checkNotNull(resourceAsStream, "File %s was null", resourceAsStream);
                        return resourceAsStream;
                    }
                }));
    }

    @Test
    public void testTranslation() throws Exception {
        Document doc = loadDocument(xmlPath);
        System.out.println(toString(doc.getDocumentElement()));

        ContainerNode built = new ContainerNodeDomParser().fromDom(Collections.singletonList(doc.getDocumentElement()),
                containerNode, XmlDocumentUtils.defaultValueCodecProvider());

        if(expectedNode != null)
            junit.framework.Assert.assertEquals(expectedNode, built);

        List<Element> els = new ContainerNodeDomSerializer().toDom(containerNode, built,
                XmlDocumentUtils.defaultValueCodecProvider(), newDocument());
        Element el = els.get(0);
        System.out.println(toString(el));

        Assert.assertEquals(toString(doc.getDocumentElement()).replaceAll("\\s*", ""),
                toString(el).replaceAll("\\s*", ""));
    }


    private Document loadDocument(String xmlPath) throws Exception {
        InputStream resourceAsStream = getClass().getResourceAsStream(xmlPath);

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

    private Document newDocument() {
        DocumentBuilder dBuilder;
        try {
            dBuilder = BUILDERFACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to parse XML document", e);
        }
        return dBuilder.newDocument();
    }


    private InstanceIdentifier.NodeIdentifier getNodeIdentifier(QName q) {
        return new InstanceIdentifier.NodeIdentifier(q);
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
