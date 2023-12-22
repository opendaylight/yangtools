/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

class NormalizedNodeDataTreeCandidateNodeTest {
    @Test
    void testNormalizedNodeDataTreeCandidateNode() {
        final var mockedNormalizedNode = mock(LeafNode.class);
        final var normalizedNodeDataTreeCandidateNode = new
                NormalizedNodeDataTreeCandidateNode(mockedNormalizedNode);

        final var mockedPathArgument = new NodeIdentifier(QName.create("test", "test"));
        doReturn(mockedPathArgument).when(mockedNormalizedNode).name();
        assertSame(mockedPathArgument, normalizedNodeDataTreeCandidateNode.name());

        final var childNodes = normalizedNodeDataTreeCandidateNode.childNodes();
        assertInstanceOf(List.class, childNodes);
        assertTrue(childNodes.isEmpty());

        assertNull(normalizedNodeDataTreeCandidateNode.modifiedChild(mockedPathArgument));

        assertEquals(ModificationType.WRITE, normalizedNodeDataTreeCandidateNode.modificationType());
        assertEquals(mockedNormalizedNode, normalizedNodeDataTreeCandidateNode.dataAfter());
        assertNull(normalizedNodeDataTreeCandidateNode.dataBefore());

        final var mockedNormalizedNodeContainer = mock(ContainerNode.class);
        final var normalizedNodeDataTreeCandidateNode2 = new
                NormalizedNodeDataTreeCandidateNode(mockedNormalizedNodeContainer);
        final var mockedChildNormNode1 = mock(LeafNode.class);
        final var mockedChildNormNode2 = mock(LeafNode.class);
        final var mockedChildNodes = Arrays.asList(mockedChildNormNode1, mockedChildNormNode2, null);
        doReturn(mockedChildNodes).when(mockedNormalizedNodeContainer).body();
        final var childNodes2 = normalizedNodeDataTreeCandidateNode2.childNodes();
        assertEquals(3, childNodes2.size());

        doReturn(null).when(mockedNormalizedNodeContainer).childByArg(any());
        doCallRealMethod().when(mockedNormalizedNodeContainer).findChildByArg(any());
        assertNull(normalizedNodeDataTreeCandidateNode2.modifiedChild(mockedPathArgument));

        doReturn(mockedChildNormNode1).when(mockedNormalizedNodeContainer).childByArg(any());
        assertNotNull(normalizedNodeDataTreeCandidateNode2.modifiedChild(mockedPathArgument));
    }
}
