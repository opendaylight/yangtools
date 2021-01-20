/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

public class YT1195Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAZ = QName.create("foo", "baz");

    @Test
    public void testWhenReuse() throws Exception {
        final EffectiveModelContext ctx = StmtTestUtils.parseYangSource("/bugs/YT1195/foo.yang");

        final GroupingDefinition grp = Iterables.getOnlyElement(ctx.getGroupings());
        final DataSchemaNode grpBaz = grp.getDataChildByName(BAZ);
        assertThat(grpBaz, instanceOf(LeafSchemaNode.class));
        final QualifiedBound grpBazWhen = ((LeafSchemaNode) grpBaz).getWhenCondition().get();

        final DataSchemaNode foo = ctx.getDataChildByName(FOO);
        assertThat(foo, instanceOf(ContainerSchemaNode.class));
        final DataSchemaNode fooBaz = ((ContainerSchemaNode) foo).getDataChildByName(BAZ);
        assertThat(fooBaz, instanceOf(LeafSchemaNode.class));
        final QualifiedBound fooBazWhen = ((LeafSchemaNode) fooBaz).getWhenCondition().get();
        // Both 'when' statements should be the same
        assertSame(grpBazWhen, fooBazWhen);
    }
}
