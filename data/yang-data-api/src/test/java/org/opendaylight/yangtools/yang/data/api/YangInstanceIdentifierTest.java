/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
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
    private static final QName NODENAME1 = QName.create("test", "2014-05-28", "node1");
    private static final QName NODENAME2 = QName.create("test", "2014-05-28", "node2");
    private static final QName NODENAME3 = QName.create("test", "2014-05-28", "node3");
    private static final QName NODENAME4 = QName.create("test", "2014-05-28", "node4");
    private static final QName KEY1 = QName.create("test", "2014-05-28", "key1");
    private static final QName KEY2 = QName.create("test", "2014-05-28", "key2");
    private static final QName KEY3 = QName.create("test", "2014-05-28", "key3");

    @Test
    public void testGetLastPathArgument() {
        YangInstanceIdentifier id1 = YangInstanceIdentifier.of(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME2));
        assertEquals(new NodeIdentifier(NODENAME2), id1.getLastPathArgument());
        YangInstanceIdentifier id2 = YangInstanceIdentifier.of();
        assertNull(id2.getLastPathArgument());
    }

    @Test
    public void testHashCodeEquals() {
        YangInstanceIdentifier id1 = YangInstanceIdentifier.of(NodeIdentifier.create(NODENAME1),
                new NodeIdentifier(NODENAME2));
        YangInstanceIdentifier id2 = YangInstanceIdentifier.of(new NodeIdentifier(NODENAME1),
                NodeIdentifier.create(NODENAME2));
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    public void testEquals() {
        final var id1 = YangInstanceIdentifier.of(new NodeIdentifier(NODENAME1), new NodeIdentifier(NODENAME2));
        final var id2 = YangInstanceIdentifier.of(new NodeIdentifier(NODENAME1), new NodeIdentifier(NODENAME2));
        final var id3 = YangInstanceIdentifier.of(new NodeIdentifier(NODENAME2), new NodeIdentifier(NODENAME1));
        final var id4 = YangInstanceIdentifier.of(new NodeIdentifier(NODENAME1));

        assertFalse(id1.equals(null));
        assertTrue(id1.equals(id1));
        assertTrue(id1.equals(id2));
        assertFalse(id1.equals(id3));
        assertFalse(id1.equals(id4));
        assertFalse(id1.equals(new Object()));
    }

    @Test
    public void testToString() {
        final var id = YangInstanceIdentifier.of(new NodeIdentifier(NODENAME1), new NodeIdentifier(NODENAME2));
        assertNotNull(id.toString());
    }

    @Test
    public void testNode() {
        final var id = YangInstanceIdentifier.of(new NodeIdentifier(NODENAME1), new NodeIdentifier(NODENAME2));

        var newID = id.node(NODENAME3);
        assertNotNull(newID);
        assertEquals(3, newID.getPathArguments().size());

        var it = newID.getPathArguments().iterator();
        assertEquals(NODENAME1, it.next().getNodeType());
        assertEquals(NODENAME2, it.next().getNodeType());
        assertEquals(NODENAME3, it.next().getNodeType());

        newID = id.node(new NodeIdentifier(NODENAME3));
        assertNotNull(newID);
        assertEquals(3, newID.getPathArguments().size());

        it = newID.getPathArguments().iterator();
        assertEquals(NODENAME1, it.next().getNodeType());
        assertEquals(NODENAME2, it.next().getNodeType());
        assertEquals(NODENAME3, it.next().getNodeType());
    }

    @Test
    public void testRelativeTo() {
        final YangInstanceIdentifier id1 = YangInstanceIdentifier.of(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME2), new NodeIdentifier(NODENAME3), new NodeIdentifier(NODENAME4));
        final YangInstanceIdentifier id2 = YangInstanceIdentifier.of(new NodeIdentifier(NODENAME1),
                new NodeIdentifier(NODENAME2));
        final YangInstanceIdentifier id3 = YangInstanceIdentifier.of(List.of(
                    new NodeIdentifier(NODENAME1), new NodeIdentifier(NODENAME2)));

        Optional<YangInstanceIdentifier> relative = id1.relativeTo(id2);
        assertTrue(relative.isPresent());

        List<PathArgument> path = relative.orElseThrow().getPathArguments();
        assertEquals(2, path.size());
        assertEquals(NODENAME3, path.get(0).getNodeType());
        assertEquals(NODENAME4, path.get(1).getNodeType());

        relative = id2.relativeTo(id3);
        assertTrue(relative.isPresent());
        assertEquals(0, relative.orElseThrow().getPathArguments().size());

        relative = id2.relativeTo(id1);
        assertFalse(relative.isPresent());
    }

    @Test
    public void testContainsNull() {
        final var id = YangInstanceIdentifier.of(NODENAME1);
        assertThrows(NullPointerException.class, () -> id.contains(null));
    }

    @Test
    public void testContains() {
        final var id1 = YangInstanceIdentifier.of(NODENAME1, NODENAME2, NODENAME3, NODENAME4);
        final var id2 = YangInstanceIdentifier.of(NODENAME1, NODENAME2);
        final var id3 = YangInstanceIdentifier.of(NODENAME1, NODENAME2);
        final var id4 = YangInstanceIdentifier.of(NODENAME1, NODENAME3);

        assertTrue(id2.contains(id1));
        assertTrue(id2.contains(id3));
        assertFalse(id1.contains(id2));
        assertFalse(id2.contains(id4));
    }

    @Test
    public void testOf() {
        final var newID = YangInstanceIdentifier.of(NODENAME1);
        assertNotNull(newID);
        assertEquals(1, newID.getPathArguments().size());
        assertEquals(NODENAME1, newID.getPathArguments().get(0).getNodeType());

        // for code coverage
        assertNotNull(newID.toString());
    }

    @Test
    public void testBuilder() {
        var newID = YangInstanceIdentifier.builder()
                .node(NODENAME1)
                .nodeWithKey(NODENAME2, Map.of(KEY1, "foo"))
                .nodeWithKey(NODENAME3, KEY2, "bar").build();

        assertNotNull(newID);
        assertEquals(3, newID.getPathArguments().size());

        var it = newID.getPathArguments().iterator();
        assertEquals(NODENAME1, it.next().getNodeType());
        verifyNodeIdentifierWithPredicates("PathArg 2", it.next(), NODENAME2, KEY1, "foo");
        verifyNodeIdentifierWithPredicates("PathArg 3", it.next(), NODENAME3, KEY2, "bar");

        newID = YangInstanceIdentifier.builder(newID).node(NODENAME4).build();

        assertNotNull(newID);
        assertEquals(4, newID.getPathArguments().size());

        it = newID.getPathArguments().iterator();
        assertEquals(NODENAME1, it.next().getNodeType());
        assertEquals(NODENAME2, it.next().getNodeType());
        assertEquals(NODENAME3, it.next().getNodeType());
        assertEquals(NODENAME4, it.next().getNodeType());

        newID = YangInstanceIdentifier.builder().node(NODENAME1).build();

        assertNotNull(newID);
        assertEquals(1, newID.getPathArguments().size());
        assertEquals(NODENAME1, newID.getPathArguments().get(0).getNodeType());
    }

    private static void verifyNodeIdentifierWithPredicates(final String prefix,
            final PathArgument arg, final QName nodeName, final QName key, final Object value) {

        assertNotNull(arg, prefix + " is null");
        assertInstanceOf(NodeIdentifierWithPredicates.class, arg);
        NodeIdentifierWithPredicates node = (NodeIdentifierWithPredicates)arg;
        assertEquals(nodeName, node.getNodeType(), prefix + " node type");
        assertEquals(1, node.size(), prefix + " key values map size");
        Entry<QName, Object> entry = node.entrySet().iterator().next();
        assertEquals(key, entry.getKey(), prefix + " key values map entry key");
        assertEquals(value, entry.getValue(), prefix + " key values map entry value");
    }

    @Test
    public void testNodeIdentifierWithPredicates() {

        NodeIdentifierWithPredicates node1 = NodeIdentifierWithPredicates.of(NODENAME1, KEY1, "foo");
        verifyNodeIdentifierWithPredicates("NodeIdentifierWithPredicates", node1, NODENAME1, KEY1, "foo");

        NodeIdentifierWithPredicates node2 = NodeIdentifierWithPredicates.of(NODENAME1, KEY1, "foo");

        assertEquals(node1.hashCode(), node2.hashCode());
        assertTrue(node1.equals(node2));

        assertFalse(node1.equals(NodeIdentifierWithPredicates.of(NODENAME2, KEY1, "foo")));
        assertFalse(node1.equals(NodeIdentifierWithPredicates.of(NODENAME1, KEY2, "foo")));
        assertFalse(node1.equals(NodeIdentifierWithPredicates.of(NODENAME1, KEY1, "bar")));
        assertFalse(node1.equals(new Object()));

        // for code coverage
        assertNotNull(node1.toString());
        assertNotNull(node1.toRelativeString(node2));

        NodeIdentifierWithPredicates node3 = NodeIdentifierWithPredicates.of(NODENAME1, Map.of(KEY1, 10, KEY2, 20));

        NodeIdentifierWithPredicates node4 = NodeIdentifierWithPredicates.of(NODENAME1, Map.of(KEY1, 10, KEY2, 20));

        assertEquals(node3.hashCode(), node4.hashCode());
        assertTrue(node3.equals(node4));

        assertFalse(node3.equals(node1));
        assertFalse(node1.equals(NodeIdentifierWithPredicates.of(NODENAME1, Map.of(KEY1, 10, KEY3, 20))));

        node1 = NodeIdentifierWithPredicates.of(NODENAME1, KEY1, new byte[]{ 1, 2 });
        node2 = NodeIdentifierWithPredicates.of(NODENAME1, KEY1, new byte[]{ 1, 2 });

        assertEquals(node1.hashCode(), node2.hashCode());
        assertTrue(node1.equals(node2));

        assertFalse(node1.equals(NodeIdentifierWithPredicates.of(NODENAME1, KEY1, new byte[]{ 1, 3})));
        assertFalse(node1.equals(NodeIdentifierWithPredicates.of(NODENAME1, KEY1, new byte[]{ 1 })));
        assertFalse(node1.equals(NodeIdentifierWithPredicates.of(NODENAME1, KEY1, new byte[]{ 1, 2, 3 })));
    }

    @Test
    public void testNodeWithValue() {

        NodeWithValue<?> node1 = new NodeWithValue<>(NODENAME1, "foo");
        assertEquals(NODENAME1, node1.getNodeType());
        assertEquals("foo", node1.getValue());

        NodeWithValue<?> node2 = new NodeWithValue<>(NODENAME1, "foo");

        assertEquals(node1.hashCode(), node2.hashCode());
        assertTrue(node1.equals(node2));

        assertFalse(node1.equals(new NodeWithValue<>(NODENAME1, "bar")));
        assertFalse(node1.equals(new NodeWithValue<>(NODENAME2, "foo")));
        assertFalse(node1.equals(new Object()));

        // for code coverage
        assertNotNull(node1.toString());
        assertNotNull(node1.toRelativeString(node2));

        NodeWithValue<?> node3 = new NodeWithValue<>(NODENAME1, new byte[]{1,2});
        NodeWithValue<?> node4 = new NodeWithValue<>(NODENAME1, new byte[]{1,2});

        assertEquals(node3.hashCode(), node4.hashCode());
        assertTrue(node3.equals(node4));

        assertFalse(node3.equals(new NodeWithValue<>(NODENAME1, new byte[]{1,3})));
        assertFalse(node3.equals(node1));
    }

    @Test
    public void testNodeIdentifier() {

        final NodeIdentifier node1 = new NodeIdentifier(NODENAME1);
        assertEquals(NODENAME1, node1.getNodeType());
        final NodeIdentifier node2 = new NodeIdentifier(NODENAME1);

        assertEquals(node1.hashCode(), node2.hashCode());
        assertEquals(0, node1.compareTo(node2));
        assertTrue(node1.compareTo(new NodeIdentifier(NODENAME3)) != 0);

        assertFalse(node1.equals(null));
        assertTrue(node1.equals(node1));
        assertTrue(node1.equals(node2));
        assertFalse(node1.equals(new NodeIdentifier(NODENAME3)));
        assertFalse(node1.equals(new Object()));

        // for code coverage
        assertNotNull(node1.toString());
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
        final var fixed = YangInstanceIdentifier.of(new NodeIdentifier(NODENAME1), new NodeIdentifier(NODENAME2));
        assertEquals(fixed, serdes(fixed));

        final var stacked = YangInstanceIdentifier.of().node(new NodeIdentifier(NODENAME1));
        assertEquals(stacked, serdes(stacked));

        final var empty = serdes(YangInstanceIdentifier.of());
        assertSame(YangInstanceIdentifier.of(), empty);
    }

    @Test
    public void testToOptimized() {
        final var fixed = YangInstanceIdentifier.of(new NodeIdentifier(NODENAME1), new NodeIdentifier(NODENAME2));
        final var stacked = YangInstanceIdentifier.of().node(NodeIdentifier.create(NODENAME1))
                .node(NodeIdentifier.create(NODENAME2));

        assertSame(fixed, fixed.toOptimized());

        final var opt = stacked.toOptimized();
        assertTrue(opt instanceof FixedYangInstanceIdentifier);
        assertEquals(fixed, stacked.toOptimized());
    }

    @Test
    public void testGetParent() {
        final var fixed = YangInstanceIdentifier.of(NODENAME1);
        final var stacked = YangInstanceIdentifier.of().node(new NodeIdentifier(NODENAME1));
        final var twoStacked = stacked.node(NODENAME2);

        assertNull(YangInstanceIdentifier.of().getParent());
        assertSame(YangInstanceIdentifier.of(), fixed.getParent());
        assertSame(YangInstanceIdentifier.of(), stacked.getParent());
        assertSame(stacked, twoStacked.getParent());
    }

    @Test
    public void testIsEmpty() {
        final YangInstanceIdentifier fixed = YangInstanceIdentifier.of(NODENAME1);
        final YangInstanceIdentifier stacked = YangInstanceIdentifier.of().node(NODENAME1);

        assertTrue(YangInstanceIdentifier.of().isEmpty());
        assertFalse(fixed.isEmpty());
        assertFalse(stacked.isEmpty());
    }
}
