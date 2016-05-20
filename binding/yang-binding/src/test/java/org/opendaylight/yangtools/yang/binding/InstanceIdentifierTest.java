/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.test.mock.FooChild;
import org.opendaylight.yangtools.yang.binding.test.mock.InstantiatedFoo;
import org.opendaylight.yangtools.yang.binding.test.mock.Node;
import org.opendaylight.yangtools.yang.binding.test.mock.NodeChild;
import org.opendaylight.yangtools.yang.binding.test.mock.NodeChildKey;
import org.opendaylight.yangtools.yang.binding.test.mock.NodeKey;
import org.opendaylight.yangtools.yang.binding.test.mock.Nodes;

public class InstanceIdentifierTest {

    @Test
    public void constructWithPredicates() {
        InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).build();

        assertNotNull(nodes);
        assertEquals(Nodes.class, nodes.getTargetType());


        InstanceIdentifier<Node> node = nodes.builder().child(Node.class).build();

        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());

        assertTrue(nodes.contains(node));
    }

    @Test
    public void fluentConstruction() {
        InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).build();
        InstanceIdentifier<Node> node =
                InstanceIdentifier.builder(Nodes.class).child(Node.class,new NodeKey(10)).build();

        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());

        assertTrue(nodes.contains(node));
    }


    @Test
    public void negativeContains() {
        InstanceIdentifier<FooChild> fooChild =
                InstanceIdentifier.builder(Nodes.class).child(InstantiatedFoo.class).child(FooChild.class).build();

        InstanceIdentifier<Node> nodeTen = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).build();
        InstanceIdentifier<Node> nodeOne = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(1)).build();
        InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).build();

        assertFalse(fooChild.contains(nodeTen));
        assertFalse(nodeTen.contains(nodes));

        assertFalse(nodeOne.contains(nodes));
        assertTrue(nodes.contains(nodeOne));

        assertTrue(nodes.containsWildcarded(nodeOne));
        assertFalse(nodeOne.containsWildcarded(nodes));
    }

    @Test
    public void containsWildcarded() {
        InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).build();
        InstanceIdentifier<Node> wildcarded = InstanceIdentifier.builder(Nodes.class).child(Node.class).build();
        InstanceIdentifier<NodeChild> wildcardedChildren = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class) //
                .child(NodeChild.class).build();

        assertTrue(wildcarded.isWildcarded());
        assertTrue(wildcardedChildren.isWildcarded());


        InstanceIdentifier<Node> nodeTen = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).build();
        InstanceIdentifier<Node> nodeOne = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(1)).build();

        assertFalse(nodeTen.isWildcarded());
        assertFalse(nodeOne.isWildcarded());
        assertTrue(nodes.containsWildcarded(nodeOne));
        assertTrue(wildcarded.containsWildcarded(nodeOne));
        assertTrue(wildcarded.containsWildcarded(nodeTen));


        InstanceIdentifier<NodeChild> nodeTenChildWildcarded = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).child(NodeChild.class).build();

        assertTrue(nodeTenChildWildcarded.isWildcarded());

        InstanceIdentifier<NodeChild> nodeTenChild = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).child(NodeChild.class, new NodeChildKey(10)).build();
        InstanceIdentifier<NodeChild> nodeOneChild = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(1)).child(NodeChild.class, new NodeChildKey(1)).build();


        assertFalse(nodeTenChildWildcarded.containsWildcarded(nodeOneChild));
        assertTrue(nodeTenChildWildcarded.containsWildcarded(nodeTenChild));

    }

    void childOfTest() {
        InstanceIdentifier.builder(Nodes.class).child(InstantiatedFoo.class).child(FooChild.class);
    }

    @Test
    public void basicTests() {
        InstanceIdentifier<DataObject> instanceIdentifier1 = InstanceIdentifier.create(DataObject.class);
        InstanceIdentifier<DataObject> instanceIdentifier2 = InstanceIdentifier.create(DataObject.class);
        Object object = new Object();

        assertTrue(instanceIdentifier1.equals(instanceIdentifier1));
        assertFalse(instanceIdentifier1.equals(null));
        assertFalse(instanceIdentifier1.equals(object));
        assertTrue(instanceIdentifier1.equals(instanceIdentifier2));

        assertNotNull(instanceIdentifier1.hashCode());

        assertNotNull(instanceIdentifier1.toString());
    }

    @Test
    public void firstIdentifierOfTest() {
        InstanceIdentifier<Node> instanceIdentifier =
                InstanceIdentifier.builder(Nodes.class).child(Node.class,new NodeKey(10)).build();

        InstanceIdentifier<Nodes> nodesIdentifier = instanceIdentifier.firstIdentifierOf(Nodes.class);
        assertNotNull(nodesIdentifier);
        InstanceIdentifier<DataObject> dataObjectIdentifier = instanceIdentifier.firstIdentifierOf(DataObject.class);
        assertNull(dataObjectIdentifier);
    }
}
