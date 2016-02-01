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

public class LeafNodeDomSerializerTest extends AbstractDomSerializerTest {

    @Test
    public void LeafNodeDomSerializerTest() throws ReactorException, IOException, YangSyntaxErrorException, ParseException {
        DataSchemaNode currentContainer = getSchemaContext().findModuleByName("serializer-test", null)
                .getDataChildByName(generateQname("root"));
        LeafSchemaNode currentLeaf = (LeafSchemaNode) ((ContainerEffectiveStatementImpl) currentContainer).getDataChildByName
                (generateQname("first-leaf"));
        assertNotNull(currentLeaf.getType());

        LeafNode<String> tempLeafNode = new ImmutableLeafNodeBuilder<String>()
                .withValue("testing data")
                .withNodeIdentifier(getNodeIdentifier("first-leaf", "dom-serializer-test", "2016-01-01"))
                .build();

        LeafNodeDomSerializer temp = new LeafNodeDomSerializer(doc, codecProvider);
        element = temp.serializeLeaf(currentLeaf, tempLeafNode);
        Iterable<Element> elementsFromAbstractClass = temp.serialize(currentLeaf, tempLeafNode);
        Element singleElementFromAbstractClass = elementsFromAbstractClass.iterator().next();
        assertTrue(element.getLocalName().equals(singleElementFromAbstractClass.getLocalName()));
        assertTrue(element.getNamespaceURI().equals(singleElementFromAbstractClass.getNamespaceURI()));
        testResults("<first-leaf xmlns=\"dom-serializer-test\">testing data</first-leaf>");
    }
}