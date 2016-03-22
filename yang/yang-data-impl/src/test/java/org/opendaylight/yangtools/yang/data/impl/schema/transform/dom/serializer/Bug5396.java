/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
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
import org.opendaylight.yangtools.yang.data.impl.RetestUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser.DomToNormalizedNodeParserFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Bug5396 {
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
    private QName root;
    private SchemaContext schemaContext;

    @Before
    public void Init() throws Exception {
        fooModuleQName = QNameModule.create(new URI("foo"), SimpleDateFormatUtil.getRevisionFormat()
                .parse("2016-03-22"));
        root = QName.create(fooModuleQName, "root");
        schemaContext = RetestUtils.parseYangSources(new File(getClass().getResource("/bug5396/yang/foo.yang").toURI()));
    }

    @Test
    public void test() throws Exception {
        final Document doc = loadDocument("/bug5396/xml/foo.xml");

        final ContainerNode output = DomToNormalizedNodeParserFactory
                .getInstance(DomUtils.defaultValueCodecProvider(), schemaContext).getContainerNodeParser()
                .parse(Collections.singletonList(doc.getDocumentElement()), schemaContext);

        assertNotNull(output);

        Optional<DataContainerChild<? extends PathArgument, ?>> child = output.getChild(new NodeIdentifier(
                root));
        assertTrue(child.orNull() instanceof ContainerNode);
        ContainerNode rootContainer = (ContainerNode) child.get();

        Optional<DataContainerChild<? extends PathArgument, ?>> child2 = rootContainer.getChild(new NodeIdentifier(QName.create(fooModuleQName, "my-leaf")));
        assertTrue(child2.orNull() instanceof LeafNode);

        System.out.println(child2.get().getValue());
    }

    private static Document loadDocument(final String xmlPath) throws IOException, SAXException {
        final InputStream resourceAsStream = Bug5396.class.getResourceAsStream(xmlPath);

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
}
