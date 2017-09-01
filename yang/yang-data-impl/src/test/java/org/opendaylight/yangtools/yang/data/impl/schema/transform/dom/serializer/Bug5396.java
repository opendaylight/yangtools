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
import static org.junit.Assert.fail;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
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
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser.DomToNormalizedNodeParserFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Bug5396 {
    private static final XMLOutputFactory XML_FACTORY;

    static {
        XML_FACTORY = XMLOutputFactory.newFactory();
        XML_FACTORY.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.FALSE);
    }

    private QNameModule fooModuleQName;
    private QName root;
    private SchemaContext schemaContext;

    @Before
    public void Init() throws Exception {
        fooModuleQName = QNameModule.create(new URI("foo"), SimpleDateFormatUtil.getRevisionFormat()
                .parse("2016-03-22"));
        root = QName.create(fooModuleQName, "root");
        schemaContext = YangParserTestUtils
                .parseYangFiles(new File(getClass().getResource("/bug5396/yang/foo.yang").toURI()));
    }

    @Test
    public void test() throws Exception {
        testInputXML("/bug5396/xml/foo.xml", "dp1o34");
        testInputXML("/bug5396/xml/foo2.xml", "dp0s3f9");
        testInputXML("/bug5396/xml/foo3.xml", "dp09P1p2s3");
        testInputXML("/bug5396/xml/foo4.xml", "dp0p3p1");
        testInputXML("/bug5396/xml/foo5.xml", "dp0s3");

        try {
            testInputXML("/bug5396/xml/invalid-foo.xml", null);
            fail("Test should fail due to invalid input string");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith(
                    "Failed to parse element [my-leaf: null] as leaf AbsoluteSchemaPath{path=[(foo?revision=2016-03-22)root, (foo?revision=2016-03-22)my-leaf]"));
        }
    }

    public void testInputXML(final String xmlPath, final String expectedValue) throws Exception {
        final Document doc = loadDocument(xmlPath);

        final ContainerNode output = DomToNormalizedNodeParserFactory
                .getInstance(DomUtils.defaultValueCodecProvider(), schemaContext).getContainerNodeParser()
                .parse(Collections.singletonList(doc.getDocumentElement()), schemaContext);

        assertNotNull(output);

        Optional<DataContainerChild<? extends PathArgument, ?>> child = output.getChild(new NodeIdentifier(root));
        assertTrue(child.orNull() instanceof ContainerNode);
        ContainerNode rootContainer = (ContainerNode) child.get();

        Optional<DataContainerChild<? extends PathArgument, ?>> myLeaf = rootContainer.getChild(new NodeIdentifier(
                QName.create(fooModuleQName, "my-leaf")));
        assertTrue(myLeaf.orNull() instanceof LeafNode);

        assertEquals(expectedValue, myLeaf.get().getValue());
    }

    private static Document loadDocument(final String xmlPath) throws IOException, SAXException {
        final InputStream resourceAsStream = Bug5396.class.getResourceAsStream(xmlPath);

        final Document currentConfigElement = readXmlToDocument(resourceAsStream);
        Preconditions.checkNotNull(currentConfigElement);
        return currentConfigElement;
    }

    private static Document readXmlToDocument(final InputStream xmlContent) throws IOException, SAXException {
        final Document doc = UntrustedXML.newDocumentBuilder().parse(xmlContent);
        doc.getDocumentElement().normalize();
        return doc;
    }
}
