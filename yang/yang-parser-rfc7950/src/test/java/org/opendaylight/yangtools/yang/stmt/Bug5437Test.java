/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

public class Bug5437Test {
    private static final String NS = "foo";
    private static final String REV = "2016-03-01";

    @Test
    public void test() throws Exception {
        EffectiveModelContext context = StmtTestUtils.parseYangSources("/bugs/bug5437");
        assertNotNull(context);

        QName root = QName.create(NS, REV, "root");

        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterSchemaTree(root);
        stack.enterSchemaTree(QName.create(NS, REV, "con-grp"));
        stack.enterSchemaTree(QName.create(NS, REV, "leaf-ref"));

        SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context, stack);
        assertThat(findDataSchemaNode, isA(LeafSchemaNode.class));

        LeafSchemaNode leafRefNode = (LeafSchemaNode) findDataSchemaNode;
        assertThat(leafRefNode.getType(), isA(LeafrefTypeDefinition.class));

        TypeDefinition<?> baseTypeForLeafRef = SchemaContextUtil.getBaseTypeForLeafRef(
                (LeafrefTypeDefinition) leafRefNode.getType(), context, leafRefNode, stack);
        assertThat(baseTypeForLeafRef, isA(BinaryTypeDefinition.class));

        stack.clear();
        stack.enterSchemaTree(root);
        stack.enterSchemaTree(QName.create(NS, REV, "leaf-ref-2"));

        SchemaNode findDataSchemaNode2 = SchemaContextUtil.findDataSchemaNode(context, stack);
        assertThat(findDataSchemaNode2, isA(LeafSchemaNode.class));

        LeafSchemaNode leafRefNode2 = (LeafSchemaNode) findDataSchemaNode2;
        assertThat(leafRefNode2.getType(), isA(LeafrefTypeDefinition.class));

        TypeDefinition<?> baseTypeForLeafRef2 = SchemaContextUtil.getBaseTypeForLeafRef(
                (LeafrefTypeDefinition) leafRefNode2.getType(), context, leafRefNode2, stack);
        stack.clear();
        assertThat(baseTypeForLeafRef2, isA(Int16TypeDefinition.class));
    }
}
