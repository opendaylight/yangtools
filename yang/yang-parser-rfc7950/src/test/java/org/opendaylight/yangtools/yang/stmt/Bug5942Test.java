/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

public class Bug5942Test {
    @Test
    public void test() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources("/bugs/bug5942");
        assertNotNull(schemaContext);

        final DataSchemaNode root = schemaContext.getDataChildByName(QName.create("foo", "2016-06-02", "root"));
        assertTrue(root instanceof ContainerSchemaNode);

        final Collection<? extends UsesNode> uses = ((ContainerSchemaNode) root).getUses();
        assertEquals(1, uses.size());
        final UsesNode usesNode = uses.iterator().next();

        assertEquals(Optional.of("uses description"), usesNode.getDescription());
        assertEquals(Optional.of("uses reference"), usesNode.getReference());
        assertEquals(Status.DEPRECATED, usesNode.getStatus());

        assertEquals("0!=1", usesNode.getWhenCondition().orElseThrow().toString());

        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = usesNode.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());
        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.iterator().next();
        assertEquals("argument", unknownSchemaNode.getNodeParameter());
        assertEquals(QName.create("foo", "2016-06-02", "e"), unknownSchemaNode.getExtensionDefinition().getQName());
    }
}