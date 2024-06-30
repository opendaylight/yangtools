/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.KeyStep;
import org.opendaylight.yangtools.binding.test.mock.FooChild;
import org.opendaylight.yangtools.binding.test.mock.FooRoot;
import org.opendaylight.yangtools.binding.test.mock.InstantiatedFoo;
import org.opendaylight.yangtools.binding.test.mock.Node;
import org.opendaylight.yangtools.binding.test.mock.NodeAugmentation;
import org.opendaylight.yangtools.binding.test.mock.NodeChild;
import org.opendaylight.yangtools.binding.test.mock.NodeChildKey;
import org.opendaylight.yangtools.binding.test.mock.NodeKey;
import org.opendaylight.yangtools.binding.test.mock.Nodes;
import org.powermock.reflect.Whitebox;

class InstanceIdentifierTest {
    @Test
    void constructWithPredicates() {
        final var nodes = InstanceIdentifier.builder(Nodes.class).build();

        assertNotNull(nodes);
        assertEquals(Nodes.class, nodes.getTargetType());

        final var node = nodes.toBuilder().child(Node.class).build();

        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());

        assertTrue(nodes.contains(node));
    }

    @Test
    void fluentConstruction() {
        final var nodes = InstanceIdentifier.builder(Nodes.class).build();
        final var node = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();

        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());

        assertTrue(nodes.contains(node));
    }

    @Test
    void negativeContains() {
        final var fooChild = InstanceIdentifier.builder(Nodes.class)
            .child(InstantiatedFoo.class)
            .child(FooChild.class)
            .build();

        final var nodeTen = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        final var nodeOne = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(1)).build();
        final var nodes = InstanceIdentifier.builder(Nodes.class).build();

        assertFalse(fooChild.contains(nodeTen));
        assertFalse(nodeTen.contains(nodes));

        assertFalse(nodeOne.contains(nodes));
        assertTrue(nodes.contains(nodeOne));

        assertTrue(nodes.containsWildcarded(nodeOne));
        assertFalse(nodeOne.containsWildcarded(nodes));
    }

    @Test
    void containsWildcarded() {
        final var nodes = InstanceIdentifier.builder(Nodes.class).build();
        final var wildcarded = InstanceIdentifier.builder(Nodes.class).child(Node.class).build();
        final var wildcardedChildren = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class)
                .child(NodeChild.class).build();

        assertFalse(wildcarded.isExact());
        assertTrue(wildcarded.isWildcarded());
        assertFalse(wildcardedChildren.isExact());
        assertTrue(wildcardedChildren.isWildcarded());

        final var nodeTen = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        final var nodeOne = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(1)).build();

        assertFalse(nodeTen.isWildcarded());
        assertFalse(nodeOne.isWildcarded());
        assertTrue(nodes.containsWildcarded(nodeOne));
        assertTrue(wildcarded.containsWildcarded(nodeOne));
        assertTrue(wildcarded.containsWildcarded(nodeTen));
        assertFalse(InstanceIdentifier.builder(Nodes.class)
                .child(InstantiatedFoo.class).build().containsWildcarded(wildcarded));

        final var nodeTenChildWildcarded = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(10)).child(NodeChild.class).build();

        assertTrue(nodeTenChildWildcarded.isWildcarded());

        final var nodeTenChild = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(10)).child(NodeChild.class, new NodeChildKey(10)).build();
        final var nodeOneChild = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(1)).child(NodeChild.class, new NodeChildKey(1)).build();

        assertFalse(nodeTenChildWildcarded.containsWildcarded(nodeOneChild));
        assertTrue(nodeTenChildWildcarded.containsWildcarded(nodeTenChild));
    }

    @Test
    void basicTests() {
        final var fooRoot1 = InstanceIdentifier.create(FooRoot.class);
        final var fooRoot2 = InstanceIdentifier.create(FooRoot.class);
        final var object = new Object();

        assertTrue(fooRoot1.equals(fooRoot1));
        assertFalse(fooRoot1.equals(null));
        assertFalse(fooRoot1.equals(object));
        assertTrue(fooRoot1.equals(fooRoot2));

        Whitebox.setInternalState(fooRoot2, "steps", fooRoot1.steps());

        assertTrue(fooRoot1.equals(fooRoot2));
        assertFalse(fooRoot1.equals(InstanceIdentifier.builder(Nodes.class)
            .child(Node.class, new NodeKey(10))
            .child(NodeChild.class)
            .build()));

        // FIXME: what?
        final var node = InstanceIdentifier.create(Nodes.class).child(Node.class);
        Whitebox.setInternalState(node, "wildcarded", false);

        assertNotNull(InstanceIdentifier.unsafeOf(ImmutableList.copyOf(fooRoot1.steps())));
        assertNotNull(InstanceIdentifier.create(Nodes.class).child(Node.class));
        assertNotNull(InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(5)));
        assertNotNull(node.augmentation(NodeAugmentation.class));
        assertNotNull(fooRoot1.hashCode());
        assertNotNull(fooRoot1.toString());

        final InstanceIdentifier.Builder instanceIdentifierBuilder = fooRoot1.toBuilder();
        assertNotNull(instanceIdentifierBuilder.augmentation(InstantiatedFoo.class));
        assertNotNull(instanceIdentifierBuilder.build());
    }

    @Test
    void firstIdentifierOfTest() {
        final var node = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        assertEquals(InstanceIdentifier.create(Nodes.class), node.firstIdentifierOf(Nodes.class));
        assertNull(node.firstIdentifierOf(DataObject.class));
    }

    @Test
    void firstKeyOfTest() {
        final var node = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        assertNotNull(node.firstKeyOf(Node.class));

        final var fooRoot = InstanceIdentifier.create(FooRoot.class);
        assertNull(fooRoot.firstKeyOf(Node.class));
    }

    @Test
    void keyOfTest() {
        final var key = new NodeKey(42);
        assertEquals(key, InstanceIdentifier.<Node, NodeKey>keyOf(
            new KeyedInstanceIdentifier<>(ImmutableList.of(new KeyStep<>(Node.class, key)), false)));
    }

    @Test
    void serializationTest() throws IOException, ClassNotFoundException {
        final var fooRoot = InstanceIdentifier.create(FooRoot.class);

        final var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(fooRoot);
        }

        final var bytes = baos.toByteArray();
        assertEquals("""
            aced00057372002c6f72672e6f70656e6461796c696768742e79616e67746f6f6c732e79616e672e62696e64696e672e49497635000\
            00000000000010c000078707704000000017372002b6f72672e6f70656e6461796c696768742e79616e67746f6f6c732e62696e6469\
            6e672e4e6f64655374657000000000000000000200024c000863617365547970657400114c6a6176612f6c616e672f436c6173733b4\
            c00047479706571007e0003787070767200346f72672e6f70656e6461796c696768742e79616e67746f6f6c732e62696e64696e672e\
            746573742e6d6f636b2e466f6f526f6f740000000000000000000000787078""", HexFormat.of().formatHex(bytes));

        final var inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        final var deserialized = assertInstanceOf(InstanceIdentifier.class, inputStream.readObject());

        assertEquals(fooRoot, deserialized);
    }

    @Test
    void verifyTargetTest() {
        final var nodes = InstanceIdentifier.create(Nodes.class);
        assertSame(nodes, nodes.verifyTarget(Nodes.class));
        assertThrows(VerifyException.class, () -> nodes.verifyTarget(Node.class));
    }
}
