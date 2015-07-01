/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec.xml.retest;

import com.google.common.base.Charsets;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Before;
import org.opendaylight.yangtools.yang.data.impl.RetestUtils;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlDocumentUtilsTest {

    private static final DocumentBuilderFactory BUILDERFACTORY;

    static {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        BUILDERFACTORY = factory;
    }

    public static final String XML_CONTENT = "<input xmlns=\"urn:opendaylight:controller:rpc:test\">\n"
            + "<a>value</a>\n" + "<ref xmlns:ltha=\"urn:opendaylight:controller:rpc:test\">"
            + "/ltha:cont/ltha:l[  ltha:id='id/foo/bar'  ]" + "</ref>\n" + "</input>";

    public static final String RPC_REPLY = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"m-1\">\n"
            + " <ok/>\n" + "</rpc-reply>";

    private SchemaContext schema;
    private RpcDefinition testRpc;

    @Before
    public void setUp() throws Exception {
        File rpcTestYang = new File(getClass().getResource("/rpc-test.yang").toURI());
        schema = RetestUtils.parseYangSources(rpcTestYang);
        final Module rpcTestModule = schema.getModules().iterator().next();
        testRpc = rpcTestModule.getRpcs().iterator().next();
    }

    public static Document readXmlToDocument(final String xmlContent) throws SAXException, IOException {
        return readXmlToDocument(new ByteArrayInputStream(xmlContent.getBytes(Charsets.UTF_8)));
    }

    public static Document readXmlToDocument(final InputStream xmlContent) throws SAXException, IOException {
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
