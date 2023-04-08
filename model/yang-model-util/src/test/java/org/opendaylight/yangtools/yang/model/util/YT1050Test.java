/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1050Test {
    private static final QName SECONDARY = QName.create("yt1050", "secondary");
    private static final QName TYPE = QName.create(SECONDARY, "type");
    private static final QName GRP_USES = QName.create(SECONDARY, "grp-uses");

    private EffectiveModelContext context;
    private LeafSchemaNode secondaryType;
    private LeafSchemaNode primaryType;
    private Module module;

    @Before
    public void before() {
        context = YangParserTestUtils.parseYangResource("/yt1050.yang");
        module = context.getModules().iterator().next();

        final var grpUses = (ListSchemaNode) module.getDataChildByName(GRP_USES);
        primaryType = (LeafSchemaNode) grpUses.getDataChildByName(TYPE);

        final var grp = module.getGroupings().iterator().next();
        secondaryType = (LeafSchemaNode) ((ListSchemaNode) grp.getDataChildByName(SECONDARY))
                .getDataChildByName(TYPE);
    }

    @Test
    public void testFindDataSchemaNodeForRelativeXPathWithDeref() {
        final var typeNodeType = secondaryType.getType();
        assertThat(typeNodeType, isA(LeafrefTypeDefinition.class));

        final var stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(QName.create(module.getQNameModule(), "grp"));
        stack.enterSchemaTree(QName.create(module.getQNameModule(), "secondary"));
        stack.enterSchemaTree(secondaryType.getQName());
        final var found = stack.resolvePathExpression(((LeafrefTypeDefinition) typeNodeType).getPathStatement());
        assertSame(primaryType, found);
    }
}
