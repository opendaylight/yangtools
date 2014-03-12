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
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.parser.ContainerNodeDomParser;
import org.opendaylight.yangtools.yang.data.impl.schema.dom.serializer.ContainerNodeDomSerializer;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NormalizedDataBuilderTest {

    private ContainerSchemaNode containerNode;
    private SchemaContext schema;

    SchemaContext parseTestSchema() {
        YangParserImpl yangParserImpl = new YangParserImpl();
        Set<Module> modules = yangParserImpl.parseYangModelsFromStreams(getTestYangs());
        return yangParserImpl.resolveSchemaContext(modules);
    }

    List<InputStream> getTestYangs() {

        return Lists.newArrayList(Collections2.transform(Lists.newArrayList("test.yang"),
                new Function<String, InputStream>() {
                    @Override
                    public InputStream apply(String input) {
                        InputStream resourceAsStream = getClass().getResourceAsStream(input);
                        Preconditions.checkNotNull(resourceAsStream, "File %s was null", resourceAsStream);
                        return resourceAsStream;
                    }
                }));
    }

    @Before
    public void setUp() throws Exception {
        schema = parseTestSchema();
        containerNode = (ContainerSchemaNode) getSchemaNode(schema, "test", "container");
    }

    @Test
    public void testFromXml() throws Exception {

        Document doc = loadDocument("simple.xml");
        System.out.println(toString(doc.getDocumentElement()));

        ContainerNode built = new ContainerNodeDomParser().fromDom(Collections.singletonList(doc.getDocumentElement()),
                containerNode, XmlDocumentUtils.defaultValueCodecProvider());
        System.out.println(built);

        List<Element> els = new ContainerNodeDomSerializer().toDom(containerNode, built,
                XmlDocumentUtils.defaultValueCodecProvider(), newDocument());
        Element el = els.get(0);
        System.out.println(toString(el));

        Assert.assertEquals(toString(doc.getDocumentElement()).replaceAll("\\s*", ""),
                toString(el).replaceAll("\\s*", ""));
    }

    @Test
    public void testFromXmlWithAttributes() throws Exception {

        Document doc = loadDocument("simple_xml_with_attributes.xml");
        System.out.println(toString(doc.getDocumentElement()));

        ContainerNode built = new ContainerNodeDomParser().fromDom(Collections.singletonList(doc.getDocumentElement()),
                containerNode, XmlDocumentUtils.defaultValueCodecProvider());
        System.out.println(built);

        List<Element> els = new ContainerNodeDomSerializer().toDom(containerNode, built,
                XmlDocumentUtils.defaultValueCodecProvider(), newDocument());
        Element el = els.get(0);

        Assert.assertEquals("test", el.getAttribute("name"));

        System.out.println(toString(el));

        Assert.assertEquals(toString(doc.getDocumentElement()).replaceAll("\\s*", ""),
                toString(el).replaceAll("\\s*", ""));
    }

    @Test
    public void testSchemaUnaware() throws Exception {
        // Container
        DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> builder = Builders
                .containerBuilder().withNodeIdentifier(getNodeIdentifier("container"));

        // leaf
        LeafNode<String> leafChild = Builders.<String> leafBuilder().withNodeIdentifier(getNodeIdentifier("leaf"))
                .withValue("String").build();
        builder.withChild(leafChild);

        // leafList
        LeafSetNode<Integer> leafList = Builders
                .<Integer> leafSetBuilder()
                .withNodeIdentifier(getNodeIdentifier("leaf"))
                .withChildValue(1)
                .withChild(
                        Builders.<Integer> leafSetEntryBuilder()
                                .withNodeIdentifier(getNodeWithValueIdentifier("leaf", 3)).withValue(3).build())
                .build();
        builder.withChild(leafList);

        // list
        MapEntryNode listChild1 = Builders
                .mapEntryBuilder()
                .withChild(
                        Builders.<Integer> leafBuilder().withNodeIdentifier(getNodeIdentifier("uint32InList"))
                                .withValue(1).build())
                .withChild(Builders.containerBuilder().withNodeIdentifier(getNodeIdentifier("containerInList")).build())
                .withNodeIdentifier(
                        new InstanceIdentifier.NodeIdentifierWithPredicates(getNodeIdentifier("list").getNodeType(),
                                Collections.singletonMap(getNodeIdentifier("uint32InList").getNodeType(), (Object) 1)))
                .build();

        MapNode list = Builders.mapBuilder().withChild(listChild1).withNodeIdentifier(getNodeIdentifier("list"))
                .build();
        builder.withChild(list);

        AugmentationNode augmentation = Builders
                .augmentationBuilder()
                .withNodeIdentifier(
                        new InstanceIdentifier.AugmentationIdentifier(null, Sets.newHashSet(getQName("augmentUint32"))))
                .withChild(
                        Builders.<Integer> leafBuilder().withNodeIdentifier(getNodeIdentifier("augmentUint32"))
                                .withValue(11).build()).build();

        builder.withChild(augmentation);

        // This works without schema (adding child from augment as a direct
        // child)
        builder.withChild(Builders.<Integer> leafBuilder().withNodeIdentifier(getNodeIdentifier("augmentUint32"))
                .withValue(11).build());

        System.out.println(builder.build());
    }

    @Test
    public void testSchemaAware() throws Exception {
        DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifier, ContainerNode> builder = Builders
                .containerBuilder(containerNode);

        LeafSchemaNode schemaNode = (LeafSchemaNode) getSchemaNode(schema, "test", "uint32");
        LeafNode<String> leafChild = Builders.<String> leafBuilder(schemaNode).withValue("String").build();
        builder.withChild(leafChild);

        LeafListSchemaNode leafListSchemaNode = (LeafListSchemaNode) getSchemaNode(schema, "test", "leafList");
        LeafSetNode<Integer> leafList = Builders.<Integer> leafSetBuilder(leafListSchemaNode).withChildValue(1)
                .withChild(Builders.<Integer> leafSetEntryBuilder(leafListSchemaNode).withValue(3).build()).build();
        builder.withChild(leafList);

        ListSchemaNode listSchema = (ListSchemaNode) getSchemaNode(schema, "test", "list");
        LeafSchemaNode uint32InListSchemaNode = (LeafSchemaNode) getSchemaNode(schema, "test", "uint32InList");
        ContainerSchemaNode containerInListSchemaNode = (ContainerSchemaNode) getSchemaNode(schema, "test",
                "containerInList");

        MapEntryNode listChild1 = Builders.mapEntryBuilder(listSchema)
                .withChild(Builders.<Integer> leafBuilder(uint32InListSchemaNode).withValue(1).build())
                .withChild(Builders.containerBuilder(containerInListSchemaNode).build()).build();

        MapNode list = ImmutableMapNodeSchemaAwareBuilder.create(listSchema).withChild(listChild1).build();
        builder.withChild(list);

        LeafSchemaNode augmentUint32SchemaNode = (LeafSchemaNode) getSchemaNode(schema, "test", "augmentUint32");
        AugmentationSchema augmentationSchema = getAugmentationSchemaForChild(containerNode,
                augmentUint32SchemaNode.getQName());

        AugmentationNode augmentation = Builders.augmentationBuilder(augmentationSchema)
                .withChild(Builders.<Integer>leafBuilder(augmentUint32SchemaNode).withValue(11).build()).build();

        builder.withChild(augmentation);

        // This should fail with schema, since the leaf comes from augmentation
        // builder.withChild(ImmutableLeafNodeSchemaAwareBuilder.<Integer>get(augmentUint32SchemaNode).withValue(11).build());

        LeafSchemaNode augumentString1SchemaNode = (LeafSchemaNode) getSchemaNode(schema, "test", "augmentString1");
        LeafSchemaNode augumentString2SchemaNode = (LeafSchemaNode) getSchemaNode(schema, "test", "augmentString2");

        ChoiceNode choice1SchemaNode = (ChoiceNode) getSchemaNode(schema, "test", "choice");
        org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode choice = ImmutableChoiceNodeSchemaAwareBuilder
                .create(choice1SchemaNode)
                .withChild(Builders.<String> leafBuilder(augumentString1SchemaNode).withValue("case1").build())
                // This should fail, since child node belongs to different case
                // .withChild(Builders.<String>leafBuilder(augumentString2SchemaNode).withValue("case2")
                // .build())
                .build();

        ;
        builder.withChild(choice);

        // This should fail, child from case
        // builder.withChild(Builders.<String>leafBuilder(augumentString1SchemaNode).withValue("case1")
        // .build());

        System.out.println(builder.build());
    }

    private AugmentationSchema getAugmentationSchemaForChild(ContainerSchemaNode containerNode, QName qName) {
        for (AugmentationSchema augmentationSchema : containerNode.getAvailableAugmentations()) {
            if (augmentationSchema.getDataChildByName(qName) != null) {
                return augmentationSchema;
            }
        }
        throw new IllegalStateException("Unable to find child augmentation in " + containerNode);
    }

    private InstanceIdentifier.NodeWithValue getNodeWithValueIdentifier(String localName, Object value) {
        return new InstanceIdentifier.NodeWithValue(getQName(localName), value);
    }

    private QName getQName(String localName) {
        String namespace = "namespace";
        return new QName(URI.create(namespace), localName);
    }

    private InstanceIdentifier.NodeWithValue getNodeWithValueIdentifier(QName q, Object value) {
        return new InstanceIdentifier.NodeWithValue(q, value);
    }

    private InstanceIdentifier.NodeIdentifier getNodeIdentifier(String localName) {
        return new InstanceIdentifier.NodeIdentifier(getQName(localName));
    }

    private InstanceIdentifier.NodeIdentifier getNodeIdentifier(QName q) {
        return new InstanceIdentifier.NodeIdentifier(q);
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

    DataSchemaNode getSchemaNode(SchemaContext context, String moduleName, String childNodeName) {
        for (Module module : context.getModules()) {
            if (module.getName().equals(moduleName)) {
                DataSchemaNode found = findChildNode(module.getChildNodes(), childNodeName);
                Preconditions.checkState(found != null, "Unable to find %s", childNodeName);
                return found;
            }
        }
        throw new IllegalStateException("Unable to find child node " + childNodeName);
    }

    DataSchemaNode findChildNode(Set<DataSchemaNode> children, String name) {
        List<DataNodeContainer> containers = Lists.newArrayList();

        for (DataSchemaNode dataSchemaNode : children) {
            if (dataSchemaNode.getQName().getLocalName().equals(name))
                return dataSchemaNode;
            if (dataSchemaNode instanceof DataNodeContainer) {
                containers.add((DataNodeContainer) dataSchemaNode);
            } else if (dataSchemaNode instanceof ChoiceNode) {
                containers.addAll(((ChoiceNode) dataSchemaNode).getCases());
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
