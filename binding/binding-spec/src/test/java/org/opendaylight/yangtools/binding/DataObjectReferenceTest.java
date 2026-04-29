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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.test.mock.FooRoot;
import org.opendaylight.yangtools.binding.test.mock.Node;
import org.opendaylight.yangtools.binding.test.mock.NodeChild;
import org.opendaylight.yangtools.binding.test.mock.NodeChildKey;
import org.opendaylight.yangtools.binding.test.mock.NodeKey;
import org.opendaylight.yangtools.binding.test.mock.Nodes;

class DataObjectReferenceTest {
    @Test
    void keyedToLegacy() {
        final var nodes = DataObjectReference.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        assertEquals(10, nodes.key().getId());
    }

    @Test
    void firstKeyOfTest() {
        final var nodes = DataObjectIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(5))
                .child(NodeChild.class, new NodeChildKey(10)).build();
        assertEquals(new NodeKey(5), nodes.firstKeyOf(Node.class));
        assertEquals(new NodeChildKey(10), nodes.firstKeyOf(NodeChild.class));
        final var fooRoot = DataObjectIdentifier.builder(FooRoot.class).build();
        assertNull(fooRoot.firstKeyOf(Node.class));
    }

    @Test
    void firstKeyOfNull() {
        final var fooRoot = DataObjectIdentifier.builder(FooRoot.class).build();
        assertThrows(NullPointerException.class, () -> fooRoot.firstKeyOf(null));
        assertThrows(ClassCastException.class, () -> fooRoot.firstKeyOf((Class) Nodes.class));
    }
}
