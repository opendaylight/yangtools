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

class TreeNodeFactoryTest {
    @Test
    void testNormalizedNodeContainer() {
        final var data = Mockito.mock(ContainerNode.class);
        final var version = Version.initial(false);
        final var node = TreeNode.of(data, version);

        assertSameTreeNode(assertInstanceOf(SimpleContainerNode.class, node), data, version);
    }

    @Test
    void testOrderedNodeContainer() {
        final var data = Mockito.mock(UserMapNode.class);
        final var version = Version.initial(false);
        final var node = TreeNode.of(data, version);

        assertSameTreeNode(assertInstanceOf(SimpleContainerNode.class, node), data, version);
    }

    @Test
    void testLeaf() {
        final var data = Mockito.mock(LeafNode.class);
        final var version = Version.initial(false);
        final var node = TreeNode.of(data, version);

        assertSameTreeNode(assertInstanceOf(ValueNode.class, node), data, version);
    }

    private static void assertSameTreeNode(final TreeNode node, final NormalizedNode data, final Version version) {
        assertSame(data, node.getData());
        assertSame(version, node.getSubtreeVersion());
        assertSame(version, node.getVersion());
    }
}
