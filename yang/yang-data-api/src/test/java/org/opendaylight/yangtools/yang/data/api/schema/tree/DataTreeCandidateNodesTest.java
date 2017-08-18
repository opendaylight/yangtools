/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class DataTreeCandidateNodesTest {

    @Test
    public void testFromNormalizedNode() {
        final NormalizedNode<?, ?> mockedNormalizedNode = mock(NormalizedNode.class);
        final DataTreeCandidateNode dataTreeCandidateNode = DataTreeCandidateNodes.fromNormalizedNode(
                mockedNormalizedNode);
        assertNotNull(dataTreeCandidateNode);
    }

    @Test
    public void testApplyToCursorWithWriteModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.WRITE).when(mockedDataTreeCandidateNode).getModificationType();
        final NormalizedNode<?, ?> mockedNormalizedNode = mock(NormalizedNode.class);
        doReturn(Optional.of(mockedNormalizedNode)).when(mockedDataTreeCandidateNode).getDataAfter();
        DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).write(any(PathArgument.class), any(NormalizedNode.class));
    }

    @Test
    public void testApplyToCursorWithDeleteModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).getModificationType();
        DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).delete(any(PathArgument.class));
    }

    @Test
    public void testApplyToCursorWithSubtreeModifiedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).getModificationType();

        final DataTreeCandidateNode mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).getModificationType();

        final DataTreeCandidateNode mockedChildNode2 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.WRITE).when(mockedChildNode2).getModificationType();
        final NormalizedNode<?, ?> mockedNormalizedNode = mock(NormalizedNode.class);
        doReturn(Optional.of(mockedNormalizedNode)).when(mockedChildNode2).getDataAfter();

        final DataTreeCandidateNode mockedChildNode3 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedChildNode3).getModificationType();
        final DataTreeCandidateNode mockedChildNode3ChildNode = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode3ChildNode).getModificationType();
        doReturn(Lists.newArrayList(mockedChildNode3ChildNode)).when(mockedChildNode3).getChildNodes();

        final Collection<DataTreeCandidateNode> childNodes = Lists.newArrayList(mockedChildNode1, mockedChildNode2,
                mockedChildNode3);
        doReturn(childNodes).when(mockedDataTreeCandidateNode).getChildNodes();

        DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(2)).enter(any(PathArgument.class));
        verify(mockedCursor, times(2)).delete(any(PathArgument.class));
        verify(mockedCursor, times(1)).write(any(PathArgument.class), any(NormalizedNode.class));
    }

    @Test
    public void testApplyToCursorWithUnsupportedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).getModificationType();
        try {
            DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode);
            fail("An IllegalArgumentException should have been thrown!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Unsupported modification"));
        }
    }

    @Test
    public void testApplyRootedNodeToCursorWithWriteModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.WRITE).when(mockedDataTreeCandidateNode).getModificationType();
        final NormalizedNode<?, ?> mockedNormalizedNode = mock(NormalizedNode.class);
        doReturn(Optional.of(mockedNormalizedNode)).when(mockedDataTreeCandidateNode).getDataAfter();
        DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, mockedRootPath, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).write(any(PathArgument.class), any(NormalizedNode.class));
    }

    @Test
    public void testApplyRootedNodeToCursorWithDeleteModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).getModificationType();
        DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, mockedRootPath, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).delete(any(PathArgument.class));
    }

    @Test
    public void testApplyRootedNodeToCursorWithSubtreeModifiedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).getModificationType();

        final DataTreeCandidateNode mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).getModificationType();
        doReturn(Lists.newArrayList(mockedChildNode1)).when(mockedDataTreeCandidateNode).getChildNodes();

        DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, mockedRootPath, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).enter(any(PathArgument.class));
        verify(mockedCursor, times(1)).delete(any(PathArgument.class));
    }

    @Test
    public void testApplyRootedNodeToCursorWithUnsupportedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).getModificationType();
        try {
            DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, mockedRootPath, mockedDataTreeCandidateNode);
            fail("An IllegalArgumentException should have been thrown!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Unsupported modification"));
        }
    }

    @Test
    public void testApplyRootToCursorWithSubtreeModifiedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).getModificationType();

        final DataTreeCandidateNode mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).getModificationType();
        doReturn(Lists.newArrayList(mockedChildNode1)).when(mockedDataTreeCandidateNode).getChildNodes();

        DataTreeCandidateNodes.applyRootToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).delete(any(PathArgument.class));
    }

    @Test
    public void testApplyRootToCursorWithDeleteModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).getModificationType();
        try {
            DataTreeCandidateNodes.applyRootToCursor(mockedCursor, mockedDataTreeCandidateNode);
            fail("An IllegalArgumentException should have been thrown!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Can not delete root"));
        }
    }

    @Test
    public void testApplyRootToCursorWithUnsupportedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).getModificationType();
        try {
            DataTreeCandidateNodes.applyRootToCursor(mockedCursor, mockedDataTreeCandidateNode);
            fail("An IllegalArgumentException should have been thrown!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Unsupported modification"));
        }
    }
}
