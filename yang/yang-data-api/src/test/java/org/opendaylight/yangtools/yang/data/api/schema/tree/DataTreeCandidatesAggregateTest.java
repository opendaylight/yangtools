/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class DataTreeCandidatesAggregateTest {
    private static final YangInstanceIdentifier ROOT_PATH = YangInstanceIdentifier.of(QName.create(
            "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test:container",
            "2014-03-13", "container"));
    private static final YangInstanceIdentifier.PathArgument CHILD_ID = new NodeIdentifier(QName.create(
            "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test:container:data",
            "2014-03-13", "data"));

    @Test
    public void testLeafUnmodifiedUnmodified() {
        NormalizedNode<?, ?> normalizedNode1 = normalizedNode("value1");
        NormalizedNode<?, ?> normalizedNode2 = normalizedNode("value1");
        NormalizedNode<?, ?> normalizedNode3 = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1, normalizedNode2,
                ModificationType.UNMODIFIED);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode2, normalizedNode3,
                ModificationType.UNMODIFIED);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.UNMODIFIED, aggregationResult.getRootNode().getModificationType());
        assertEquals("value1", aggregationResult.getRootNode().getDataBefore().get().getValue());
        assertEquals("value1", aggregationResult.getRootNode().getDataAfter().get().getValue());
    }

    @Test
    public void testLeaftUnmodifiedWrite() {
        NormalizedNode<?, ?> normalizedNode1 = normalizedNode("value1");
        NormalizedNode<?, ?> normalizedNode2 = normalizedNode("value1");
        NormalizedNode<?, ?> normalizedNode3 = normalizedNode("value2");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1, normalizedNode2,
                ModificationType.UNMODIFIED);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode2, normalizedNode3,
                ModificationType.WRITE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.WRITE, aggregationResult.getRootNode().getModificationType());
        assertEquals("value1", aggregationResult.getRootNode().getDataBefore().get().getValue());
        assertEquals("value2", aggregationResult.getRootNode().getDataAfter().get().getValue());
    }

    @Test
    public void testLeafUnmodifiedDelete() {
        NormalizedNode<?, ?> normalizedNode1 = normalizedNode("value1");
        NormalizedNode<?, ?> normalizedNode2 = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1, normalizedNode2,
                ModificationType.UNMODIFIED);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode2, null,
                ModificationType.DELETE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.DELETE, aggregationResult.getRootNode().getModificationType());
        assertEquals("value1", aggregationResult.getRootNode().getDataBefore().get().getValue());
        assertEquals(Optional.empty(), aggregationResult.getRootNode().getDataAfter());
    }

    @Test
    public void testLeafUnmodifiedDeleteWithoutDataBefore() {
        DataTreeCandidateNode node1 = dataTreeCandidateNode(null, null,
                ModificationType.UNMODIFIED);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null, null,
                ModificationType.DELETE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    @Test
    public void testLeafUnmodifiedSubtreeModifiedWithoutDataBefore() {
        DataTreeCandidateNode node1 = dataTreeCandidateNode(null, null,
                ModificationType.UNMODIFIED);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null, null,
                ModificationType.SUBTREE_MODIFIED);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    @Test
    public void testLeafWriteUnmodified() {
        NormalizedNode<?, ?> normalizedNode1 = normalizedNode("value1");
        NormalizedNode<?, ?> normalizedNode2 = normalizedNode("value2");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1, normalizedNode2,
                ModificationType.WRITE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode2, normalizedNode2,
                ModificationType.UNMODIFIED);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.WRITE, aggregationResult.getRootNode().getModificationType());
        assertEquals("value1", aggregationResult.getRootNode().getDataBefore().get().getValue());
        assertEquals("value2", aggregationResult.getRootNode().getDataAfter().get().getValue());
    }

    @Test
    public void testLeafWriteWrite() {
        NormalizedNode<?, ?> normalizedNode1 = normalizedNode("value1");
        NormalizedNode<?, ?> normalizedNode2 = normalizedNode("value2");
        NormalizedNode<?, ?> normalizedNode3 = normalizedNode("value3");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1, normalizedNode2,
                ModificationType.WRITE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode2, normalizedNode3,
                ModificationType.WRITE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.WRITE, aggregationResult.getRootNode().getModificationType());
        assertEquals("value1", aggregationResult.getRootNode().getDataBefore().get().getValue());
        assertEquals("value3", aggregationResult.getRootNode().getDataAfter().get().getValue());
    }

    @Test
    public void testLeafWriteDeleteWithoutChanges() {
        NormalizedNode<?, ?> normalizedNode = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(null, normalizedNode,
                ModificationType.WRITE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode, null,
                ModificationType.DELETE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.UNMODIFIED, aggregationResult.getRootNode().getModificationType());
        assertEquals(Optional.empty(), aggregationResult.getRootNode().getDataBefore());
        assertEquals(Optional.empty(), aggregationResult.getRootNode().getDataAfter());
    }

    @Test
    public void testLeafWriteDelete() {
        NormalizedNode<?, ?> normalizedNode1 = normalizedNode("value1");
        NormalizedNode<?, ?> normalizedNode2 = normalizedNode("value2");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1, normalizedNode2,
                ModificationType.WRITE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(normalizedNode2, null,
                ModificationType.DELETE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.DELETE, aggregationResult.getRootNode().getModificationType());
        assertEquals("value1", aggregationResult.getRootNode().getDataBefore().get().getValue());
        assertEquals(Optional.empty(), aggregationResult.getRootNode().getDataAfter());
    }

    @Test
    public void testLeafDeleteUnmodified() {
        NormalizedNode<?, ?> normalizedNode = normalizedNode("value");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode, null,
                ModificationType.DELETE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null, null,
                ModificationType.UNMODIFIED);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.DELETE, aggregationResult.getRootNode().getModificationType());
        assertEquals("value", aggregationResult.getRootNode().getDataBefore().get().getValue());
        assertEquals(Optional.empty(), aggregationResult.getRootNode().getDataAfter());
    }

    @Test
    public void testLeafDeleteWrite() {
        NormalizedNode<?, ?> normalizedNode1 = normalizedNode("value1");
        NormalizedNode<?, ?> normalizedNode2 = normalizedNode("value2");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1, null,
                ModificationType.DELETE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null, normalizedNode2,
                ModificationType.WRITE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.WRITE, aggregationResult.getRootNode().getModificationType());
        assertEquals("value1", aggregationResult.getRootNode().getDataBefore().get().getValue());
        assertEquals("value2", aggregationResult.getRootNode().getDataAfter().get().getValue());
    }

    @Test
    public void testLeafDeleteDelete() {
        NormalizedNode<?, ?> normalizedNode1 = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1, null,
                ModificationType.DELETE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null, null,
                ModificationType.DELETE);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    @Test
    public void testLeafDeleteDisappear() {
        NormalizedNode<?, ?> normalizedNode1 = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1, null,
                ModificationType.DELETE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null, null,
                ModificationType.DISAPPEARED);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    @Test
    public void testLeafDeleteSubtreeModified() {
        NormalizedNode<?, ?> normalizedNode1 = normalizedNode("value1");

        DataTreeCandidateNode node1 = dataTreeCandidateNode(normalizedNode1, null,
                ModificationType.DELETE);
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        DataTreeCandidateNode node2 = dataTreeCandidateNode(null, null,
                ModificationType.SUBTREE_MODIFIED);
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    @Test
    public void testUnmodifiedUnmodified() throws NoSuchFieldException {
        NormalizedNode<?, ?> parentNode = normalizedNode("container");
        NormalizedNode<?, ?> childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode, parentNode,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode, childNode,
                ModificationType.UNMODIFIED);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode, parentNode,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode, childNode,
                ModificationType.UNMODIFIED);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.UNMODIFIED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testUnmodifiedDelete() {
        NormalizedNode<?, ?> parentNode = normalizedNode("container");
        NormalizedNode<?, ?> childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode, parentNode,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode, childNode,
                ModificationType.UNMODIFIED);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode, null,
                ModificationType.DELETE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode, null,
                ModificationType.DELETE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.DELETE, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testUnmodifiedWrite() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container2");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, parentNode1,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, childNode1,
                ModificationType.UNMODIFIED);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, parentNode2,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.WRITE, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testUnmodifiedSubtreeModifiedWithoutDataBefore() {
        NormalizedNode<?, ?> parentNode = normalizedNode("container");
        NormalizedNode<?, ?> childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null, null,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null, null,
                ModificationType.UNMODIFIED);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode, parentNode,
                ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, childNode,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    //FIXME
    @Test
    public void testUnmodifiedSubtreeModified() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, parentNode1,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, childNode1,
                ModificationType.UNMODIFIED);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, parentNode2,
                ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.SUBTREE_MODIFIED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testUnmodifiedAppearedWithDataBefore() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, parentNode1,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, childNode1,
                ModificationType.UNMODIFIED);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, parentNode2,
                ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    //FIXME
    @Test
    public void testUnmodifiedAppeared() {
        NormalizedNode<?, ?> parentNode = normalizedNode("container");
        NormalizedNode<?, ?> childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null, null,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null, null,
                ModificationType.UNMODIFIED);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode, parentNode,
                ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, childNode,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.APPEARED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testUnmodifiedDisappearWithoutDataBefore() {
        NormalizedNode<?, ?> parentNode = normalizedNode("container");
        NormalizedNode<?, ?> childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null, null,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null, null,
                ModificationType.UNMODIFIED);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode, parentNode,
                ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, childNode,
                ModificationType.DELETE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    @Test
    public void testUnmodifiedDisappear() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, parentNode1,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, childNode1,
                ModificationType.UNMODIFIED);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, null,
                ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, null,
                ModificationType.DELETE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.DISAPPEARED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDeleteUnmodified() {
        NormalizedNode<?, ?> parentNode = normalizedNode("container");
        NormalizedNode<?, ?> childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode, null,
                ModificationType.DELETE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode, null,
                ModificationType.DELETE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null, null,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, null,
                ModificationType.UNMODIFIED);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.DELETE, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDeleteWrite() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container2");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, null,
                ModificationType.DELETE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, null,
                ModificationType.DELETE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null, parentNode2,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.WRITE, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDeleteAppear() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container2");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, null,
                ModificationType.DELETE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, null,
                ModificationType.DELETE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null, parentNode2,
                ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.WRITE, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteUnmodified() {
        NormalizedNode<?, ?> parentNode = normalizedNode("container");
        NormalizedNode<?, ?> childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null, parentNode,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null, childNode,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode, parentNode,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode, childNode,
                ModificationType.UNMODIFIED);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.WRITE, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteDeleteWithoutChanges() {
        NormalizedNode<?, ?> parentNode = normalizedNode("container");
        NormalizedNode<?, ?> childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null, parentNode,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null, childNode,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode, null,
                ModificationType.DELETE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode, null,
                ModificationType.DELETE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.UNMODIFIED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteDelete() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container2");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, parentNode2,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode2, null,
                ModificationType.DELETE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode2, null,
                ModificationType.DELETE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.DELETE, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteWrite() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container2");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null, parentNode1,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null, childNode1,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, parentNode2,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.WRITE, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteSubtreeModified() {
        NormalizedNode<?, ?> parentNode = normalizedNode("container");
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container1");
        NormalizedNode<?, ?> childNode = normalizedNode("child");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode, parentNode1,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode, childNode1,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, parentNode2,
                ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.WRITE, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteAppear() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container2");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");
        NormalizedNode<?, ?> childNode3 = normalizedNode("child3");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, parentNode2,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode2, parentNode2,
                ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode2, childNode3,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    @Test
    public void testWriteDisappearWithoutChanges() {
        NormalizedNode<?, ?> parentNode = normalizedNode("container");
        NormalizedNode<?, ?> childNode = normalizedNode("child");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null, parentNode,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null, childNode,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode, null,
                ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode, null,
                ModificationType.DELETE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.UNMODIFIED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testWriteDisappear() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container2");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, parentNode2,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode2, null,
                ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode2, null,
                ModificationType.DELETE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.DISAPPEARED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testSubtreeModifiedUnmodified() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container1");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, parentNode2,
                ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode2, parentNode2,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode2, childNode2,
                ModificationType.UNMODIFIED);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.SUBTREE_MODIFIED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testSubtreeModifiedDelete() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container1");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, parentNode2,
                ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode2, null,
                ModificationType.DELETE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode2, null,
                ModificationType.DELETE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.DELETE, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testSubtreeModifiedWrite() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("value2");
        NormalizedNode<?, ?> childNode = normalizedNode("childNode");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, parentNode1,
                ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode, childNode1,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, parentNode2,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.WRITE, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testSubtreeModifiedSubtreeModified() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> childNode = normalizedNode("childNode");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, parentNode1,
                ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode, childNode1,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, parentNode1,
                ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.SUBTREE_MODIFIED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testSubtreeModifiedAppear() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("value2");
        NormalizedNode<?, ?> childNode = normalizedNode("childNode");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, parentNode1,
                ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode, childNode1,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null, parentNode2,
                ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    @Test
    public void testSubtreeModifiedDisappear() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> childNode = normalizedNode("childNode");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, parentNode1,
                ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode, childNode1,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, null,
                ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, null,
                ModificationType.DELETE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.DISAPPEARED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testAppearedUnmodified() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null, parentNode1,
                ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null, childNode1,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, parentNode1,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, childNode1,
                ModificationType.UNMODIFIED);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.APPEARED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testAppearedDelete() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null, parentNode1,
                ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null, childNode1,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, null,
                ModificationType.DELETE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, null,
                ModificationType.DELETE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.UNMODIFIED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testAppearedWriteWithoutChanges() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("value2");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null, parentNode1,
                ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null, childNode1,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, parentNode2,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.WRITE, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testAppearedSubtreeModified() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null, parentNode1,
                ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null, childNode1,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, parentNode1,
                ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.APPEARED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testAppearedAppeared() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null, parentNode1,
                ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null, childNode1,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null, parentNode1,
                ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, childNode1,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    @Test
    public void testAppearedDisappeared() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(null, parentNode1,
                ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(null, childNode1,
                ModificationType.WRITE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(parentNode1, null,
                ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(childNode1, null,
                ModificationType.DELETE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.UNMODIFIED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDisappearedUnmodified() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, null,
                ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, null,
                ModificationType.DELETE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null, null,
                ModificationType.UNMODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, null,
                ModificationType.UNMODIFIED);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.DISAPPEARED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDisappearedDelete() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, null,
                ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, null,
                ModificationType.DELETE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null, null,
                ModificationType.DELETE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, null,
                ModificationType.DELETE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    @Test
    public void testDisappearedWrite() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container1");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container2");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, null,
                ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, null,
                ModificationType.DELETE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null, parentNode2,
                ModificationType.WRITE);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.WRITE, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDisappearedSubtreeModified() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, null,
                ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, null,
                ModificationType.DELETE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null, null,
                ModificationType.SUBTREE_MODIFIED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    @Test
    public void testDisappearedAppeared() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> parentNode2 = normalizedNode("container");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");
        NormalizedNode<?, ?> childNode2 = normalizedNode("child2");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, null,
                ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, null,
                ModificationType.DELETE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null, parentNode2,
                ModificationType.APPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, childNode2,
                ModificationType.WRITE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2));

        assertEquals(ModificationType.SUBTREE_MODIFIED, aggregationResult.getRootNode().getModificationType());
    }

    @Test
    public void testDisappearedDisappear() {
        NormalizedNode<?, ?> parentNode1 = normalizedNode("container");
        NormalizedNode<?, ?> childNode1 = normalizedNode("child1");

        TerminalDataTreeCandidateNode node1 = dataTreeCandidateNode(parentNode1, null,
                ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child1 = dataTreeCandidateNode(childNode1, null,
                ModificationType.DELETE);
        setChildNodes(node1, Collections.singletonList(child1));
        DataTreeCandidate candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        TerminalDataTreeCandidateNode node2 = dataTreeCandidateNode(null, null,
                ModificationType.DISAPPEARED);
        TerminalDataTreeCandidateNode child2 = dataTreeCandidateNode(null, null,
                ModificationType.DELETE);
        setChildNodes(node2, Collections.singletonList(child2));
        DataTreeCandidate candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(Arrays.asList(candidate1, candidate2)));
    }

    private static NormalizedNode<?, ?> normalizedNode(final String value) {
        NormalizedNode<?, ?> node = mock(NormalizedNode.class);
        doReturn(value).when(node).getValue();
        return node;
    }

    private static TerminalDataTreeCandidateNode dataTreeCandidateNode(final NormalizedNode<?, ?> before,
                                                                       final NormalizedNode<?, ?> after,
                                                                       final ModificationType modification) {
        TerminalDataTreeCandidateNode dataTreeCandidateNode = mock(TerminalDataTreeCandidateNode.class);
        doReturn(null).when(dataTreeCandidateNode).getIdentifier();
        doReturn(Optional.ofNullable(before)).when(dataTreeCandidateNode).getDataBefore();
        doReturn(Optional.ofNullable(after)).when(dataTreeCandidateNode).getDataAfter();
        doReturn(modification).when(dataTreeCandidateNode).getModificationType();
        return dataTreeCandidateNode;
    }

    private static void setChildNodes(final TerminalDataTreeCandidateNode parentNode,
                                      final List<DataTreeCandidateNode> childNodes) {
        when(parentNode.getIdentifier()).thenReturn(null);
        childNodes.forEach(child -> {
            when(child.getIdentifier()).thenReturn(CHILD_ID);
        });
        when(parentNode.getChildNodes()).thenReturn(childNodes);
    }

}
