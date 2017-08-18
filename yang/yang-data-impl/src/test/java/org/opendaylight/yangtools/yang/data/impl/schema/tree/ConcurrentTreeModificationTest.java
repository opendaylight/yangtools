/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentTreeModificationTest {
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentTreeModificationTest.class);

    private static final Short ONE_ID = 1;
    private static final Short TWO_ID = 2;

    private static final YangInstanceIdentifier OUTER_LIST_1_PATH = YangInstanceIdentifier.builder(TestModel.OUTER_LIST_PATH)
            .nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, ONE_ID) //
            .build();

    private static final YangInstanceIdentifier OUTER_LIST_2_PATH = YangInstanceIdentifier.builder(TestModel.OUTER_LIST_PATH)
            .nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, TWO_ID) //
            .build();

    private static final MapEntryNode FOO_NODE = mapEntryBuilder(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, ONE_ID) //
            .withChild(mapNodeBuilder(TestModel.INNER_LIST_QNAME) //
                    .build()) //
            .build();

    private static final MapEntryNode BAR_NODE = mapEntryBuilder(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, TWO_ID) //
            .withChild(mapNodeBuilder(TestModel.INNER_LIST_QNAME) //
                    .build()) //
            .build();

    private SchemaContext schemaContext;
    private InMemoryDataTree inMemoryDataTree;

    @Before
    public void prepare() throws ReactorException {
        schemaContext = TestModel.createTestContext();
        assertNotNull("Schema context must not be null.", schemaContext);
        inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(TreeType.OPERATIONAL);
        inMemoryDataTree.setSchemaContext(schemaContext);
    }

    private static ContainerNode createFooTestContainerNode() {
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(
                        mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
                                .withChild(FOO_NODE).build()).build();
    }

    private static ContainerNode createBarTestContainerNode() {
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(
                        mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
                                .withChild(BAR_NODE).build()).build();
    }

    private static <T> T assertPresentAndType(final Optional<?> potential, final Class<T> type) {
        assertNotNull(potential);
        assertTrue(potential.isPresent());
        assertTrue(type.isInstance(potential.get()));
        return type.cast(potential.get());
    }

    @Test
    public void writeWrite1stLevelEmptyTreeTest() throws DataValidationFailedException {
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        modificationTree2.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));

        modificationTree1.ready();
        modificationTree2.ready();
        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            fail("Exception should have been thrown.");
        } catch (final ConflictingModificationAppliedException ex) {
            LOG.debug("ConflictingModificationAppliedException - '{}' was thrown as expected.");
        }
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final Optional<NormalizedNode<?, ?>> testNodeAfterCommits = modificationTree1.readNode(TestModel.TEST_PATH);
        assertPresentAndType(testNodeAfterCommits, ContainerNode.class);
    }

    @Test
    public void writeMerge1stLevelEmptyTreeTest() throws DataValidationFailedException {
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        modificationTree2.merge(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));

        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final Optional<NormalizedNode<?, ?>> testNodeAfterCommits = modificationTree1.readNode(TestModel.TEST_PATH);
        assertPresentAndType(testNodeAfterCommits, ContainerNode.class);
    }

    @Test
    public void writeWriteFooBar1stLevelEmptyTreeTest() throws DataValidationFailedException {
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.write(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            fail("Exception should have been thrown.");
            final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
        } catch (final ConflictingModificationAppliedException ex) {
            LOG.debug("ConflictingModificationAppliedException - '{}' was thrown as expected.");
        }

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertFalse(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH).isPresent());
    }

    @Test
    public void writeMergeFooBar1stLevelEmptyTreeTest() throws DataValidationFailedException {
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.merge(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    public void mergeWriteFooBar1stLevelEmptyTreeTest() throws DataValidationFailedException {
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.merge(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.write(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            fail("Exception should have been thrown.");
            final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
        } catch (final ConflictingModificationAppliedException ex) {
            LOG.debug("ConflictingModificationAppliedException - '{}' was thrown as expected.");
        }

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertFalse(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH).isPresent());
    }

    @Test
    public void mergeMergeFooBar1stLevelEmptyTreeTest() throws DataValidationFailedException {
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.merge(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.merge(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    public void writeWriteFooBar1stLevelEmptyContainerTest() throws DataValidationFailedException {
        final DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.write(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            fail("Exception should have been thrown.");
            final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
        } catch (final ConflictingModificationAppliedException ex) {
            LOG.debug("ConflictingModificationAppliedException - '{}' was thrown as expected.");
        }

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertFalse(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH).isPresent());
    }

    @Test
    public void writeMergeFooBar1stLevelEmptyContainerTest() throws DataValidationFailedException {
        final DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.merge(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    public void mergeWriteFooBar1stLevelEmptyContainerTest() throws DataValidationFailedException {
        final DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.merge(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.write(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            fail("Exception should have been thrown.");
            final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
        } catch (final ConflictingModificationAppliedException ex) {
            LOG.debug("ConflictingModificationAppliedException - '{}' was thrown as expected.");
        }


        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertFalse(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH).isPresent());
    }

    @Test
    public void mergeMergeFooBar1stLevelEmptyContainerTest() throws DataValidationFailedException {
        final DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.merge(TestModel.TEST_PATH, createFooTestContainerNode());
        modificationTree2.merge(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    public void deleteWriteFooBar1stLevelEmptyContainerTest() throws DataValidationFailedException {
        final DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.delete(TestModel.TEST_PATH);
        modificationTree2.write(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            fail("Exception should have been thrown.");
            final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
        } catch (final ConflictingModificationAppliedException ex) {
            LOG.debug("ConflictingModificationAppliedException - '{}' was thrown as expected.");
        }


        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertFalse(snapshotAfterCommits.readNode(TestModel.TEST_PATH).isPresent());
    }

    @Test
    public void deleteMergeFooBar1stLevelEmptyContainerTest() throws DataValidationFailedException {
        final DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.delete(TestModel.TEST_PATH);
        modificationTree2.merge(TestModel.TEST_PATH, createBarTestContainerNode());
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    public void writeWriteFooBar2ndLevelEmptyContainerTest() throws DataValidationFailedException {
        final DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        initialDataTreeModification.write(TestModel.OUTER_LIST_PATH, mapNodeBuilder(TestModel.OUTER_LIST_QNAME).build());
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(OUTER_LIST_1_PATH, FOO_NODE);
        modificationTree2.write(OUTER_LIST_2_PATH, BAR_NODE);
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    public void writeMergeFooBar2ndLevelEmptyContainerTest() throws DataValidationFailedException {
        final DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        initialDataTreeModification.write(TestModel.OUTER_LIST_PATH, mapNodeBuilder(TestModel.OUTER_LIST_QNAME).build());
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.write(OUTER_LIST_1_PATH, FOO_NODE);
        modificationTree2.merge(OUTER_LIST_2_PATH, BAR_NODE);
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    public void mergeWriteFooBar2ndLevelEmptyContainerTest() throws DataValidationFailedException {
        final DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        initialDataTreeModification.write(TestModel.OUTER_LIST_PATH, mapNodeBuilder(TestModel.OUTER_LIST_QNAME).build());
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.merge(OUTER_LIST_1_PATH, FOO_NODE);
        modificationTree2.write(OUTER_LIST_2_PATH, BAR_NODE);
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    public void mergeMergeFooBar2ndLevelEmptyContainerTest() throws DataValidationFailedException {
        final DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        initialDataTreeModification.write(TestModel.OUTER_LIST_PATH, mapNodeBuilder(TestModel.OUTER_LIST_QNAME).build());
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.merge(OUTER_LIST_1_PATH, FOO_NODE);
        modificationTree2.merge(OUTER_LIST_2_PATH, BAR_NODE);
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_1_PATH), MapEntryNode.class);
        assertPresentAndType(snapshotAfterCommits.readNode(OUTER_LIST_2_PATH), MapEntryNode.class);
    }

    @Test
    public void deleteWriteFooBar2ndLevelEmptyContainerTest() throws DataValidationFailedException {
        final DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        initialDataTreeModification.write(TestModel.OUTER_LIST_PATH, mapNodeBuilder(TestModel.OUTER_LIST_QNAME).build());
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.delete(TestModel.TEST_PATH);
        modificationTree2.merge(OUTER_LIST_2_PATH, BAR_NODE);
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
            fail("Exception should have been thrown");
        } catch (final Exception e) {
            LOG.debug("Exception was thrown because path no longer exist in tree");
        }

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertFalse(snapshotAfterCommits.readNode(TestModel.TEST_PATH).isPresent());
    }

    @Test
    public void deleteMergeFooBar2ndLevelEmptyContainerTest() throws DataValidationFailedException {
        final DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        initialDataTreeModification.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        initialDataTreeModification.write(TestModel.OUTER_LIST_PATH, mapNodeBuilder(TestModel.OUTER_LIST_QNAME).build());
        initialDataTreeModification.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(initialDataTreeModification));
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final DataTreeModification modificationTree1 = initialDataTreeSnapshot.newModification();
        final DataTreeModification modificationTree2 = initialDataTreeSnapshot.newModification();

        modificationTree1.delete(TestModel.TEST_PATH);
        modificationTree2.merge(OUTER_LIST_2_PATH, BAR_NODE);
        modificationTree1.ready();
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        try {
            inMemoryDataTree.validate(modificationTree2);
            final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
            fail("Exception should have been thrown");
        } catch (final Exception e) {
            LOG.debug("Exception was thrown because path no longer exist in tree");
        }

        final InMemoryDataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        assertFalse(snapshotAfterCommits.readNode(TestModel.TEST_PATH).isPresent());
    }
}
