/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import com.google.common.base.Stopwatch;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class UniqueConstraintPerformanceTest extends AbstractUniqueConstraintTest {
    private static final Logger LOG = LoggerFactory.getLogger(UniqueConstraintPerformanceTest.class);
    private static final String NS = "foo";
    private static final String REV = "2016-05-17";
    private static final QName TASK_CONTAINER = QName.create(NS, REV, "task-container");
    private static final QName TASK = QName.create(NS, REV, "task");
    private static final QName TASK_ID = QName.create(NS, REV, "task-id");
    private static final QName MY_LEAF_1 = QName.create(NS, REV, "my-leaf-1");
    private static final QName MY_LEAF_2 = QName.create(NS, REV, "my-leaf-2");
    private static final QName MY_LEAF_3 = QName.create(NS, REV, "my-leaf-3");
    private static final QName MY_CONTAINER = QName.create(NS, REV, "my-container");

    private static final int INITIAL_WRITE_COUNT = 100000;
    private static final int SECOND_WRITE_COUNT = 1000;

    @Parameters(name = "{index}: uniqueEnabled={0}")
    public static Iterable<Object[]> parameters() {
        return List.of(new Object[] { false }, new Object[] { true });
    }

    @Parameter
    public boolean enabled;

    @Test
    public void mapEntryTest() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(enabled, INITIAL_WRITE_COUNT);

        final Stopwatch sw = Stopwatch.createStarted();
        for (int i = 0; i < SECOND_WRITE_COUNT; i++) {
            final int rowNumber = INITIAL_WRITE_COUNT + i;
            writeMapEntry(inMemoryDataTree, rowNumber, "l" + rowNumber, "l" + (rowNumber + 1), "l" + (rowNumber + 2));
        }

        LOG.info("{} Entries written in {}", SECOND_WRITE_COUNT, sw);
    }

    @Test
    public void mapEntryBatchTest() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(enabled, INITIAL_WRITE_COUNT);

        final Stopwatch sw = Stopwatch.createStarted();
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        for (int i = 0; i < SECOND_WRITE_COUNT; i++) {
            final int rowNumber = INITIAL_WRITE_COUNT + i;
            writeMapEntry(modificationTree, rowNumber, "l" + rowNumber, "l" + (rowNumber + 1), "l" + (rowNumber + 2));
        }
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        LOG.info("{} Entries written in {}", SECOND_WRITE_COUNT, sw);
    }

    private static InMemoryDataTree initDataTree(final boolean uniqueIndex, final int mapEntriesCount)
            throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) new InMemoryDataTreeFactory().create(
                new DataTreeConfiguration.Builder(TreeType.CONFIGURATION).setUniqueIndexes(uniqueIndex).build(),
                TEST_MODEL);

        final var taskNodeBuilder = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(TASK));
        addMapEntries(taskNodeBuilder, mapEntriesCount);

        final Stopwatch sw = Stopwatch.createStarted();
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK), taskNodeBuilder.build());
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
        LOG.info("Data tree initialized in {}", sw);
        return inMemoryDataTree;
    }

    private static void addMapEntries(final CollectionNodeBuilder<MapEntryNode, SystemMapNode> taskNodeBuilder,
            final int mapEntriesCount) throws DataValidationFailedException {
        for (int i = 0; i < mapEntriesCount; i++) {
            taskNodeBuilder.withChild(createMapEntry(i, "l" + i, "l" + (i + 1), "l" + (i + 2)));
        }
    }

    private static void writeMapEntry(final InMemoryDataTree inMemoryDataTree, final Object taskIdValue,
            final Object myLeaf1Value, final Object myLeaf2Value, final Object myLeaf3Value)
            throws DataValidationFailedException {
        final MapEntryNode taskEntryNode = Builders
                .mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(TASK, TASK_ID, taskIdValue))
                .withChild(ImmutableNodes.leafNode(TASK_ID, taskIdValue))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_1, myLeaf1Value))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_2, myLeaf2Value))
                .withChild(
                        Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(MY_CONTAINER))
                                .withChild(ImmutableNodes.leafNode(MY_LEAF_3, myLeaf3Value)).build()).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
                YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK)
                        .node(NodeIdentifierWithPredicates.of(TASK, TASK_ID, taskIdValue)),
                taskEntryNode);
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    private static void writeMapEntry(final InMemoryDataTreeModification modificationTree, final Object taskIdValue,
            final Object myLeaf1Value, final Object myLeaf2Value, final Object myLeaf3Value) {
        modificationTree.write(
            YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK)
                .node(NodeIdentifierWithPredicates.of(TASK, TASK_ID, taskIdValue)),
            Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(TASK, TASK_ID, taskIdValue))
                .withChild(ImmutableNodes.leafNode(TASK_ID, taskIdValue))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_1, myLeaf1Value))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_2, myLeaf2Value))
                .withChild(Builders.containerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(MY_CONTAINER))
                    .withChild(ImmutableNodes.leafNode(MY_LEAF_3, myLeaf3Value))
                    .build())
                .build());
    }

    private static MapEntryNode createMapEntry(final Object taskIdValue, final Object myLeaf1Value,
            final Object myLeaf2Value, final Object myLeaf3Value) throws DataValidationFailedException {
        return Builders.mapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(TASK, TASK_ID, taskIdValue))
            .withChild(ImmutableNodes.leafNode(TASK_ID, taskIdValue))
            .withChild(ImmutableNodes.leafNode(MY_LEAF_1, myLeaf1Value))
            .withChild(ImmutableNodes.leafNode(MY_LEAF_2, myLeaf2Value))
            .withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(MY_CONTAINER))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_3, myLeaf3Value))
                .build())
            .build();
    }
}
