/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.json.schema.cnsn.serializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.json.schema.TestUtils;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

public class SerializeNormalizedStructToCnSnStructTest {

    private static DataSchemaNode resolvedDataSchemaNode;

    @BeforeClass
    public static void loadData() {
        Set<Module> modules = TestUtils.loadModulesFrom("/cnsn-to-normalized-node/yang");
        Module resolvedModule = TestUtils.resolveModule("simple-container-yang", modules);
        resolvedDataSchemaNode = TestUtils.resolveDataSchemaNode("cont", resolvedModule);
    }

    @Test
    public void testCnSnToNormalizedNode() throws URISyntaxException {
        ContainerNode containerNode = TestUtils.prepareNormalizedNodeStruct();

        Iterable<Node<?>> serialized = CnSnFromNormalizedNodeSerializerFactory.getInstance()
                .getContainerNodeSerializer().serialize((ContainerSchemaNode) resolvedDataSchemaNode, containerNode);

        assertNotNull(serialized);
        assertNotNull(serialized.iterator());
        assertNotNull(serialized.iterator().hasNext());

        CompositeNode compNode = TestUtils.prepareCompositeNodeStruct();
        assertEquals(serialized.iterator().next(), compNode);
    }
}
