/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ContainerEffectiveStatementImpl;
import org.w3c.dom.Element;

public class LeafNodeDomSerializerTest {

    @Test
    public void leafNodeDomSerializerTest() throws ReactorException, IOException, YangSyntaxErrorException,
            ParseException, URISyntaxException {
        final DataSchemaNode currentContainer = DomSerializerTestUtils.getSchemaContext().findModuleByName
                ("serializer-test", null).getDataChildByName(DomSerializerTestUtils.generateQname("root"));
        final LeafSchemaNode currentLeaf = (LeafSchemaNode) ((ContainerEffectiveStatementImpl) currentContainer)
                .getDataChildByName(DomSerializerTestUtils.generateQname("first-leaf"));
        assertNotNull(currentLeaf.getType());

        final LeafNode<String> tempLeafNode = new ImmutableLeafNodeBuilder<String>().withValue("testing DATA")
                .withNodeIdentifier(DomSerializerTestUtils
                        .getNodeIdentifier("first-leaf", "dom-serializer-test", "2016-01-01"))
                .build();

        final LeafNodeDomSerializer temp = new LeafNodeDomSerializer(DomSerializerTestUtils.DOC, DomSerializerTestUtils
                .CODEC_PROVIDER);
        final Element element = temp.serializeLeaf(currentLeaf, tempLeafNode);
        final Iterable<Element> elementsFromAbstractClass = temp.serialize(currentLeaf, tempLeafNode);
        final Element singleElementFromAbstractClass = elementsFromAbstractClass.iterator().next();

        assertTrue(element.getLocalName().equals(singleElementFromAbstractClass.getLocalName()));
        assertTrue(element.getNamespaceURI().equals(singleElementFromAbstractClass.getNamespaceURI()));

        DomSerializerTestUtils.testResults("<first-leaf xmlns=\"dom-serializer-test\">testing DATA</first-leaf>",
                element);
    }
}