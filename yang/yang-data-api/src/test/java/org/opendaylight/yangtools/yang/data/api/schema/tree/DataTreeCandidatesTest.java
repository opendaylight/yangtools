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

public class DataTreeCandidatesTest {

    @Test
    public void testNewDataTreeCandidate() {
        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        final DataTreeCandidate dataTreeCandidate = DataTreeCandidates.newDataTreeCandidate(mockedRootPath,
                mockedDataTreeCandidateNode);

        assertNotNull(dataTreeCandidate);
        assertTrue(dataTreeCandidate instanceof DefaultDataTreeCandidate);
        assertEquals(mockedRootPath, dataTreeCandidate.getRootPath());
        assertEquals(mockedDataTreeCandidateNode, dataTreeCandidate.getRootNode());
        assertTrue(dataTreeCandidate.toString().contains(
                "DefaultDataTreeCandidate{rootPath=/, rootNode=Mock for DataTreeCandidateNode, hashCode: "));
    }

    @Test
    public void testFromNormalizedNode() {
        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        final NormalizedNode<?, ?> mockedNormalizedNode = mock(NormalizedNode.class);
        final DataTreeCandidate dataTreeCandidate = DataTreeCandidates.fromNormalizedNode(mockedRootPath,
                mockedNormalizedNode);

        assertNotNull(dataTreeCandidate);
        assertTrue(dataTreeCandidate instanceof DefaultDataTreeCandidate);
        assertEquals(mockedRootPath, dataTreeCandidate.getRootPath());
        assertTrue(dataTreeCandidate.getRootNode() instanceof NormalizedNodeDataTreeCandidateNode);
    }

    @Test
    public void testApplyToCursor() {
        final DataTreeCandidate mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);

        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).getModificationType();
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        DataTreeCandidates.applyToCursor(mockedCursor, mockedDataTreeCandidate);
        verify(mockedCursor, times(1)).delete(any(PathArgument.class));
    }

    @Test
    public void testApplyToCursorAwareModification() {
        final DataTreeCandidate mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final CursorAwareDataTreeModification mockedModification = mock(CursorAwareDataTreeModification.class);

        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        doReturn(mockedRootPath).when(mockedDataTreeCandidate).getRootPath();
        final DataTreeModificationCursor mockedCursor = mock(DataTreeModificationCursor.class);
        doReturn(mockedCursor).when(mockedModification).createCursor(any(YangInstanceIdentifier.class));
        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).getModificationType();

        DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate);
        verify(mockedModification, times(1)).createCursor(any(YangInstanceIdentifier.class));
        verify(mockedCursor, times(1)).delete(any(PathArgument.class));

        doReturn(true).when(mockedRootPath).isEmpty();
        try {
            DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate);
            fail("An IllegalArgumentException should have been thrown!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Can not delete root"));
        }
    }

    @Test
    public void testApplyToModificationWithDeleteModificationType() {
        final DataTreeCandidate mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final DataTreeModification mockedModification = mock(DataTreeModification.class);

        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        doReturn(mockedRootPath).when(mockedDataTreeCandidate).getRootPath();

        doReturn(ModificationType.DELETE).when(mockedDataTreeCandidateNode).getModificationType();

        DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate);
        verify(mockedModification, times(1)).delete(any(YangInstanceIdentifier.class));
    }

    @Test
    public void testApplyToModificationWithWriteModificationType() {
        final DataTreeCandidate mockedDataTreeCandidate = mock(DataTreeCandidate.class);
        final DataTreeModification mockedModification = mock(DataTreeModification.class);

        final DataTreeCandidateNode mockedDataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(mockedDataTreeCandidateNode).when(mockedDataTreeCandidate).getRootNode();
        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        doReturn(mockedRootPath).when(mockedDataTreeCandidate).getRootPath();
        final NormalizedNode<?, ?> mockedNormalizedNode = mock(NormalizedNode.class);
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
        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        doReturn(mockedRootPath).when(mockedDataTreeCandidate).getRootPath();

        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedDataTreeCandidateNode).getModificationType();

        final DataTreeCandidateNode mockedChildNode1 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode1).getModificationType();
        final PathArgument mockedPathArgument1 = mock(PathArgument.class);
        doReturn(mockedPathArgument1).when(mockedChildNode1).getIdentifier();

        final DataTreeCandidateNode mockedChildNode2 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.WRITE).when(mockedChildNode2).getModificationType();
        final NormalizedNode<?, ?> mockedNormalizedNode = mock(NormalizedNode.class);
        doReturn(Optional.of(mockedNormalizedNode)).when(mockedChildNode2).getDataAfter();
        final PathArgument mockedPathArgument2 = mock(PathArgument.class);
        doReturn(mockedPathArgument2).when(mockedChildNode2).getIdentifier();

        final DataTreeCandidateNode mockedChildNode3 = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.SUBTREE_MODIFIED).when(mockedChildNode3).getModificationType();
        final PathArgument mockedPathArgument3 = mock(PathArgument.class);
        doReturn(mockedPathArgument3).when(mockedChildNode3).getIdentifier();
        final DataTreeCandidateNode mockedChildNode3ChildNode = mock(DataTreeCandidateNode.class);
        doReturn(ModificationType.DELETE).when(mockedChildNode3ChildNode).getModificationType();
        final PathArgument mockedPathArgument31 = mock(PathArgument.class);
        doReturn(mockedPathArgument3).when(mockedChildNode3).getIdentifier();
        doReturn(mockedPathArgument31).when(mockedChildNode3ChildNode).getIdentifier();
        doReturn(Lists.newArrayList(mockedChildNode3ChildNode)).when(mockedChildNode3).getChildNodes();

        final Collection<DataTreeCandidateNode> childNodes = Lists.newArrayList(mockedChildNode1, mockedChildNode2,
                mockedChildNode3);
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
        final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
        doReturn(mockedRootPath).when(mockedDataTreeCandidate).getRootPath();

        doReturn(ModificationType.APPEARED).when(mockedDataTreeCandidateNode).getModificationType();

        try {
            DataTreeCandidates.applyToModification(mockedModification, mockedDataTreeCandidate);
            fail("An IllegalArgumentException should have been thrown!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Unsupported modification"));
        }
    }
}
