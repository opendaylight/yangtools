/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;

public class TreeNodeFactoryTest {

    private static void checkTreeNode(final TreeNode node, final NormalizedNode data, final Version version) {
        assertSame(data, node.getData());
        assertSame(version, node.getSubtreeVersion());
        assertSame(version, node.getVersion());
    }

    @Test
    public void testNormalizedNodeContainer() {
        final ContainerNode data = Mockito.mock(ContainerNode.class);
        final Version version = Version.initial();
        final TreeNode node = TreeNode.of(data, version);

        assertInstanceOf(SimpleContainerNode.class, node);
        checkTreeNode(node, data, version);
    }

    @Test
    public void testOrderedNodeContainer() {
        final UserMapNode data = Mockito.mock(UserMapNode.class);
        final Version version = Version.initial();
        final TreeNode node = TreeNode.of(data, version);

        assertInstanceOf(SimpleContainerNode.class, node);
        checkTreeNode(node, data, version);
    }

    @Test
    public void testLeaf() {
        final LeafNode<?> data = Mockito.mock(LeafNode.class);
        final Version version = Version.initial();
        final TreeNode node = TreeNode.of(data, version);

        assertInstanceOf(ValueNode.class, node);
        checkTreeNode(node, data, version);
    }
}
