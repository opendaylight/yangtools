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
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
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
        final InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).build();

        assertNotNull(nodes);
        assertEquals(Nodes.class, nodes.getTargetType());

        final InstanceIdentifier<Node> node = nodes.builder().child(Node.class).build();

        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());

        assertTrue(nodes.contains(node));
    }

    @Test
    public void fluentConstruction() {
        final InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).build();
        final InstanceIdentifier<Node> node =
                InstanceIdentifier.builder(Nodes.class).child(Node.class,new NodeKey(10)).build();

        assertNotNull(node);
        assertEquals(Node.class, node.getTargetType());

        assertTrue(nodes.contains(node));
    }

    @Test
    public void negativeContains() {
        final InstanceIdentifier<FooChild> fooChild =
                InstanceIdentifier.builder(Nodes.class).child(InstantiatedFoo.class).child(FooChild.class).build();

        final InstanceIdentifier<Node> nodeTen = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).build();
        final InstanceIdentifier<Node> nodeOne = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(1)).build();
        final InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).build();

        assertFalse(fooChild.contains(nodeTen));
        assertFalse(nodeTen.contains(nodes));

        assertFalse(nodeOne.contains(nodes));
        assertTrue(nodes.contains(nodeOne));

        assertTrue(nodes.containsWildcarded(nodeOne));
        assertFalse(nodeOne.containsWildcarded(nodes));
    }

    @Test
    public void containsWildcarded() {
        final InstanceIdentifier<Nodes> nodes = InstanceIdentifier.builder(Nodes.class).build();
        final InstanceIdentifier<Node> wildcarded = InstanceIdentifier.builder(Nodes.class).child(Node.class).build();
        final InstanceIdentifier<NodeChild> wildcardedChildren = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class) //
                .child(NodeChild.class).build();

        assertTrue(wildcarded.isWildcarded());
        assertTrue(wildcardedChildren.isWildcarded());

        final InstanceIdentifier<Node> nodeTen = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).build();
        final InstanceIdentifier<Node> nodeOne = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(1)).build();

        assertFalse(nodeTen.isWildcarded());
        assertFalse(nodeOne.isWildcarded());
        assertTrue(nodes.containsWildcarded(nodeOne));
        assertTrue(wildcarded.containsWildcarded(nodeOne));
        assertTrue(wildcarded.containsWildcarded(nodeTen));
        assertFalse(InstanceIdentifier.builder(Nodes.class)
                .child(InstantiatedFoo.class).build().containsWildcarded(wildcarded));

        final InstanceIdentifier<NodeChild> nodeTenChildWildcarded = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).child(NodeChild.class).build();

        assertTrue(nodeTenChildWildcarded.isWildcarded());

        final InstanceIdentifier<NodeChild> nodeTenChild = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(10)).child(NodeChild.class, new NodeChildKey(10)).build();
        final InstanceIdentifier<NodeChild> nodeOneChild = InstanceIdentifier.builder(Nodes.class) //
                .child(Node.class,new NodeKey(1)).child(NodeChild.class, new NodeChildKey(1)).build();

        assertFalse(nodeTenChildWildcarded.containsWildcarded(nodeOneChild));
        assertTrue(nodeTenChildWildcarded.containsWildcarded(nodeTenChild));
    }

    @Test
    public void basicTests() throws Exception {
        final InstanceIdentifier<DataObject> instanceIdentifier1 = InstanceIdentifier.create(DataObject.class);
        final InstanceIdentifier<DataObject> instanceIdentifier2 = InstanceIdentifier.create(DataObject.class);
        final InstanceIdentifier<DataObject> instanceIdentifier4 = InstanceIdentifier.create(DataObject.class);
        final InstanceIdentifier<NodeChild> instanceIdentifier3 = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(10)).child(NodeChild.class).build();
        final Object object = new Object();

        assertTrue(instanceIdentifier1.equals(instanceIdentifier1));
        assertFalse(instanceIdentifier1.equals(null));
        assertFalse(instanceIdentifier1.equals(object));
        assertTrue(instanceIdentifier1.equals(instanceIdentifier2));

        final Field pathArgumentsField = InstanceIdentifier.class.getDeclaredField("pathArguments");
        final Field wildCardField = InstanceIdentifier.class.getDeclaredField("wildcarded");
        pathArgumentsField.setAccessible(true);
        wildCardField.setAccessible(true);
        pathArgumentsField.set(instanceIdentifier2, instanceIdentifier1.pathArguments);
        wildCardField.set(instanceIdentifier4, true);

        assertTrue(instanceIdentifier1.equals(instanceIdentifier2));

        assertFalse(instanceIdentifier1.equals(instanceIdentifier3));
        assertFalse(instanceIdentifier1.equals(instanceIdentifier4));
        final InstanceIdentifier instanceIdentifier5 = InstanceIdentifier.create(Node.class);
        final Field hashField = InstanceIdentifier.class.getDeclaredField("hash");
        hashField.setAccessible(true);
        hashField.set(instanceIdentifier5, instanceIdentifier1.hashCode());
        wildCardField.set(instanceIdentifier5, false);

        assertFalse(instanceIdentifier1.equals(instanceIdentifier5));
        assertNotNull(InstanceIdentifier.create(ImmutableList.copyOf(instanceIdentifier1.getPathArguments())));
        assertNotNull(InstanceIdentifier.create(Nodes.class).child(Node.class));
        assertNotNull(InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(5)));
        assertNotNull(instanceIdentifier5.augmentation(DataObject.class));
        assertNotNull(instanceIdentifier1.hashCode());
        assertNotNull(instanceIdentifier1.toString());

        final InstanceIdentifierBuilder instanceIdentifierBuilder = instanceIdentifier1.builder();
        assertEquals(instanceIdentifier1.hashCode(), instanceIdentifierBuilder.hashCode());
        assertNotNull(instanceIdentifierBuilder.augmentation(InstantiatedFoo.class));
        assertNotNull(instanceIdentifierBuilder.build());
    }

    @Test
    public void firstIdentifierOfTest() {
        final InstanceIdentifier<Node> instanceIdentifier =
                InstanceIdentifier.builder(Nodes.class).child(Node.class,new NodeKey(10)).build();
        final InstanceIdentifier<Nodes> nodesIdentifier = instanceIdentifier.firstIdentifierOf(Nodes.class);
        assertNotNull(nodesIdentifier);
        final InstanceIdentifier<DataObject> dataObjectIdentifier =
                instanceIdentifier.firstIdentifierOf(DataObject.class);
        assertNull(dataObjectIdentifier);
    }

    @Test
    public void firstKeyOfTest() {
        final InstanceIdentifier<Node> instanceIdentifier =
                InstanceIdentifier.builder(Nodes.class).child(Node.class,new NodeKey(10)).build();
        final InstanceIdentifier<DataObject> instanceIdentifier1 = InstanceIdentifier.create(DataObject.class);
        assertNotNull(instanceIdentifier.firstKeyOf(Node.class, null));
        assertNull(instanceIdentifier1.firstKeyOf(Node.class));
    }

    @Test
    public void keyOfTest() throws Exception {
        final Identifier<?> identifier = mock(Identifier.class);
        assertEquals(identifier, InstanceIdentifier.keyOf(
                new KeyedInstanceIdentifier(Identifiable.class, ImmutableList.of(), false, 0, identifier)));
    }

    @Test
    public void serializationTest() throws Exception {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);

        final InstanceIdentifier<DataObject> instanceIdentifier = InstanceIdentifier.create(DataObject.class);
        outputStream.writeObject(instanceIdentifier);
        outputStream.flush();
        outputStream.close();

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        final ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
        final InstanceIdentifier<?> deserialized = (InstanceIdentifier<?>) inputStream.readObject();

        assertEquals(instanceIdentifier, deserialized);
    }

    @Test
    public void equalsTest() {
        final InstanceIdentifierBuilder<DataObject> builder1 =  InstanceIdentifier.create(DataObject.class).builder();
        final InstanceIdentifierBuilder<DataObject> builder2 =  InstanceIdentifier.create(DataObject.class).builder();
        final InstanceIdentifierBuilder<Nodes> builder3 =  InstanceIdentifier.create(Nodes.class).builder();
        final InstanceIdentifierBuilder<Nodes> builder4 =  InstanceIdentifier.create(Nodes.class).builder();
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

        InstanceIdentifierBuilder<Nodes> iib1 = new InstanceIdentifierBuilderImpl(null, null, 31, true);
        InstanceIdentifierBuilder<Nodes> iib2 = new InstanceIdentifierBuilderImpl(null, null, 31, false);
        InstanceIdentifierBuilder<Nodes> iib3 = new InstanceIdentifierBuilderImpl(null, null, 31, true);
        assertFalse(iib1.equals(iib2));
        assertFalse(iib2.equals(iib1));
        assertTrue(iib1.equals(iib3));
        assertTrue(iib3.equals(iib1));
    }

    @Test
    public void hashCodeTest() {
        final InstanceIdentifierBuilder<DataObject> builder1 =  InstanceIdentifier.create(DataObject.class).builder();
        final InstanceIdentifierBuilder<DataObject> builder2 =  InstanceIdentifier.create(DataObject.class).builder();
        final InstanceIdentifierBuilder<Nodes> builder3 =  InstanceIdentifier.create(Nodes.class).builder();
        final InstanceIdentifierBuilder<Nodes> builder4 =  InstanceIdentifier.create(Nodes.class).builder();
        final Object obj = new Object();

        assertTrue(builder1.hashCode() == builder2.hashCode());
        assertTrue(builder1.hashCode() != builder3.hashCode());
        assertTrue(builder3.hashCode() == builder4.hashCode());
        assertTrue(builder2.hashCode() != builder4.hashCode());
        assertTrue(builder1.hashCode() != obj.hashCode());

        builder3.child(Node.class, new NodeKey(10));

        assertTrue(builder3.hashCode() != builder4.hashCode());
    }

}
