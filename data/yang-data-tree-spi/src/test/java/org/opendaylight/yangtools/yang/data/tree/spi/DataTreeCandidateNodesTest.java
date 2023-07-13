/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DataTreeCandidateNodesTest {
    @Test
    public void testFromNormalizedNode() {
        final NormalizedNode mockedNormalizedNode = mock(NormalizedNode.class);
        final DataTreeCandidateNode dataTreeCandidateNode = DataTreeCandidateNodes.written(mockedNormalizedNode);
        assertNotNull(dataTreeCandidateNode);
    }

    @Test
    public void testApplyToCursorWithWriteModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.WRITE).when(mockedDataTreeCandidateNode).modificationType();
        final NormalizedNode mockedNormalizedNode = mock(NormalizedNode.class);
        doReturn(mockedNormalizedNode).when(mockedDataTreeCandidateNode).dataAfter();
        DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).write(isNull(), any(NormalizedNode.class));
    }

    @Test
    public void testApplyToCursorWithDeleteModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).modificationType();
        DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).delete(isNull());
    }

    @Test
    public void testApplyToCursorWithSubtreeModifiedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).modificationType();

        final DataTreeCandidateNode mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).modificationType();

        final DataTreeCandidateNode mockedChildNode2 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.WRITE).when(mockedChildNode2).modificationType();
        final NormalizedNode mockedNormalizedNode = mock(NormalizedNode.class);
        doReturn(mockedNormalizedNode).when(mockedChildNode2).dataAfter();

        final DataTreeCandidateNode mockedChildNode3 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedChildNode3).modificationType();
        final DataTreeCandidateNode mockedChildNode3ChildNode = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode3ChildNode).modificationType();
        doReturn(List.of(mockedChildNode3ChildNode)).when(mockedChildNode3).childNodes();

        doReturn(List.of(mockedChildNode1, mockedChildNode2, mockedChildNode3))
            .when(mockedDataTreeCandidateNode).childNodes();

        DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(2)).enter((PathArgument) isNull());
        verify(mockedCursor, times(2)).delete(isNull());
        verify(mockedCursor, times(1)).write(isNull(), any(NormalizedNode.class));
    }

    @Test
    public void testApplyToCursorWithUnsupportedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).modificationType();
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode));
        assertTrue(ex.getMessage().contains("Unsupported modification"));
    }

    @Test
    public void testApplyRootedNodeToCursorWithWriteModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.WRITE).when(mockedDataTreeCandidateNode).modificationType();
        final NormalizedNode mockedNormalizedNode = mock(NormalizedNode.class);
        doReturn(mockedNormalizedNode).when(mockedDataTreeCandidateNode).dataAfter();
        DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, YangInstanceIdentifier.of(),
            mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).write(isNull(), any(NormalizedNode.class));
    }

    @Test
    public void testApplyRootedNodeToCursorWithDeleteModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).modificationType();
        DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, YangInstanceIdentifier.of(),
            mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).delete(isNull());
    }

    @Test
    public void testApplyRootedNodeToCursorWithSubtreeModifiedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).modificationType();

        final DataTreeCandidateNode mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).modificationType();
        doReturn(List.of(mockedChildNode1)).when(mockedDataTreeCandidateNode).childNodes();

        DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, YangInstanceIdentifier.of(),
            mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).enter((PathArgument) isNull());
        verify(mockedCursor, times(1)).delete(isNull());
    }

    @Test
    public void testApplyRootedNodeToCursorWithUnsupportedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).modificationType();
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, YangInstanceIdentifier.of(),
                mockedDataTreeCandidateNode));
        assertTrue(ex.getMessage().contains("Unsupported modification"));
    }

    @Test
    public void testApplyRootToCursorWithSubtreeModifiedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).modificationType();

        final DataTreeCandidateNode mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).modificationType();
        doReturn(List.of(mockedChildNode1)).when(mockedDataTreeCandidateNode).childNodes();

        DataTreeCandidateNodes.applyRootToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).delete(isNull());
    }

    @Test
    public void testApplyRootToCursorWithDeleteModificationType() {
        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final var mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).modificationType();
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidateNodes.applyRootToCursor(mockedCursor, mockedDataTreeCandidateNode));
        assertTrue(ex.getMessage().contains("Can not delete root"));
    }

    @Test
    public void testApplyRootToCursorWithUnsupportedModificationType() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).modificationType();
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidateNodes.applyRootToCursor(mockedCursor, mockedDataTreeCandidateNode));
        assertTrue(ex.getMessage().contains("Unsupported modification"));
    }
}
