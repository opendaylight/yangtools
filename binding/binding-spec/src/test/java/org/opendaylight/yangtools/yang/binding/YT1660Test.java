/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectReference.WithKey;
import org.opendaylight.yangtools.binding.test.mock.Node;
import org.opendaylight.yangtools.binding.test.mock.NodeChild;
import org.opendaylight.yangtools.binding.test.mock.NodeChildKey;
import org.opendaylight.yangtools.binding.test.mock.NodeKey;
import org.opendaylight.yangtools.binding.test.mock.Nodes;

/**
 *
 */
class YT1660Test {
    private final WithKey<NodeChild, NodeChildKey> id = DataObjectIdentifier.builder(Nodes.class)
        .child(Node.class, new NodeKey(3))
        .child(NodeChild.class, new NodeChildKey(5))
        .build();
    private final WithKey<NodeChild, NodeChildKey> ref = DataObjectReference.builder(Nodes.class)
        .child(Node.class)
        .child(NodeChild.class, new NodeChildKey(4))
        .build();

    @Test
    void testFindFirstKey() {
        assertEquals(Optional.of(new NodeKey(3)), id.findFirstKeyOf(Node.class));
        assertEquals(Optional.of(id.key()), id.findFirstKeyOf(NodeChild.class));

        assertEquals(Optional.of(ref.key()), ref.findFirstKeyOf(NodeChild.class));
        assertEquals(Optional.empty(), ref.findFirstKeyOf(Node.class));
    }

    @Test
    void testGetFirstKey() {
        assertEquals(new NodeKey(3), id.getFirstKeyOf(Node.class));
        assertEquals(id.key(), id.getFirstKeyOf(NodeChild.class));

        assertEquals(ref.key(), ref.getFirstKeyOf(NodeChild.class));
        final var ex = assertThrows(NoSuchElementException.class, () -> ref.getFirstKeyOf(Node.class));
        assertEquals("""
            No step matching interface org.opendaylight.yangtools.binding.test.mock.Node found in DataObjectReference[
              org.opendaylight.yangtools.binding.test.mock.Nodes
              org.opendaylight.yangtools.binding.test.mock.Node(any)
              org.opendaylight.yangtools.binding.test.mock.NodeChild[org.opendaylight.yangtools.binding.test.mock\
            .NodeChildKey@4]
            ]""", ex.getMessage());
    }
}
