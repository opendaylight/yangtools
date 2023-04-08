/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT588Test {
    private static final String NS = "foo";
    private static final String REV = "2016-03-01";

    @Test
    public void test() {
        var context = YangParserTestUtils.parseYangResource("/yt588.yang");

        QName root = QName.create(NS, REV, "root");
        QName leafRef2 = QName.create(NS, REV, "leaf-ref-2");
        QName conGrp = QName.create(NS, REV, "con-grp");
        QName leafRef = QName.create(NS, REV, "leaf-ref");

        var findDataSchemaNode = context.findDataTreeChild(root, conGrp, leafRef).orElseThrow();
        var findDataSchemaNode2 = context.findDataTreeChild(root, leafRef2).orElseThrow();
        assertThat(findDataSchemaNode, isA(LeafSchemaNode.class));
        assertThat(findDataSchemaNode2, isA(LeafSchemaNode.class));

        var leafRefNode = (LeafSchemaNode) findDataSchemaNode;
        var leafRefNode2 = (LeafSchemaNode) findDataSchemaNode2;

        assertThat(leafRefNode.getType(), isA(LeafrefTypeDefinition.class));
        assertThat(leafRefNode2.getType(), isA(LeafrefTypeDefinition.class));

        var found = SchemaInferenceStack.ofDataTreePath(context, root, conGrp, leafRef)
                .resolvePathExpression(((LeafrefTypeDefinition) leafRefNode.getType()).getPathStatement());
        assertThat(((TypedDataSchemaNode)found).getType(), isA(BinaryTypeDefinition.class));

        found = SchemaInferenceStack.ofDataTreePath(context, root, leafRef2)
            .resolvePathExpression(((LeafrefTypeDefinition) leafRefNode2.getType()).getPathStatement());
        assertThat(((TypedDataSchemaNode)found).getType(), isA(Int16TypeDefinition.class));
    }
}
