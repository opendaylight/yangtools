/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.collect.ImmutableMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class UniqueConstraintPerformanceTestUtils {
    private static final String NS = "foo";
    private static final String REV = "2016-05-17";
    private static final QName TASK_CONTAINER = QName.create(NS, REV, "task-container");
    private static final QName TASK = QName.create(NS, REV, "task");
    private static final QName TASK_ID = QName.create(NS, REV, "task-id");
    private static final QName MY_LEAF_1 = QName.create(NS, REV, "my-leaf-1");
    private static final QName MY_LEAF_2 = QName.create(NS, REV, "my-leaf-2");
    private static final QName MY_LEAF_3 = QName.create(NS, REV, "my-leaf-3");
    private static final QName MY_CONTAINER = QName.create(NS, REV, "my-container");

    public static InMemoryDataTree initDataTree(final SchemaContext schemaContext, final boolean uniqueIndex,
            final int mapEntriesCount) throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(
                new DataTreeConfiguration.Builder(TreeType.CONFIGURATION).setUniqueIndexes(uniqueIndex).build());
        inMemoryDataTree.setSchemaContext(schemaContext);

        final CollectionNodeBuilder<MapEntryNode, MapNode> taskNodeBuilder = Builders.mapBuilder().withNodeIdentifier(
                new NodeIdentifier(TASK));
        addMapEntries(taskNodeBuilder, mapEntriesCount);

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK), taskNodeBuilder.build());
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
        return inMemoryDataTree;
    }

    private static void addMapEntries(final CollectionNodeBuilder<MapEntryNode, MapNode> taskNodeBuilder,
            final int mapEntriesCount) throws DataValidationFailedException {
        for (int i = 0; i < mapEntriesCount; i++) {
            taskNodeBuilder.withChild(createMapEntry(i, "l" + i, "l" + (i + 1), "l" + (i + 2)));
        }
    }

    public static void writeMapEntriesInSequence(final InMemoryDataTree inMemoryDataTree, final int mapEntriesCount,
            final int initialEntriesCount) throws DataValidationFailedException {
        for (int i = 0; i < mapEntriesCount; i++) {
            final int rowNumber = initialEntriesCount + i;
            writeMapEntry(inMemoryDataTree, rowNumber, "l" + rowNumber, "l" + (rowNumber + 1), "l" + (rowNumber + 2));
        }
    }

    private static void writeMapEntry(final InMemoryDataTree inMemoryDataTree, final Object taskIdValue,
            final Object myLeaf1Value, final Object myLeaf2Value, final Object myLeaf3Value)
            throws DataValidationFailedException {
        final MapEntryNode taskEntryNode = Builders
                .mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, taskIdValue)))
                .withChild(ImmutableNodes.leafNode(TASK_ID, taskIdValue))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_1, myLeaf1Value))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_2, myLeaf2Value))
                .withChild(
                        Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(MY_CONTAINER))
                                .withChild(ImmutableNodes.leafNode(MY_LEAF_3, myLeaf3Value)).build()).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
                YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK)
                        .node(new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, taskIdValue))),
                taskEntryNode);
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    public static void writeBatchOfMapEntries(final InMemoryDataTree inMemoryDataTree, final int mapEntriesCount,
            final int initialEntriesCount) throws DataValidationFailedException {
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        for (int i = 0; i < mapEntriesCount; i++) {
            final int rowNumber = initialEntriesCount + i;
            writeMapEntry(modificationTree, rowNumber, "l" + rowNumber, "l" + (rowNumber + 1), "l" + (rowNumber + 2));
        }
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    private static void writeMapEntry(final InMemoryDataTreeModification modificationTree, final Object taskIdValue,
            final Object myLeaf1Value, final Object myLeaf2Value, final Object myLeaf3Value)
            throws DataValidationFailedException {
        final MapEntryNode taskEntryNode = Builders
                .mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, taskIdValue)))
                .withChild(ImmutableNodes.leafNode(TASK_ID, taskIdValue))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_1, myLeaf1Value))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_2, myLeaf2Value))
                .withChild(
                        Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(MY_CONTAINER))
                                .withChild(ImmutableNodes.leafNode(MY_LEAF_3, myLeaf3Value)).build()).build();

        modificationTree.write(
                YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK)
                        .node(new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, taskIdValue))),
                taskEntryNode);
    }

    private static MapEntryNode createMapEntry(final Object taskIdValue, final Object myLeaf1Value,
            final Object myLeaf2Value, final Object myLeaf3Value) throws DataValidationFailedException {
        return Builders
                .mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, taskIdValue)))
                .withChild(ImmutableNodes.leafNode(TASK_ID, taskIdValue))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_1, myLeaf1Value))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_2, myLeaf2Value))
                .withChild(
                        Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(MY_CONTAINER))
                                .withChild(ImmutableNodes.leafNode(MY_LEAF_3, myLeaf3Value)).build()).build();
    }
}
