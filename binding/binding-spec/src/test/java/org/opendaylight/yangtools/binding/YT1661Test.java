/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.test.mock.Node;
import org.opendaylight.yangtools.binding.test.mock.NodeChild;
import org.opendaylight.yangtools.binding.test.mock.NodeKey;
import org.opendaylight.yangtools.binding.test.mock.Nodes;

class YT1661Test {
    @Test
    void testPlainToReference() {
        final var nodes = DataObjectIdentifier.builder(Nodes.class).build();
        assertTrue(nodes.isExact());
        final var builder = nodes.toBuilder();

        // this fails...
        assertThrows(IllegalArgumentException.class, () -> builder.child(Node.class));
        // .. but this works
        final var node = builder.toReferenceBuilder().child(Node.class).build();

        assertFalse(node.isExact());
        assertEquals(DataObjectReference.builder(Nodes.class).child(Node.class).build(), node);
    }

    @Test
    void testWithToReference() {
        final var node = DataObjectIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(3)).build();
        assertTrue(node.isExact());
        final var builder = node.toBuilder();

        // this fails...
        assertThrows(IllegalArgumentException.class, () -> builder.child(NodeChild.class));
        // .. but this works
        final var nodeChild= builder.toReferenceBuilder().child(NodeChild.class).build();

        assertFalse(nodeChild.isExact());
        assertEquals(
            DataObjectReference.builder(Nodes.class).child(Node.class, new NodeKey(3)).child(NodeChild.class).build(),
            nodeChild);
    }
}
