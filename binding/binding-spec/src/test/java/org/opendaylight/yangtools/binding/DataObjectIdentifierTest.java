/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.test.mock.FooRoot;
import org.opendaylight.yangtools.binding.test.mock.Node;
import org.opendaylight.yangtools.binding.test.mock.NodeChild;
import org.opendaylight.yangtools.binding.test.mock.NodeChildKey;
import org.opendaylight.yangtools.binding.test.mock.NodeKey;
import org.opendaylight.yangtools.binding.test.mock.Nodes;

class DataObjectIdentifierTest {
    @Test
    void keyedToLegacy() {
        final var nodes = DataObjectIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        assertEquals(10, nodes.key().getId());
    }

    @Test
    void firstKeyOfTest() {
        final var nodes = DataObjectIdentifier.builder(Nodes.class)
            .child(Node.class, new NodeKey(5))
            .child(NodeChild.class, new NodeChildKey(10))
            .build();
        assertEquals(new NodeKey(5), nodes.firstKeyOf(Node.class));
        assertEquals(new NodeChildKey(10), nodes.firstKeyOf(NodeChild.class));
        final var fooRoot = DataObjectIdentifier.builder(FooRoot.class).build();
        assertNull(fooRoot.firstKeyOf(Node.class));
    }

    @Test
    @SuppressWarnings("removal")
    void plainToIdentifierReturnsSelf() {
        final var nodes = DataObjectIdentifier.builder(Nodes.class).build();
        assertSame(nodes, nodes.toIdentifier());
    }

    @Test
    @SuppressWarnings("removal")
    void withKeyToIdentifierReturnsSelf() {
        final var nodes = DataObjectIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(5)).build();
        assertSame(nodes, nodes.toIdentifier());
    }

    @Test
    void trimToNullThrows() {
        final var nodes = DataObjectIdentifier.builder(Nodes.class).build();
        assertThrows(NullPointerException.class, () -> nodes.trimTo(null));
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void trimToBadClassThrows() {
        final var nodes = DataObjectIdentifier.builder(Nodes.class).build();
        assertThrows(ClassCastException.class, () -> nodes.trimTo((Class) String.class));
    }

    @Test
    void trimToWorks() {
        final var nodes = DataObjectIdentifier.builder(Nodes.class)
            .child(Node.class, new NodeKey(5))
            .child(NodeChild.class, new NodeChildKey(10))
            .build();

        assertEquals(DataObjectIdentifier.builder(Nodes.class).build(), nodes.trimTo(Nodes.class));
        assertEquals(DataObjectIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(5)).build(),
            nodes.trimTo(Node.class));
        assertEquals(nodes, nodes.trimTo(NodeChild.class));
    }

    @Test
    void trimToNotFound() {
        final var nodes = DataObjectIdentifier.builder(Nodes.class).build();
        final var ex = assertThrows(NoSuchElementException.class, () -> nodes.trimTo(Node.class));
        assertEquals("""
            No step matching org.opendaylight.yangtools.binding.test.mock.Node found in DataObjectIdentifier[
              org.opendaylight.yangtools.binding.test.mock.Nodes
            ]""", ex.getMessage());
    }

    @Test
    void tryTrimToNotFound() {
        final var nodes = DataObjectIdentifier.builder(Nodes.class).build();
        assertNull(nodes.tryTrimTo(Node.class));
    }
}
