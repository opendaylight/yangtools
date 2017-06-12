/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.util.TopologicalSort.Node;
import org.opendaylight.yangtools.util.TopologicalSort.NodeImpl;

public class TopologicalSortTest {

    @Test(expected = IllegalStateException.class)
    public void test() {
        Set<Node> nodes = new HashSet<>();

        NodeImpl node1 = new NodeImpl();
        nodes.add(node1);
        NodeImpl node2 = new NodeImpl();
        nodes.add(node2);
        NodeImpl node3 = new NodeImpl();
        nodes.add(node3);

        node1.addEdge(node2);
        node2.addEdge(node3);
        node3.addEdge(node1);

        // We expect an IllegalStateException here
        TopologicalSort.sort(nodes);
    }

    @Test
    public void testValidSimple() throws Exception {
        Set<Node> nodes = new HashSet<>();

        Node node1 = new NodeImpl();
        nodes.add(node1);
        Node node2 = new NodeImpl();
        nodes.add(node2);
        Node node3 = new NodeImpl();
        nodes.add(node3);
        Node node4 = new NodeImpl();
        nodes.add(node4);

        ((NodeImpl) node1).addEdge(node2);
        ((NodeImpl) node1).addEdge(node3);
        ((NodeImpl) node2).addEdge(node4);
        ((NodeImpl) node3).addEdge(node2);

        List<Node> sorted = TopologicalSort.sort(nodes);

        assertEquals(node4, sorted.get(0));
        assertEquals(node2, sorted.get(1));
        assertEquals(node3, sorted.get(2));
        assertEquals(node1, sorted.get(3));
    }
}
