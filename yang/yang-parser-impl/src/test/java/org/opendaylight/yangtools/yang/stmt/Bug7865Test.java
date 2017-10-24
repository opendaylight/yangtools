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

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class Bug7865Test {
    private static final String NS = "foo";

    @Test
    public void test() throws Exception {
        final SchemaContext context = TestUtils.parseYangSources("/bugs/bug7865");
        assertNotNull(context);

        final DataSchemaNode root = context.getDataChildByName(foo("root"));
        assertTrue(root instanceof ContainerSchemaNode);
        final List<UnknownSchemaNode> unknownSchemaNodes = root.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        final UnknownSchemaNode unknownNode = unknownSchemaNodes.iterator().next();
        final List<UnknownSchemaNode> subUnknownSchemaNodes = unknownNode.getUnknownSchemaNodes();
        assertEquals(1, subUnknownSchemaNodes.size());

        final UnknownSchemaNode subUnknownNode = subUnknownSchemaNodes.iterator().next();
        final List<UnknownSchemaNode> subSubUnknownSchemaNodes = subUnknownNode.getUnknownSchemaNodes();
        assertEquals(1, subSubUnknownSchemaNodes.size());

        final UnknownSchemaNode subSubUnknownNode = subSubUnknownSchemaNodes.iterator().next();
        final SchemaPath expectedPath = SchemaPath.create(true, foo("root"), foo("p"), foo("p"), foo("p"));
        assertEquals(expectedPath, subSubUnknownNode.getPath());
    }

    private static QName foo(final String localName) {
        return QName.create(NS, localName);
    }

}
