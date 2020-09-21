/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class DataTreeCandidateNodesTest {
    @Test
    public void testFromNormalizedNode() {
        final NormalizedNode<?, ?> mockedNormalizedNode = mock(NormalizedNode.class);
        final DataTreeCandidateNode dataTreeCandidateNode = DataTreeCandidateNodes.written(mockedNormalizedNode);
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
        verify(mockedCursor, times(1)).write(isNull(), any(NormalizedNode.class));
    }

    @Test
    public void testApplyToCursorWithDeleteModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).getModificationType();
        DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).delete(isNull());
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
        doReturn(Collections.singletonList(mockedChildNode3ChildNode)).when(mockedChildNode3).getChildNodes();

        final Collection<DataTreeCandidateNode> childNodes = ImmutableList.of(mockedChildNode1, mockedChildNode2,
                mockedChildNode3);
        doReturn(childNodes).when(mockedDataTreeCandidateNode).getChildNodes();

        DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(2)).enter((PathArgument) isNull());
        verify(mockedCursor, times(2)).delete(isNull());
        verify(mockedCursor, times(1)).write(isNull(), any(NormalizedNode.class));
    }

    @Test
    public void testApplyToCursorWithUnsupportedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).getModificationType();
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode));
        assertThat(ex.getMessage(), containsString("Unsupported modification"));
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
        verify(mockedCursor, times(1)).write(isNull(), any(NormalizedNode.class));
    }

    @Test
    public void testApplyRootedNodeToCursorWithDeleteModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).getModificationType();
        DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, mockedRootPath, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).delete(isNull());
    }

    @Test
    public void testApplyRootedNodeToCursorWithSubtreeModifiedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).getModificationType();

        final DataTreeCandidateNode mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).getModificationType();
        doReturn(Collections.singletonList(mockedChildNode1)).when(mockedDataTreeCandidateNode).getChildNodes();

        DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, mockedRootPath, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).enter((PathArgument) isNull());
        verify(mockedCursor, times(1)).delete(isNull());
    }

    @Test
    public void testApplyRootedNodeToCursorWithUnsupportedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).getModificationType();
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, mockedRootPath,
                mockedDataTreeCandidateNode));
        assertThat(ex.getMessage(), containsString("Unsupported modification"));
    }

    @Test
    public void testApplyRootToCursorWithSubtreeModifiedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).getModificationType();

        final DataTreeCandidateNode mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).getModificationType();
        doReturn(Collections.singletonList(mockedChildNode1)).when(mockedDataTreeCandidateNode).getChildNodes();

        DataTreeCandidateNodes.applyRootToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).delete(isNull());
    }

    @Test
    public void testApplyRootToCursorWithDeleteModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).getModificationType();
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidateNodes.applyRootToCursor(mockedCursor, mockedDataTreeCandidateNode));
        assertThat(ex.getMessage(), containsString("Can not delete root"));
    }

    @Test
    public void testApplyRootToCursorWithUnsupportedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).getModificationType();
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidateNodes.applyRootToCursor(mockedCursor, mockedDataTreeCandidateNode));
        assertThat(ex.getMessage(), containsString("Unsupported modification"));
    }
}
