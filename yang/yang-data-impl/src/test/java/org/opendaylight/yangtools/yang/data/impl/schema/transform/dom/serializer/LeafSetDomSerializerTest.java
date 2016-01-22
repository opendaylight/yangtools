/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import java.io.IOException;
import java.text.ParseException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.w3c.dom.Element;

public class LeafSetDomSerializerTest {
    private LeafSetEntryNodeDomSerializer temp;
    private LeafListSchemaNode currentLeafList;
    private LeafSetEntryNode<?> tempLeafList;

    @Test
    public void leafSetDomSerializerTest() throws IOException, YangSyntaxErrorException, ParseException, ReactorException {
        leafSetEntryNodeDomSerializerTest();
        leafSetNodeDomSerializerTest();
    }

    private void leafSetEntryNodeDomSerializerTest() throws ReactorException, IOException, YangSyntaxErrorException,
            ParseException {
        final ContainerSchemaNode currentContainer = (ContainerSchemaNode) DomSerializerTestUtils.getSchemaContext()
                .findModuleByName("serializer-test", null)
                .getDataChildByName(DomSerializerTestUtils.generateQname("root"));
        currentLeafList = (LeafListSchemaNode) currentContainer.getDataChildByName(DomSerializerTestUtils
                .generateQname("first-leaf-list"));

        final NodeWithValue<String> barPath = new NodeWithValue<>(DomSerializerTestUtils.generateQname("first-leaf-list"), "bar");
        tempLeafList = ImmutableLeafSetEntryNodeBuilder.<String>create()
                .withNodeIdentifier(barPath)
                .withValue("bar")
                .build();

        temp = new LeafSetEntryNodeDomSerializer(DomSerializerTestUtils.DOC, DomSerializerTestUtils.CODEC_PROVIDER);
        final Element element = temp.serializeLeaf(currentLeafList, tempLeafList);

        DomSerializerTestUtils.testResults("<first-leaf-list xmlns=\"dom-serializer-test\">bar</first-leaf-list>",
                element);
    }

    private void leafSetNodeDomSerializerTest() {
        final LeafSetNodeDomSerializer nodeDomSerializer = new LeafSetNodeDomSerializer(temp);
        final LeafSetEntryNodeDomSerializer leafSetEntryNodeSerializer = (LeafSetEntryNodeDomSerializer)
                nodeDomSerializer.getLeafSetEntryNodeSerializer();
        final Element element = temp.serializeLeaf(currentLeafList, tempLeafList);

        leafSetEntryNodeSerializer.serializeLeaf(currentLeafList, tempLeafList);
        DomSerializerTestUtils.testResults("<first-leaf-list xmlns=\"dom-serializer-test\">bar</first-leaf-list>",
                element);
    }
}