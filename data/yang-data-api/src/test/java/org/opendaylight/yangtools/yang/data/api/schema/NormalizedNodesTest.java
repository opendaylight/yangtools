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
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

@ExtendWith(MockitoExtension.class)
public class NormalizedNodesTest {
    @Test
    public void testGetDirectChild() {
        final PathArgument mockedPathArgument = mock(PathArgument.class);

        final LeafNode<?> mockedLeafNode = mock(LeafNode.class);
        assertEquals(Optional.empty(), NormalizedNodes.getDirectChild(mockedLeafNode, mockedPathArgument));

        final LeafSetEntryNode<?> mockedLeafSetEntryNode = mock(LeafSetEntryNode.class);
        assertEquals(Optional.empty(), NormalizedNodes.getDirectChild(mockedLeafSetEntryNode, mockedPathArgument));

        final DataContainerNode mockedDataContainerNode = mock(DataContainerNode.class);
        final ContainerNode mockedContainerNode = mock(ContainerNode.class);
        doReturn(mockedContainerNode).when(mockedDataContainerNode).childByArg(any(PathArgument.class));
        doCallRealMethod().when(mockedDataContainerNode).findChildByArg(any(PathArgument.class));

        assertEquals(mockedContainerNode, NormalizedNodes.getDirectChild(mockedDataContainerNode, mockedPathArgument)
                .get());

        final SystemMapNode mockedMapNode = mock(SystemMapNode.class);
        final QName listQName = QName.create("test-ns", "test-list");
        final QName listKeyQName = QName.create("test-ns", "test-list-key");
        final NodeIdentifierWithPredicates nodeIdentifierWithPredicates =
                NodeIdentifierWithPredicates.of(listQName, listKeyQName, "str-value");
        final MapEntryNode mockedMapEntryNode = mock(MapEntryNode.class);
        doReturn(mockedMapEntryNode).when(mockedMapNode).childByArg(any(NodeIdentifierWithPredicates.class));
        doCallRealMethod().when(mockedMapNode).findChildByArg(any(NodeIdentifierWithPredicates.class));

        assertEquals(mockedMapEntryNode, NormalizedNodes.getDirectChild(mockedMapNode, nodeIdentifierWithPredicates)
                .get());
        assertEquals(Optional.empty(), NormalizedNodes.getDirectChild(mockedMapNode, mockedPathArgument));

        final SystemLeafSetNode<?> mockedLeafSetNode = mock(SystemLeafSetNode.class);
        final QName leafListQName = QName.create("test-ns", "test-leaf-list");
        final NodeWithValue<?> nodeWithValue = new NodeWithValue<>(leafListQName, "str-value");
        doReturn(mockedLeafSetEntryNode).when(mockedLeafSetNode).childByArg(any(NodeWithValue.class));
        doCallRealMethod().when(mockedLeafSetNode).findChildByArg(any(NodeWithValue.class));
        assertEquals(mockedLeafSetEntryNode, NormalizedNodes.getDirectChild(mockedLeafSetNode, nodeWithValue).get());
    }

    @Test
    public void testFindNode() {
        final DataContainerNode mockedDataContainerNode = mock(DataContainerNode.class);
        final ContainerNode mockedContainerNode = mock(ContainerNode.class);
        final LeafNode<?> mockedLeafNode = mock(LeafNode.class);
        doReturn(mockedContainerNode).when(mockedDataContainerNode).childByArg(any(PathArgument.class));
        doCallRealMethod().when(mockedDataContainerNode).findChildByArg(any(PathArgument.class));
        doReturn(mockedLeafNode).when(mockedContainerNode).childByArg(any(PathArgument.class));
        doCallRealMethod().when(mockedContainerNode).findChildByArg(any(PathArgument.class));

        final QName node1QName = QName.create("test-ns", "2016-09-16", "node1");
        final QName node2Qname = QName.create("test-ns", "2016-09-16", "node2");
        final QName node3QName = QName.create("test-ns", "2016-09-16", "node3");
        final QName node4Qname = QName.create("test-ns", "2016-09-16", "node4");

        final YangInstanceIdentifier rootPath = YangInstanceIdentifier.create(new NodeIdentifier(node1QName),
                new NodeIdentifier(node2Qname));
        final YangInstanceIdentifier childPath = YangInstanceIdentifier.create(new NodeIdentifier(node1QName),
                new NodeIdentifier(node2Qname), new NodeIdentifier(node3QName), new NodeIdentifier(node4Qname));

        assertEquals(mockedLeafNode, NormalizedNodes.findNode(rootPath, mockedDataContainerNode, childPath)
                .orElseThrow());
        assertEquals(Optional.empty(), NormalizedNodes.findNode(childPath, mockedDataContainerNode, rootPath));

        final Optional<YangInstanceIdentifier> relativePath = childPath.relativeTo(rootPath);
        final PathArgument[] pathArguments = relativePath.get().getPathArguments().toArray(new PathArgument[2]);

        assertEquals(mockedLeafNode, NormalizedNodes.findNode(Optional.of(mockedDataContainerNode),
                pathArguments).get());

        assertEquals(mockedLeafNode, NormalizedNodes.findNode(mockedDataContainerNode, pathArguments).get());
    }

    @Test
    public void testToStringTree() {
        final LeafNode<?> mockedLeafNode = mock(LeafNode.class);
        final QName leafNodeQName = QName.create("test-ns", "2016-09-16", "leaf-node");
        final NodeIdentifier leafNodeId = new NodeIdentifier(leafNodeQName);
        doReturn(leafNodeId).when(mockedLeafNode).getIdentifier();
        doReturn("str-value-1").when(mockedLeafNode).body();

        String stringTree = NormalizedNodes.toStringTree(mockedLeafNode);
        assertNotNull(stringTree);
        assertEquals("leaf-node str-value-1\n", stringTree);

        final AugmentationNode mockedAugmentationNode = mock(AugmentationNode.class);
        final QName listQName = QName.create("test-ns", "2016-09-16", "list-node");
        final AugmentationIdentifier augNodeId = new AugmentationIdentifier(ImmutableSet.of(listQName));
        doReturn(augNodeId).when(mockedAugmentationNode).getIdentifier();

        final SystemMapNode mockedMapNode = mock(SystemMapNode.class);
        final NodeIdentifier listNodeId = new NodeIdentifier(listQName);
        doReturn(listNodeId).when(mockedMapNode).getIdentifier();
        doReturn(List.of(mockedMapNode)).when(mockedAugmentationNode).body();

        final MapEntryNode mockedMapEntryNode = mock(MapEntryNode.class);
        final NodeIdentifierWithPredicates listEntryNodeId = NodeIdentifierWithPredicates.of(listQName,
                leafNodeQName, "key-leaf-value");
        doReturn(listEntryNodeId).when(mockedMapEntryNode).getIdentifier();
        doReturn(List.of(mockedMapEntryNode)).when(mockedMapNode).body();

        doReturn(List.of(mockedLeafNode)).when(mockedMapEntryNode).body();

        stringTree = NormalizedNodes.toStringTree(mockedAugmentationNode);
        assertNotNull(stringTree);
        assertEquals("""
            augmentation {
                list-node {
                    list-node[key-leaf-value] {
                        leaf-node str-value-1
                    }
                }
            }
            """, stringTree);
    }
}
