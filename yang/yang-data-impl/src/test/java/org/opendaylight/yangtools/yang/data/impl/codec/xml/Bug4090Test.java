/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class Bug4090Test {

    private static QNameModule foo;

    private static QName l2;
    private static QName interconnection;
    private static QName bridgeBased;
    private static QName bridgeDomain;

    private static SchemaContext schema;
    public static final String XML_CONTENT = "<l2 xmlns=\"foo\"><bridge-domain>32</bridge-domain></l2>";

    private static final DocumentBuilderFactory BUILDERFACTORY;

    static {
        final DocumentBuilderFactory factory = DocumentBuilderFactory
                .newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        BUILDERFACTORY = factory;
    }

    @Before
    public void setUp() throws Exception {
        initQnames();
        initSchemaContext();
    }

    private static void initQnames() throws URISyntaxException, ParseException {
        foo = QNameModule.create(new URI("foo"), SimpleDateFormatUtil
                .getRevisionFormat().parse("2015-09-10"));

        l2 = QName.create(foo, "l2");
        interconnection = QName.create(foo, "interconnection");
        bridgeBased = QName.create(foo, "bridge-based");
        bridgeDomain = QName.create(foo, "bridge-domain");
    }

    private static void initSchemaContext() throws URISyntaxException,
            IOException, YangSyntaxErrorException {
        final File resourceFile = new File(Bug4090Test.class.getResource(
                "/bug-4090/foo.yang").toURI());

        final File resourceDir = resourceFile.getParentFile();

        final YangParserImpl parser = YangParserImpl.getInstance();
        schema = parser.parseFile(resourceFile, resourceDir);
    }

    @Test
    public void testLeafrefDeserialization() throws Exception {

        final Document document = readXmlToDocument(XML_CONTENT);
        final Element bridgeDomainElement = (Element) document
                .getDocumentElement().getFirstChild();

        ContainerSchemaNode l2Schema = (ContainerSchemaNode) schema
                .getDataChildByName(l2);
        ChoiceNode interconnectionSchema = (ChoiceNode) l2Schema
                .getDataChildByName(interconnection);
        DataSchemaNode bridgeDomainSchema = interconnectionSchema
                .getCaseNodeByName(bridgeBased)
                .getDataChildByName(bridgeDomain);

        Node<?> domNode = XmlDocumentUtils.toDomNode(bridgeDomainElement, Optional.of(bridgeDomainSchema), Optional.of(DomUtils.defaultValueCodecProvider()), Optional.of(schema));

        assertNotNull(domNode);
        assertNotNull(domNode.getValue());
        assertEquals(Integer.class, domNode.getValue().getClass());
    }

    public static Document readXmlToDocument(final String xmlContent)
            throws SAXException, IOException {
        return readXmlToDocument(new ByteArrayInputStream(
                xmlContent.getBytes(Charsets.UTF_8)));
    }

    public static Document readXmlToDocument(final InputStream xmlContent)
            throws SAXException, IOException {
        final DocumentBuilder dBuilder;
        try {
            dBuilder = BUILDERFACTORY.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new IllegalStateException("Failed to parse XML document", e);
        }
        final Document doc = dBuilder.parse(xmlContent);

        doc.getDocumentElement().normalize();
        return doc;
    }

}
