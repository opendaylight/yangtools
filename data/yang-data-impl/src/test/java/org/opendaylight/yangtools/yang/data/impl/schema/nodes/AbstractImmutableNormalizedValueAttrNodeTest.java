/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public class AbstractImmutableNormalizedValueAttrNodeTest {
    // FIXME: Record once we have JDK17
    private static final class TestValue {
        private final int value;

        TestValue(final int value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public boolean equals(final Object obj) {
            return obj == this || obj instanceof TestValue && value == ((TestValue) obj).value;
        }
    }

    private static final QName ROOT_QNAME = QName.create("urn:test", "2014-03-13", "root");
    private static final QName LEAF_QNAME = QName.create(ROOT_QNAME, "my-leaf");
    private static final QName SAME_LEAF_QNAME = QName.create(ROOT_QNAME, "my-leaf");
    private static final QName OTHER_LEAF_QNAME = QName.create(ROOT_QNAME, "my-other-leaf");

    @Test
    // This test is based on using different references; we're testing equals()
    @SuppressWarnings({"RedundantStringConstructorCall", "EqualsWithItself"})
    public void equalsByteTest() {
        byte[] value = "test".getBytes();
        byte[] equalValue = "test".getBytes();

        LeafNode<byte[]> leafNode = ImmutableNodes.leafNode(LEAF_QNAME, value);
        LeafNode<byte[]> equalLeafNode = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue);

        assertTrue(leafNode.equals(leafNode));
        assertTrue(leafNode.equals(equalLeafNode));
        assertTrue(equalLeafNode.equals(leafNode));

        TestValue[] value2 = new TestValue[] { new TestValue(1), new TestValue(2) };
        TestValue[] equalValue2 = new TestValue[] { new TestValue(1), new TestValue(2) };

        LeafNode<TestValue[]> leafNode2 = ImmutableNodes.leafNode(LEAF_QNAME, value2);
        LeafNode<TestValue[]> equalLeafNode2 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue2);

        assertTrue(leafNode2.equals(leafNode2));
        assertTrue(leafNode2.equals(equalLeafNode2));
        assertTrue(equalLeafNode2.equals(leafNode2));

        byte[][] value3 = new byte[][] { "test".getBytes(), "test2".getBytes() };
        byte[][] equalValue3 = new byte[][] { "test".getBytes(), "test2".getBytes() };

        LeafNode<byte[][]> leafNode3 = ImmutableNodes.leafNode(LEAF_QNAME,
                value3);
        LeafNode<byte[][]> equalLeafNode3 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue3);

        assertTrue(leafNode3.equals(leafNode3));
        assertTrue(leafNode3.equals(equalLeafNode3));
        assertTrue(equalLeafNode3.equals(leafNode3));

        TestValue[][] value4 = new TestValue[][] {
            new TestValue[] { new TestValue(1), new TestValue(2) },
            new TestValue[] { new TestValue(3), new TestValue(4) },
        };
        TestValue[][] equalValue4 = new TestValue[][] {
            new TestValue[] { new TestValue(1), new TestValue(2) },
            new TestValue[] { new TestValue(3), new TestValue(4) },
        };

        LeafNode<TestValue[][]> leafNode4 = ImmutableNodes.leafNode(LEAF_QNAME,value4);
        LeafNode<TestValue[][]> equalLeafNode4 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue4);

        assertTrue(leafNode4.equals(leafNode4));
        assertTrue(leafNode4.equals(equalLeafNode4));
        assertTrue(equalLeafNode4.equals(leafNode4));

        TestValue value6 = new TestValue(1);
        TestValue equalValue6 = new TestValue(1);

        LeafNode<TestValue> leafNode6 = ImmutableNodes.leafNode(LEAF_QNAME, value6);
        LeafNode<TestValue> equalLeafNode6 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue6);

        assertTrue(leafNode6.equals(leafNode6));
        assertTrue(leafNode6.equals(equalLeafNode6));
        assertTrue(equalLeafNode6.equals(leafNode6));

        String value5 = "test";
        String equalValue5 = new String("test");

        LeafNode<String> leafNode5 = ImmutableNodes.leafNode(LEAF_QNAME, value5);
        LeafNode<String> equalLeafNode5 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue5);

        assertTrue(leafNode5.equals(leafNode5));
        assertTrue(leafNode5.equals(equalLeafNode5));
        assertTrue(equalLeafNode5.equals(leafNode5));
    }

    @Test
    // We're testing equals()
    @SuppressWarnings({"ObjectEqualsNull", "EqualsBetweenInconvertibleTypes"})
    public void notEqualByteTest() {

        byte[] value = "test".getBytes();
        byte[] equalValue = "test".getBytes();

        LeafNode<byte[]> leafNode = ImmutableNodes.leafNode(LEAF_QNAME, value);
        LeafNode<byte[]> otherLeafNode = ImmutableNodes.leafNode(OTHER_LEAF_QNAME, equalValue);

        assertFalse(leafNode.equals(null));
        assertFalse(leafNode.equals(new Object()));
        assertFalse(leafNode.equals(otherLeafNode));
        assertFalse(otherLeafNode.equals(leafNode));

        byte[] value1 = "test".getBytes();
        byte[] otherValue1 = "test1".getBytes();

        LeafNode<byte[]> leafNode1 = ImmutableNodes.leafNode(LEAF_QNAME, value1);
        LeafNode<byte[]> otherLeafNode1 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, otherValue1);

        assertFalse(leafNode1.equals(otherLeafNode1));
        assertFalse(otherLeafNode1.equals(leafNode1));

        TestValue[] value2 = new TestValue[] { new TestValue(1), new TestValue(1) };
        TestValue[] otherValue2 = new TestValue[] { new TestValue(1), new TestValue(2) };

        LeafNode<TestValue[]> leafNode2 = ImmutableNodes.leafNode(LEAF_QNAME, value2);
        LeafNode<TestValue[]> otherLeafNode2 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, otherValue2);

        assertFalse(leafNode2.equals(otherLeafNode2));
        assertFalse(otherLeafNode2.equals(leafNode2));

        byte[][] value3 = new byte[][] { "test".getBytes(), "test2".getBytes() };
        byte[][] otherValue3 = new byte[][] { "test".getBytes(), "test3".getBytes() };

        LeafNode<byte[][]> leafNode3 = ImmutableNodes.leafNode(LEAF_QNAME, value3);
        LeafNode<byte[][]> otherLeafNode3 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, otherValue3);

        assertFalse(leafNode3.equals(otherLeafNode3));
        assertFalse(otherLeafNode3.equals(leafNode3));

        TestValue[][] value4 = new TestValue[][] {
            new TestValue[] { new TestValue(1), new TestValue(2) },
            new TestValue[] { new TestValue(3), new TestValue(4) },
        };
        TestValue[][] otherValue4 = new TestValue[][] {
            new TestValue[] { new TestValue(1), new TestValue(2) },
            new TestValue[] { new TestValue(3), new TestValue(5) },
        };

        LeafNode<TestValue[][]> leafNode4 = ImmutableNodes.leafNode(LEAF_QNAME, value4);
        LeafNode<TestValue[][]> otherLeafNode4 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, otherValue4);

        assertFalse(leafNode4.equals(otherLeafNode4));
        assertFalse(otherLeafNode4.equals(leafNode4));

        TestValue value6 = new TestValue(1);
        TestValue otherValue6 = new TestValue(2);

        LeafNode<TestValue> leafNode6 = ImmutableNodes.leafNode(LEAF_QNAME, value6);
        LeafNode<TestValue> otherLeafNode6 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, otherValue6);

        assertFalse(leafNode6.equals(otherLeafNode6));
        assertFalse(otherLeafNode6.equals(leafNode6));

        String value5 = "test";
        String otherValue5 = "test2";

        LeafNode<String> leafNode5 = ImmutableNodes.leafNode(LEAF_QNAME, value5);
        LeafNode<String> otherLeafNode5 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, otherValue5);

        assertFalse(leafNode5.equals(otherLeafNode5));
        assertFalse(otherLeafNode5.equals(leafNode5));
        assertFalse(leafNode5.equals(leafNode));
        assertFalse(leafNode5.equals(leafNode1));
        assertFalse(leafNode5.equals(leafNode2));
        assertFalse(leafNode5.equals(leafNode3));
        assertFalse(leafNode5.equals(leafNode4));
        assertFalse(leafNode5.equals(leafNode6));
        assertFalse(leafNode.equals(leafNode5));
        assertFalse(leafNode1.equals(leafNode5));
        assertFalse(leafNode2.equals(leafNode5));
        assertFalse(leafNode3.equals(leafNode5));
        assertFalse(leafNode4.equals(leafNode5));
        assertFalse(leafNode6.equals(leafNode5));

        byte[] byteValue = new byte[] { 1, 1 };

        LeafNode<byte[]> byteLeafNode = ImmutableNodes.leafNode(SAME_LEAF_QNAME, byteValue);
        assertFalse(byteLeafNode.equals(leafNode2));
        assertFalse(leafNode2.equals(byteLeafNode));
    }

    @Test
    // We're testing equals()
    @SuppressWarnings({"EqualsWithItself", "EqualsBetweenInconvertibleTypes"})
    public void equalsOtherTypesTest() {

        char[] valueChar = "test".toCharArray();
        char[] equalValueChar = "test".toCharArray();

        LeafNode<char[]> leafNodeChar = ImmutableNodes.leafNode(LEAF_QNAME, valueChar);
        LeafNode<char[]> equalLeafNodeChar = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValueChar);

        assertTrue(leafNodeChar.equals(leafNodeChar));
        assertTrue(leafNodeChar.equals(equalLeafNodeChar));
        assertTrue(equalLeafNodeChar.equals(leafNodeChar));

        boolean[] value = new boolean[] { true, false };
        boolean[] equalValue = new boolean[] { true, false };

        LeafNode<boolean[]> leafNode = ImmutableNodes.leafNode(LEAF_QNAME, value);
        LeafNode<boolean[]> equalLeafNode = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue);

        assertTrue(leafNode.equals(leafNode));
        assertTrue(leafNode.equals(equalLeafNode));
        assertTrue(equalLeafNode.equals(leafNode));

        int[] value2 = new int[] { 1, 2 };
        int[] equalValue2 = new int[] { 1, 2 };

        LeafNode<int[]> leafNode2 = ImmutableNodes.leafNode(LEAF_QNAME, value2);
        LeafNode<int[]> equalLeafNode2 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue2);

        assertTrue(leafNode2.equals(leafNode2));
        assertTrue(leafNode2.equals(equalLeafNode2));
        assertTrue(equalLeafNode2.equals(leafNode2));

        short[] value3 = new short[] { 1, 2 };
        short[] equalValue3 = new short[] { 1, 2 };

        LeafNode<short[]> leafNode3 = ImmutableNodes.leafNode(LEAF_QNAME, value3);
        LeafNode<short[]> equalLeafNode3 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue3);

        assertTrue(leafNode3.equals(leafNode3));
        assertTrue(leafNode3.equals(equalLeafNode3));
        assertTrue(equalLeafNode3.equals(leafNode3));

        long[] value4 = new long[] { 1, 2 };
        long[] equalValue4 = new long[] { 1, 2 };

        LeafNode<long[]> leafNode4 = ImmutableNodes.leafNode(LEAF_QNAME, value4);
        LeafNode<long[]> equalLeafNode4 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue4);

        assertTrue(leafNode4.equals(leafNode4));
        assertTrue(leafNode4.equals(equalLeafNode4));
        assertTrue(equalLeafNode4.equals(leafNode4));

        double[] value6 = new double[] { 1, 2 };
        double[] equalValue6 = new double[] { 1, 2 };

        LeafNode<double[]> leafNode6 = ImmutableNodes.leafNode(LEAF_QNAME, value6);
        LeafNode<double[]> equalLeafNode6 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue6);

        assertTrue(leafNode6.equals(leafNode6));
        assertTrue(leafNode6.equals(equalLeafNode6));
        assertTrue(equalLeafNode6.equals(leafNode6));

        float[] value5 = new float[] { 1, 2 };
        float[] equalValue5 = new float[] { 1, 2 };

        LeafNode<float[]> leafNode5 = ImmutableNodes.leafNode(LEAF_QNAME, value5);
        LeafNode<float[]> equalLeafNode5 = ImmutableNodes.leafNode(SAME_LEAF_QNAME, equalValue5);

        assertTrue(leafNode5.equals(leafNode5));
        assertTrue(leafNode5.equals(equalLeafNode5));
        assertTrue(equalLeafNode5.equals(leafNode5));

        assertFalse(leafNode.equals(leafNode5));
        assertFalse(leafNode2.equals(leafNode5));
        assertFalse(leafNode3.equals(leafNode5));
        assertFalse(leafNode4.equals(leafNode5));
        assertFalse(leafNodeChar.equals(leafNode5));
        assertFalse(leafNode6.equals(leafNode5));

        assertFalse(leafNode5.equals(leafNode));
        assertFalse(leafNode5.equals(leafNode2));
        assertFalse(leafNode5.equals(leafNode3));
        assertFalse(leafNode5.equals(leafNode4));
        assertFalse(leafNode5.equals(leafNodeChar));
        assertFalse(leafNode5.equals(leafNode6));
    }
}
