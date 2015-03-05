/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.composite.node.schema.serializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.opendaylight.yangtools.yang.data.composite.node.schema.cnsn.serializer.CnSnFromNormalizedNodeSerializerFactory;


import org.opendaylight.yangtools.yang.data.composite.node.schema.TestUtils;

import java.net.URISyntaxException;
import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

@Ignore
public class SerializeNormalizedStructToCnSnStructTest {

    private static DataSchemaNode resolvedDataSchemaNode;

    @BeforeClass
    public static void loadData() throws URISyntaxException {
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

        assertEquals(serialized.iterator().next().getNodeType(), compNode.getNodeType());

        Set<Node<?>> value = Sets.newHashSet(((CompositeNode)serialized.iterator().next()).getValue());
        assertEquals(value, Sets.newHashSet(compNode.getValue()));
    }
}
