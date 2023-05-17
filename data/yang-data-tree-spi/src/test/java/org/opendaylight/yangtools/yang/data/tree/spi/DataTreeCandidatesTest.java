/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.CursorAwareDataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DataTreeCandidatesTest {
    private static final NodeIdentifier FOO = new NodeIdentifier(QName.create("foo", "foo"));

    @Test
    public void testNewDataTreeCandidate() {
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeCandidate dataTreeCandidate = DataTreeCandidates.newDataTreeCandidate(
            YangInstanceIdentifier.empty(), mockedDataTreeCandidateNode);

        assertThat(dataTreeCandidate, instanceOf(DefaultDataTreeCandidate.class));
        assertSame(YangInstanceIdentifier.empty(), dataTreeCandidate.getRootPath());
        assertEquals(mockedDataTreeCandidateNode, dataTreeCandidate.getRootNode());
        assertThat(dataTreeCandidate.toString(),
            containsString("DefaultDataTreeCandidate{rootPath=/, rootNode=Mock for DataTreeCandidateNode, hashCode: "));
    }

    @Test
    public void testFromNormalizedNode() {
        final NormalizedNode mockedNormalizedNode = mock(NormalizedNode.class);
        final DataTreeCandidate dataTreeCandidate = DataTreeCandidates.fromNormalizedNode(
            YangInstanceIdentifier.empty(), mockedNormalizedNode);

        assertThat(dataTreeCandidate, instanceOf(DefaultDataTreeCandidate.class));
        assertSame(YangInstanceIdentifier.empty(), dataTreeCandidate.getRootPath());
        assertThat(dataTreeCandidate.getRootNode(), instanceOf(NormalizedNodeDataTreeCandidateNode.class));
    }

    @Test
    public void testApplyToCursor() {
        final DataTreeCandidate mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).getModificationType();
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        DataTreeCandidates.applyToCursor(mockedCursor, mockedDataTreeCandidate);
        verify(mockedCursor, times(1)).delete(isNull());
    }

    @Test
    public void testApplyToCursorAwareModification() {
        final DataTreeCandidate mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final CursorAwareDataTreeModification mockedModification = mock(CursorAwareDataTreeModification.class);

        doReturn(YangInstanceIdentifier.create(FOO)).when(mockedDataTreeCandidate).getRootPath();

        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);
        doReturn(Optional.of(mockedCursor)).when(mockedModification).openCursor(YangInstanceIdentifier.empty());
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).getModificationType();

        DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate);
        verify(mockedModification, times(1)).openCursor(YangInstanceIdentifier.empty());
        verify(mockedCursor, times(1)).delete(FOO);
    }

    @Test
    public void testApplyToCursorAwareModificationRoot() {
        final DataTreeCandidate mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final CursorAwareDataTreeModification mockedModification = mock(CursorAwareDataTreeModification.class);
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(YangInstanceIdentifier.empty()).when(mockedDataTreeCandidate).getRootPath();
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).getModificationType();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate));
        assertEquals("Can not delete root.", thrown.getMessage());
    }

    @Test
    public void testApplyToModificationWithDeleteModificationType() {
        final DataTreeCandidate mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final DataTreeModification mockedModification = mock(DataTreeModification.class);

        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        doReturn(YangInstanceIdentifier.empty()).when(mockedDataTreeCandidate).getRootPath();

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).getModificationType();

        DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate);
        verify(mockedModification, times(1)).delete(YangInstanceIdentifier.empty());
    }

    @Test
    public void testApplyToModificationWithWriteModificationType() {
        final DataTreeCandidate mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final DataTreeModification mockedModification = mock(DataTreeModification.class);

        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        doReturn(YangInstanceIdentifier.empty()).when(mockedDataTreeCandidate).getRootPath();
        final NormalizedNode mockedNormalizedNode = mock(NormalizedNode.class);
        doReturn(Optional.of(mockedNormalizedNode)).when(mockedDataTreeCandidateNode).getDataAfter();

        doReturn(ModificationType.WRITE).when(mockedDataTreeCandidateNode).getModificationType();

        DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate);
        verify(mockedModification, times(1)).write(any(YangInstanceIdentifier.class), any(NormalizedNode.class));
    }

    @Test
    public void testApplyToModificationWithSubtreeModifiedModificationType() {
        final DataTreeCandidate mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final DataTreeModification mockedModification = mock(DataTreeModification.class);

        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        doReturn(YangInstanceIdentifier.empty()).when(mockedDataTreeCandidate).getRootPath();

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).getModificationType();

        final DataTreeCandidateNode mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).getModificationType();
        doReturn(new NodeIdentifier(QName.create("test", "test1"))).when(mockedChildNode1).getIdentifier();

        final DataTreeCandidateNode mockedChildNode2 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.WRITE).when(mockedChildNode2).getModificationType();
        final NormalizedNode mockedNormalizedNode = mock(NormalizedNode.class);
        doReturn(Optional.of(mockedNormalizedNode)).when(mockedChildNode2).getDataAfter();
        doReturn(new NodeIdentifier(QName.create("test", "test2"))).when(mockedChildNode2).getIdentifier();

        final DataTreeCandidateNode mockedChildNode3 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedChildNode3).getModificationType();
        final DataTreeCandidateNode mockedChildNode3ChildNode = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode3ChildNode).getModificationType();
        doReturn(new NodeIdentifier(QName.create("test", "test3"))).when(mockedChildNode3).getIdentifier();
        doReturn(new NodeIdentifier(QName.create("test", "test4"))).when(mockedChildNode3ChildNode).getIdentifier();
        doReturn(List.of(mockedChildNode3ChildNode)).when(mockedChildNode3).getChildNodes();

        final List<DataTreeCandidateNode> childNodes = List.of(mockedChildNode1, mockedChildNode2, mockedChildNode3);
        doReturn(childNodes).when(mockedDataTreeCandidateNode).getChildNodes();

        DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate);
        verify(mockedModification, times(2)).delete(any(YangInstanceIdentifier.class));
        verify(mockedModification, times(1)).write(any(YangInstanceIdentifier.class), any(NormalizedNode.class));
    }

    @Test
    public void testApplyToModificationWithUnsupportedModificationType() {
        final DataTreeCandidate mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final DataTreeModification mockedModification = mock(DataTreeModification.class);

        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        doReturn(YangInstanceIdentifier.empty()).when(mockedDataTreeCandidate).getRootPath();

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).getModificationType();

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate));
        assertThat(ex.getMessage(), containsString("Unsupported modification"));
    }
}
