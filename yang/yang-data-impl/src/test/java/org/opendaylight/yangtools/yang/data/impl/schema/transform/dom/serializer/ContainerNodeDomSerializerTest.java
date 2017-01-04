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

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.w3c.dom.Element;

public class ContainerNodeDomSerializerTest {

    @Test
    public void containerNodeDomSerializerTest() throws ReactorException, IOException, YangSyntaxErrorException,
            ParseException, URISyntaxException {
        final ContainerSchemaNode currentContainer = (ContainerSchemaNode) DomSerializerTestUtils.getSchemaContext()
                .findModuleByName("serializer-test", null)
                .getDataChildByName(DomSerializerTestUtils.generateQname("root"));
        final ContainerNode tempContainer = ImmutableContainerNodeBuilder.create().withNodeIdentifier(
                DomSerializerTestUtils.getNodeIdentifier
                ("root", "dom-serializer-test", "2016-01-01")).build();
        final ContainerNodeDomSerializer temp = new ContainerNodeDomSerializer(DomSerializerTestUtils.DOC,
                DomSerializerTestUtils.MOCK_DISPATCHER);
        final Iterable<Element> serialize = temp.serialize(currentContainer, tempContainer);

        DomSerializerTestUtils.testResults("<root xmlns=\"dom-serializer-test\"/>", serialize.iterator().next());
        assertNotNull(temp.getNodeDispatcher());
    }

    @Test
    public void choiceNodeDomSerializerTest() {
        final ChoiceNodeDomSerializer choice = new ChoiceNodeDomSerializer(DomSerializerTestUtils.MOCK_DISPATCHER);
        assertEquals(DomSerializerTestUtils.MOCK_DISPATCHER, choice.getNodeDispatcher());
    }

    @Test
    public void augmentationNodeDomSerializerTest() {
        final AugmentationNodeDomSerializer augment = new AugmentationNodeDomSerializer(DomSerializerTestUtils
                .MOCK_DISPATCHER);
        assertEquals(DomSerializerTestUtils.MOCK_DISPATCHER, augment.getNodeDispatcher());
    }
}