/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

class DataTreeCandidateNodesTest {
    @Test
    void testFromNormalizedNode() {
        assertNotNull(DataTreeCandidateNodes.written(mock(LeafNode.class)));
    }

    @Test
    void testApplyToCursorWithWriteModificationType() {
        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final var mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.WRITE).when(mockedDataTreeCandidateNode).modificationType();
        doReturn(mock(LeafNode.class)).when(mockedDataTreeCandidateNode).dataAfter();
        DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).write(isNull(), any());
    }

    @Test
    void testApplyToCursorWithDeleteModificationType() {
        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final var mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).modificationType();
        DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).delete(isNull());
    }

    @Test
    void testApplyToCursorWithSubtreeModifiedModificationType() {
        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final var mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).modificationType();

        final var mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).modificationType();

        final var mockedChildNode2 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.WRITE).when(mockedChildNode2).modificationType();
        doReturn(mock(LeafNode.class)).when(mockedChildNode2).dataAfter();

        final var mockedChildNode3 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedChildNode3).modificationType();
        final var mockedChildNode3ChildNode = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode3ChildNode).modificationType();
        doReturn(List.of(mockedChildNode3ChildNode)).when(mockedChildNode3).childNodes();

        doReturn(List.of(mockedChildNode1, mockedChildNode2, mockedChildNode3))
            .when(mockedDataTreeCandidateNode).childNodes();

        DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(2)).enter((PathArgument) isNull());
        verify(mockedCursor, times(2)).delete(isNull());
        verify(mockedCursor, times(1)).write(isNull(), any());
    }

    @Test
    void testApplyToCursorWithUnsupportedModificationType() {
        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final var mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).modificationType();
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidateNodes.applyToCursor(mockedCursor, mockedDataTreeCandidateNode));
        assertEquals("Unsupported modification APPEARED", ex.getMessage());
    }

    @Test
    void testApplyRootedNodeToCursorWithWriteModificationType() {
        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final var mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.WRITE).when(mockedDataTreeCandidateNode).modificationType();
        doReturn(mock(LeafNode.class)).when(mockedDataTreeCandidateNode).dataAfter();
        DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, YangInstanceIdentifier.of(),
            mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).write(isNull(), any());
    }

    @Test
    void testApplyRootedNodeToCursorWithDeleteModificationType() {
        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final var mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).modificationType();
        DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, YangInstanceIdentifier.of(),
            mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).delete(isNull());
    }

    @Test
    void testApplyRootedNodeToCursorWithSubtreeModifiedModificationType() {
        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final var mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).modificationType();

        final var mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).modificationType();
        doReturn(List.of(mockedChildNode1)).when(mockedDataTreeCandidateNode).childNodes();

        DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, YangInstanceIdentifier.of(),
            mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).enter((PathArgument) isNull());
        verify(mockedCursor, times(1)).delete(isNull());
    }

    @Test
    void testApplyRootedNodeToCursorWithUnsupportedModificationType() {
        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final var mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).modificationType();
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidateNodes.applyRootedNodeToCursor(mockedCursor, YangInstanceIdentifier.of(),
                mockedDataTreeCandidateNode));
        assertEquals("Unsupported modification APPEARED", ex.getMessage());
    }

    @Test
    void testApplyRootToCursorWithSubtreeModifiedModificationType() {
        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final var mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).modificationType();

        final var mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).modificationType();
        doReturn(List.of(mockedChildNode1)).when(mockedDataTreeCandidateNode).childNodes();

        DataTreeCandidateNodes.applyRootToCursor(mockedCursor, mockedDataTreeCandidateNode);
        verify(mockedCursor, times(1)).delete(isNull());
    }

    @Test
    void testApplyRootToCursorWithDeleteModificationType() {
        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final var mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).modificationType();
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidateNodes.applyRootToCursor(mockedCursor, mockedDataTreeCandidateNode));
        assertEquals("Can not delete root.", ex.getMessage());
    }

    @Test
    void testApplyRootToCursorWithUnsupportedModificationType() {
        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final var mockedCursor = mock(DataTreeModificationCursor.class);

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).modificationType();
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidateNodes.applyRootToCursor(mockedCursor, mockedDataTreeCandidateNode));
        assertEquals("Unsupported modification APPEARED", ex.getMessage());
    }
}
