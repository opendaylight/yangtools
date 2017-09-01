/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.class)
public class InstanceIdentifierForXmlCodecTest {

    public static final String XML_CONTENT = "<input xmlns=\"urn:opendaylight:controller:iid:test\">" + "<a>/cont</a>"
            + "</input>";

    private static final String NS = "urn:opendaylight:controller:iid:test";
    private static final String REVISION = "2014-07-28";

    private SchemaContext schemaContext;
    private Element elementOrig;

    @Before
    public void setup() throws Exception {
        final File rpcTestYang = new File(getClass().getResource("iid-test.yang").toURI());
        this.schemaContext = YangParserTestUtils.parseYangFiles(rpcTestYang);

        final YangInstanceIdentifier.NodeIdentifier container = new YangInstanceIdentifier.NodeIdentifier(
                QName.create(InstanceIdentifierForXmlCodecTest.NS, InstanceIdentifierForXmlCodecTest.REVISION, "cont"));
        final NormalizedNode<?, ?> data = ImmutableNodes.fromInstanceId(this.schemaContext,
                YangInstanceIdentifier.create(container));
        assertNotNull(data);

        final Document doc = XmlDocumentUtilsTest.readXmlToDocument(XML_CONTENT);
        this.elementOrig = XmlDocumentUtils.createElementFor(doc, data);
        assertNotNull(this.elementOrig);
    }

    @Test
    public void deserializeTest() throws Exception {
        final YangInstanceIdentifier deserialize = InstanceIdentifierForXmlCodec.deserialize(this.elementOrig,
                this.schemaContext);
        assertEquals("/", deserialize.toString());
    }

    @Test
    public void serializeTest() throws Exception {

        final QName name = QName.create(InstanceIdentifierForXmlCodecTest.NS,
                InstanceIdentifierForXmlCodecTest.REVISION, "cont");
        final YangInstanceIdentifier id = YangInstanceIdentifier.builder().node(name).build();

        final Element el = InstanceIdentifierForXmlCodec.serialize(id, this.elementOrig, this.schemaContext);
        assertEquals(this.elementOrig, el);
    }
}
