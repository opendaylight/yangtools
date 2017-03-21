/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeSchemaAwareBuilder;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangModeledAnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YangModeledAnyXMLDeserializationTest {

    private QNameModule fooModuleQName;
    private QNameModule barModuleQName;
    private QName myContainer1;
    private QName myContainer2;
    private QName innerContainer;
    private QName myLeaf3;
    private QName myLeaf2;
    private QName myLeaf1;
    private QName myAnyXMLDataBar;
    private QName myAnyXMLDataFoo;
    private SchemaContext schemaContext;

    @Before
    public void Init() throws Exception {
        barModuleQName = QNameModule.create(new URI("bar"), SimpleDateFormatUtil.getRevisionFormat()
                .parse("1970-01-01"));
        myContainer1 = QName.create(barModuleQName, "my-container-1");
        myLeaf1 = QName.create(barModuleQName, "my-leaf-1");
        myAnyXMLDataBar = QName.create(barModuleQName, "my-anyxml-data");

        fooModuleQName = QNameModule.create(new URI("foo"), SimpleDateFormatUtil.getRevisionFormat()
                .parse("1970-01-01"));
        myContainer2 = QName.create(fooModuleQName, "my-container-2");
        innerContainer = QName.create(fooModuleQName, "inner-container");
        myLeaf3 = QName.create(fooModuleQName, "my-leaf-3");
        myLeaf2 = QName.create(fooModuleQName, "my-leaf-2");
        myAnyXMLDataFoo = QName.create(fooModuleQName, "my-anyxml-data");

        schemaContext = YangParserTestUtils.parseYangSources("/anyxml-support/yang");
    }

    @Test
    public void testRawAnyXMLFromBar() throws Exception {
        DataSchemaNode barContainer = schemaContext.getDataChildByName(QName.create(barModuleQName, "bar"));
        assertTrue(barContainer instanceof ContainerSchemaNode);
        final YangModeledAnyXmlSchemaNode yangModeledAnyXML = new YangModeledAnyXMLSchemaNodeImplTest(myAnyXMLDataBar,
                (ContainerSchemaNode) barContainer);

        final InputStream resourceAsStream = YangModeledAnyXMLDeserializationTest.class.getResourceAsStream(
                "/anyxml-support/xml/bar.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final ImmutableContainerNodeBuilder result = (ImmutableContainerNodeBuilder) ImmutableContainerNodeSchemaAwareBuilder
                        .create(yangModeledAnyXML.getSchemaOfAnyXmlData());

        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, yangModeledAnyXML);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> output = result.build();

        Collection<DataContainerChild<? extends PathArgument, ?>> value = ((ContainerNode) output).getValue();
        assertEquals(2, value.size());

        Optional<DataContainerChild<? extends PathArgument, ?>> child = ((ContainerNode) output)
                .getChild(new NodeIdentifier(myContainer1));
        assertTrue(child.orNull() instanceof ContainerNode);
        ContainerNode myContainerNode1 = (ContainerNode) child.get();

        Optional<DataContainerChild<? extends PathArgument, ?>> child2 = myContainerNode1.getChild(new NodeIdentifier(
                myLeaf1));
        assertTrue(child2.orNull() instanceof LeafNode);
        LeafNode<?> LeafNode1 = (LeafNode<?>) child2.get();

        Object leafNode1Value = LeafNode1.getValue();
        assertEquals("value1", leafNode1Value);
    }

    @Test
    public void testRealSchemaContextFromFoo() throws Exception {
        final InputStream resourceAsStream = YangModeledAnyXMLDeserializationTest.class.getResourceAsStream(
                "/anyxml-support/xml/foo.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final ImmutableContainerNodeBuilder result = (ImmutableContainerNodeBuilder) ImmutableContainerNodeSchemaAwareBuilder
                .create(schemaContext);

        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, schemaContext);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> output = result.build();

        Optional<DataContainerChild<? extends PathArgument, ?>> child = ((ContainerNode) output).getChild(
                new NodeIdentifier(myAnyXMLDataFoo));
        assertTrue(child.orNull() instanceof YangModeledAnyXmlNode);
        YangModeledAnyXmlNode yangModeledAnyXmlNode = (YangModeledAnyXmlNode) child.get();

        DataSchemaNode schemaOfAnyXmlData = yangModeledAnyXmlNode.getSchemaOfAnyXmlData();
        DataSchemaNode expectedSchemaOfAnyXmlData = schemaContext.getDataChildByName(myContainer2);
        assertEquals(expectedSchemaOfAnyXmlData, schemaOfAnyXmlData);

        Collection<DataContainerChild<? extends PathArgument, ?>> value = yangModeledAnyXmlNode.getValue();
        assertEquals(2, value.size());

        Optional<DataContainerChild<? extends PathArgument, ?>> child2 = yangModeledAnyXmlNode
                .getChild(new NodeIdentifier(innerContainer));
        assertTrue(child2.orNull() instanceof ContainerNode);
        ContainerNode innerContainerNode = (ContainerNode) child2.get();

        Optional<DataContainerChild<? extends PathArgument, ?>> child3 = innerContainerNode
                .getChild(new NodeIdentifier(myLeaf2));
        assertTrue(child3.orNull() instanceof LeafNode);
        LeafNode<?> LeafNode2 = (LeafNode<?>) child3.get();

        Object leafNode2Value = LeafNode2.getValue();
        assertEquals("any-xml-leaf-2-value", leafNode2Value);

        Optional<DataContainerChild<? extends PathArgument, ?>> child4 = yangModeledAnyXmlNode
                .getChild(new NodeIdentifier(myLeaf3));
        assertTrue(child4.orNull() instanceof LeafNode);
        LeafNode<?> LeafNode3 = (LeafNode<?>) child4.get();

        Object leafNode3Value = LeafNode3.getValue();
        assertEquals("any-xml-leaf-3-value", leafNode3Value);
    }

    private static class YangModeledAnyXMLSchemaNodeImplTest implements YangModeledAnyXmlSchemaNode {
        private final QName qName;
        private final ContainerSchemaNode contentSchema;

        private YangModeledAnyXMLSchemaNodeImplTest(final QName qName, final ContainerSchemaNode contentSchema) {
            this.qName = qName;
            this.contentSchema = contentSchema;
        }

        @Override
        public boolean isAugmenting() {
            return false;
        }

        @Override
        public boolean isAddedByUses() {
            return false;
        }

        @Override
        public boolean isConfiguration() {
            return false;
        }

        @Override
        public ConstraintDefinition getConstraints() {
            return null;
        }

        @Nonnull
        @Override
        public QName getQName() {
            return qName;
        }

        @Nonnull
        @Override
        public SchemaPath getPath() {
            return null;
        }

        @Nonnull
        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String getReference() {
            return null;
        }

        @Nonnull
        @Override
        public Status getStatus() {
            return null;
        }

        @Nonnull
        @Override
        public ContainerSchemaNode getSchemaOfAnyXmlData() {
            return contentSchema;
        }
    }
}
