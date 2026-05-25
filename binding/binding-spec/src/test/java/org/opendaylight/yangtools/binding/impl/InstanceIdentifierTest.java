/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.test.mock.FooRoot;
import org.opendaylight.yangtools.binding.test.mock.InstantiatedFoo;
import org.opendaylight.yangtools.binding.test.mock.Node;
import org.opendaylight.yangtools.binding.test.mock.NodeAugmentation;
import org.opendaylight.yangtools.binding.test.mock.NodeChild;
import org.opendaylight.yangtools.binding.test.mock.NodeKey;
import org.opendaylight.yangtools.binding.test.mock.Nodes;

class InstanceIdentifierTest {
    @Test
    void constructWithPredicates() {
        final var nodes = DataObjectIdentifier.builder(Nodes.class).build();

        assertNotNull(nodes);
        assertEquals(Nodes.class, nodes.lastStep().type());

        final var node = nodes.toBuilder().toReferenceBuilder().child(Node.class).build();

        assertNotNull(node);
        assertEquals(Node.class, node.lastStep().type());
    }

    @Test
    void fluentConstruction() {
        final var node = DataObjectReference.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();

        assertNotNull(node);
        assertEquals(Node.class, node.lastStep().type());
    }

    @Test
    void basicTests() {
        final var fooRoot1 = DataObjectReference.builder(FooRoot.class).build();
        final var fooRoot2 = DataObjectReference.builder(FooRoot.class).build();
        final var object = new Object();

        assertTrue(fooRoot1.equals(fooRoot1));
        assertFalse(fooRoot1.equals(null));
        assertFalse(fooRoot1.equals(object));
        assertTrue(fooRoot1.equals(fooRoot2));

        assertTrue(fooRoot1.equals(fooRoot2));
        assertFalse(fooRoot1.equals(DataObjectReference.builder(Nodes.class)
            .child(Node.class, new NodeKey(10))
            .child(NodeChild.class)
            .build()));

        // FIXME: what?
        final var node = DataObjectReference.builder(Nodes.class).child(Node.class).build();

        assertNotNull(DataObjectReference.ofUnsafeSteps(ImmutableList.copyOf(fooRoot1.steps())));
        assertNotNull(DataObjectReference.builder(Nodes.class).child(Node.class));
        assertNotNull(DataObjectReference.builder(Nodes.class).child(Node.class, new NodeKey(5)));
        assertNotNull(node.toBuilder().augmentation(NodeAugmentation.class));
        assertNotNull(fooRoot1.hashCode());
        assertNotNull(fooRoot1.toString());

        @SuppressWarnings("rawtypes")
        final DataObjectReference.Builder instanceIdentifierBuilder = fooRoot1.toBuilder();
        assertNotNull(instanceIdentifierBuilder.augmentation(InstantiatedFoo.class));
        assertNotNull(instanceIdentifierBuilder.build());
    }

    @Test
    void firstIdentifierOfTest() {
        final var node = DataObjectIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        assertEquals(DataObjectReference.builder(Nodes.class).build(), node.tryTrimTo(Nodes.class));
        assertNull(node.tryTrimTo(DataObject.class));
    }

    @Test
    void firstKeyOfTest() {
        final var node = DataObjectIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        assertNotNull(node.firstKeyOf(Node.class));

        final var fooRoot = DataObjectIdentifier.builder(FooRoot.class).build();
        assertNull(fooRoot.firstKeyOf(Node.class));
    }

    @Test
    void serializationTest() throws IOException, ClassNotFoundException {
        final var fooRoot = DataObjectReference.builder(FooRoot.class).build();

        final var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(fooRoot);
        }

        final var bytes = baos.toByteArray();
        assertEquals("""
            aced00057372002c6f72672e6f70656e6461796c696768742e79616e67746f6f6c732e62696e64696e672e696d706c2e4f527631000\
            00000000000010c000078707704000000017372002b6f72672e6f70656e6461796c696768742e79616e67746f6f6c732e62696e6469\
            6e672e4e6f64655374657000000000000000000200024c000863617365547970657400114c6a6176612f6c616e672f436c6173733b4\
            c00047479706571007e0003787070767200346f72672e6f70656e6461796c696768742e79616e67746f6f6c732e62696e64696e672e\
            746573742e6d6f636b2e466f6f526f6f740000000000000000000000787078""", HexFormat.of().formatHex(bytes));

        final var inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        final var deserialized = assertInstanceOf(DataObjectIdentifier.class, inputStream.readObject());

        assertEquals(fooRoot, deserialized);
    }
}
