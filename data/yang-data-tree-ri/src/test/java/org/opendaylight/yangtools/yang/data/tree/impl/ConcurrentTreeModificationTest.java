/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConcurrentTreeModificationTest extends AbstractTestModelTest {
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentTreeModificationTest.class);

    private static final Short ONE_ID = 1;
    private static final Short TWO_ID = 2;

    private static final YangInstanceIdentifier OUTER_LIST_1_PATH = YangInstanceIdentifier.builder(
        TestModel.OUTER_LIST_PATH)
            .nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, ONE_ID)
            .build();

    private static final YangInstanceIdentifier OUTER_LIST_2_PATH = YangInstanceIdentifier.builder(
        TestModel.OUTER_LIST_PATH)
            .nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, TWO_ID)
            .build();

    private static final MapEntryNode FOO_NODE = ImmutableNodes.newMapEntryBuilder()
        .withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, ONE_ID))
        .withChild(ImmutableNodes.leafNode(TestModel.ID_QNAME, ONE_ID))
        .withChild(ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.INNER_LIST_QNAME))
            .build())
        .build();

    private static final MapEntryNode BAR_NODE = ImmutableNodes.newMapEntryBuilder()
        .withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, TWO_ID))
        .withChild(ImmutableNodes.leafNode(TestModel.ID_QNAME, TWO_ID))
        .withChild(ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.INNER_LIST_QNAME))
            .build())
        .build();

    private DataTree inMemoryDataTree;


    @BeforeEach
    void prepare() {
        inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL,
            SCHEMA_CONTEXT);
    }

    private static ContainerNode createFooTestContainerNode() {
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
                .withChild(FOO_NODE)
                .build())
            .build();
    }

    private static ContainerNode createBarTestContainerNode() {
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
                .withChild(BAR_NODE)
                .build())
            .build();
    }

    private static <T> T assertPresentAndType(final Optional<?> potential, final Class<T> type) {
        assertNotNull(potential);
        assertTrue(potential.isPresent());
        assertTrue(type.isInstance(potential.orElseThrow()));
        return type.cast(potential.orElseThrow());
    }

    @Test
    void writeWrite1stLevelEmptyTreeTest() throws DataValidationFailedException {
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(TestModel.TEST_PATH,
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME)).build());
        modificationTree2.write(TestModel.TEST_PATH,
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME)).build());

        modificationTree1.ready();
        modificationTree2.ready();
        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            fail("Exception should have been thrown.");
        } catch (final ConflictingModificationAppliedException ex) {
            LOG.debug("ConflictingModificationAppliedException - was thrown as expected", ex);
        }
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final var testNodeAfterCommits = modificationTree1.readNode(TestModel.TEST_PATH);
        assertPresentAndType(testNodeAfterCommits, ContainerNode.class);
    }

    @Test
    void writeMerge1stLevelEmptyTreeTest() throws DataValidationFailedException {
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(TestModel.TEST_PATH,
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME)).build());
        modificationTree2.merge(TestModel.TEST_PATH,
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME)).build());

        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final var testNodeAfterCommits = modificationTree1.readNode(TestModel.TEST_PATH);
        assertPresentAndType(testNodeAfterCommits, ContainerNode.class);
    }

    @Test
    void writeWriteFooBar1stLevelEmptyTreeTest() throws DataValidationFailedException {
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.write(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            fail("Exception should have been thrown.");
            final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
        } catch (final ConflictingModificationAppliedException ex) {
            LOG.debug("ConflictingModificationAppliedException - was thrown as expected", ex);
        }

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertFalse(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH).isPresent());
    }

    @Test
    void writeMergeFooBar1stLevelEmptyTreeTest() throws DataValidationFailedException {
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.merge(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    void mergeWriteFooBar1stLevelEmptyTreeTest() throws DataValidationFailedException {
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.merge(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.write(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            fail("Exception should have been thrown.");
            final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
        } catch (final ConflictingModificationAppliedException ex) {
            LOG.debug("ConflictingModificationAppliedException - was thrown as expected", ex);
        }

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertFalse(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH).isPresent());
    }

    @Test
    void mergeMergeFooBar1stLevelEmptyTreeTest() throws DataValidationFailedException {
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.merge(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.merge(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    void writeWriteFooBar1stLevelEmptyContainerTest() throws DataValidationFailedException {
        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, emptyContainer(TestModel.TEST_QNAME));
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.write(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            fail("Exception should have been thrown.");
            final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
        } catch (final ConflictingModificationAppliedException ex) {
            LOG.debug("ConflictingModificationAppliedException was thrown as expected", ex);
        }

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertFalse(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH).isPresent());
    }

    @Test
    void writeMergeFooBar1stLevelEmptyContainerTest() throws DataValidationFailedException {
        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, emptyContainer(TestModel.TEST_QNAME));
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.merge(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    void mergeWriteFooBar1stLevelEmptyContainerTest() throws DataValidationFailedException {
        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, emptyContainer(TestModel.TEST_QNAME));
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.merge(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.write(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            fail("Exception should have been thrown.");
            final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
        } catch (final ConflictingModificationAppliedException ex) {
            LOG.debug("ConflictingModificationAppliedException was thrown as expected", ex);
        }


        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertFalse(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH).isPresent());
    }

    @Test
    void mergeMergeFooBar1stLevelEmptyContainerTest() throws DataValidationFailedException {
        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, emptyContainer(TestModel.TEST_QNAME));
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.merge(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.merge(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    void deleteWriteFooBar1stLevelEmptyContainerTest() throws DataValidationFailedException {
        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, emptyContainer(TestModel.TEST_QNAME));
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.delete(TestModel.TEST_PATH);
        modificationTree2.write(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            fail("Exception should have been thrown.");
            final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
        } catch (final ConflictingModificationAppliedException ex) {
            LOG.debug("ConflictingModificationAppliedException was thrown as expected", ex);
        }


        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertFalse(snapshotAfterCommits.readNode(TestModel.TEST_PATH).isPresent());
    }

    @Test
    void deleteMergeFooBar1stLevelEmptyContainerTest() throws DataValidationFailedException {
        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, emptyContainer(TestModel.TEST_QNAME));
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.delete(TestModel.TEST_PATH);
        modificationTree2.merge(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    void writeWriteFooBar2ndLevelEmptyContainerTest() throws DataValidationFailedException {
        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, emptyContainer(TestModel.TEST_QNAME));
        initialDataTreeModification.write(TestModel.OUTER_LIST_PATH, ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
            .build());
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(OUTER_LIST_1_PATH, FOO_NODE);
        modificationTree2.write(OUTER_LIST_2_PATH, BAR_NODE);
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    void writeMergeFooBar2ndLevelEmptyContainerTest() throws DataValidationFailedException {
        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, emptyContainer(TestModel.TEST_QNAME));
        initialDataTreeModification.write(TestModel.OUTER_LIST_PATH, ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
            .build());
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(OUTER_LIST_1_PATH, FOO_NODE);
        modificationTree2.merge(OUTER_LIST_2_PATH, BAR_NODE);
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    void mergeWriteFooBar2ndLevelEmptyContainerTest() throws DataValidationFailedException {
        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, emptyContainer(TestModel.TEST_QNAME));
        initialDataTreeModification.write(TestModel.OUTER_LIST_PATH, ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
            .build());
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.merge(OUTER_LIST_1_PATH, FOO_NODE);
        modificationTree2.write(OUTER_LIST_2_PATH, BAR_NODE);
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    void mergeMergeFooBar2ndLevelEmptyContainerTest() throws DataValidationFailedException {
        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, emptyContainer(TestModel.TEST_QNAME));
        initialDataTreeModification.write(TestModel.OUTER_LIST_PATH, ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
            .build());
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.merge(OUTER_LIST_1_PATH, FOO_NODE);
        modificationTree2.merge(OUTER_LIST_2_PATH, BAR_NODE);
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    void deleteWriteFooBar2ndLevelEmptyContainerTest() throws DataValidationFailedException {
        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, emptyContainer(TestModel.TEST_QNAME));
        initialDataTreeModification.write(TestModel.OUTER_LIST_PATH, ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
            .build());
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.delete(TestModel.TEST_PATH);
        modificationTree2.write(OUTER_LIST_2_PATH, BAR_NODE);
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
            fail("Exception should have been thrown");
        } catch (final ConflictingModificationAppliedException e) {
            LOG.debug("Exception was thrown because path no longer exist in tree", e);
        }

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertFalse(snapshotAfterCommits.readNode(TestModel.TEST_PATH).isPresent());
    }

    @Test
    void deleteMergeFooBar2ndLevelEmptyContainerTest() throws DataValidationFailedException {
        final var initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, emptyContainer(TestModel.TEST_QNAME));
        initialDataTreeModification.write(TestModel.OUTER_LIST_PATH, ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
            .build());
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final var modificationTree1 = initialDataTreeSnapshot.newModification();
        final var modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.delete(TestModel.TEST_PATH);
        modificationTree2.merge(OUTER_LIST_2_PATH, BAR_NODE);
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
            fail("Exception should have been thrown");
        } catch (final ConflictingModificationAppliedException e) {
            LOG.debug("Exception was thrown because path no longer exist in tree", e);
        }

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertFalse(snapshotAfterCommits.readNode(TestModel.TEST_PATH).isPresent());
    }

    private static ContainerNode emptyContainer(final QName name) {
        return ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(name)).build();
    }
}
