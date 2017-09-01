/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class Bug2964Test {

    public static final String XML_CONTENT = "<cont2 xmlns=\"urn:opendaylight:yangtools:leafref:test\">\n"
            + "<point-to-identityrefleaf>test-identity</point-to-identityrefleaf>\n" + "</cont2>";

    private static final String NAMESPACE = "urn:opendaylight:yangtools:leafref:test";
    private static final String TEST_IDENTITY = "test-identity";
    private static final String CONT_2 = "cont2";
    private static final String IDENTITY_LEAFREF = "point-to-identityrefleaf";

    private SchemaContext schema;

    @Before
    public void setUp() throws Exception {
        final File leafRefTestYang = new File(getClass().getResource("/leafref-test.yang").toURI());
        schema = YangParserTestUtils.parseYangFiles(leafRefTestYang);
    }

    public static Document readXmlToDocument(final String xmlContent) throws SAXException, IOException {
        return readXmlToDocument(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testLeafrefIdentityRefDeserialization() throws Exception {
        final URI namespaceUri = new URI(NAMESPACE);

        final Document document = readXmlToDocument(XML_CONTENT);
        final Element identityLeafRefElement = (Element) document.getDocumentElement().getFirstChild().getNextSibling();

        final Module leafrefModule = schema.findModuleByNamespaceAndRevision(namespaceUri, null);
        final ContainerSchemaNode cont2 = (ContainerSchemaNode) leafrefModule.getDataChildByName(QName.create(
                leafrefModule.getQNameModule(), CONT_2));
        final DataSchemaNode identityLeafRefSchema = cont2.getDataChildByName(QName.create(
                leafrefModule.getQNameModule(), IDENTITY_LEAFREF));
        final Object parsedValue = DomUtils.parseXmlValue(identityLeafRefElement, DomUtils.defaultValueCodecProvider(),
                identityLeafRefSchema, ((LeafSchemaNode) identityLeafRefSchema).getType(), schema);

        assertThat(parsedValue, instanceOf(QName.class));
        final QName parsedQName = (QName) parsedValue;
        assertEquals(namespaceUri, parsedQName.getNamespace());
        assertEquals(TEST_IDENTITY, parsedQName.getLocalName());
    }

    public static Document readXmlToDocument(final InputStream xmlContent) throws SAXException, IOException {
        final Document doc = UntrustedXML.newDocumentBuilder().parse(xmlContent);
        doc.getDocumentElement().normalize();
        return doc;
    }
}
