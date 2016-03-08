/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public class AbstractImmutableNormalizedValueAttrNodeTest {

    private static QName ROOT_QNAME = QName.create("urn:test", "2014-03-13",
            "root");
    private static QName LEAF_QNAME = QName.create(ROOT_QNAME, "my-leaf");
    private static QName SAME_LEAF_QNAME = QName.create(ROOT_QNAME, "my-leaf");
    private static QName OTHER_LEAF_QNAME = QName.create(ROOT_QNAME,
            "my-other-leaf");

    @Test
    public void equalsByteTest() {

        byte[] valueNull = null;
        byte[] equalValueNull = null;

        LeafNode<byte[]> leafNodeNull = ImmutableNodes.leafNode(LEAF_QNAME,
                valueNull);
        LeafNode<byte[]> equalLeafNodeNull = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValueNull);

        assertEquals(leafNodeNull, leafNodeNull);
        assertEquals(leafNodeNull, equalLeafNodeNull);
        assertEquals(equalLeafNodeNull, leafNodeNull);

        byte[] value = "test".getBytes();
        byte[] equalValue = "test".getBytes();

        LeafNode<byte[]> leafNode = ImmutableNodes.leafNode(LEAF_QNAME, value);
        LeafNode<byte[]> equalLeafNode = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValue);

        assertEquals(leafNode, leafNode);
        assertEquals(leafNode, equalLeafNode);
        assertEquals(equalLeafNode, leafNode);

        Byte[] value2 = new Byte[] { new Byte("1"), new Byte("2") };
        Byte[] equalValue2 = new Byte[] { new Byte("1"), new Byte("2") };

        LeafNode<Byte[]> leafNode2 = ImmutableNodes
                .leafNode(LEAF_QNAME, value2);
        LeafNode<Byte[]> equalLeafNode2 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValue2);

        assertEquals(leafNode2, leafNode2);
        assertEquals(leafNode2, equalLeafNode2);
        assertEquals(equalLeafNode2, leafNode2);

        byte[][] value3 = new byte[][] { "test".getBytes(), "test2".getBytes() };
        byte[][] equalValue3 = new byte[][] { "test".getBytes(),
                "test2".getBytes() };

        LeafNode<byte[][]> leafNode3 = ImmutableNodes.leafNode(LEAF_QNAME,
                value3);
        LeafNode<byte[][]> equalLeafNode3 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValue3);

        assertEquals(leafNode3, leafNode3);
        assertEquals(leafNode3, equalLeafNode3);
        assertEquals(equalLeafNode3, leafNode3);

        Byte[][] value4 = new Byte[][] {
                new Byte[] { new Byte("1"), new Byte("2") },
                new Byte[] { new Byte("3"), new Byte("4") } };
        Byte[][] equalValue4 = new Byte[][] {
                new Byte[] { new Byte("1"), new Byte("2") },
                new Byte[] { new Byte("3"), new Byte("4") } };

        LeafNode<Byte[][]> leafNode4 = ImmutableNodes.leafNode(LEAF_QNAME,
                value4);
        LeafNode<Byte[][]> equalLeafNode4 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValue4);

        assertEquals(leafNode4, leafNode4);
        assertEquals(leafNode4, equalLeafNode4);
        assertEquals(equalLeafNode4, leafNode4);

        Byte value6 = new Byte("1");
        Byte equalValue6 = new Byte("1");

        LeafNode<Byte> leafNode6 = ImmutableNodes.leafNode(LEAF_QNAME, value6);
        LeafNode<Byte> equalLeafNode6 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValue6);

        assertEquals(leafNode6, leafNode6);
        assertEquals(leafNode6, equalLeafNode6);
        assertEquals(equalLeafNode6, leafNode6);

        String value5 = new String("test");
        String equalValue5 = new String("test");

        LeafNode<String> leafNode5 = ImmutableNodes
                .leafNode(LEAF_QNAME, value5);
        LeafNode<String> equalLeafNode5 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValue5);

        assertEquals(leafNode5, leafNode5);
        assertEquals(leafNode5, equalLeafNode5);
        assertEquals(equalLeafNode5, leafNode5);

    }

    @Test
    public void notEqualByteTest() {

        byte[] value = "test".getBytes();
        byte[] equalValue = "test".getBytes();

        LeafNode<byte[]> leafNode = ImmutableNodes.leafNode(LEAF_QNAME, value);
        LeafNode<byte[]> otherLeafNode = ImmutableNodes.leafNode(
                OTHER_LEAF_QNAME, equalValue);

        assertNotEquals(leafNode, null);
        assertNotEquals(leafNode, new Object());
        assertNotEquals(leafNode, otherLeafNode);
        assertNotEquals(otherLeafNode, leafNode);

        byte[] value1 = "test".getBytes();
        byte[] otherValue1 = "test1".getBytes();

        LeafNode<byte[]> leafNode1 = ImmutableNodes
                .leafNode(LEAF_QNAME, value1);
        LeafNode<byte[]> otherLeafNode1 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, otherValue1);

        assertNotEquals(leafNode1, otherLeafNode1);
        assertNotEquals(otherLeafNode1, leafNode1);

        Byte[] value2 = new Byte[] { new Byte("1"), new Byte("1") };
        Byte[] otherValue2 = new Byte[] { new Byte("1"), new Byte("2") };

        LeafNode<Byte[]> leafNode2 = ImmutableNodes
                .leafNode(LEAF_QNAME, value2);
        LeafNode<Byte[]> otherLeafNode2 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, otherValue2);

        assertNotEquals(leafNode2, otherLeafNode2);
        assertNotEquals(otherLeafNode2, leafNode2);

        byte[][] value3 = new byte[][] { "test".getBytes(), "test2".getBytes() };
        byte[][] otherValue3 = new byte[][] { "test".getBytes(),
                "test3".getBytes() };

        LeafNode<byte[][]> leafNode3 = ImmutableNodes.leafNode(LEAF_QNAME,
                value3);
        LeafNode<byte[][]> otherLeafNode3 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, otherValue3);

        assertNotEquals(leafNode3, otherLeafNode3);
        assertNotEquals(otherLeafNode3, leafNode3);

        Byte[][] value4 = new Byte[][] {
                new Byte[] { new Byte("1"), new Byte("2") },
                new Byte[] { new Byte("3"), new Byte("4") } };
        Byte[][] otherValue4 = new Byte[][] {
                new Byte[] { new Byte("1"), new Byte("2") },
                new Byte[] { new Byte("3"), new Byte("5") } };

        LeafNode<Byte[][]> leafNode4 = ImmutableNodes.leafNode(LEAF_QNAME,
                value4);
        LeafNode<Byte[][]> otherLeafNode4 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, otherValue4);

        assertNotEquals(leafNode4, otherLeafNode4);
        assertNotEquals(otherLeafNode4, leafNode4);

        Byte value6 = new Byte("1");
        Byte otherValue6 = new Byte("2");

        LeafNode<Byte> leafNode6 = ImmutableNodes.leafNode(LEAF_QNAME, value6);
        LeafNode<Byte> otherLeafNode6 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, otherValue6);

        assertNotEquals(leafNode6, otherLeafNode6);
        assertNotEquals(otherLeafNode6, leafNode6);

        String value5 = new String("test");
        String otherValue5 = new String("test2");

        LeafNode<String> leafNode5 = ImmutableNodes
                .leafNode(LEAF_QNAME, value5);
        LeafNode<String> otherLeafNode5 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, otherValue5);

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

        byte[] valueNull = null;

        LeafNode<byte[]> leafNodeNull = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, valueNull);
        assertNotEquals(leafNodeNull, leafNode);
        assertNotEquals(leafNode, leafNodeNull);

        byte[] byteValue = new byte[] { new Byte("1").byteValue(),
                new Byte("1").byteValue() };

        LeafNode<byte[]> byteLeafNode = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, byteValue);
        assertNotEquals(byteLeafNode, leafNode2);
        assertNotEquals(leafNode2, byteLeafNode);

    }

    @Test
    public void equalsOtherTypesTest() {

        char[] valueChar = "test".toCharArray();
        char[] equalValueChar = "test".toCharArray();

        LeafNode<char[]> leafNodeChar = ImmutableNodes.leafNode(LEAF_QNAME,
                valueChar);
        LeafNode<char[]> equalLeafNodeChar = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValueChar);

        assertEquals(leafNodeChar, leafNodeChar);
        assertEquals(leafNodeChar, equalLeafNodeChar);
        assertEquals(equalLeafNodeChar, leafNodeChar);

        boolean[] value = new boolean[] { true, false };
        boolean[] equalValue = new boolean[] { true, false };

        LeafNode<boolean[]> leafNode = ImmutableNodes.leafNode(LEAF_QNAME,
                value);
        LeafNode<boolean[]> equalLeafNode = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValue);

        assertEquals(leafNode, leafNode);
        assertEquals(leafNode, equalLeafNode);
        assertEquals(equalLeafNode, leafNode);

        int[] value2 = new int[] { 1, 2 };
        int[] equalValue2 = new int[] { 1, 2 };

        LeafNode<int[]> leafNode2 = ImmutableNodes.leafNode(LEAF_QNAME, value2);
        LeafNode<int[]> equalLeafNode2 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValue2);

        assertEquals(leafNode2, leafNode2);
        assertEquals(leafNode2, equalLeafNode2);
        assertEquals(equalLeafNode2, leafNode2);

        short[] value3 = new short[] { 1, 2 };
        short[] equalValue3 = new short[] { 1, 2 };

        LeafNode<short[]> leafNode3 = ImmutableNodes.leafNode(LEAF_QNAME,
                value3);
        LeafNode<short[]> equalLeafNode3 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValue3);

        assertEquals(leafNode3, leafNode3);
        assertEquals(leafNode3, equalLeafNode3);
        assertEquals(equalLeafNode3, leafNode3);

        long[] value4 = new long[] { 1, 2 };
        long[] equalValue4 = new long[] { 1, 2 };

        LeafNode<long[]> leafNode4 = ImmutableNodes
                .leafNode(LEAF_QNAME, value4);
        LeafNode<long[]> equalLeafNode4 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValue4);

        assertEquals(leafNode4, leafNode4);
        assertEquals(leafNode4, equalLeafNode4);
        assertEquals(equalLeafNode4, leafNode4);

        double[] value6 = new double[] { 1, 2 };
        double[] equalValue6 = new double[] { 1, 2 };

        LeafNode<double[]> leafNode6 = ImmutableNodes.leafNode(LEAF_QNAME,
                value6);
        LeafNode<double[]> equalLeafNode6 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValue6);

        assertEquals(leafNode6, leafNode6);
        assertEquals(leafNode6, equalLeafNode6);
        assertEquals(equalLeafNode6, leafNode6);

        float[] value5 = new float[] { 1, 2 };
        float[] equalValue5 = new float[] { 1, 2 };

        LeafNode<float[]> leafNode5 = ImmutableNodes.leafNode(LEAF_QNAME,
                value5);
        LeafNode<float[]> equalLeafNode5 = ImmutableNodes.leafNode(
                SAME_LEAF_QNAME, equalValue5);

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
