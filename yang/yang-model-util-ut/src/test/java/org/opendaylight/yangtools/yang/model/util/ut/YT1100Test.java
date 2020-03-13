/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.ut;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1100Test {
    @Test
    public void testChoiceCaseRelativeLeafref() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/yt1100.yang");
        final Module module = context.findModule("yt1100").get();
        final QNameModule qnm = module.getQNameModule();
        final DataSchemaNode leaf = module.findDataTreeChild(
            QName.create(qnm, "foo"), QName.create(qnm, "scheduler-node"), QName.create(qnm, "child-scheduler-nodes"),
            QName.create(qnm, "name")).get();
        assertThat(leaf, instanceOf(LeafSchemaNode.class));

        final TypeDefinition<?> type = ((LeafSchemaNode) leaf).getType();
        assertThat(type, instanceOf(LeafrefTypeDefinition.class));
        final PathExpression leafref = ((LeafrefTypeDefinition) type).getPathStatement();

        final SchemaNode ref = SchemaContextUtil.findDataSchemaNodeForRelativeXPath(context, module, leaf, leafref);
        assertThat(ref, instanceOf(LeafSchemaNode.class));
        final LeafSchemaNode targetLeaf = (LeafSchemaNode) ref;
        assertEquals(QName.create(qnm, "name"), targetLeaf.getQName());
        assertThat(targetLeaf.getType(), instanceOf(StringTypeDefinition.class));
    }
}
