/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLOutputFactory;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser.DomToNormalizedNodeParserFactory;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangModeledAnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class YangModeledAnyXMLDeserializationTest {
    private static final XMLOutputFactory XML_FACTORY;

    static {
        XML_FACTORY = XMLOutputFactory.newFactory();
        XML_FACTORY.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.FALSE);
    }

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
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/anyxml-support/yang");
    }

    @Test
    public void testRawAnyXMLFromBar() throws Exception {
        final Document doc = loadDocument("/anyxml-support/xml/bar.xml");

        final DataSchemaNode barContainer = schemaContext.getDataChildByName(QName.create(barModuleQName, "bar"));
        assertTrue(barContainer instanceof ContainerSchemaNode);
        final YangModeledAnyXmlSchemaNode yangModeledAnyXML = new YangModeledAnyXMLSchemaNodeImplTest(myAnyXMLDataBar,
                (ContainerSchemaNode) barContainer);

        final YangModeledAnyXmlNode output = DomToNormalizedNodeParserFactory
                .getInstance(DomUtils.defaultValueCodecProvider(), schemaContext).getYangModeledAnyXmlNodeParser()
                .parse(Collections.singletonList(doc.getDocumentElement()), yangModeledAnyXML);

        assertNotNull(output);

        final DataSchemaNode schemaOfAnyXmlData = output.getSchemaOfAnyXmlData();
        assertEquals(barContainer, schemaOfAnyXmlData);

        final Collection<DataContainerChild<? extends PathArgument, ?>> value = output.getValue();
        assertEquals(2, value.size());

        final Optional<DataContainerChild<? extends PathArgument, ?>> child = output
                .getChild(new NodeIdentifier(myContainer1));
        assertTrue(child.orNull() instanceof ContainerNode);
        final ContainerNode myContainerNode1 = (ContainerNode) child.get();

        final Optional<DataContainerChild<? extends PathArgument, ?>> child2 = myContainerNode1.getChild(new NodeIdentifier(
                myLeaf1));
        assertTrue(child2.orNull() instanceof LeafNode);
        final LeafNode<?> LeafNode1 = (LeafNode<?>) child2.get();

        final Object leafNode1Value = LeafNode1.getValue();
        assertEquals("value1", leafNode1Value);
    }

    @Test
    public void testRealSchemaContextFromFoo() throws Exception {
        final Document doc = loadDocument("/anyxml-support/xml/foo.xml");

        final ContainerNode output = DomToNormalizedNodeParserFactory
                .getInstance(DomUtils.defaultValueCodecProvider(), schemaContext).getContainerNodeParser()
                .parse(Collections.singletonList(doc.getDocumentElement()), schemaContext);

        assertNotNull(output);

        final Optional<DataContainerChild<? extends PathArgument, ?>> child = output.getChild(new NodeIdentifier(
                myAnyXMLDataFoo));
        assertTrue(child.orNull() instanceof YangModeledAnyXmlNode);
        final YangModeledAnyXmlNode yangModeledAnyXmlNode = (YangModeledAnyXmlNode) child.get();

        final DataSchemaNode schemaOfAnyXmlData = yangModeledAnyXmlNode.getSchemaOfAnyXmlData();
        final DataSchemaNode expectedSchemaOfAnyXmlData = schemaContext.getDataChildByName(myContainer2);
        assertEquals(expectedSchemaOfAnyXmlData, schemaOfAnyXmlData);

        final Collection<DataContainerChild<? extends PathArgument, ?>> value = yangModeledAnyXmlNode.getValue();
        assertEquals(2, value.size());

        final Optional<DataContainerChild<? extends PathArgument, ?>> child2 = yangModeledAnyXmlNode
                .getChild(new NodeIdentifier(innerContainer));
        assertTrue(child2.orNull() instanceof ContainerNode);
        final ContainerNode innerContainerNode = (ContainerNode) child2.get();

        final Optional<DataContainerChild<? extends PathArgument, ?>> child3 = innerContainerNode
                .getChild(new NodeIdentifier(myLeaf2));
        assertTrue(child3.orNull() instanceof LeafNode);
        final LeafNode<?> LeafNode2 = (LeafNode<?>) child3.get();

        final Object leafNode2Value = LeafNode2.getValue();
        assertEquals("any-xml-leaf-2-value", leafNode2Value);

        final Optional<DataContainerChild<? extends PathArgument, ?>> child4 = yangModeledAnyXmlNode
                .getChild(new NodeIdentifier(myLeaf3));
        assertTrue(child4.orNull() instanceof LeafNode);
        final LeafNode<?> LeafNode3 = (LeafNode<?>) child4.get();

        final Object leafNode3Value = LeafNode3.getValue();
        assertEquals("any-xml-leaf-3-value", leafNode3Value);
    }

    private static Document loadDocument(final String xmlPath) throws IOException, SAXException {
        final InputStream resourceAsStream = YangModeledAnyXMLDeserializationTest.class.getResourceAsStream(xmlPath);

        final Document currentConfigElement = readXmlToDocument(resourceAsStream);
        return Preconditions.checkNotNull(currentConfigElement);
    }

    private static Document readXmlToDocument(final InputStream xmlContent) throws IOException, SAXException {
        final Document doc = UntrustedXML.newDocumentBuilder().parse(xmlContent);
        doc.getDocumentElement().normalize();
        return doc;
    }

    static class YangModeledAnyXMLSchemaNodeImplTest implements YangModeledAnyXmlSchemaNode {
        private final QName qName;
        private final ContainerSchemaNode contentSchema;

        public YangModeledAnyXMLSchemaNodeImplTest(final QName qName, final ContainerSchemaNode contentSchema) {
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
