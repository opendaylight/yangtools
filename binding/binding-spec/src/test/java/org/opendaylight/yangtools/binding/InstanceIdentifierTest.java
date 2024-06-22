/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.test.mock.FooChild;
import org.opendaylight.yangtools.binding.test.mock.FooRoot;
import org.opendaylight.yangtools.binding.test.mock.InstantiatedFoo;
import org.opendaylight.yangtools.binding.test.mock.Node;
import org.opendaylight.yangtools.binding.test.mock.NodeAugmentation;
import org.opendaylight.yangtools.binding.test.mock.NodeChild;
import org.opendaylight.yangtools.binding.test.mock.NodeChildKey;
import org.opendaylight.yangtools.binding.test.mock.NodeKey;
import org.opendaylight.yangtools.binding.test.mock.Nodes;

class InstanceIdentifierTest {
    @Test
    void constructWithPredicates() {
        final var nodes = DataObjectWildcard.builder(Nodes.class).build();

        assertNotNull(nodes);
        assertEquals(Nodes.class, nodes.getTargetType());

        final var node = nodes.toBuilder().child(Node.class).build();

        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());

        assertTrue(nodes.contains(node));
    }

    @Test
    void fluentConstruction() {
        final var nodes = DataObjectWildcard.builder(Nodes.class).build();
        final var node = DataObjectWildcard.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();

        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());

        assertTrue(nodes.contains(node));
    }

    @Test
    void negativeContains() {
        final var fooChild =
            DataObjectWildcard.builder(Nodes.class).child(InstantiatedFoo.class).child(FooChild.class).build();

        final var nodeTen = DataObjectWildcard.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        final var nodeOne = DataObjectWildcard.builder(Nodes.class).child(Node.class, new NodeKey(1)).build();
        final var nodes = DataObjectWildcard.builder(Nodes.class).build();

        assertFalse(fooChild.contains(nodeTen));
        assertFalse(nodeTen.contains(nodes));

        assertFalse(nodeOne.contains(nodes));
        assertTrue(nodes.contains(nodeOne));

        assertTrue(nodes.containsWildcarded(nodeOne));
        assertFalse(nodeOne.containsWildcarded(nodes));
    }

    @Test
    void containsWildcarded() {
        final var nodes = DataObjectWildcard.builder(Nodes.class).build();
        final var wildcarded = DataObjectWildcard.builder(Nodes.class).child(Node.class).build();
        final var wildcardedChildren =
            DataObjectWildcard.builder(Nodes.class).child(Node.class).child(NodeChild.class).build();

        assertNull(wildcarded.tryToIdentifier());
        assertNull(wildcardedChildren.tryToIdentifier());

        final var nodeTen = DataObjectWildcard.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        final var nodeOne = DataObjectWildcard.builder(Nodes.class).child(Node.class, new NodeKey(1)).build();

        assertNotNull(nodeTen.toIdentifier());
        assertNotNull(nodeOne.toIdentifier());
        assertTrue(nodes.containsWildcarded(nodeOne));
        assertTrue(wildcarded.containsWildcarded(nodeOne));
        assertTrue(wildcarded.containsWildcarded(nodeTen));
        assertFalse(DataObjectWildcard.builder(Nodes.class)
                .child(InstantiatedFoo.class).build().containsWildcarded(wildcarded));

        final var nodeTenChildWildcarded = DataObjectWildcard.builder(Nodes.class)
                .child(Node.class, new NodeKey(10)).child(NodeChild.class).build();

        assertNull(nodeTenChildWildcarded.tryToIdentifier());

        final var nodeTenChild = DataObjectWildcard.builder(Nodes.class)
                .child(Node.class, new NodeKey(10)).child(NodeChild.class, new NodeChildKey(10)).build();
        final var nodeOneChild = DataObjectWildcard.builder(Nodes.class)
                .child(Node.class, new NodeKey(1)).child(NodeChild.class, new NodeChildKey(1)).build();

        assertFalse(nodeTenChildWildcarded.containsWildcarded(nodeOneChild));
        assertTrue(nodeTenChildWildcarded.containsWildcarded(nodeTenChild));
    }

    @Test
    void basicTests() {
        final var fooRoot1 = DataObjectWildcard.create(FooRoot.class);
        final var fooRoot2 = DataObjectWildcard.create(FooRoot.class);
        final var nodeChild =
            DataObjectWildcard.builder(Nodes.class).child(Node.class, new NodeKey(10)).child(NodeChild.class).build();
        final var object = new Object();

        assertEquals(fooRoot1, fooRoot1);
        assertNotEquals(fooRoot1, null);
        assertNotEquals(fooRoot1, object);
        assertEquals(fooRoot1, fooRoot2);
        assertNotEquals(fooRoot1, nodeChild);
        assertEquals(fooRoot1.hashCode(), fooRoot2.hashCode());

        final var node = DataObjectWildcard.create(Nodes.class).child(Node.class);

        assertNotNull(DataObjectWildcard.unsafeOf(ImmutableList.copyOf(fooRoot1.steps())));
        assertNotNull(DataObjectWildcard.create(Nodes.class).child(Node.class));
        assertNotNull(DataObjectWildcard.create(Nodes.class).child(Node.class, new NodeKey(5)));
        assertNotNull(node.augmentation(NodeAugmentation.class));
        assertNotNull(fooRoot1.toString());

        final DataObjectWildcard.Builder instanceIdentifierBuilder = fooRoot1.toBuilder();
        assertEquals(fooRoot1.hashCode(), instanceIdentifierBuilder.build().hashCode());
        assertNotNull(instanceIdentifierBuilder.augmentation(InstantiatedFoo.class));
        assertNotNull(instanceIdentifierBuilder.build());
    }

    @Test
    void firstIdentifierOfTest() {
        final var node = DataObjectWildcard.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        assertEquals(DataObjectWildcard.create(Nodes.class), node.firstIdentifierOf(Nodes.class));
        assertNull(node.firstIdentifierOf(DataObject.class));
    }

    @Test
    void firstKeyOfTest() {
        final var nodeKey = new NodeKey(10);
        final var node = DataObjectWildcard.builder(Nodes.class).child(Node.class, nodeKey).build();
        assertEquals(nodeKey, node.firstKeyOf(Node.class));
        assertNull(DataObjectWildcard.create(FooRoot.class).firstKeyOf(Node.class));
    }

    @Test
    void keyOfTest() {
        final var key = new NodeKey(42);
        final var step = new KeyStep<>(Node.class, key);
        assertEquals(key, DataObjectWildcard.keyOf(new DataObjectWildcard.WithKey<>(List.of(step), step)));
    }

    @Test
    void serializationTest() throws Exception {
        final var fooRoot = DataObjectWildcard.create(FooRoot.class);
        final var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(fooRoot);
            oos.flush();
        }

        final var bytes = baos.toByteArray();
        assertEquals("""
            aced0005737200276f72672e6f70656e6461796c696768742e79616e67746f6f6c732e62696e64696e672e4f5776310000000000000\
            0010c0000787200276f72672e6f70656e6461796c696768742e79616e67746f6f6c732e62696e64696e672e4f527631000000000000\
            00010c000078707704000000017372002b6f72672e6f70656e6461796c696768742e79616e67746f6f6c732e62696e64696e672e4e6\
            f64655374657000000000000000000200024c000863617365547970657400114c6a6176612f6c616e672f436c6173733b4c00047479\
            706571007e0004787070767200346f72672e6f70656e6461796c696768742e79616e67746f6f6c732e62696e64696e672e746573742\
            e6d6f636b2e466f6f526f6f740000000000000000000000787078""", HexFormat.of().formatHex(bytes));

        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertEquals(fooRoot, ois.readObject());
        }
    }

    @Test
    void equalsTest() {
        final var fooRoot1 = DataObjectWildcard.create(FooRoot.class);
        final var fooRoot2 = DataObjectWildcard.create(FooRoot.class);
        assertEquals(fooRoot1, fooRoot2);
        assertEquals(fooRoot2, fooRoot1);
        assertEquals(fooRoot2, fooRoot2);

        final var nodes1 = DataObjectWildcard.create(Nodes.class);
        final var nodes2 = DataObjectWildcard.create(Nodes.class);
        assertEquals(nodes1, nodes2);
        assertEquals(nodes2, nodes2);

        assertNotEquals(nodes1, fooRoot1);
        assertNotEquals(nodes1, null);
        assertNotEquals(nodes2, null);
        assertNotEquals(fooRoot1, new Object());

        final var node1 = nodes1.child(Node.class, new NodeKey(10));
        assertEquals(nodes1, nodes2);
        assertNotEquals(nodes1, node1);
        assertNotEquals(nodes2, node1);
        assertEquals(nodes1, nodes2);

        final var node2 = nodes2.child(Node.class, new NodeKey(20));
        assertEquals(nodes1, nodes2);
        assertNotEquals(nodes1, node2);
        assertNotEquals(nodes2, node2);
        assertNotEquals(node1, node2);
    }

    @Test
    void hashCodeTest() {
        final var fooRoot1 = DataObjectWildcard.create(FooRoot.class);
        final var fooRoot2 = DataObjectWildcard.create(FooRoot.class);
        final var nodes1 = DataObjectWildcard.create(Nodes.class);
        final var nodes2 = DataObjectWildcard.create(Nodes.class);
        final var obj = new Object();

        assertEquals(fooRoot1.hashCode(), fooRoot2.hashCode());
        assertNotEquals(fooRoot1.hashCode(), nodes1.hashCode());
        assertEquals(nodes1.hashCode(), nodes2.hashCode());
        assertNotEquals(fooRoot2.hashCode(), nodes2.hashCode());
        assertNotEquals(fooRoot1.hashCode(), obj.hashCode());

        final var node = nodes1.child(Node.class, new NodeKey(10));
        assertNotEquals(nodes1.hashCode(), node.hashCode());
    }

    @Test
    void verifyTargetTest() {
        final var nodeId = DataObjectWildcard.create(Nodes.class);
        assertSame(nodeId, nodeId.verifyTarget(Nodes.class));
        assertThrows(VerifyException.class, () -> nodeId.verifyTarget(Node.class));
    }
}
