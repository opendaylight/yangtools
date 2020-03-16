/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api.schema.tree;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataTreeCandidatesAggregateTest {

    final YangInstanceIdentifier mockedRootPath = mock(YangInstanceIdentifier.class);
    final YangInstanceIdentifier.PathArgument parentMockedIdentifier = mock(YangInstanceIdentifier.PathArgument.class);
    final YangInstanceIdentifier.PathArgument childMockedIdentifier = mock(YangInstanceIdentifier.PathArgument.class);

    @Test
    public void testLeafUnmodifiedUnmodified(){
        NormalizedNode normalizedNode1 = normalizedNode("value1");
        NormalizedNode normalizedNode2 = normalizedNode("value1");
        NormalizedNode normalizedNode3 = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1,normalizedNode2,ModificationType.UNMODIFIED);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode2,normalizedNode3,ModificationType.UNMODIFIED);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
        Assert.assertEquals("value1",aggregationResult.getRootNode().getDataBefore().get().getValue());
        Assert.assertEquals("value1",aggregationResult.getRootNode().getDataAfter().get().getValue());
    }

    @Test
    public void tesLeaftUnmodifiedWrite(){
        NormalizedNode normalizedNode1 = normalizedNode("value1");
        NormalizedNode normalizedNode2 = normalizedNode("value1");
        NormalizedNode normalizedNode3 = normalizedNode("value2");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1,normalizedNode2,ModificationType.UNMODIFIED);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode2,normalizedNode3, ModificationType.WRITE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.WRITE,aggregationResult.getRootNode().getModificationType());
        Assert.assertEquals("value1",aggregationResult.getRootNode().getDataBefore().get().getValue());
        Assert.assertEquals("value2",aggregationResult.getRootNode().getDataAfter().get().getValue());
    }

    @Test
    public void testLeafUnmodifiedDelete(){
        NormalizedNode normalizedNode1 = normalizedNode("value1");
        NormalizedNode normalizedNode2 = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1,normalizedNode2,ModificationType.UNMODIFIED);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode2,null,ModificationType.DELETE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.DELETE,aggregationResult.getRootNode().getModificationType());
        Assert.assertEquals("value1",aggregationResult.getRootNode().getDataBefore().get().getValue());
        Assert.assertEquals(Optional.empty(),aggregationResult.getRootNode().getDataAfter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLeafUnmodifiedDeleteWithoutDataBefore(){
        DataTreeCandidateNode node1 = dataTreeCandidateNode(null,null,ModificationType.UNMODIFIED);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null,null,ModificationType.DELETE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLeafUnmodifiedSubtreeModifiedWithoutDataBefore(){
        DataTreeCandidateNode node1 = dataTreeCandidateNode(null,null,ModificationType.UNMODIFIED);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null,null,ModificationType.SUBTREE_MODIFIED);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    @Test
    public void testLeafWriteUnmodified(){
        NormalizedNode normalizedNode1 = normalizedNode("value1");
        NormalizedNode normalizedNode2 = normalizedNode("value2");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1,normalizedNode2,ModificationType.WRITE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode2,normalizedNode2,ModificationType.UNMODIFIED);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.WRITE,aggregationResult.getRootNode().getModificationType());
        Assert.assertEquals("value1",aggregationResult.getRootNode().getDataBefore().get().getValue());
        Assert.assertEquals("value2",aggregationResult.getRootNode().getDataAfter().get().getValue());
    }

    @Test
    public void testLeafWriteWriteWithoutChanges(){

        NormalizedNode normalizedNode1 = normalizedNode("value1");
        NormalizedNode normalizedNode2 = normalizedNode("value2");
        NormalizedNode normalizedNode3 = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1,normalizedNode2,ModificationType.WRITE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode2,normalizedNode3,ModificationType.WRITE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
        Assert.assertEquals("value1",aggregationResult.getRootNode().getDataBefore().get().getValue());
        Assert.assertEquals("value1",aggregationResult.getRootNode().getDataAfter().get().getValue());
    }

    @Test
    public void testLeafWriteWrite(){
        NormalizedNode normalizedNode1 = normalizedNode("value1");
        NormalizedNode normalizedNode2 = normalizedNode("value2");
        NormalizedNode normalizedNode3 = normalizedNode("value3");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1,normalizedNode2,ModificationType.WRITE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode2,normalizedNode3,ModificationType.WRITE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.WRITE,aggregationResult.getRootNode().getModificationType());
        Assert.assertEquals("value1",aggregationResult.getRootNode().getDataBefore().get().getValue());
        Assert.assertEquals("value3",aggregationResult.getRootNode().getDataAfter().get().getValue());
    }

    @Test
    public void testLeafWriteDeleteWithoutChanges(){
        NormalizedNode normalizedNode = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(null,normalizedNode,ModificationType.WRITE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode,null,ModificationType.DELETE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
        Assert.assertEquals(Optional.empty(),aggregationResult.getRootNode().getDataBefore());
        Assert.assertEquals(Optional.empty(),aggregationResult.getRootNode().getDataAfter());
    }

    @Test
    public void testLeafWriteDelete(){
        NormalizedNode normalizedNode1 = normalizedNode("value1");
        NormalizedNode normalizedNode2 = normalizedNode("value2");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1,normalizedNode2,ModificationType.WRITE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode2,null,ModificationType.DELETE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.DELETE,aggregationResult.getRootNode().getModificationType());
        Assert.assertEquals("value1",aggregationResult.getRootNode().getDataBefore().get().getValue());
        Assert.assertEquals(Optional.empty(),aggregationResult.getRootNode().getDataAfter());
    }

    @Test
    public void testLeafDeleteUnmodified(){
        NormalizedNode normalizedNode = normalizedNode("value");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode,null,ModificationType.DELETE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null,null,ModificationType.UNMODIFIED);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.DELETE,aggregationResult.getRootNode().getModificationType());
        Assert.assertEquals("value",aggregationResult.getRootNode().getDataBefore().get().getValue());
        Assert.assertEquals(Optional.empty(),aggregationResult.getRootNode().getDataAfter());
    }

    @Test
    public void testLeafDeleteWriteWithoutChanges(){
        NormalizedNode normalizedNode1 = normalizedNode("value1");
        NormalizedNode normalizedNode2 = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1,null,ModificationType.DELETE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null,normalizedNode2,ModificationType.WRITE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
        Assert.assertEquals("value1",aggregationResult.getRootNode().getDataBefore().get().getValue());
        Assert.assertEquals("value1",aggregationResult.getRootNode().getDataAfter().get().getValue());
    }

    @Test
    public void testLeafDeleteWrite(){
        NormalizedNode normalizedNode1 = normalizedNode("value1");
        NormalizedNode normalizedNode2 = normalizedNode("value2");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1,null,ModificationType.DELETE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null,normalizedNode2,ModificationType.WRITE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.WRITE,aggregationResult.getRootNode().getModificationType());
        Assert.assertEquals("value1",aggregationResult.getRootNode().getDataBefore().get().getValue());
        Assert.assertEquals("value2",aggregationResult.getRootNode().getDataAfter().get().getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLeafDeleteDelete(){
        NormalizedNode normalizedNode1 = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1,null,ModificationType.DELETE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null,null,ModificationType.DELETE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLeafDeleteDisappear(){
        NormalizedNode normalizedNode1 = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1,null,ModificationType.DELETE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null,null,ModificationType.DISAPPEARED);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLeafDeleteSubtreeModified(){
        NormalizedNode normalizedNode1 = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1,null,ModificationType.DELETE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null,null,ModificationType.SUBTREE_MODIFIED);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    @Test
    public void testUnmodifiedUnmodified() throws NoSuchFieldException{
        NormalizedNode parentNode = normalizedNode("container");
        NormalizedNode childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode,parentNode,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode,childNode,ModificationType.UNMODIFIED);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode,parentNode,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode,childNode,ModificationType.UNMODIFIED);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testUnmodifiedDelete() throws NoSuchFieldException{
        NormalizedNode parentNode = normalizedNode("container");
        NormalizedNode childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode,parentNode,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode,childNode,ModificationType.UNMODIFIED);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode,null,ModificationType.DELETE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode,null,ModificationType.DELETE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.DELETE,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testUnmodifiedWrite() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode parentNode2 = normalizedNode("container2");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,childNode1,ModificationType.UNMODIFIED);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.WRITE,aggregationResult.getRootNode().getModificationType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiedSubtreeModifiedWithoutDataBefore() throws NoSuchFieldException{
        NormalizedNode parentNode = normalizedNode("container");
        NormalizedNode childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null,null,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null,null,ModificationType.UNMODIFIED);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode,parentNode,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    //FIXME
    @Test
    public void testUnmodifiedSubtreeModified() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode parentNode2 = normalizedNode("container1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,childNode1,ModificationType.UNMODIFIED);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.SUBTREE_MODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiedAppearedWithDataBefore() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode parentNode2 = normalizedNode("container1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,childNode1,ModificationType.UNMODIFIED);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    //FIXME
    @Test
    public void testUnmodifiedAppeared() throws NoSuchFieldException{
        NormalizedNode parentNode = normalizedNode("container");
        NormalizedNode childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null,null,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null,null,ModificationType.UNMODIFIED);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode,parentNode,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.APPEARED,aggregationResult.getRootNode().getModificationType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiedDisappearWithoutDataBefore() throws NoSuchFieldException{
        NormalizedNode parentNode = normalizedNode("container");
        NormalizedNode childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null,null,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null,null,ModificationType.UNMODIFIED);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode,parentNode,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode,ModificationType.DELETE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    //FIXME
    @Test
    public void testUnmodifiedDisappear() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,childNode1,ModificationType.UNMODIFIED);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.DISAPPEARED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDeleteUnmodified() throws NoSuchFieldException{
        NormalizedNode parentNode = normalizedNode("container");
        NormalizedNode childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode,null,ModificationType.DELETE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode,null,ModificationType.DELETE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,null,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,null,ModificationType.UNMODIFIED);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.DELETE,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDeleteWriteWithoutChanges() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode parentNode2 = normalizedNode("container");
        NormalizedNode childNode1 = normalizedNode("child");
        NormalizedNode childNode2 = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,null,ModificationType.DELETE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,parentNode2,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDeleteWrite() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode parentNode2 = normalizedNode("container2");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,null,ModificationType.DELETE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,parentNode2,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.WRITE,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDeleteAppearWithoutChanges() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode parentNode2 = normalizedNode("container");
        NormalizedNode childNode1 = normalizedNode("child");
        NormalizedNode childNode2 = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,null,ModificationType.DELETE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,parentNode2,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode2,ModificationType.APPEARED);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDeleteAppear() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode parentNode2 = normalizedNode("container2");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,null,ModificationType.DELETE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,parentNode2,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.WRITE,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteUnmodified() throws NoSuchFieldException{
        NormalizedNode parentNode = normalizedNode("container");
        NormalizedNode childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null,parentNode,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null,childNode,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode,parentNode,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode,childNode,ModificationType.UNMODIFIED);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.WRITE,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteDeleteWithoutChanges() throws NoSuchFieldException{
        NormalizedNode parentNode = normalizedNode("container");
        NormalizedNode childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null,parentNode,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null,childNode,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode,null,ModificationType.DELETE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode,null,ModificationType.DELETE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteDelete() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode parentNode2 = normalizedNode("container2");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode2,null,ModificationType.DELETE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode2,null,ModificationType.DELETE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.DELETE,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteWriteWithoutChanges() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode parentNode2 = normalizedNode("container2");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode2,parentNode1,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode2,childNode1,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteWrite() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode parentNode2 = normalizedNode("container2");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null,parentNode1,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.WRITE,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteSubtreeModifiedWithoutChanges() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode parentNode2 = normalizedNode("container1");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode2,parentNode1,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode2,childNode1,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteSubtreeModified() throws NoSuchFieldException{
        NormalizedNode parentNode = normalizedNode("container");
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode parentNode2 = normalizedNode("container1");
        NormalizedNode childNode = normalizedNode("child");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode,parentNode1,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.WRITE,aggregationResult.getRootNode().getModificationType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteAppear() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode parentNode2 = normalizedNode("container2");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");
        NormalizedNode childNode3 = normalizedNode("child3");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode2,parentNode2,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode2,childNode3,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    @Test
    public void testWriteDisappearWithoutChanges() throws NoSuchFieldException{
        NormalizedNode parentNode = normalizedNode("container");
        NormalizedNode childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null,parentNode,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null,childNode,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode,null,ModificationType.DELETE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteDisappear() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode parentNode2 = normalizedNode("container2");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode2,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode2,null,ModificationType.DELETE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.DISAPPEARED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testSubtreeModifiedUnmodified() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode parentNode2 = normalizedNode("container1");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode2,parentNode2,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode2,childNode2,ModificationType.UNMODIFIED);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.SUBTREE_MODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testSubtreeModifiedDelete() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode parentNode2 = normalizedNode("container1");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode2,null,ModificationType.DELETE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode2,null,ModificationType.DELETE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.DELETE,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testSubtreeModifiedWrite() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode parentNode2 = normalizedNode("value2");
        NormalizedNode childNode = normalizedNode("childNode");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.WRITE,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testSubtreeModifiedWriteWithoutChanges() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode parentNode2 = normalizedNode("value2");
        NormalizedNode childNode = normalizedNode("childNode");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,childNode,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testSubtreeModifiedSubtreeModifiedWithoutChanges() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode childNode = normalizedNode("childNode");
        NormalizedNode childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,childNode,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testSubtreeModifiedSubtreeModified() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode childNode = normalizedNode("childNode");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.SUBTREE_MODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtreeModifiedAppear() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode parentNode2 = normalizedNode("value2");
        NormalizedNode childNode = normalizedNode("childNode");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,parentNode2,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    @Test
    public void testSubtreeModifiedDisappear() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode childNode = normalizedNode("childNode");
        NormalizedNode childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.DISAPPEARED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testAppearedUnmodified() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null,parentNode1,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,childNode1,ModificationType.UNMODIFIED);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.APPEARED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testAppearedDelete() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null,parentNode1,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,null,ModificationType.DELETE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testAppearedWriteWithoutChanges() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode parentNode2 = normalizedNode("value2");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null,parentNode1,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,parentNode2,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.WRITE,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testAppearedSubtreeModified() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null,parentNode1,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,parentNode1,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.APPEARED,aggregationResult.getRootNode().getModificationType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppearedAppeared() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null,parentNode1,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,parentNode1,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode1,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    @Test
    public void testAppearedDisappeared() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null,parentNode1,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null,childNode1,ModificationType.WRITE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDisappearedUnmodified() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,null,ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,null,ModificationType.UNMODIFIED);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.DISAPPEARED,aggregationResult.getRootNode().getModificationType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisappearedDelete() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,null,ModificationType.DELETE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,null,ModificationType.DELETE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    @Test
    public void testDisappearedWriteWithoutChanges() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,parentNode1,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode1,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDisappearedWrite() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode parentNode2 = normalizedNode("container2");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,parentNode2,ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.WRITE,aggregationResult.getRootNode().getModificationType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisappearedSubtreeModified() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,null,ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    @Test
    public void testDisappearedAppearedWithoutChanges() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container1");
        NormalizedNode childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,parentNode1,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode1,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.UNMODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDisappearedAppeared() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode parentNode2 = normalizedNode("container");
        NormalizedNode childNode1 = normalizedNode("child1");
        NormalizedNode childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,parentNode2,ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,childNode2,ModificationType.WRITE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));

        Assert.assertEquals(ModificationType.SUBTREE_MODIFIED,aggregationResult.getRootNode().getModificationType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisappearedDisappear() throws NoSuchFieldException{
        NormalizedNode parentNode1 = normalizedNode("container");
        NormalizedNode childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1,null,ModificationType.DELETE);
        setChildNodes(node1,Arrays.asList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(mockedRootPath,node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null,null,ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null,null,ModificationType.DELETE);
        setChildNodes(node2,Arrays.asList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(mockedRootPath,node2);

        DataTreeCandidates.aggregate(Arrays.asList(candidate1,candidate2));
    }

    private NormalizedNode normalizedNode(String value){
        NormalizedNode node = mock(NormalizedNode.class);
        Mockito.when(node.getValue()).thenReturn(value);
        return node;
    }

    private TerminalDataTreeCandidateNode dataTreeCandidateNode(NormalizedNode before,NormalizedNode after, ModificationType modification){
        TerminalDataTreeCandidateNode dataTreeCandidateNode = mock(TerminalDataTreeCandidateNode.class);
        Mockito.when(dataTreeCandidateNode.getIdentifier()).thenReturn(childMockedIdentifier);
        Mockito.when(dataTreeCandidateNode.getDataBefore()).thenReturn(Optional.ofNullable(before));
        Mockito.when(dataTreeCandidateNode.getDataAfter()).thenReturn(Optional.ofNullable(after));
        Mockito.when(dataTreeCandidateNode.getModificationType()).thenReturn(modification);
        return dataTreeCandidateNode;
    }

    private void setChildNodes(TerminalDataTreeCandidateNode parentNode, List<DataTreeCandidateNode> childNodes) throws NoSuchFieldException {
        when(parentNode.getIdentifier()).thenReturn(parentMockedIdentifier);
        when(parentNode.getChildNodes()).thenReturn(childNodes);
    }

}
