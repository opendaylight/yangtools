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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.RetestUtils;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.class)
public class InstanceIdentifierForXmlCodecTest {

    @Mock
    private Element element;

    private SchemaContext schemaContext;

    public static final String XML_CONTENT = "<input xmlns=\"urn:opendaylight:controller:iid:test\">" + "<a>value</a>"
            + "</input>";

    private final String NS = "urn:opendaylight:controller:iid:test";

    private final String REVISION = "2014-07-28";

    @Before
    public void setup() throws Exception {
        final File rpcTestYang = new File(getClass().getResource("iid-test.yang").toURI());
        this.schemaContext = RetestUtils.parseYangSources(rpcTestYang);
    }

    @Test
    public void deserializeTest() {
        Mockito.when(this.element.getTextContent()).thenReturn("");
        final YangInstanceIdentifier deserialize = InstanceIdentifierForXmlCodec.deserialize(this.element,
                this.schemaContext);
        Assert.assertEquals("/", deserialize.toString());
    }

    @Test
    public void serializeTest() throws Exception {
        final YangInstanceIdentifier id = YangInstanceIdentifier.builder().build();

        final Element el = InstanceIdentifierForXmlCodec.serialize(id, this.element, this.schemaContext);
        Assert.assertEquals(this.element, el);
    }

}
