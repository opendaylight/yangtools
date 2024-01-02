/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.opendaylight.yangtools.yang.data.tree.api.ModificationType.APPEARED;
import static org.opendaylight.yangtools.yang.data.tree.api.ModificationType.DELETE;
import static org.opendaylight.yangtools.yang.data.tree.api.ModificationType.DISAPPEARED;
import static org.opendaylight.yangtools.yang.data.tree.api.ModificationType.SUBTREE_MODIFIED;
import static org.opendaylight.yangtools.yang.data.tree.api.ModificationType.UNMODIFIED;
import static org.opendaylight.yangtools.yang.data.tree.api.ModificationType.WRITE;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

@ExtendWith(MockitoExtension.class)
class DataTreeCandidatesAggregateTest {
    private static final YangInstanceIdentifier ROOT_PATH = YangInstanceIdentifier.of(QName.create(
            "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test:container",
            "2014-03-13", "container"));
    private static final YangInstanceIdentifier.PathArgument CHILD_ID = new NodeIdentifier(QName.create(
            "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test:container:data",
            "2014-03-13", "data"));

    @Test
    void testLeafUnmodifiedUnmodified() {
        final var normalizedNode1 = normalizedNode("value1");
        final var normalizedNode2 = normalizedNode("value1");
        final var normalizedNode3 = normalizedNode("value1");

        final var node1 = dataTreeCandidateNode(normalizedNode1, normalizedNode2, UNMODIFIED);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(normalizedNode2, normalizedNode3, UNMODIFIED);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(UNMODIFIED, aggregationResult.getRootNode().modificationType());
        assertEquals("value1", aggregationResult.getRootNode().dataBefore().body());
        assertEquals("value1", aggregationResult.getRootNode().dataAfter().body());
    }

    @Test
    void testLeaftUnmodifiedWrite() {
        final var normalizedNode1 = normalizedNode("value1");
        final var normalizedNode2 = normalizedNode("value1");
        final var normalizedNode3 = normalizedNode("value2");

        final var node1 = dataTreeCandidateNode(normalizedNode1, normalizedNode2, UNMODIFIED);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(normalizedNode2, normalizedNode3, WRITE);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(WRITE, aggregationResult.getRootNode().modificationType());
        assertEquals("value1", aggregationResult.getRootNode().dataBefore().body());
        assertEquals("value2", aggregationResult.getRootNode().dataAfter().body());
    }

    @Test
    void testLeafUnmodifiedDelete() {
        final var normalizedNode1 = normalizedNode("value1");
        final var normalizedNode2 = normalizedNode("value1");

        final var node1 = dataTreeCandidateNode(normalizedNode1, normalizedNode2, UNMODIFIED);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(normalizedNode2, null, DELETE);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        DataTreeCandidate aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(DELETE, aggregationResult.getRootNode().modificationType());
        assertEquals("value1", aggregationResult.getRootNode().dataBefore().body());
        assertNull(aggregationResult.getRootNode().dataAfter());
    }

    @Test
    void testLeafUnmodifiedDeleteWithoutDataBefore() {
        final var node1 = dataTreeCandidateNode(null, null, UNMODIFIED);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, null, DELETE);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    @Test
    void testLeafUnmodifiedSubtreeModifiedWithoutDataBefore() {
        final var node1 = dataTreeCandidateNode(null, null, UNMODIFIED);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, null, SUBTREE_MODIFIED);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    @Test
    void testLeafWriteUnmodified() {
        final var normalizedNode1 = normalizedNode("value1");
        final var normalizedNode2 = normalizedNode("value2");

        final var node1 = dataTreeCandidateNode(normalizedNode1, normalizedNode2, WRITE);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(normalizedNode2, normalizedNode2, UNMODIFIED);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(WRITE, aggregationResult.getRootNode().modificationType());
        assertEquals("value1", aggregationResult.getRootNode().dataBefore().body());
        assertEquals("value2", aggregationResult.getRootNode().dataAfter().body());
    }

    @Test
    void testLeafWriteWrite() {
        final var normalizedNode1 = normalizedNode("value1");
        final var normalizedNode2 = normalizedNode("value2");
        final var normalizedNode3 = normalizedNode("value3");

        final var node1 = dataTreeCandidateNode(normalizedNode1, normalizedNode2, WRITE);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(normalizedNode2, normalizedNode3, WRITE);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(WRITE, aggregationResult.getRootNode().modificationType());
        assertEquals("value1", aggregationResult.getRootNode().dataBefore().body());
        assertEquals("value3", aggregationResult.getRootNode().dataAfter().body());
    }

    @Test
    void testLeafWriteDeleteWithoutChanges() {
        final var normalizedNode = normalizedNode("value1");

        final var node1 = dataTreeCandidateNode(null, normalizedNode, WRITE);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(normalizedNode, null, DELETE);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(UNMODIFIED, aggregationResult.getRootNode().modificationType());
        assertNull(aggregationResult.getRootNode().dataBefore());
        assertNull(aggregationResult.getRootNode().dataAfter());
    }

    @Test
    void testLeafWriteDelete() {
        final var normalizedNode1 = normalizedNode("value1");
        final var normalizedNode2 = normalizedNode("value2");

        final var node1 = dataTreeCandidateNode(normalizedNode1, normalizedNode2, WRITE);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(normalizedNode2, null, DELETE);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(DELETE, aggregationResult.getRootNode().modificationType());
        assertEquals("value1", aggregationResult.getRootNode().dataBefore().body());
        assertNull(aggregationResult.getRootNode().dataAfter());
    }

    @Test
    void testLeafDeleteUnmodified() {
        final var normalizedNode = normalizedNode("value");

        final var node1 = dataTreeCandidateNode(normalizedNode, null, DELETE);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, null, UNMODIFIED);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(DELETE, aggregationResult.getRootNode().modificationType());
        assertEquals("value", aggregationResult.getRootNode().dataBefore().body());
        assertNull(aggregationResult.getRootNode().dataAfter());
    }

    @Test
    void testLeafDeleteWrite() {
        final var normalizedNode1 = normalizedNode("value1");
        final var normalizedNode2 = normalizedNode("value2");

        final var node1 = dataTreeCandidateNode(normalizedNode1, null, DELETE);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, normalizedNode2, WRITE);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(WRITE, aggregationResult.getRootNode().modificationType());
        assertEquals("value1", aggregationResult.getRootNode().dataBefore().body());
        assertEquals("value2", aggregationResult.getRootNode().dataAfter().body());
    }

    @Test
    void testLeafDeleteDelete() {
        final var normalizedNode1 = normalizedNode("value1");

        final var node1 = dataTreeCandidateNode(normalizedNode1, null, DELETE);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, null, DELETE);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    @Test
    void testLeafDeleteDisappear() {
        final var normalizedNode1 = normalizedNode("value1");

        final var node1 = dataTreeCandidateNode(normalizedNode1, null, DELETE);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, null, DISAPPEARED);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    @Test
    void testLeafDeleteSubtreeModified() {
        final var normalizedNode1 = normalizedNode("value1");

        final var node1 = dataTreeCandidateNode(normalizedNode1, null, DELETE);
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, null, SUBTREE_MODIFIED);
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    @Test
    void testUnmodifiedUnmodified() throws NoSuchFieldException {
        final var parentNode = normalizedNode("container");
        final var childNode = normalizedNode("child");

        final var node1 = dataTreeCandidateNode(parentNode, parentNode, UNMODIFIED);
        final var child1 = dataTreeCandidateNode(childNode, childNode, UNMODIFIED);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode, parentNode, UNMODIFIED);
        final var child2 = dataTreeCandidateNode(childNode, childNode, UNMODIFIED);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(UNMODIFIED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testUnmodifiedDelete() {
        final var parentNode = normalizedNode("container");
        final var childNode = normalizedNode("child");

        final var node1 = dataTreeCandidateNode(parentNode, parentNode, UNMODIFIED);
        final var child1 = dataTreeCandidateNode(childNode, childNode, UNMODIFIED);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode, null, DELETE);
        final var child2 = dataTreeCandidateNode(childNode, null, DELETE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(DELETE, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testUnmodifiedWrite() {
        final var parentNode1 = normalizedNode("container1");
        final var childNode1 = normalizedNode("child1");
        final var parentNode2 = normalizedNode("container2");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, parentNode1, UNMODIFIED);
        final var child1 = dataTreeCandidateNode(childNode1, childNode1, UNMODIFIED);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, parentNode2, WRITE);
        final var child2 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(WRITE, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testUnmodifiedSubtreeModifiedWithoutDataBefore() {
        final var parentNode = normalizedNode("container");
        final var childNode = normalizedNode("child");

        final var node1 = dataTreeCandidateNode(null, null, UNMODIFIED);
        final var child1 = dataTreeCandidateNode(null, null, UNMODIFIED);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode, parentNode, SUBTREE_MODIFIED);
        final var child2 = dataTreeCandidateNode(null, childNode, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    //FIXME
    @Test
    void testUnmodifiedSubtreeModified() {
        final var parentNode1 = normalizedNode("container1");
        final var childNode1 = normalizedNode("child1");
        final var parentNode2 = normalizedNode("container1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, parentNode1, UNMODIFIED);
        final var child1 = dataTreeCandidateNode(childNode1, childNode1, UNMODIFIED);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, parentNode2, SUBTREE_MODIFIED);
        final var child2 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(SUBTREE_MODIFIED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testUnmodifiedAppearedWithDataBefore() {
        final var parentNode1 = normalizedNode("container1");
        final var childNode1 = normalizedNode("child1");
        final var parentNode2 = normalizedNode("container1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, parentNode1, UNMODIFIED);
        final var child1 = dataTreeCandidateNode(childNode1, childNode1, UNMODIFIED);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, parentNode2, APPEARED);
        final var child2 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    //FIXME
    @Test
    void testUnmodifiedAppeared() {
        final var parentNode = normalizedNode("container");
        final var childNode = normalizedNode("child");

        final var node1 = dataTreeCandidateNode(null, null, UNMODIFIED);
        final var child1 = dataTreeCandidateNode(null, null, UNMODIFIED);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode, parentNode, APPEARED);
        final var child2 = dataTreeCandidateNode(null, childNode, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(APPEARED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testUnmodifiedDisappearWithoutDataBefore() {
        final var parentNode = normalizedNode("container");
        final var childNode = normalizedNode("child");

        final var node1 = dataTreeCandidateNode(null, null, UNMODIFIED);
        final var child1 = dataTreeCandidateNode(null, null, UNMODIFIED);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode, parentNode, DISAPPEARED);
        final var child2 = dataTreeCandidateNode(null, childNode, DELETE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    @Test
    void testUnmodifiedDisappear() {
        final var parentNode1 = normalizedNode("container1");
        final var childNode1 = normalizedNode("child1");

        final var node1 = dataTreeCandidateNode(parentNode1, parentNode1, UNMODIFIED);
        final var child1 = dataTreeCandidateNode(childNode1, childNode1, UNMODIFIED);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, null, DISAPPEARED);
        final var child2 = dataTreeCandidateNode(childNode1, null, DELETE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(DISAPPEARED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testDeleteUnmodified() {
        final var parentNode = normalizedNode("container");
        final var childNode = normalizedNode("child");

        final var node1 = dataTreeCandidateNode(parentNode, null, DELETE);
        final var child1 = dataTreeCandidateNode(childNode, null, DELETE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, null, UNMODIFIED);
        final var child2 = dataTreeCandidateNode(null, null, UNMODIFIED);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(DELETE, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testDeleteWrite() {
        final var parentNode1 = normalizedNode("container1");
        final var parentNode2 = normalizedNode("container2");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, null, DELETE);
        final var child1 = dataTreeCandidateNode(childNode1, null, DELETE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, parentNode2, WRITE);
        final var child2 = dataTreeCandidateNode(null, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(WRITE, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testDeleteAppear() {
        final var parentNode1 = normalizedNode("container1");
        final var parentNode2 = normalizedNode("container2");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, null, DELETE);
        final var child1 = dataTreeCandidateNode(childNode1, null, DELETE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, parentNode2, APPEARED);
        final var child2 = dataTreeCandidateNode(null, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(WRITE, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testWriteUnmodified() {
        final var parentNode = normalizedNode("container");
        final var childNode = normalizedNode("child");

        final var node1 = dataTreeCandidateNode(null, parentNode, WRITE);
        final var child1 = dataTreeCandidateNode(null, childNode, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode, parentNode, UNMODIFIED);
        final var child2 = dataTreeCandidateNode(childNode, childNode, UNMODIFIED);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(WRITE, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testWriteDeleteWithoutChanges() {
        final var parentNode = normalizedNode("container");
        final var childNode = normalizedNode("child");

        final var node1 = dataTreeCandidateNode(null, parentNode, WRITE);
        final var child1 = dataTreeCandidateNode(null, childNode, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode, null, DELETE);
        final var child2 = dataTreeCandidateNode(childNode, null, DELETE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(UNMODIFIED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testWriteDelete() {
        final var parentNode1 = normalizedNode("container1");
        final var parentNode2 = normalizedNode("container2");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, parentNode2, WRITE);
        final var child1 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode2, null, DELETE);
        final var child2 = dataTreeCandidateNode(childNode2, null, DELETE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(DELETE, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testWriteWrite() {
        final var parentNode1 = normalizedNode("container1");
        final var parentNode2 = normalizedNode("container2");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(null, parentNode1, WRITE);
        final var child1 = dataTreeCandidateNode(null, childNode1, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, parentNode2, WRITE);
        final var child2 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(WRITE, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testWriteSubtreeModified() {
        final var parentNode = normalizedNode("container");
        final var parentNode1 = normalizedNode("container1");
        final var parentNode2 = normalizedNode("container1");
        final var childNode = normalizedNode("child");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode, parentNode1, WRITE);
        final var child1 = dataTreeCandidateNode(childNode, childNode1, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, parentNode2, SUBTREE_MODIFIED);
        final var child2 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(WRITE, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testWriteAppear() {
        final var parentNode1 = normalizedNode("container1");
        final var parentNode2 = normalizedNode("container2");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");
        final var childNode3 = normalizedNode("child3");

        final var node1 = dataTreeCandidateNode(parentNode1, parentNode2, WRITE);
        final var child1 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode2, parentNode2, APPEARED);
        final var child2 = dataTreeCandidateNode(childNode2, childNode3, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    @Test
    void testWriteDisappearWithoutChanges() {
        final var parentNode = normalizedNode("container");
        final var childNode = normalizedNode("child");

        final var node1 = dataTreeCandidateNode(null, parentNode, WRITE);
        final var child1 = dataTreeCandidateNode(null, childNode, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode, null, DISAPPEARED);
        final var child2 = dataTreeCandidateNode(childNode, null, DELETE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(UNMODIFIED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testWriteDisappear() {
        final var parentNode1 = normalizedNode("container1");
        final var parentNode2 = normalizedNode("container2");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, parentNode2, WRITE);
        final var child1 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode2, null, DISAPPEARED);
        final var child2 = dataTreeCandidateNode(childNode2, null, DELETE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(DISAPPEARED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testSubtreeModifiedUnmodified() {
        final var parentNode1 = normalizedNode("container1");
        final var parentNode2 = normalizedNode("container1");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, parentNode2, SUBTREE_MODIFIED);
        final var child1 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode2, parentNode2, UNMODIFIED);
        final var child2 = dataTreeCandidateNode(childNode2, childNode2, UNMODIFIED);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(SUBTREE_MODIFIED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testSubtreeModifiedDelete() {
        final var parentNode1 = normalizedNode("container1");
        final var parentNode2 = normalizedNode("container1");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, parentNode2, SUBTREE_MODIFIED);
        final var child1 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode2, null, DELETE);
        final var child2 = dataTreeCandidateNode(childNode2, null, DELETE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(DELETE, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testSubtreeModifiedWrite() {
        final var parentNode1 = normalizedNode("container");
        final var parentNode2 = normalizedNode("value2");
        final var childNode = normalizedNode("childNode");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, parentNode1, SUBTREE_MODIFIED);
        final var child1 = dataTreeCandidateNode(childNode, childNode1, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, parentNode2, WRITE);
        final var child2 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(WRITE, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testSubtreeModifiedSubtreeModified() {
        final var parentNode1 = normalizedNode("container");
        final var childNode = normalizedNode("childNode");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, parentNode1, SUBTREE_MODIFIED);
        final var child1 = dataTreeCandidateNode(childNode, childNode1, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, parentNode1, SUBTREE_MODIFIED);
        final var child2 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(SUBTREE_MODIFIED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testSubtreeModifiedAppear() {
        final var parentNode1 = normalizedNode("container");
        final var parentNode2 = normalizedNode("value2");
        final var childNode = normalizedNode("childNode");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, parentNode1, SUBTREE_MODIFIED);
        final var child1 = dataTreeCandidateNode(childNode, childNode1, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, parentNode2, APPEARED);
        final var child2 = dataTreeCandidateNode(null, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    @Test
    void testSubtreeModifiedDisappear() {
        final var parentNode1 = normalizedNode("container");
        final var childNode = normalizedNode("childNode");
        final var childNode1 = normalizedNode("child1");

        final var node1 = dataTreeCandidateNode(parentNode1, parentNode1, SUBTREE_MODIFIED);
        final var child1 = dataTreeCandidateNode(childNode, childNode1, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, null, DISAPPEARED);
        final var child2 = dataTreeCandidateNode(childNode1, null, DELETE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(DISAPPEARED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testAppearedUnmodified() {
        final var parentNode1 = normalizedNode("container");
        final var childNode1 = normalizedNode("child1");

        final var node1 = dataTreeCandidateNode(null, parentNode1, APPEARED);
        final var child1 = dataTreeCandidateNode(null, childNode1, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, parentNode1, UNMODIFIED);
        final var child2 = dataTreeCandidateNode(childNode1, childNode1, UNMODIFIED);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(APPEARED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testAppearedDelete() {
        final var parentNode1 = normalizedNode("container");
        final var childNode1 = normalizedNode("child1");

        final var node1 = dataTreeCandidateNode(null, parentNode1, APPEARED);
        final var child1 = dataTreeCandidateNode(null, childNode1, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, null, DELETE);
        final var child2 = dataTreeCandidateNode(childNode1, null, DELETE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(UNMODIFIED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testAppearedWriteWithoutChanges() {
        final var parentNode1 = normalizedNode("container");
        final var parentNode2 = normalizedNode("value2");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(null, parentNode1, APPEARED);
        final var child1 = dataTreeCandidateNode(null, childNode1, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, parentNode2, WRITE);
        final var child2 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(WRITE, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testAppearedSubtreeModified() {
        final var parentNode1 = normalizedNode("container");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(null, parentNode1, APPEARED);
        final var child1 = dataTreeCandidateNode(null, childNode1, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, parentNode1, SUBTREE_MODIFIED);
        final var child2 = dataTreeCandidateNode(childNode1, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(APPEARED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testAppearedAppeared() {
        final var parentNode1 = normalizedNode("container");
        final var childNode1 = normalizedNode("child1");

        final var node1 = dataTreeCandidateNode(null, parentNode1, APPEARED);
        final var child1 = dataTreeCandidateNode(null, childNode1, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, parentNode1, APPEARED);
        final var child2 = dataTreeCandidateNode(null, childNode1, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    @Test
    void testAppearedDisappeared() {
        final var parentNode1 = normalizedNode("container");
        final var childNode1 = normalizedNode("child1");

        final var node1 = dataTreeCandidateNode(null, parentNode1, APPEARED);
        final var child1 = dataTreeCandidateNode(null, childNode1, WRITE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(parentNode1, null, DISAPPEARED);
        final var child2 = dataTreeCandidateNode(childNode1, null, DELETE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(UNMODIFIED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testDisappearedUnmodified() {
        final var parentNode1 = normalizedNode("container");
        final var childNode1 = normalizedNode("child1");

        final var node1 = dataTreeCandidateNode(parentNode1, null, DISAPPEARED);
        final var child1 = dataTreeCandidateNode(childNode1, null, DELETE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, null, UNMODIFIED);
        final var child2 = dataTreeCandidateNode(null, null, UNMODIFIED);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(DISAPPEARED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testDisappearedDelete() {
        final var parentNode1 = normalizedNode("container");
        final var childNode1 = normalizedNode("child1");

        final var node1 = dataTreeCandidateNode(parentNode1, null, DISAPPEARED);
        final var child1 = dataTreeCandidateNode(childNode1, null, DELETE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, null, DELETE);
        final var child2 = dataTreeCandidateNode(null, null, DELETE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    @Test
    void testDisappearedWrite() {
        final var parentNode1 = normalizedNode("container1");
        final var parentNode2 = normalizedNode("container2");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, null, DISAPPEARED);
        final var child1 = dataTreeCandidateNode(childNode1, null, DELETE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, parentNode2, WRITE);
        final var child2 = dataTreeCandidateNode(null, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(WRITE, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testDisappearedSubtreeModified() {
        final var parentNode1 = normalizedNode("container");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, null, DISAPPEARED);
        final var child1 = dataTreeCandidateNode(childNode1, null, DELETE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, null, SUBTREE_MODIFIED);
        final var child2 = dataTreeCandidateNode(null, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    @Test
    void testDisappearedAppeared() {
        final var parentNode1 = normalizedNode("container");
        final var parentNode2 = normalizedNode("container");
        final var childNode1 = normalizedNode("child1");
        final var childNode2 = normalizedNode("child2");

        final var node1 = dataTreeCandidateNode(parentNode1, null, DISAPPEARED);
        final var child1 = dataTreeCandidateNode(childNode1, null, DELETE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, parentNode2, APPEARED);
        final var child2 = dataTreeCandidateNode(null, childNode2, WRITE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        final var aggregationResult = DataTreeCandidates.aggregate(List.of(candidate1, candidate2));

        assertEquals(SUBTREE_MODIFIED, aggregationResult.getRootNode().modificationType());
    }

    @Test
    void testDisappearedDisappear() {
        final var parentNode1 = normalizedNode("container");
        final var childNode1 = normalizedNode("child1");

        final var node1 = dataTreeCandidateNode(parentNode1, null, DISAPPEARED);
        final var child1 = dataTreeCandidateNode(childNode1, null, DELETE);
        setChildNodes(node1, List.of(child1));
        final var candidate1 = new DefaultDataTreeCandidate(ROOT_PATH, node1);

        final var node2 = dataTreeCandidateNode(null, null, DISAPPEARED);
        final var child2 = dataTreeCandidateNode(null, null, DELETE);
        setChildNodes(node2, List.of(child2));
        final var candidate2 = new DefaultDataTreeCandidate(ROOT_PATH, node2);

        assertThrows(IllegalArgumentException.class,
            () -> DataTreeCandidates.aggregate(List.of(candidate1, candidate2)));
    }

    private static LeafNode<String> normalizedNode(final String value) {
        return ImmutableNodes.leafNode(QName.create("foo", "qn" + value), value);
    }

    private static TerminalDataTreeCandidateNode dataTreeCandidateNode(final NormalizedNode before,
                                                                       final NormalizedNode after,
                                                                       final ModificationType modification) {
        return new TerminalDataTreeCandidateNode(null, modification, before, after);
    }

    private static void setChildNodes(final TerminalDataTreeCandidateNode parentNode,
                                      final List<DataTreeCandidateNode> childNodes) {
        doReturn(null).when(parentNode).name();
        childNodes.forEach(child -> doReturn(CHILD_ID).when(child).name());
        doReturn(childNodes).when(parentNode).childNodes();
    }
}
