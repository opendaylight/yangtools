/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

class AbstractImmutableNormalizedValueAttrNodeTest {
    private record TestValue(int value) {
        // Simple enough
    }

    private static final QName ROOT_QNAME = QName.create("urn:test", "2014-03-13", "root");
    private static final QName LEAF_QNAME = QName.create(ROOT_QNAME, "my-leaf");
    private static final QName SAME_LEAF_QNAME = QName.create(ROOT_QNAME, "my-leaf");
    private static final QName OTHER_LEAF_QNAME = QName.create(ROOT_QNAME, "my-other-leaf");

    @Test
    // This test is based on using different references; we're testing equals()
    @SuppressWarnings({"RedundantStringConstructorCall", "EqualsWithItself"})
    void equalsByteTest() {
        final var value = "test".getBytes();
        final var equalValue = "test".getBytes();

        final var leafNode = ImmutableNodes.leafNode(LEAF_QNAME, value);
        final var equalLeafNode = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue);

        assertEquals(leafNode, leafNode);
        assertEquals(leafNode, equalLeafNode);
        assertEquals(equalLeafNode, leafNode);

        final var value2 = new TestValue[] { new TestValue(1), new TestValue(2) };
        final var equalValue2 = new TestValue[] { new TestValue(1), new TestValue(2) };

        final var leafNode2 = ImmutableNodes.leafNode(LEAF_QNAME, value2);
        final var equalLeafNode2 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue2);

        assertEquals(leafNode2, leafNode2);
        assertEquals(leafNode2, equalLeafNode2);
        assertEquals(equalLeafNode2, leafNode2);

        final var value3 = new byte[][] { "test".getBytes(), "test2".getBytes() };
        final var equalValue3 = new byte[][] { "test".getBytes(), "test2".getBytes() };

        final var leafNode3 = ImmutableNodes.leafNode(LEAF_QNAME,
                value3);
        final var equalLeafNode3 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue3);

        assertEquals(leafNode3, leafNode3);
        assertEquals(leafNode3, equalLeafNode3);
        assertEquals(equalLeafNode3, leafNode3);

        final var value4 = new TestValue[][] {
            new TestValue[] { new TestValue(1), new TestValue(2) },
            new TestValue[] { new TestValue(3), new TestValue(4) },
        };
        final var equalValue4 = new TestValue[][] {
            new TestValue[] { new TestValue(1), new TestValue(2) },
            new TestValue[] { new TestValue(3), new TestValue(4) },
        };

        final var leafNode4 = ImmutableNodes.leafNode(LEAF_QNAME,value4);
        final var equalLeafNode4 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue4);

        assertEquals(leafNode4, leafNode4);
        assertEquals(leafNode4, equalLeafNode4);
        assertEquals(equalLeafNode4, leafNode4);

        final var value6 = new TestValue(1);
        final var equalValue6 = new TestValue(1);

        final var leafNode6 = ImmutableNodes.leafNode(LEAF_QNAME, value6);
        final var equalLeafNode6 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue6);

        assertEquals(leafNode6, leafNode6);
        assertEquals(leafNode6, equalLeafNode6);
        assertEquals(equalLeafNode6, leafNode6);

        final var value5 = "test";
        final var equalValue5 = new String("test");

        final var leafNode5 = ImmutableNodes.leafNode(LEAF_QNAME, value5);
        final var equalLeafNode5 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue5);

        assertEquals(leafNode5, leafNode5);
        assertEquals(leafNode5, equalLeafNode5);
        assertEquals(equalLeafNode5, leafNode5);
    }

    @Test
    // We're testing equals()
    @SuppressWarnings({"ObjectEqualsNull", "EqualsBetweenInconvertibleTypes"})
    void notEqualByteTest() {

        final var value = "test".getBytes();
        final var equalValue = "test".getBytes();

        final var leafNode = ImmutableNodes.leafNode(LEAF_QNAME, value);
        final var otherLeafNode = ImmutableNodes.leafNode(OTHER_LEAF_QNAME, equalValue);

        assertNotEquals(null, leafNode);
        assertNotEquals(leafNode, new Object());
        assertNotEquals(leafNode, otherLeafNode);
        assertNotEquals(otherLeafNode, leafNode);

        final var value1 = "test".getBytes();
        final var otherValue1 = "test1".getBytes();

        final var leafNode1 = ImmutableNodes.leafNode(LEAF_QNAME, value1);
        final var otherLeafNode1 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, otherValue1);

        assertNotEquals(leafNode1, otherLeafNode1);
        assertNotEquals(otherLeafNode1, leafNode1);

        final var value2 = new TestValue[] { new TestValue(1), new TestValue(1) };
        final var otherValue2 = new TestValue[] { new TestValue(1), new TestValue(2) };

        final var leafNode2 = ImmutableNodes.leafNode(LEAF_QNAME, value2);
        final var otherLeafNode2 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, otherValue2);

        assertNotEquals(leafNode2, otherLeafNode2);
        assertNotEquals(otherLeafNode2, leafNode2);

        final var value3 = new byte[][] { "test".getBytes(), "test2".getBytes() };
        final var otherValue3 = new byte[][] { "test".getBytes(), "test3".getBytes() };

        final var leafNode3 = ImmutableNodes.leafNode(LEAF_QNAME, value3);
        final var otherLeafNode3 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, otherValue3);

        assertNotEquals(leafNode3, otherLeafNode3);
        assertNotEquals(otherLeafNode3, leafNode3);

        final var value4 = new TestValue[][] {
            new TestValue[] { new TestValue(1), new TestValue(2) },
            new TestValue[] { new TestValue(3), new TestValue(4) },
        };
        final var otherValue4 = new TestValue[][] {
            new TestValue[] { new TestValue(1), new TestValue(2) },
            new TestValue[] { new TestValue(3), new TestValue(5) },
        };

        final var leafNode4 = ImmutableNodes.leafNode(LEAF_QNAME, value4);
        final var otherLeafNode4 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, otherValue4);

        assertNotEquals(leafNode4, otherLeafNode4);
        assertNotEquals(otherLeafNode4, leafNode4);

        final var value6 = new TestValue(1);
        final var otherValue6 = new TestValue(2);

        final var leafNode6 = ImmutableNodes.leafNode(LEAF_QNAME, value6);
        final var otherLeafNode6 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, otherValue6);

        assertNotEquals(leafNode6, otherLeafNode6);
        assertNotEquals(otherLeafNode6, leafNode6);

        final var value5 = "test";
        final var otherValue5 = "test2";

        final var leafNode5 = ImmutableNodes.leafNode(LEAF_QNAME, value5);
        final var otherLeafNode5 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, otherValue5);

        assertNotEquals(leafNode5, otherLeafNode5);
        assertNotEquals(otherLeafNode5, leafNode5);
        assertNotEquals(leafNode5, leafNode);
        assertNotEquals(leafNode5, leafNode1);
        assertNotEquals(leafNode5, leafNode2);
        assertNotEquals(leafNode5, leafNode3);
        assertNotEquals(leafNode5, leafNode4);
        assertNotEquals(leafNode5, leafNode6);
        assertNotEquals(leafNode, leafNode5);
        assertNotEquals(leafNode1, leafNode5);
        assertNotEquals(leafNode2, leafNode5);
        assertNotEquals(leafNode3, leafNode5);
        assertNotEquals(leafNode4, leafNode5);
        assertNotEquals(leafNode6, leafNode5);

        final var byteValue = new byte[] { 1, 1 };

        final var byteLeafNode = ImmutableNodes.leafNode(SAME_LEAF_QNAME, byteValue);
        assertNotEquals(byteLeafNode, leafNode2);
        assertNotEquals(leafNode2, byteLeafNode);
    }

    @Test
    // We're testing equals()
    @SuppressWarnings({"EqualsWithItself", "EqualsBetweenInconvertibleTypes"})
    void equalsOtherTypesTest() {

        final var valueChar = "test".toCharArray();
        final var equalValueChar = "test".toCharArray();

        final var leafNodeChar = ImmutableNodes.leafNode(LEAF_QNAME, valueChar);
        final var equalLeafNodeChar = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValueChar);

        assertEquals(leafNodeChar, leafNodeChar);
        assertEquals(leafNodeChar, equalLeafNodeChar);
        assertEquals(equalLeafNodeChar, leafNodeChar);

        final var value = new boolean[] { true, false };
        final var equalValue = new boolean[] { true, false };

        final var leafNode = ImmutableNodes.leafNode(LEAF_QNAME, value);
        final var equalLeafNode = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue);

        assertEquals(leafNode, leafNode);
        assertEquals(leafNode, equalLeafNode);
        assertEquals(equalLeafNode, leafNode);

        final var value2 = new int[] { 1, 2 };
        final var equalValue2 = new int[] { 1, 2 };

        final var leafNode2 = ImmutableNodes.leafNode(LEAF_QNAME, value2);
        final var equalLeafNode2 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue2);

        assertEquals(leafNode2, leafNode2);
        assertEquals(leafNode2, equalLeafNode2);
        assertEquals(equalLeafNode2, leafNode2);

        final var value3 = new short[] { 1, 2 };
        final var equalValue3 = new short[] { 1, 2 };

        final var leafNode3 = ImmutableNodes.leafNode(LEAF_QNAME, value3);
        final var equalLeafNode3 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue3);

        assertEquals(leafNode3, leafNode3);
        assertEquals(leafNode3, equalLeafNode3);
        assertEquals(equalLeafNode3, leafNode3);

        final var value4 = new long[] { 1, 2 };
        final var equalValue4 = new long[] { 1, 2 };

        final var leafNode4 = ImmutableNodes.leafNode(LEAF_QNAME, value4);
        final var equalLeafNode4 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue4);

        assertEquals(leafNode4, leafNode4);
        assertEquals(leafNode4, equalLeafNode4);
        assertEquals(equalLeafNode4, leafNode4);

        final var value6 = new double[] { 1, 2 };
        final var equalValue6 = new double[] { 1, 2 };

        final var leafNode6 = ImmutableNodes.leafNode(LEAF_QNAME, value6);
        final var equalLeafNode6 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue6);

        assertEquals(leafNode6, leafNode6);
        assertEquals(leafNode6, equalLeafNode6);
        assertEquals(equalLeafNode6, leafNode6);

        final var value5 = new float[] { 1, 2 };
        final var equalValue5 = new float[] { 1, 2 };

        final var leafNode5 = ImmutableNodes.leafNode(LEAF_QNAME, value5);
        final var equalLeafNode5 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue5);

        assertEquals(leafNode5, leafNode5);
        assertEquals(leafNode5, equalLeafNode5);
        assertEquals(equalLeafNode5, leafNode5);

        assertNotEquals(leafNode, leafNode5);
        assertNotEquals(leafNode2, leafNode5);
        assertNotEquals(leafNode3, leafNode5);
        assertNotEquals(leafNode4, leafNode5);
        assertNotEquals(leafNodeChar, leafNode5);
        assertNotEquals(leafNode6, leafNode5);

        assertNotEquals(leafNode5, leafNode);
        assertNotEquals(leafNode5, leafNode2);
        assertNotEquals(leafNode5, leafNode3);
        assertNotEquals(leafNode5, leafNode4);
        assertNotEquals(leafNode5, leafNodeChar);
        assertNotEquals(leafNode5, leafNode6);
    }
}
