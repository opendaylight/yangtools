/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public abstract class AbstractAnydataTest {
    static final QName FOO_QNAME = QName.create("test-anydata", "foo");
    static final QName CONT_QNAME = QName.create(FOO_QNAME, "cont");
    static final QName CONT_ANY_QNAME = QName.create(FOO_QNAME, "cont-any");
    static final QName CONT_LEAF_QNAME = QName.create(FOO_QNAME, "cont-leaf");

    static final NodeIdentifier FOO_NODEID = NodeIdentifier.create(FOO_QNAME);
    static final NodeIdentifier CONT_NODEID = NodeIdentifier.create(CONT_QNAME);
    static final NodeIdentifier CONT_ANY_NODEID = NodeIdentifier.create(CONT_ANY_QNAME);
    static final NodeIdentifier CONT_LEAF_NODEID = NodeIdentifier.create(CONT_LEAF_QNAME);

    static final LeafNode<String> CONT_LEAF = ImmutableNodes.leafNode(CONT_LEAF_NODEID, "abc");

    static EffectiveModelContext SCHEMA_CONTEXT;

    @BeforeClass
    public static void beforeClass() throws Exception {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResource("/test-anydata.yang");
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    static DOMSourceAnydata toDOMSource(final String str) {
        try {
            return new DOMSourceAnydata(new DOMSource(
                // DOMSource must have a single document element, which we are ignoring
                readXmlToDocument(toInputStream("<IGNORED>" + str + "</IGNORED>")).getDocumentElement()));
        } catch (IOException | SAXException e) {
            throw new IllegalStateException(e);
        }
    }

    static InputStream toInputStream(final String str) {
        return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
    }

    static Document readXmlToDocument(final InputStream xmlContent) throws IOException, SAXException {
        final Document doc = UntrustedXML.newDocumentBuilder().parse(xmlContent);
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * Load external XML resources.
     * @param xmlPath {@link String} path to source
     * @return {@link Document}
     * @throws IOException Exception in Loading file
     * @throws SAXException XML parse exception
     */
    static Document loadXmlDocument(final String xmlPath) throws IOException, SAXException {
        return requireNonNull(readXmlToDocument(loadResourcesAsInputStream(xmlPath)));
    }

    /**
     * Load external resources as {@link InputStream}.
     * @param xmlPath {@link String} Path to file
     * @return {@link InputStream}
     */
    static InputStream loadResourcesAsInputStream(final String xmlPath) {
        return SchemalessXMLStreamNormalizedNodeStreamWriterTest.class.getResourceAsStream(xmlPath);
    }

    /**
     * Transform Node object to formatted XML string.
     * @param xml {@link Node}
     * @return {@link String}
     */
    static String toString(final Node xml) {
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            final StreamResult result = new StreamResult(new StringWriter());
            final DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Unable to serialize xml element " + xml, e);
        }
    }

    /**
     * Transform DomSource object to formatted XML string.
     * @param node {@link DOMSource}
     * @return {@link String}
     * @throws TransformerException Internal {@link Transformer} exception
     */
    static String getXmlFromDOMSource(final DOMSource node) throws TransformerException {
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(node, new StreamResult(writer));
        return writer.toString();
    }
}
