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
    public void containerNodeDomSerializerTest() throws ReactorException, IOException, YangSyntaxErrorException, ParseException {

        ContainerSchemaNode currentContainer = (ContainerSchemaNode) DomSerializerTestUtils.getSchemaContext()
                .findModuleByName("serializer-test", null)
                .getDataChildByName(DomSerializerTestUtils.generateQname("root"));

        ContainerNode tempContainer = ImmutableContainerNodeBuilder.create().withNodeIdentifier(DomSerializerTestUtils.getNodeIdentifier
                ("root", "dom-serializer-test", "2016-01-01")).build();

        ContainerNodeDomSerializer temp = new ContainerNodeDomSerializer(DomSerializerTestUtils.doc, DomSerializerTestUtils.mockDispatcher);
        Iterable<Element> serialize = temp.serialize(currentContainer, tempContainer);
        DomSerializerTestUtils.element = serialize.iterator().next();
        DomSerializerTestUtils.testResults("<root xmlns=\"dom-serializer-test\"/>");

        assertNotNull(temp.getNodeDispatcher());
    }

    @Test
    public void choiceNodeDomSerializerTest() {
        ChoiceNodeDomSerializer choice = new ChoiceNodeDomSerializer(DomSerializerTestUtils.mockDispatcher);
        assertEquals(DomSerializerTestUtils.mockDispatcher, choice.getNodeDispatcher());
    }

    @Test
    public void augmentationNodeDomSerializerTest() {
        AugmentationNodeDomSerializer augment = new AugmentationNodeDomSerializer(DomSerializerTestUtils.mockDispatcher);
        assertEquals(DomSerializerTestUtils.mockDispatcher, augment.getNodeDispatcher());
    }
}