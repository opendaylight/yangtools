/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

@ExtendWith(MockitoExtension.class)
class NormalizedNodesTest {
    @Mock
    private ContainerNode mockedDataContainerNode;
    @Mock
    private ContainerNode mockedContainerNode;
    @Mock
    private LeafNode<?> mockedLeafNode;
    @Mock
    private LeafSetEntryNode<?> mockedLeafSetEntryNode;
    @Mock
    private SystemMapNode mockedMapNode;
    @Mock
    private MapEntryNode mockedMapEntryNode;
    @Mock
    private SystemLeafSetNode<?> mockedLeafSetNode;

    @Test
    void testGetDirectChild() {
        final var mockedPathArgument = new NodeIdentifier(QName.create("test", "test"));

        assertEquals(Optional.empty(), NormalizedNodes.getDirectChild(mockedLeafNode, mockedPathArgument));

        assertEquals(Optional.empty(), NormalizedNodes.getDirectChild(mockedLeafSetEntryNode, mockedPathArgument));

        doReturn(mockedContainerNode).when(mockedDataContainerNode).childByArg(any());

        assertEquals(Optional.of(mockedContainerNode),
            NormalizedNodes.getDirectChild(mockedDataContainerNode, mockedPathArgument));

        final QName listQName = QName.create("test-ns", "test-list");
        final QName listKeyQName = QName.create("test-ns", "test-list-key");
        final var nodeIdentifierWithPredicates = NodeIdentifierWithPredicates.of(listQName, listKeyQName, "str-value");
        doReturn(mockedMapEntryNode).when(mockedMapNode).childByArg(any(NodeIdentifierWithPredicates.class));

        assertEquals(Optional.of(mockedMapEntryNode),
            NormalizedNodes.getDirectChild(mockedMapNode, nodeIdentifierWithPredicates));
        assertEquals(Optional.empty(), NormalizedNodes.getDirectChild(mockedMapNode, mockedPathArgument));

        final QName leafListQName = QName.create("test-ns", "test-leaf-list");
        final NodeWithValue<?> nodeWithValue = new NodeWithValue<>(leafListQName, "str-value");
        doReturn(mockedLeafSetEntryNode).when(mockedLeafSetNode).childByArg(any(NodeWithValue.class));
        assertEquals(Optional.of(mockedLeafSetEntryNode),
            NormalizedNodes.getDirectChild(mockedLeafSetNode, nodeWithValue));
    }

    @Test
    void testFindNode() {
        doReturn(mockedContainerNode).when(mockedDataContainerNode).childByArg(any());
        doReturn(mockedLeafNode).when(mockedContainerNode).childByArg(any());

        final QName node1QName = QName.create("test-ns", "2016-09-16", "node1");
        final QName node2Qname = QName.create("test-ns", "2016-09-16", "node2");
        final QName node3QName = QName.create("test-ns", "2016-09-16", "node3");
        final QName node4Qname = QName.create("test-ns", "2016-09-16", "node4");

        final YangInstanceIdentifier rootPath = YangInstanceIdentifier.of(new NodeIdentifier(node1QName),
                new NodeIdentifier(node2Qname));
        final YangInstanceIdentifier childPath = YangInstanceIdentifier.of(new NodeIdentifier(node1QName),
                new NodeIdentifier(node2Qname), new NodeIdentifier(node3QName), new NodeIdentifier(node4Qname));

        assertEquals(Optional.of(mockedLeafNode),
            NormalizedNodes.findNode(rootPath, mockedDataContainerNode, childPath));
        assertEquals(Optional.empty(), NormalizedNodes.findNode(childPath, mockedDataContainerNode, rootPath));

        final var pathArguments = childPath.relativeTo(rootPath).orElseThrow().getPathArguments()
            .toArray(new PathArgument[0]);

        assertEquals(Optional.of(mockedLeafNode),
            NormalizedNodes.findNode(Optional.of(mockedDataContainerNode), pathArguments));

        assertEquals(Optional.of(mockedLeafNode), NormalizedNodes.findNode(mockedDataContainerNode, pathArguments));
    }

    @Test
    void testToStringTree() {
        final QName leafNodeQName = QName.create("test-ns", "2016-09-16", "leaf-node");
        final NodeIdentifier leafNodeId = new NodeIdentifier(leafNodeQName);
        doReturn(leafNodeId).when(mockedLeafNode).name();
        doReturn("str-value-1").when(mockedLeafNode).body();

        String stringTree = NormalizedNodes.toStringTree(mockedLeafNode);
        assertNotNull(stringTree);
        assertEquals("leaf-node str-value-1\n", stringTree);

        final QName listQName = QName.create("test-ns", "2016-09-16", "list-node");

        final NodeIdentifier listNodeId = new NodeIdentifier(listQName);
        doReturn(listNodeId).when(mockedMapNode).name();

        final NodeIdentifierWithPredicates listEntryNodeId = NodeIdentifierWithPredicates.of(listQName,
                leafNodeQName, "key-leaf-value");
        doReturn(listEntryNodeId).when(mockedMapEntryNode).name();
        doReturn(List.of(mockedMapEntryNode)).when(mockedMapNode).body();

        doReturn(List.of(mockedLeafNode)).when(mockedMapEntryNode).body();

        stringTree = NormalizedNodes.toStringTree(mockedMapNode);
        assertNotNull(stringTree);
        assertEquals("""
            list-node {
                list-node[key-leaf-value] {
                    leaf-node str-value-1
                }
            }
            """, stringTree);
    }
}
