/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
public class RpcReplyToDomTest {

    private static final DocumentBuilderFactory BUILDER_FACTORY;
    private static final String RES_SCHEMA_DIR = "/org/opendaylight/yangtools/yang/data/impl/schema";

    private final static String MODEL_NAMESPACE = "org:opendaylight:rpc-reply:test:ns:yang";
    private final static String MODEL_REVISION = "2014-07-17";

    private final static QName RPC_OUTPUT_QNAME = QName.create(MODEL_NAMESPACE, MODEL_REVISION, "output");
    private final static QName ROCK_THE_HOUSE_QNAME = QName.create(MODEL_NAMESPACE, MODEL_REVISION, "rock-the-house");
    private final static QName ACTIV_SW_IMAGE_QNAME = QName.create(MODEL_NAMESPACE, MODEL_REVISION, "activate-software-image");

    private SchemaContext schemaContext;
    private Document testPayload1;
    private Document testPayload2;

    static {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        BUILDER_FACTORY = factory;
    }

    @Before
    public void setup() throws Exception {
        final List<InputStream> modelsToParse = Collections
                .singletonList(getClass().getResourceAsStream(RES_SCHEMA_DIR + "/rpc-test-model.yang"));
        assertNotNull(modelsToParse);
        assertNotNull(modelsToParse.get(0));

        final YangContextParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModelsFromStreams(modelsToParse);
        assertTrue(!modules.isEmpty());
        schemaContext = parser.resolveSchemaContext(modules);
        assertNotNull(schemaContext);

        final InputStream rpcPayloadStream1 = getClass().getResourceAsStream(RES_SCHEMA_DIR + "/rpc-test-payload1.xml");
        InputStream rpcPayloadStream2 = getClass().getResourceAsStream(RES_SCHEMA_DIR + "/rpc-test-payload2.xml");

        assertNotNull(rpcPayloadStream1);
        assertNotNull(rpcPayloadStream2);

        testPayload1 = readXmlToDocument(rpcPayloadStream1);
        testPayload2 = readXmlToDocument(rpcPayloadStream2);

        assertNotNull(testPayload1);
        assertNotNull(testPayload2);
    }

    private static Document readXmlToDocument(final InputStream xmlContent) throws SAXException, IOException {
        DocumentBuilder dBuilder;
        try {
            dBuilder = BUILDER_FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Failed to parse XML document", e);
        }
        Document doc = dBuilder.parse(xmlContent);

        doc.getDocumentElement().normalize();
        return doc;
    }

    @Test
    public void test() {
        final CompositeNode rockNode = XmlDocumentUtils
                .rpcReplyToDomNodes(testPayload1, ROCK_THE_HOUSE_QNAME, schemaContext);
        assertNotNull(rockNode);

        final String namespace = "org:opendaylight:rpc-reply:test:ns:yang";
        final String revision = "2014-07-17";

        CompositeNode output = rockNode.getFirstCompositeByName(RPC_OUTPUT_QNAME);
        assertNotNull(output);

        final SimpleNode<?> zipCode = output.getFirstSimpleByName(
                QName.create(namespace, revision, "zip-code"));
        assertNotNull(zipCode);

        final CompositeNode activNode = XmlDocumentUtils
                .rpcReplyToDomNodes(testPayload2, ACTIV_SW_IMAGE_QNAME, schemaContext);
        assertNotNull(activNode);

        output = activNode.getFirstCompositeByName(RPC_OUTPUT_QNAME);
        assertNotNull(output);

        final CompositeNode imgProps = output
                .getFirstCompositeByName(QName.create(namespace, revision, "image-properties"));
        assertNotNull(imgProps);
        final CompositeNode imgProperty = imgProps
                .getFirstCompositeByName(QName.create(namespace, revision, "image-property"));
        assertNotNull(imgProperty);

        final SimpleNode<?> imgId = imgProperty
                .getFirstSimpleByName(QName.create(namespace, revision, "image-id"));
        assertNotNull(imgId);
    }
}
