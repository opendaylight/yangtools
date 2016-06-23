/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeConfig;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class UniqueConstraintTest {
    private static final String NS = "foo";
    private static final String REV = "2016-05-17";
    private static final QName TASK_CONTAINER = QName.create(NS, REV, "task-container");
    private static final QName TASK = QName.create(NS, REV, "task");
    private static final QName TASK_ID = QName.create(NS, REV, "task-id");
    private static final QName MY_LEAF_1 = QName.create(NS, REV, "my-leaf-1");
    private static final QName MY_LEAF_2 = QName.create(NS, REV, "my-leaf-2");
    private static final QName MY_LEAF_3 = QName.create(NS, REV, "my-leaf-3");
    private static final QName MY_CONTAINER = QName.create(NS, REV, "my-container");

    private static InMemoryDataTree initDataTree(final SchemaContext schemaContext)
            throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(
                new TreeConfig(TreeType.CONFIGURATION, true));
        inMemoryDataTree.setSchemaContext(schemaContext);

        final MapNode taskNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(TASK)).build();
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK), taskNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
        return inMemoryDataTree;
    }

    private static InMemoryDataTree emptyDataTree(final SchemaContext schemaContext)
            throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(
                new TreeConfig(TreeType.CONFIGURATION, true));
        inMemoryDataTree.setSchemaContext(schemaContext);

        return inMemoryDataTree;
    }

    @Test
    public void mapTest() throws ReactorException, DataValidationFailedException {
        final SchemaContext schemaContext = TestModel.createTestContext("/bug4955/foo.yang");
        assertNotNull("Schema context must not be null.", schemaContext);

        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        try {
            writeMap(inMemoryDataTree, true);
            fail("should fail due to unique constraint violation");
        } catch (final IllegalArgumentException e) {
            assertEquals("Node (foo?revision=2016-05-17)task"
                    + "[{(foo?revision=2016-05-17)task-id=3}] violates unique constraint. Stored node "
                    + "(foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=1}] already contains "
                    + "the same value combination of leafs: [/(foo?revision=2016-05-17)my-leaf-1, "
                    + "/(foo?revision=2016-05-17)my-leaf-2]", e.getMessage());
        }
        writeMap(inMemoryDataTree, false);
        try {
            writeMapEntry(inMemoryDataTree, "4", "l1", "l2", "l3");
            fail("should fail due to unique constraint violation");
        } catch (final IllegalArgumentException e) {
            assertEquals("Node (foo?revision=2016-05-17)task"
                    + "[{(foo?revision=2016-05-17)task-id=4}] violates unique constraint. Stored node "
                    + "(foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=1}] already contains "
                    + "the same value combination of leafs: [/(foo?revision=2016-05-17)my-leaf-1, "
                    + "/(foo?revision=2016-05-17)my-leaf-2]", e.getMessage());
        }

        // :FIXME delete
        System.out.println(inMemoryDataTree);
    }

    @Test
    public void mapEntryTest() throws ReactorException, DataValidationFailedException {
        final SchemaContext schemaContext = TestModel.createTestContext("/bug4955/foo.yang");
        assertNotNull("Schema context must not be null.", schemaContext);

        final InMemoryDataTree inMemoryDataTree = initDataTree(schemaContext);
        writeAndRemoveMapEntries(inMemoryDataTree, true);
        writeAndRemoveMapEntries(inMemoryDataTree, false);
        // :FIXME delete
        System.out.println(inMemoryDataTree);
    }

    private void writeAndRemoveMapEntries(final InMemoryDataTree inMemoryDataTree, final boolean clear)
            throws DataValidationFailedException {
        writeMapEntry(inMemoryDataTree, "1", "l1", "l2", "l3");
        writeMapEntry(inMemoryDataTree, "2", "l2", "l3", "l4");
        writeMapEntry(inMemoryDataTree, "3", "l3", "l4", "l5");
        writeMapEntry(inMemoryDataTree, "2", "l2", "l3", "l6");
        writeMapEntry(inMemoryDataTree, "10", "l2", "l10", "l4");
        try {
            writeMapEntry(inMemoryDataTree, "4", "l1", "l5", "l3");
            fail("should fail due to unique constraint violation");
        } catch (final IllegalArgumentException e) {
            assertEquals("Node (foo?revision=2016-05-17)task"
                    + "[{(foo?revision=2016-05-17)task-id=4}] violates unique constraint. Stored node "
                    + "(foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=1}] already contains "
                    + "the same value combination of leafs: [/(foo?revision=2016-05-17)my-leaf-1, "
                    + "/(foo?revision=2016-05-17)my-container/my-leaf-3]", e.getMessage());
        }
        writeMapEntry(inMemoryDataTree, "4", "l4", "l5", "l6");
        try {
            writeMapEntry(inMemoryDataTree, "5", "l3", "l4", "l7");
            fail("should fail due to unique constraint violation");
        } catch (final IllegalArgumentException e) {
            assertEquals("Node (foo?revision=2016-05-17)task"
                    + "[{(foo?revision=2016-05-17)task-id=5}] violates unique constraint. Stored node "
                    + "(foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=3}] already contains "
                    + "the same value combination of leafs: [/(foo?revision=2016-05-17)my-leaf-1, "
                    + "/(foo?revision=2016-05-17)my-leaf-2]", e.getMessage());
        }
        removeMapEntry(inMemoryDataTree, taskEntryKey("3"));
        writeMapEntry(inMemoryDataTree, "5", "l3", "l4", "l7");
        writeMapEntry(inMemoryDataTree, "5", "l3", "l4", "l7");
        try {
            writeMapEntry(inMemoryDataTree, "6", "l3", "l4", "l7");
            fail("should fail due to unique constraint violation");
        } catch (final IllegalArgumentException e) {
            assertEquals("Node (foo?revision=2016-05-17)task"
                    + "[{(foo?revision=2016-05-17)task-id=6}] violates unique constraint. Stored node "
                    + "(foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=5}] already contains "
                    + "the same value combination of leafs: [/(foo?revision=2016-05-17)my-leaf-1, "
                    + "/(foo?revision=2016-05-17)my-leaf-2]", e.getMessage());
        }

        if (clear) {
            removeMapEntry(inMemoryDataTree, taskEntryKey("1"));
            removeMapEntry(inMemoryDataTree, taskEntryKey("2"));
            removeMapEntry(inMemoryDataTree, taskEntryKey("4"));
            removeMapEntry(inMemoryDataTree, taskEntryKey("5"));
            removeMapEntry(inMemoryDataTree, taskEntryKey("10"));
        }
    }

    private static void writeMap(final InMemoryDataTree inMemoryDataTree, final boolean withUniqueViolation)
            throws DataValidationFailedException {
        final MapNode taskNode = Builders
                .mapBuilder()
                .withNodeIdentifier(new NodeIdentifier(TASK))
                .withChild(createMapEntry("1", "l1", "l2", "l3"))
                .withChild(createMapEntry("2", "l2", "l3", "l4"))
                .withChild(
                        withUniqueViolation ? createMapEntry("3", "l1", "l2", "l3") : createMapEntry("3", "l3", "l4",
                                "l5")).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK), taskNode);
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
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

    private static void removeMapEntry(final InMemoryDataTree inMemoryDataTree,
            final NodeIdentifierWithPredicates mapEntryKey) throws DataValidationFailedException {
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.delete(YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK).node(mapEntryKey));
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
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

    private static NodeIdentifierWithPredicates taskEntryKey(final String taskId) {
        return new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, taskId));
    }
}
