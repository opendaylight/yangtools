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
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

public class NormalizedNodeDataTreeCandidateNodeTest {
    @Test
    public void testNormalizedNodeDataTreeCandidateNode() {
        final NormalizedNode mockedNormalizedNode = mock(NormalizedNode.class);
        final NormalizedNodeDataTreeCandidateNode normalizedNodeDataTreeCandidateNode = new
                NormalizedNodeDataTreeCandidateNode(mockedNormalizedNode);

        final var mockedPathArgument = new NodeIdentifier(QName.create("test", "test"));
        doReturn(mockedPathArgument).when(mockedNormalizedNode).name();
        assertSame(mockedPathArgument, normalizedNodeDataTreeCandidateNode.name());

        final Collection<DataTreeCandidateNode> childNodes = normalizedNodeDataTreeCandidateNode.childNodes();
        assertInstanceOf(List.class, childNodes);
        assertTrue(childNodes.isEmpty());

        assertNull(normalizedNodeDataTreeCandidateNode.modifiedChild(mockedPathArgument));

        assertEquals(ModificationType.WRITE, normalizedNodeDataTreeCandidateNode.modificationType());
        assertEquals(mockedNormalizedNode, normalizedNodeDataTreeCandidateNode.dataAfter());
        assertEquals(null, normalizedNodeDataTreeCandidateNode.dataBefore());

        final DistinctNodeContainer mockedNormalizedNodeContainer = mock(DistinctNodeContainer.class);
        final NormalizedNodeDataTreeCandidateNode normalizedNodeDataTreeCandidateNode2 = new
                NormalizedNodeDataTreeCandidateNode(mockedNormalizedNodeContainer);
        final NormalizedNode mockedChildNormNode1 = mock(NormalizedNode.class);
        final NormalizedNode mockedChildNormNode2 = mock(NormalizedNode.class);
        final var mockedChildNodes = Arrays.asList(mockedChildNormNode1, mockedChildNormNode2, null);
        doReturn(mockedChildNodes).when(mockedNormalizedNodeContainer).body();
        final var childNodes2 = normalizedNodeDataTreeCandidateNode2.childNodes();
        assertEquals(3, childNodes2.size());

        doReturn(null).when(mockedNormalizedNodeContainer).childByArg(any(PathArgument.class));
        doCallRealMethod().when(mockedNormalizedNodeContainer).findChildByArg(any(PathArgument.class));
        assertNull(normalizedNodeDataTreeCandidateNode2.modifiedChild(mockedPathArgument));

        doReturn(mockedChildNormNode1).when(mockedNormalizedNodeContainer).childByArg(any(PathArgument.class));
        assertNotNull(normalizedNodeDataTreeCandidateNode2.modifiedChild(mockedPathArgument));
    }
}
