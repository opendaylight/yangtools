/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import org.junit.Test;
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

public class InstanceIdentifierTest {

    @Test
    public void constructWithPredicates() {
        final var nodes = DataObjectWildcard.builder(Nodes.class).build();

        assertNotNull(nodes);
        assertEquals(Nodes.class, nodes.getTargetType());

        final var node = nodes.toBuilder().child(Node.class).build();

        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());

        assertTrue(nodes.contains(node));
    }

    @Test
    public void fluentConstruction() {
        final var nodes = DataObjectWildcard.builder(Nodes.class).build();
        final var node = DataObjectWildcard.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();

        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());

        assertTrue(nodes.contains(node));
    }

    @Test
    public void negativeContains() {
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
    public void containsWildcarded() {
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
    public void basicTests() {
        final DataObjectWildcard<FooRoot> instanceIdentifier1 = DataObjectWildcard.create(FooRoot.class);
        final DataObjectWildcard<FooRoot> instanceIdentifier2 = DataObjectWildcard.create(FooRoot.class);
        final DataObjectWildcard<FooRoot> instanceIdentifier4 = DataObjectWildcard.create(FooRoot.class);
        final DataObjectWildcard<NodeChild> instanceIdentifier3 = DataObjectWildcard.builder(Nodes.class)
                .child(Node.class, new NodeKey(10)).child(NodeChild.class).build();
        final Object object = new Object();

        assertTrue(instanceIdentifier1.equals(instanceIdentifier1));
        assertFalse(instanceIdentifier1.equals(null));
        assertFalse(instanceIdentifier1.equals(object));
        assertTrue(instanceIdentifier1.equals(instanceIdentifier2));

        Whitebox.setInternalState(instanceIdentifier2, "pathArguments", instanceIdentifier1.steps);
        Whitebox.setInternalState(instanceIdentifier4, "wildcarded", true);

        assertTrue(instanceIdentifier1.equals(instanceIdentifier2));
        assertFalse(instanceIdentifier1.equals(instanceIdentifier3));
        assertFalse(instanceIdentifier1.equals(instanceIdentifier4));

        final DataObjectWildcard<Node> instanceIdentifier5 = DataObjectWildcard.create(Nodes.class).child(Node.class);
        Whitebox.setInternalState(instanceIdentifier5, "hash", instanceIdentifier1.hashCode());
        Whitebox.setInternalState(instanceIdentifier5, "wildcarded", false);

        assertNotNull(DataObjectWildcard.unsafeOf(ImmutableList.copyOf(instanceIdentifier1.steps())));
        assertNotNull(DataObjectWildcard.create(Nodes.class).child(Node.class));
        assertNotNull(DataObjectWildcard.create(Nodes.class).child(Node.class, new NodeKey(5)));
        assertNotNull(instanceIdentifier5.augmentation(NodeAugmentation.class));
        assertNotNull(instanceIdentifier1.hashCode());
        assertNotNull(instanceIdentifier1.toString());

        final DataObjectWildcard.Builder instanceIdentifierBuilder = instanceIdentifier1.toBuilder();
        assertEquals(instanceIdentifier1.hashCode(), instanceIdentifierBuilder.hashCode());
        assertNotNull(instanceIdentifierBuilder.augmentation(InstantiatedFoo.class));
        assertNotNull(instanceIdentifierBuilder.build());
    }

    @Test
    public void firstIdentifierOfTest() {
        final DataObjectWildcard<Node> instanceIdentifier =
                DataObjectWildcard.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        final DataObjectWildcard<Nodes> nodesIdentifier = instanceIdentifier.firstIdentifierOf(Nodes.class);
        assertNotNull(nodesIdentifier);
        final DataObjectWildcard<DataObject> dataObjectIdentifier =
                instanceIdentifier.firstIdentifierOf(DataObject.class);
        assertNull(dataObjectIdentifier);
    }

    @Test
    public void firstKeyOfTest() {
        final DataObjectWildcard<Node> instanceIdentifier =
                DataObjectWildcard.builder(Nodes.class).child(Node.class, new NodeKey(10)).build();
        final DataObjectWildcard<FooRoot> instanceIdentifier1 = DataObjectWildcard.create(FooRoot.class);
        assertNotNull(instanceIdentifier.firstKeyOf(Node.class));
        assertNull(instanceIdentifier1.firstKeyOf(Node.class));
    }

    @Test
    public void keyOfTest() {
        final var key = new NodeKey(42);
        final var step = new KeyStep<>(Node.class, key);
        assertEquals(key, DataObjectWildcard.keyOf(new DataObjectWildcard.WithKey<>(List.of(step), step)));
    }

    @Test
    public void serializationTest() throws IOException, ClassNotFoundException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);

        final DataObjectWildcard<FooRoot> instanceIdentifier = DataObjectWildcard.create(FooRoot.class);
        outputStream.writeObject(instanceIdentifier);
        outputStream.flush();
        outputStream.close();

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        final ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
        final DataObjectWildcard<?> deserialized = (DataObjectWildcard<?>) inputStream.readObject();

        assertEquals(instanceIdentifier, deserialized);
    }

    @Test
    public void equalsTest() {
        final DataObjectWildcard.Builder<FooRoot> builder1 =  DataObjectWildcard.create(FooRoot.class).toBuilder();
        final DataObjectWildcard.Builder<FooRoot> builder2 =  DataObjectWildcard.create(FooRoot.class).toBuilder();
        final DataObjectWildcard.Builder<Nodes> builder3 =  DataObjectWildcard.create(Nodes.class).toBuilder();
        final DataObjectWildcard.Builder<Nodes> builder4 =  DataObjectWildcard.create(Nodes.class).toBuilder();
        final Object obj = new Object();

        assertTrue(builder1.equals(builder2));
        assertTrue(builder2.equals(builder1));
        assertTrue(builder2.equals(builder2));
        assertTrue(builder3.equals(builder4));
        assertTrue(builder4.equals(builder4));
        assertFalse(builder3.equals(builder1));
        assertFalse(builder3.equals(null));
        assertFalse(builder4.equals(null));
        assertFalse(builder1.equals(obj));

        builder3.child(Node.class, new NodeKey(10));
        assertFalse(builder3.equals(builder4));
        assertFalse(builder4.equals(builder3));

        builder4.child(Node.class, new NodeKey(20));
        assertFalse(builder3.equals(builder4));
        assertFalse(builder4.equals(builder3));
    }

    @Test
    public void hashCodeTest() {
        final DataObjectWildcard.Builder<FooRoot> builder1 =  DataObjectWildcard.create(FooRoot.class).toBuilder();
        final DataObjectWildcard.Builder<FooRoot> builder2 =  DataObjectWildcard.create(FooRoot.class).toBuilder();
        final DataObjectWildcard.Builder<Nodes> builder3 =  DataObjectWildcard.create(Nodes.class).toBuilder();
        final DataObjectWildcard.Builder<Nodes> builder4 =  DataObjectWildcard.create(Nodes.class).toBuilder();
        final Object obj = new Object();

        assertTrue(builder1.hashCode() == builder2.hashCode());
        assertTrue(builder1.hashCode() != builder3.hashCode());
        assertTrue(builder3.hashCode() == builder4.hashCode());
        assertTrue(builder2.hashCode() != builder4.hashCode());
        assertTrue(builder1.hashCode() != obj.hashCode());

        builder3.child(Node.class, new NodeKey(10));

        assertTrue(builder3.hashCode() != builder4.hashCode());
    }

    @Test
    public void verifyTargetTest() {
        final DataObjectWildcard<Nodes> nodeId = DataObjectWildcard.create(Nodes.class);
        assertSame(nodeId, nodeId.verifyTarget(Nodes.class));
        assertThrows(VerifyException.class, () -> nodeId.verifyTarget(Node.class));
    }
}
