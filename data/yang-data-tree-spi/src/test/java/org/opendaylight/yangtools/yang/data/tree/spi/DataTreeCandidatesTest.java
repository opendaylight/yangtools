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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

class DataTreeCandidatesTest {
    private static final NodeIdentifier FOO = new NodeIdentifier(QName.create("foo", "foo"));

    @Test
    void testNewDataTreeCandidate() {
        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final var dataTreeCandidate = DataTreeCandidates.newDataTreeCandidate(
            YangInstanceIdentifier.of(), mockedDataTreeCandidateNode);

        assertInstanceOf(DefaultDataTreeCandidate.class, dataTreeCandidate);
        assertSame(YangInstanceIdentifier.of(), dataTreeCandidate.getRootPath());
        assertEquals(mockedDataTreeCandidateNode, dataTreeCandidate.getRootNode());
        assertTrue(dataTreeCandidate.toString()
                .contains("DefaultDataTreeCandidate{rootPath=/, rootNode=Mock for DataTreeCandidateNode, hashCode: "));
    }

    @Test
    void testFromNormalizedNode() {
        final var mockedNormalizedNode = mock(LeafNode.class);
        final var dataTreeCandidate = DataTreeCandidates.fromNormalizedNode(
            YangInstanceIdentifier.of(), mockedNormalizedNode);

        assertInstanceOf(DefaultDataTreeCandidate.class, dataTreeCandidate);
        assertSame(YangInstanceIdentifier.of(), dataTreeCandidate.getRootPath());
        assertInstanceOf(NormalizedNodeDataTreeCandidateNode.class, dataTreeCandidate.getRootNode());
    }

    @Test
    void testApplyToCursor() {
        final var mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final var mockedCursor = mock(DataTreeModificationCursor.class);

        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).modificationType();
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        DataTreeCandidates.applyToCursor(mockedCursor, mockedDataTreeCandidate);
        verify(mockedCursor, times(1)).delete(isNull());
    }

//    @Test
//    void testApplyToCursorAwareModification() {
//        final var mockedDataTreeCandidate = mock(DataTreeCandidate.class);
//        final var mockedModification = mock(CursorAwareDataTreeModification.class);
//
//        doReturn(YangInstanceIdentifier.of(FOO)).when(mockedDataTreeCandidate).getRootPath();
//
//        final var mockedCursor = mock(DataTreeModificationCursor.class);
//        doReturn(Optional.of(mockedCursor)).when(mockedModification).openCursor(YangInstanceIdentifier.of());
//        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
//        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
//
//        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).modificationType();
//
//        DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate);
//        verify(mockedModification, times(1)).openCursor(YangInstanceIdentifier.of());
//        verify(mockedCursor, times(1)).delete(FOO);
//    }
//
//    @Test
//    void testApplyToCursorAwareModificationRoot() {
//        final var mockedDataTreeCandidate = mock(DataTreeCandidate.class);
//        final var mockedModification = mock(CursorAwareDataTreeModification.class);
//        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
//        doReturn(YangInstanceIdentifier.of()).when(mockedDataTreeCandidate).getRootPath();
//        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
//        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).modificationType();
//
//        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
//            () -> DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate));
//        assertEquals("Can not delete root.", thrown.getMessage());
//    }

    @Test
    void testApplyToModificationWithDeleteModificationType() {
        final var mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final var mockedModification = mock(DataTreeModification.class);

        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        doReturn(YangInstanceIdentifier.of()).when(mockedDataTreeCandidate).getRootPath();

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).modificationType();

        DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate);
        verify(mockedModification, times(1)).delete(YangInstanceIdentifier.of());
    }

    @Test
    void testApplyToModificationWithWriteModificationType() {
        final var mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final var mockedModification = mock(DataTreeModification.class);

        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        doReturn(YangInstanceIdentifier.of()).when(mockedDataTreeCandidate).getRootPath();
        final var mockedNormalizedNode = mock(LeafNode.class);
        doReturn(mockedNormalizedNode).when(mockedDataTreeCandidateNode).dataAfter();

        doReturn(ModificationType.WRITE).when(mockedDataTreeCandidateNode).modificationType();

        DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate);
        verify(mockedModification, times(1)).write(any(YangInstanceIdentifier.class), any(NormalizedNode.class));
    }

    @Test
    void testApplyToModificationWithSubtreeModifiedModificationType() {
        final var mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final var mockedModification = mock(DataTreeModification.class);

        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        doReturn(YangInstanceIdentifier.of()).when(mockedDataTreeCandidate).getRootPath();

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).modificationType();

        final var mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).modificationType();
        doReturn(new NodeIdentifier(QName.create("test", "test1"))).when(mockedChildNode1).name();

        final var mockedChildNode2 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.WRITE).when(mockedChildNode2).modificationType();
        final var mockedNormalizedNode = mock(LeafNode.class);
        doReturn(mockedNormalizedNode).when(mockedChildNode2).dataAfter();
        doReturn(new NodeIdentifier(QName.create("test", "test2"))).when(mockedChildNode2).name();

        final var mockedChildNode3 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedChildNode3).modificationType();
        final var mockedChildNode3ChildNode = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode3ChildNode).modificationType();
        doReturn(new NodeIdentifier(QName.create("test", "test3"))).when(mockedChildNode3).name();
        doReturn(new NodeIdentifier(QName.create("test", "test4"))).when(mockedChildNode3ChildNode).name();
        doReturn(List.of(mockedChildNode3ChildNode)).when(mockedChildNode3).childNodes();

        final var childNodes = List.of(mockedChildNode1, mockedChildNode2, mockedChildNode3);
        doReturn(childNodes).when(mockedDataTreeCandidateNode).childNodes();

        DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate);
        verify(mockedModification, times(2)).delete(any(YangInstanceIdentifier.class));
        verify(mockedModification, times(1)).write(any(YangInstanceIdentifier.class), any(NormalizedNode.class));
    }

    @Test
    void testApplyToModificationWithUnsupportedModificationType() {
        final var mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final var mockedModification = mock(DataTreeModification.class);

        final var mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        doReturn(YangInstanceIdentifier.of()).when(mockedDataTreeCandidate).getRootPath();

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).modificationType();

        final var ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate));
        assertTrue(ex.getMessage().contains("Unsupported modification"));
    }
}
