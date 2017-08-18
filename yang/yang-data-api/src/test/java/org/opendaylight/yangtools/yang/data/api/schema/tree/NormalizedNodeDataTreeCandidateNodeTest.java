/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

public class NormalizedNodeDataTreeCandidateNodeTest {

    @Test
    public void testNormalizedNodeDataTreeCandidateNode() {
        final NormalizedNode<?, ?> mockedNormalizedNode = mock(NormalizedNode.class);
        final NormalizedNodeDataTreeCandidateNode normalizedNodeDataTreeCandidateNode = new
                NormalizedNodeDataTreeCandidateNode(mockedNormalizedNode);

        final PathArgument mockedPathArgument = mock(PathArgument.class);
        doReturn(mockedPathArgument).when(mockedNormalizedNode).getIdentifier();
        assertEquals(mockedPathArgument, normalizedNodeDataTreeCandidateNode.getIdentifier());

        final Collection<DataTreeCandidateNode> childNodes = normalizedNodeDataTreeCandidateNode.getChildNodes();
        assertTrue(childNodes instanceof List);
        assertTrue(childNodes.isEmpty());

        assertNull(normalizedNodeDataTreeCandidateNode.getModifiedChild(mockedPathArgument));

        assertEquals(ModificationType.WRITE, normalizedNodeDataTreeCandidateNode.getModificationType());
        assertEquals(Optional.of(mockedNormalizedNode), normalizedNodeDataTreeCandidateNode.getDataAfter());
        assertEquals(Optional.empty(), normalizedNodeDataTreeCandidateNode.getDataBefore());

        final NormalizedNodeContainer mockedNormalizedNodeContainer = mock(NormalizedNodeContainer.class);
        final NormalizedNodeDataTreeCandidateNode normalizedNodeDataTreeCandidateNode2 = new
                NormalizedNodeDataTreeCandidateNode(mockedNormalizedNodeContainer);
        final NormalizedNode<?, ?> mockedChildNormNode1 = mock(NormalizedNode.class);
        final NormalizedNode<?, ?> mockedChildNormNode2 = mock(NormalizedNode.class);
        final Collection<NormalizedNode<?, ?>> mockedChildNodes = Lists.newArrayList(mockedChildNormNode1,
                mockedChildNormNode2, null);
        doReturn(mockedChildNodes).when(mockedNormalizedNodeContainer).getValue();
        final Collection<DataTreeCandidateNode> childNodes2 = normalizedNodeDataTreeCandidateNode2.getChildNodes();
        assertEquals(3, childNodes2.size());

        doReturn(Optional.empty()).when(mockedNormalizedNodeContainer).getChild(any(PathArgument.class));
        assertNull(normalizedNodeDataTreeCandidateNode2.getModifiedChild(mockedPathArgument));

        doReturn(Optional.of(mockedChildNormNode1)).when(mockedNormalizedNodeContainer).getChild(
                any(PathArgument.class));
        assertNotNull(normalizedNodeDataTreeCandidateNode2.getModifiedChild(mockedPathArgument));
    }
}
