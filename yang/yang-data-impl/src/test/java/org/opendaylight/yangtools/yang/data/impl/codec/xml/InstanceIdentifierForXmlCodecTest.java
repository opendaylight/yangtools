/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.RetestUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.class)
public class InstanceIdentifierForXmlCodecTest {
    private SchemaContext schemaContext;

    public static final String XML_CONTENT = "<input xmlns=\"urn:opendaylight:controller:iid:test\">" + "<a>/cont</a>"
            + "</input>";

    private final String NS = "urn:opendaylight:controller:iid:test";

    private final String REVISION = "2014-07-28";

    private Element elementOrig;

    @Before
    public void setup() throws Exception {
        final File rpcTestYang = new File(getClass().getResource("iid-test.yang").toURI());
        this.schemaContext = RetestUtils.parseYangSources(rpcTestYang);
        final Document doc = XmlDocumentUtilsTest.readXmlToDocument(XML_CONTENT);
        final YangInstanceIdentifier.NodeIdentifier container = new YangInstanceIdentifier.NodeIdentifier(
                QName.create(this.NS, this.REVISION, "cont"));
        final NormalizedNode<?, ?> data = ImmutableNodes.fromInstanceId(this.schemaContext,
                YangInstanceIdentifier.create(container));
        Assert.assertNotNull(data);

        this.elementOrig = XmlDocumentUtils.createElementFor(doc, data);
    }

    @Test
    public void deserializeTest() throws Exception {
        final YangInstanceIdentifier deserialize = InstanceIdentifierForXmlCodec.deserialize(this.elementOrig,
                this.schemaContext);
        Assert.assertEquals("/", deserialize.toString());
    }

    @Test
    public void serializeTest() throws Exception {

        final QName name = QName.create(this.NS, this.REVISION, "cont");
        final YangInstanceIdentifier id = YangInstanceIdentifier.builder().node(name).build();

        final Element el = InstanceIdentifierForXmlCodec.serialize(id, this.elementOrig, this.schemaContext);
        Assert.assertEquals(this.elementOrig, el);
    }
}
