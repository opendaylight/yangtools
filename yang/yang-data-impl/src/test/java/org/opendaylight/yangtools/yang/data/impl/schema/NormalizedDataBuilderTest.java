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
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
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

        Document doc = loadDocument("simple.xml");
        // TODO finish from XML

//        ContainerNode built = NormalizedDataBuilder.buildFromDomElement(doc.getDocumentElement(), containerNode,
//                XmlDocumentUtils.defaultValueCodecProvider());
//        System.err.println(built);
    }

    @Test
    public void testSchemaUnaware() throws Exception {
        // Container
        ImmutableContainerNodeBuilder builder = ImmutableContainerNodeBuilder.get().withNodeIdentifier(
                getNodeIdentifier("container"));

        // leaf
        LeafNode<String> leafChild = ImmutableLeafNodeBuilder.<String>get()
                .withNodeIdentifier(getNodeIdentifier("leaf")).withValue("String").build();
        builder.withChild(leafChild);

        // leafList
        ImmutableLeafSetNode<Integer> leafList = ImmutableLeafSetNodeBuilder.<Integer> get()
                .withNodeIdentifier(getNodeIdentifier("leaf"))
                .withChild(1)
                .withChild(new ImmutableLeafSetEntryNode<>(getNodeWithValueIdentifier("leaf", 2), 2))
                .withChild(ImmutableLeafSetEntryNodeBuilder.<Integer>get().withNodeIdentifier(getNodeWithValueIdentifier("leaf", 3)).withValue(3).build())
                .build();
        builder.withChild(leafList);

        // list
        ImmutableMapEntryNode listChild1 = ImmutableMapEntryNodeBuilder.get()
                .withChild(
                        ImmutableLeafNodeBuilder.<Integer>get()
                                .withNodeIdentifier(getNodeIdentifier("uint32InList")).withValue(1).build())
                .withChild(
                        ImmutableContainerNodeBuilder.get().withNodeIdentifier(
                                getNodeIdentifier("containerInList"))
                                .build())
                .withNodeIdentifier(
                        new InstanceIdentifier.NodeIdentifierWithPredicates(getNodeIdentifier("list").getNodeType(),
                                Collections.singletonMap(getNodeIdentifier("uint32InList").getNodeType(), (Object) 1)))
                .build();

        MapNode list = ImmutableMapNodeBuilder.get().withChild(listChild1).withNodeIdentifier(getNodeIdentifier("list")).build();
        builder.withChild(list);

        AugmentationNode augmentation = ImmutableAugmentationNodeBuilder.get()
                .withNodeIdentifier(new InstanceIdentifier.AugmentationIdentifier(null, Sets.newHashSet(getQName("augmentUint32"))))
                .withChild(
                        ImmutableLeafNodeBuilder.<Integer>get().withNodeIdentifier(getNodeIdentifier("augmentUint32")).withValue(11).build())
                .build();

        builder.withChild(augmentation);

        // This works without schema (adding child from augment as a direct child)
        builder.withChild(ImmutableLeafNodeBuilder.<Integer>get().withNodeIdentifier(getNodeIdentifier("augmentUint32")).withValue(11).build());

        System.out.println(builder.build());
    }

    @Test
    public void testSchemaAware() throws Exception {
        ImmutableContainerNodeSchemaAwareBuilder builder = ImmutableContainerNodeSchemaAwareBuilder.get(containerNode);

        LeafSchemaNode schemaNode = (LeafSchemaNode) getSchemaNode(schema, "test", "uint32");
        LeafNode<String> leafChild = ImmutableLeafNodeSchemaAwareBuilder
                .<String>get(schemaNode)
                .withValue("String").build();
        builder.withChild(leafChild);

        LeafListSchemaNode leafListSchemaNode = (LeafListSchemaNode) getSchemaNode(schema, "test", "leafList");
        QName qName = leafListSchemaNode.getQName();
        ImmutableLeafSetNode<Integer> leafList = ImmutableLeafSetNodeSchemaAwareBuilder.<Integer>get(leafListSchemaNode)
                .withChild(1)
                .withChild(new ImmutableLeafSetEntryNode<>(getNodeWithValueIdentifier(qName, 2), 2))
                .withChild(ImmutableLeafSetEntryNodeSchemaAwareBuilder.<Integer>get(leafListSchemaNode).withValue(3).build())
                .build();
        builder.withChild(leafList);

        ListSchemaNode listSchema = (ListSchemaNode) getSchemaNode(schema, "test", "list");
        LeafSchemaNode uint32InListSchemaNode = (LeafSchemaNode) getSchemaNode(schema, "test", "uint32InList");
        ContainerSchemaNode containerInListSchemaNode = (ContainerSchemaNode) getSchemaNode(schema, "test", "containerInList");

        ImmutableMapEntryNode listChild1 = ImmutableMapEntryNodeSchemaAwareBuilder.get(listSchema)
                .withChild(
                        ImmutableLeafNodeSchemaAwareBuilder.<Integer>get(uint32InListSchemaNode).withValue(1).build())
                .withChild(
                        ImmutableContainerNodeSchemaAwareBuilder.get(containerInListSchemaNode).build())
                .build();

        MapNode list = ImmutableMapNodeSchemaAwareBuilder.get(listSchema).withChild(listChild1).build();
        builder.withChild(list);

        LeafSchemaNode augmentUint32SchemaNode = (LeafSchemaNode) getSchemaNode(schema, "test", "augmentUint32");
        AugmentationSchema augmentationSchema = getAugmentationSchemaForChild(containerNode, augmentUint32SchemaNode.getQName());

        AugmentationNode augmentation = ImmutableAugmentationNodeSchemaAwareBuilder.get(augmentationSchema).withChild(
                ImmutableLeafNodeSchemaAwareBuilder.<Integer>get(augmentUint32SchemaNode).withValue(11).build())
                .build();

        builder.withChild(augmentation);

        // This should fail with schema, since the leaf comes from augmentation
        // builder.withChild(ImmutableLeafNodeSchemaAwareBuilder.<Integer>get(augmentUint32SchemaNode).withValue(11).build());

        LeafSchemaNode augumentString1SchemaNode = (LeafSchemaNode) getSchemaNode(schema, "test", "augmentString1");
        LeafSchemaNode augumentString2SchemaNode = (LeafSchemaNode) getSchemaNode(schema, "test", "augmentString2");

        ChoiceNode choice1SchemaNode = (ChoiceNode) getSchemaNode(schema, "test", "choice");
        org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode choice = ImmutableChoiceNodeSchemaAwareBuilder.get(choice1SchemaNode)
                .withChild(ImmutableLeafNodeSchemaAwareBuilder.<String>get(augumentString1SchemaNode).withValue("case1")
                        .build())
                        // This should fail, since child node belongs to different case
//                .withChild(ImmutableLeafNodeSchemaAwareBuilder.<String>get(augumentString2SchemaNode).withValue("case2")
//                        .build())
                .build();

        builder.withChild(choice);

        System.out.println(builder.build());
    }

    private AugmentationSchema getAugmentationSchemaForChild(ContainerSchemaNode containerNode, QName qName) {
        for (AugmentationSchema augmentationSchema : containerNode.getAvailableAugmentations()) {
            if(augmentationSchema.getDataChildByName(qName) != null) {
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

    DataSchemaNode getSchemaNode(SchemaContext context, String moduleName, String childNodeName) {
        for (Module module : context.getModules()) {
            if (module.getName().equals(moduleName)) {
                DataSchemaNode found = findChildNode(module.getChildNodes(), childNodeName);
                Preconditions.checkState(found!=null, "Unable to find %s", childNodeName);
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
            if(dataSchemaNode instanceof DataNodeContainer) {
                containers.add((DataNodeContainer) dataSchemaNode);
            } else if(dataSchemaNode instanceof ChoiceNode) {
                containers.addAll(((ChoiceNode) dataSchemaNode).getCases());
            }
        }

        for (DataNodeContainer container : containers) {
            DataSchemaNode retVal = findChildNode(container.getChildNodes(), name);
            if(retVal != null) {
                return retVal;
            }
        }

        return null;
    }
}
