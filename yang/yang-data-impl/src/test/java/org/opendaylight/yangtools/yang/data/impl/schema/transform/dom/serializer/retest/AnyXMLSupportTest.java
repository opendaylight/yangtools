/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer.retest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.RetestUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedDataBuilderTest;
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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class AnyXMLSupportTest {
    private static final XMLOutputFactory XML_FACTORY;
    private static final DocumentBuilderFactory BUILDERFACTORY;

    static {
        XML_FACTORY = XMLOutputFactory.newFactory();
        XML_FACTORY.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);

        BUILDERFACTORY = DocumentBuilderFactory.newInstance();
        BUILDERFACTORY.setNamespaceAware(true);
        BUILDERFACTORY.setCoalescing(true);
        BUILDERFACTORY.setIgnoringElementContentWhitespace(true);
        BUILDERFACTORY.setIgnoringComments(true);
    }

    private QNameModule fooModuleQName;
    private QName myContainer1;
    private QName myContainer2;
    private QName myLeaf1;
    private QName myLeaf2;
    private QName myAnyXMLData;
    private SchemaPath myAnyXMLDataPath;
    private SchemaContext schemaContext;

    @Before
    public void Init() throws Exception {
        fooModuleQName = QNameModule.create(new URI("foo"), SimpleDateFormatUtil.getRevisionFormat().parse("1970-01-01"));
        myContainer1 = QName.create(fooModuleQName, "my-container-1");
        myContainer2 = QName.create(fooModuleQName, "my-container-2");
        myLeaf1 = QName.create(fooModuleQName, "my-leaf-1");
        myLeaf2 = QName.create(fooModuleQName, "my-leaf-2");
        myAnyXMLData = QName.create(fooModuleQName, "my-anyxml-data");
        myAnyXMLDataPath = SchemaPath.create(true, myContainer1, myAnyXMLData);
        schemaContext = RetestUtils.parseYangSources(new File(getClass().getResource(
                "/anyxml-support/yang/foo.yang").toURI()),
                new File(getClass().getResource(
                        "/anyxml-support/yang/bar.yang").toURI()));
    }

    @Test
    public void testRawAnyXML() throws Exception {
        final Document doc = loadDocument("/anyxml-support/xml/bar.xml");

        DataSchemaNode barContainer = schemaContext.getDataChildByName("bar");
        assertTrue(barContainer instanceof ContainerSchemaNode);
        final YangModeledAnyXmlSchemaNode yangModeledAnyXML = new YangModeledAnyXMLSchemaNodeImplTest(myAnyXMLData, myAnyXMLDataPath, barContainer);

        final YangModeledAnyXmlNode output = DomToNormalizedNodeParserFactory
                .getInstance(DomUtils.defaultValueCodecProvider(), schemaContext).getYangModeledAnyXmlNodeParser()
                .parse(Collections.singletonList(doc.getDocumentElement()), yangModeledAnyXML);

        assertNotNull(output);
    }

    @Test
    public void testWithSchemaContext() throws Exception {
        final Document doc = loadDocument("/anyxml-support/xml/foo.xml");

        final ContainerNode output = DomToNormalizedNodeParserFactory
                .getInstance(DomUtils.defaultValueCodecProvider(), schemaContext).getContainerNodeParser()
                .parse(Collections.singletonList(doc.getDocumentElement()), schemaContext);

        assertNotNull(output);
    }

    private static Document loadDocument(final String xmlPath) throws IOException, SAXException {
        final InputStream resourceAsStream = NormalizedDataBuilderTest.class.getResourceAsStream(xmlPath);

        final Document currentConfigElement = readXmlToDocument(resourceAsStream);
        Preconditions.checkNotNull(currentConfigElement);
        return currentConfigElement;
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

    static class YangModeledAnyXMLSchemaNodeImplTest implements YangModeledAnyXmlSchemaNode{
        private final SchemaPath path;
        private final QName qName;
        private final DataSchemaNode contentSchema;

        public YangModeledAnyXMLSchemaNodeImplTest(QName qName, SchemaPath path, DataSchemaNode contentSchema) {
            this.qName = qName;
            this.path = path;
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

        @Override
        public QName getQName() {
            return qName;
        }

        @Override
        public SchemaPath getPath() {
            return path;
        }

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

        @Override
        public Status getStatus() {
            return null;
        }

        @Override
        public DataSchemaNode getSchemaOfAnyXmlData() {
            return contentSchema;
        }
    }
}
