/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

public class Bug7865Test {
    private static final String NS = "foo";

    @Test
    public void test() throws Exception {
        final EffectiveModelContext context = TestUtils.parseYangSources("/bugs/bug7865");
        assertNotNull(context);
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        final DataSchemaNode root = context.getDataChildByName(foo("root"));
        stack.enterSchemaTree(root.getQName());
        assertTrue(root instanceof ContainerSchemaNode);
        final UnrecognizedEffectiveStatement expectedUnrecognizedStmt = stack.currentStatement()
                .findFirstEffectiveSubstatement(UnrecognizedEffectiveStatement.class).get()
                .findFirstEffectiveSubstatement(UnrecognizedEffectiveStatement.class).get()
                .findFirstEffectiveSubstatement(UnrecognizedEffectiveStatement.class).get();
        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = root.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        final UnknownSchemaNode unknownNode = unknownSchemaNodes.iterator().next();
        final Collection<? extends UnknownSchemaNode> subUnknownSchemaNodes = unknownNode.getUnknownSchemaNodes();
        assertEquals(1, subUnknownSchemaNodes.size());

        final UnknownSchemaNode subUnknownNode = subUnknownSchemaNodes.iterator().next();
        final Collection<? extends UnknownSchemaNode> subSubUnknownSchemaNodes = subUnknownNode.getUnknownSchemaNodes();
        assertEquals(1, subSubUnknownSchemaNodes.size());

        final UnknownSchemaNode subSubUnknownNode = subSubUnknownSchemaNodes.iterator().next();
        assertEquals(((SchemaNode)expectedUnrecognizedStmt).getQName(), subSubUnknownNode.getQName());
    }

    private static QName foo(final String localName) {
        return QName.create(NS, localName);
    }
}
