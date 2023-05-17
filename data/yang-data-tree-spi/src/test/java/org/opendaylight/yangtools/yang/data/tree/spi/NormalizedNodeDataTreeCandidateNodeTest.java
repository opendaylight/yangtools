/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
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

        final PathArgument mockedPathArgument = new NodeIdentifier(QName.create("test", "test"));
        doReturn(mockedPathArgument).when(mockedNormalizedNode).getIdentifier();
        assertSame(mockedPathArgument, normalizedNodeDataTreeCandidateNode.getIdentifier());

        final Collection<DataTreeCandidateNode> childNodes = normalizedNodeDataTreeCandidateNode.getChildNodes();
        assertTrue(childNodes instanceof List);
        assertTrue(childNodes.isEmpty());

        assertEquals(Optional.empty(), normalizedNodeDataTreeCandidateNode.getModifiedChild(mockedPathArgument));

        assertEquals(ModificationType.WRITE, normalizedNodeDataTreeCandidateNode.getModificationType());
        assertEquals(Optional.of(mockedNormalizedNode), normalizedNodeDataTreeCandidateNode.getDataAfter());
        assertEquals(Optional.empty(), normalizedNodeDataTreeCandidateNode.getDataBefore());

        final DistinctNodeContainer mockedNormalizedNodeContainer = mock(DistinctNodeContainer.class);
        final NormalizedNodeDataTreeCandidateNode normalizedNodeDataTreeCandidateNode2 = new
                NormalizedNodeDataTreeCandidateNode(mockedNormalizedNodeContainer);
        final NormalizedNode mockedChildNormNode1 = mock(NormalizedNode.class);
        final NormalizedNode mockedChildNormNode2 = mock(NormalizedNode.class);
        final Collection<NormalizedNode> mockedChildNodes = Arrays.asList(mockedChildNormNode1,
                mockedChildNormNode2, null);
        doReturn(mockedChildNodes).when(mockedNormalizedNodeContainer).body();
        final Collection<DataTreeCandidateNode> childNodes2 = normalizedNodeDataTreeCandidateNode2.getChildNodes();
        assertEquals(3, childNodes2.size());

        doReturn(null).when(mockedNormalizedNodeContainer).childByArg(any(PathArgument.class));
        doCallRealMethod().when(mockedNormalizedNodeContainer).findChildByArg(any(PathArgument.class));
        assertEquals(Optional.empty(), normalizedNodeDataTreeCandidateNode2.getModifiedChild(mockedPathArgument));

        doReturn(mockedChildNormNode1).when(mockedNormalizedNodeContainer).childByArg(any(PathArgument.class));
        assertTrue(normalizedNodeDataTreeCandidateNode2.getModifiedChild(mockedPathArgument).isPresent());
    }
}
