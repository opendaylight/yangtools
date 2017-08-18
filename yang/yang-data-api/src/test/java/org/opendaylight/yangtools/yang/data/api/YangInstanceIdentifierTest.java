/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Unit tests for InstanceIdentifier.
 *
 * @author Thomas Pantelis
 */
public class YangInstanceIdentifierTest {

    private static final QName NODENAME1 = QName.create("test", "2014-5-28", "node1");
    private static final QName NODENAME2 = QName.create("test", "2014-5-28", "node2");
    private static final QName NODENAME3 = QName.create("test", "2014-5-28", "node3");
    private static final QName NODENAME4 = QName.create("test", "2014-5-28", "node4");
    private static final QName KEY1 = QName.create("test", "2014-5-28", "key1");
    private static final QName KEY2 = QName.create("test", "2014-5-28", "key2");
    private static final QName KEY3 = QName.create("test", "2014-5-28", "key3");

    @Test
    public void testGetLastPathArgument() {
        YangInstanceIdentifier id1 = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME2));
        assertEquals("getLastPathArgument", new NodeIdentifier(NODENAME2), id1.getLastPathArgument());
        YangInstanceIdentifier id2 = YangInstanceIdentifier.create();
        assertNull(id2.getLastPathArgument());
    }

    @Test
    public void testHashCodeEquals() {
        YangInstanceIdentifier id1 = YangInstanceIdentifier.create(NodeIdentifier.create(NODENAME1),
                new NodeIdentifier(NODENAME2));
        YangInstanceIdentifier id2 = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
                NodeIdentifier.create(NODENAME2));

        assertEquals("hashCode", id1.hashCode(), id2.hashCode());
    }

    @Test
    public void testEquals() {
        final YangInstanceIdentifier id1 = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME2));
        final YangInstanceIdentifier id2 = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME2));
        final YangInstanceIdentifier id3 = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME2),
                new NodeIdentifier(NODENAME1));
        final YangInstanceIdentifier id4 = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1));

        assertEquals("equals", false, id1.equals(null));
        assertEquals("equals", true, id1.equals(id1));
        assertEquals("equals", true, id1.equals(id2));
        assertEquals("equals", false, id1.equals(id3));
        assertEquals("equals", false, id1.equals(id4));
        assertEquals("equals", false, id1.equals(new Object()));
    }

    @Test
    public void testToString() {
        YangInstanceIdentifier id = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME2));

        assertNotNull(id.toString());
    }

    @Test
    public void testNode() {
        final YangInstanceIdentifier id = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME2));

        YangInstanceIdentifier newID = id.node(NODENAME3);

        assertNotNull("InstanceIdentifier is null", newID);
        assertEquals("Path size", 3, newID.getPathArguments().size());

        Iterator<PathArgument> it = newID.getPathArguments().iterator();
        assertEquals("PathArg 1 node type", NODENAME1, it.next().getNodeType());
        assertEquals("PathArg 2 node type", NODENAME2, it.next().getNodeType());
        assertEquals("PathArg 3 node type", NODENAME3, it.next().getNodeType());

        newID = id.node(new NodeIdentifier(NODENAME3));

        assertNotNull("InstanceIdentifier is null", newID);
        assertEquals("Path size", 3, newID.getPathArguments().size());

        it = newID.getPathArguments().iterator();
        assertEquals("PathArg 1 node type", NODENAME1, it.next().getNodeType());
        assertEquals("PathArg 2 node type", NODENAME2, it.next().getNodeType());
        assertEquals("PathArg 3 node type", NODENAME3, it.next().getNodeType());
    }

    @Test
    public void testRelativeTo() {
        final YangInstanceIdentifier id1 = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME2), new NodeIdentifier(NODENAME3), new NodeIdentifier(NODENAME4));
        final YangInstanceIdentifier id2 = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME2));
        final YangInstanceIdentifier id3 = YangInstanceIdentifier.create(Lists.newArrayList(
                    new NodeIdentifier(NODENAME1), new NodeIdentifier(NODENAME2)));

        Optional<YangInstanceIdentifier> relative = id1.relativeTo(id2);
        assertEquals("isPresent", true, relative.isPresent());

        List<PathArgument> path = relative.get().getPathArguments();
        assertEquals("Path size", 2, path.size());
        assertEquals("PathArg 1 node type", NODENAME3, path.get(0).getNodeType());
        assertEquals("PathArg 2 node type", NODENAME4, path.get(1).getNodeType());

        relative = id2.relativeTo(id3);
        assertEquals("isPresent", true, relative.isPresent());
        assertEquals("Path size", 0, relative.get().getPathArguments().size());

        relative = id2.relativeTo(id1);
        assertEquals("isPresent", false, relative.isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContainsNull() {
        final YangInstanceIdentifier id = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1));

        id.contains(null);
    }

    @Test
    public void testContains() {
        final YangInstanceIdentifier id1 = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME2), new NodeIdentifier(NODENAME3), new NodeIdentifier(NODENAME4));
        final YangInstanceIdentifier id2 = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME2));
        final YangInstanceIdentifier id3 = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME2));
        final YangInstanceIdentifier id4 = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME3));

        assertEquals("contains", true, id2.contains(id1));
        assertEquals("contains", true, id2.contains(id3));
        assertEquals("contains", false, id1.contains(id2));
        assertEquals("contains", false, id2.contains(id4));
    }

    @Test
    public void testOf() {
        YangInstanceIdentifier newID = YangInstanceIdentifier.of(NODENAME1);

        assertNotNull("InstanceIdentifier is null", newID);
        assertEquals("Path size", 1, newID.getPathArguments().size());
        assertEquals("PathArg 1 node type", NODENAME1, newID.getPathArguments().get(0).getNodeType());

        assertNotNull(newID.toString()); // for code coverage
    }

    @Test
    public void testBuilder() {
        YangInstanceIdentifier newID = YangInstanceIdentifier.builder()
                .node(NODENAME1)
                .nodeWithKey(NODENAME2, Collections.singletonMap(KEY1, "foo"))
                .nodeWithKey(NODENAME3, KEY2, "bar").build();

        assertNotNull("InstanceIdentifier is null", newID);
        assertEquals("Path size", 3, newID.getPathArguments().size());

        Iterator<PathArgument> it = newID.getPathArguments().iterator();
        assertEquals("PathArg 1 node type", NODENAME1, it.next().getNodeType());
        verifyNodeIdentifierWithPredicates("PathArg 2", it.next(), NODENAME2, KEY1, "foo");
        verifyNodeIdentifierWithPredicates("PathArg 3", it.next(), NODENAME3, KEY2, "bar");

        newID = YangInstanceIdentifier.builder(newID).node(NODENAME4).build();

        assertNotNull("InstanceIdentifier is null", newID);
        assertEquals("Path size", 4, newID.getPathArguments().size());

        it = newID.getPathArguments().iterator();
        assertEquals("PathArg 1 node type", NODENAME1, it.next().getNodeType());
        assertEquals("PathArg 2 node type", NODENAME2, it.next().getNodeType());
        assertEquals("PathArg 3 node type", NODENAME3, it.next().getNodeType());
        assertEquals("PathArg 4 node type", NODENAME4, it.next().getNodeType());

        newID = YangInstanceIdentifier.builder().node(NODENAME1).build();

        assertNotNull("InstanceIdentifier is null", newID);
        assertEquals("Path size", 1, newID.getPathArguments().size());
        assertEquals("PathArg 1 node type", NODENAME1, newID.getPathArguments().get(0).getNodeType());
    }

    private static void verifyNodeIdentifierWithPredicates(final String prefix,
            final PathArgument arg, final QName nodeName, final QName key, final Object value) {

        assertNotNull(prefix + " is null", arg);
        assertEquals(prefix + " class", NodeIdentifierWithPredicates.class, arg.getClass());
        NodeIdentifierWithPredicates node = (NodeIdentifierWithPredicates)arg;
        assertEquals(prefix + " node type", nodeName, node.getNodeType());
        assertEquals(prefix + " key values map size", 1, node.getKeyValues().size());
        Entry<QName, Object> entry = node.getKeyValues().entrySet().iterator().next();
        assertEquals(prefix + " key values map entry key", key, entry.getKey());
        assertEquals(prefix + " key values map entry value", value, entry.getValue());
    }

    @Test
    public void testNodeIdentifierWithPredicates() {

        NodeIdentifierWithPredicates node1 = new NodeIdentifierWithPredicates(NODENAME1, KEY1, "foo");
        verifyNodeIdentifierWithPredicates("NodeIdentifierWithPredicates", node1, NODENAME1, KEY1, "foo");

        NodeIdentifierWithPredicates node2 = new NodeIdentifierWithPredicates(NODENAME1, KEY1, "foo");

        assertEquals("hashCode", node1.hashCode(), node2.hashCode());
        assertEquals("equals", true, node1.equals(node2));

        assertEquals("equals", false,
                node1.equals(new NodeIdentifierWithPredicates(NODENAME2, KEY1, "foo")));
        assertEquals("equals", false,
                node1.equals(new NodeIdentifierWithPredicates(NODENAME1, KEY2, "foo")));
        assertEquals("equals", false,
                node1.equals(new NodeIdentifierWithPredicates(NODENAME1, KEY1, "bar")));
        assertEquals("equals", false, node1.equals(new Object()));

        assertNotNull(node1.toString()); // for code coverage
        assertNotNull(node1.toRelativeString(node2));

        NodeIdentifierWithPredicates node3 = new NodeIdentifierWithPredicates(NODENAME1,
                ImmutableMap.<QName, Object>builder().put(KEY1, 10).put(KEY2, 20).build());

        NodeIdentifierWithPredicates node4 = new NodeIdentifierWithPredicates(NODENAME1,
                ImmutableMap.<QName, Object>builder().put(KEY1, 10).put(KEY2, 20).build());

        assertEquals("hashCode", node3.hashCode(), node4.hashCode());
        assertEquals("equals", true, node3.equals(node4));

        assertEquals("equals", false, node3.equals(node1));
        assertEquals("equals", false,
                node1.equals(new NodeIdentifierWithPredicates(NODENAME1,
                        ImmutableMap.<QName, Object>builder().put(KEY1, 10).put(KEY3, 20).build())));

        node1 = new NodeIdentifierWithPredicates(NODENAME1, KEY1, new byte[]{1,2});
        node2 = new NodeIdentifierWithPredicates(NODENAME1, KEY1, new byte[]{1,2});

        assertEquals("hashCode", node1.hashCode(), node2.hashCode());
        assertEquals("equals", true, node1.equals(node2));

        assertEquals("equals", false,
                node1.equals(new NodeIdentifierWithPredicates(NODENAME1, KEY1, new byte[]{1,3})));
        assertEquals("equals", false,
                node1.equals(new NodeIdentifierWithPredicates(NODENAME1, KEY1, new byte[]{1})));
        assertEquals("equals", false,
                node1.equals(new NodeIdentifierWithPredicates(NODENAME1, KEY1, new byte[]{1,2,3})));
    }

    @Test
    public void testNodeWithValue() {

        NodeWithValue<?> node1 = new NodeWithValue<>(NODENAME1, "foo");
        assertEquals("getNodeType", NODENAME1, node1.getNodeType());
        assertEquals("getValue", "foo", node1.getValue());

        NodeWithValue<?> node2 = new NodeWithValue<>(NODENAME1, "foo");

        assertEquals("hashCode", node1.hashCode(), node2.hashCode());
        assertEquals("equals", true, node1.equals(node2));

        assertEquals("equals", false, node1.equals(new NodeWithValue<>(NODENAME1, "bar")));
        assertEquals("equals", false, node1.equals(new NodeWithValue<>(NODENAME2, "foo")));
        assertEquals("equals", false, node1.equals(new Object()));

        assertNotNull(node1.toString()); // for code coverage
        assertNotNull(node1.toRelativeString(node2));

        NodeWithValue<?> node3 = new NodeWithValue<>(NODENAME1, new byte[]{1,2});
        NodeWithValue<?> node4 = new NodeWithValue<>(NODENAME1, new byte[]{1,2});

        assertEquals("hashCode", node3.hashCode(), node4.hashCode());
        assertEquals("equals", true, node3.equals(node4));

        assertEquals("equals", false, node3.equals(new NodeWithValue<>(NODENAME1, new byte[]{1,3})));
        assertEquals("equals", false, node3.equals(node1));
    }

    @Test
    public void testNodeIdentifier() {

        final NodeIdentifier node1 = new NodeIdentifier(NODENAME1);
        assertEquals("getNodeType", NODENAME1, node1.getNodeType());
        final NodeIdentifier node2 = new NodeIdentifier(NODENAME1);
        final AugmentationIdentifier node3 = new AugmentationIdentifier(Sets.newHashSet(NODENAME1, NODENAME2));

        assertEquals("hashCode", node1.hashCode(), node2.hashCode());
        assertEquals("compareTo", 0, node1.compareTo(node2));
        assertEquals("compareTo", true, node1.compareTo(new NodeIdentifier(NODENAME3)) != 0);

        assertEquals("equals", false, node1.equals(null));
        assertEquals("equals", false, node1.equals(node3));
        assertEquals("equals", true, node1.equals(node1));
        assertEquals("equals", true, node1.equals(node2));
        assertEquals("equals", false, node1.equals(new NodeIdentifier(NODENAME3)));
        assertEquals("equals", false, node1.equals(new Object()));

        assertNotNull(node1.toString()); // for code coverage
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAugmentationIdentifierNodeType() {
        AugmentationIdentifier node1 = new AugmentationIdentifier(Sets.newHashSet(NODENAME1, NODENAME2));
        node1.getNodeType();
    }

    @Test
    public void testAugmentationIdentifier() {

        final AugmentationIdentifier node1 = new AugmentationIdentifier(Sets.newHashSet(NODENAME1, NODENAME2));
        assertEquals("getPossibleChildNames", Sets.newHashSet(NODENAME1, NODENAME2), node1.getPossibleChildNames());
        final AugmentationIdentifier node2 = new AugmentationIdentifier(Sets.newHashSet(NODENAME2, NODENAME1));
        final AugmentationIdentifier node3 = new AugmentationIdentifier(Sets.newHashSet(NODENAME1, NODENAME3));
        final AugmentationIdentifier node4 = new AugmentationIdentifier(Sets.newHashSet(NODENAME1, NODENAME2,
                    NODENAME3));
        final NodeIdentifier node5 = new NodeIdentifier(NODENAME3);

        assertEquals("hashCode", node1.hashCode(), node2.hashCode());

        assertEquals("equals", true, node1.equals(node1));
        assertEquals("equals", true, node1.equals(node2));
        assertEquals("equals", false, node1.equals(node3));
        assertEquals("equals", false, node1.equals(new AugmentationIdentifier(Sets.newHashSet(NODENAME1))));
        assertEquals("equals", false, node1.equals(new Object()));

        assertEquals("compareTo", -1, node1.compareTo(node5));
        assertNotEquals("compareTo", -1, node1.compareTo(node2));
        assertEquals("compareTo", 0, node1.compareTo(node2));
        assertEquals("compareTo", 1, node1.compareTo(node4));
        assertEquals("compareTo", -1, node4.compareTo(node1));

        assertNotNull(node1.toString()); // for code coverage
        assertNotNull(node1.toRelativeString(node5));
    }

    private static YangInstanceIdentifier serdes(final YangInstanceIdentifier id) throws IOException,
            ClassNotFoundException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(id);
        }

        final byte[] bytes = bos.toByteArray();
        final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            final YangInstanceIdentifier ret = (YangInstanceIdentifier) ois.readObject();
            assertEquals(0, ois.available());
            return ret;
        }
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        final YangInstanceIdentifier fixed = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
            new NodeIdentifier(NODENAME2));
        assertEquals(fixed, serdes(fixed));

        final YangInstanceIdentifier stacked = YangInstanceIdentifier.EMPTY.node(new NodeIdentifier(NODENAME1));
        assertEquals(stacked, serdes(stacked));

        final YangInstanceIdentifier empty = serdes(YangInstanceIdentifier.EMPTY);
        assertSame(YangInstanceIdentifier.EMPTY, empty);
    }

    @Test
    public void testToOptimized() {
        final YangInstanceIdentifier fixed = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1),
            new NodeIdentifier(NODENAME2));
        final YangInstanceIdentifier stacked = YangInstanceIdentifier.EMPTY.node(NodeIdentifier.create(NODENAME1))
                .node(NodeIdentifier.create(NODENAME2));

        assertSame(fixed, fixed.toOptimized());

        final YangInstanceIdentifier opt = stacked.toOptimized();
        assertTrue(opt instanceof FixedYangInstanceIdentifier);
        assertEquals(fixed, stacked.toOptimized());
    }

    @Test
    public void testGetParent() {
        final YangInstanceIdentifier fixed = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1));
        final YangInstanceIdentifier stacked = YangInstanceIdentifier.EMPTY.node(new NodeIdentifier(NODENAME1));
        final YangInstanceIdentifier twoStacked = stacked.node(new NodeIdentifier(NODENAME2));

        assertNull(YangInstanceIdentifier.EMPTY.getParent());
        assertSame(YangInstanceIdentifier.EMPTY, fixed.getParent());
        assertSame(YangInstanceIdentifier.EMPTY, stacked.getParent());
        assertSame(stacked, twoStacked.getParent());
    }

    @Test
    public void testIsEmpty() {
        final YangInstanceIdentifier fixed = YangInstanceIdentifier.create(new NodeIdentifier(NODENAME1));
        final YangInstanceIdentifier stacked = YangInstanceIdentifier.EMPTY.node(new NodeIdentifier(NODENAME1));

        assertTrue(YangInstanceIdentifier.EMPTY.isEmpty());
        assertFalse(fixed.isEmpty());
        assertFalse(stacked.isEmpty());
    }
}
