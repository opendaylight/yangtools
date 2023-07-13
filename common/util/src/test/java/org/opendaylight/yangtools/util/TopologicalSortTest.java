/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.TopologicalSort.Node;
import org.opendaylight.yangtools.util.TopologicalSort.NodeImpl;

class TopologicalSortTest {
    @Test
    void test() {
        final var nodes = new HashSet<Node>();

        final var node1 = new NodeImpl();
        nodes.add(node1);
        final var node2 = new NodeImpl();
        nodes.add(node2);
        final var node3 = new NodeImpl();
        nodes.add(node3);

        node1.addEdge(node2);
        node2.addEdge(node3);
        node3.addEdge(node1);

        assertThrows(IllegalStateException.class, () -> TopologicalSort.sort(nodes));
    }

    @Test
    void testValidSimple() {
        final var nodes = new HashSet<Node>();

        final var node1 = new NodeImpl();
        nodes.add(node1);
        final var node2 = new NodeImpl();
        nodes.add(node2);
        final var node3 = new NodeImpl();
        nodes.add(node3);
        final var node4 = new NodeImpl();
        nodes.add(node4);

        node1.addEdge(node2);
        node1.addEdge(node3);
        node2.addEdge(node4);
        node3.addEdge(node2);

        final var sorted = TopologicalSort.sort(nodes);

        assertEquals(node4, sorted.get(0));
        assertEquals(node2, sorted.get(1));
        assertEquals(node3, sorted.get(2));
        assertEquals(node1, sorted.get(3));
    }
}
