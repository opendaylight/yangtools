/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

public class Bug5481Test {
    @Test
    public void test() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5481");
        assertNotNull(context);

        ContainerSchemaNode topContainer = verifyTopContainer(context);
        verifyExtendedLeaf(topContainer);
    }

    private static ContainerSchemaNode verifyTopContainer(final SchemaContext context) {
        QName top = QName.create("http://example.com/module1", "2016-03-09", "top");
        DataSchemaNode dataChildByName = context.getDataChildByName(top);
        assertTrue(dataChildByName instanceof ContainerSchemaNode);

        ContainerSchemaNode topContainer = (ContainerSchemaNode) dataChildByName;

        assertFalse(topContainer.getWhenCondition().isPresent());
        assertEquals(Status.CURRENT, topContainer.getStatus());
        assertFalse(topContainer.getDescription().isPresent());
        assertFalse(topContainer.getReference().isPresent());
        return topContainer;
    }

    private static void verifyExtendedLeaf(final ContainerSchemaNode topContainer) {
        DataSchemaNode dataChildByName2 = topContainer.getDataChildByName(QName.create("http://example.com/module2",
                "2016-03-09", "extended-leaf"));
        assertTrue(dataChildByName2 instanceof LeafSchemaNode);

        LeafSchemaNode extendedLeaf = (LeafSchemaNode) dataChildByName2;
        assertEquals(Status.DEPRECATED, extendedLeaf.getStatus());
        assertEquals(Optional.of("text"), extendedLeaf.getDescription());
        assertEquals(Optional.of("ref"), extendedLeaf.getReference());

        QualifiedBound whenConditionExtendedLeaf = extendedLeaf.getWhenCondition().orElseThrow();
        assertEquals("module1:top = 'extended'", whenConditionExtendedLeaf.toString());
    }
}
