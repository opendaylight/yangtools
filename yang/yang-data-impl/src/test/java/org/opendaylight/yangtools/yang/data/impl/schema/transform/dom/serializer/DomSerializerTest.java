/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.ParseException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ContainerEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DomSerializerTest extends AbstractDomSerializerTest {
    private static final Document doc = XmlDocumentUtils.getDocument();
    private static final Element data = doc.createElement("data");
    private static final XmlCodecProvider codecProvider = new XmlCodecProvider() {
        @Override
        public TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codecFor(TypeDefinition<?> baseType) {
            return TypeDefinitionAwareCodec.from(baseType);
        }
    };

    @Test
    public void LeafNodeDomSerializerTest() throws ReactorException, IOException, YangSyntaxErrorException, ParseException {
        EffectiveSchemaContext schemaContext = createTestContext(new YangStatementSourceImpl("/dom-serializer-test/first.yang", false));
        assertNotNull("Schema context must not be null.", schemaContext);

        DataSchemaNode currentContainer = schemaContext.findModuleByName("first", null)
                .getDataChildByName(generateQname("root"));
        LeafSchemaNode currentLeaf = (LeafSchemaNode) ((ContainerEffectiveStatementImpl) currentContainer).getDataChildByName
                (generateQname("first-leaf"));
        assertNotNull(currentLeaf.getType());

        LeafNode<String> tempLeafNode = new ImmutableLeafNodeBuilder<String>()
                .withValue("testing data")
                .withNodeIdentifier(getNodeIdentifier("first-leaf", "dom-serializer-test", "2016-01-01"))
                .build();
        LeafNode<String> tempLeafNode1 = new ImmutableLeafNodeBuilder<String>()
                .withValue("testing data-fail")
                .withNodeIdentifier(getNodeIdentifier("first-leaf-fail", "dom-serializer-test-fail", "2016-01-01-fail"))
                .build();

        LeafNodeDomSerializer temp = new LeafNodeDomSerializer(doc, codecProvider);
        Element element = temp.serializeLeaf(currentLeaf, tempLeafNode);
        Element element1 = temp.serializeLeaf(currentLeaf, tempLeafNode1);
        data.appendChild(element);
        data.appendChild(element1);
        String tempXml = toString(data);
        assertNotNull(tempXml);
        System.out.println(tempXml);
    }
}