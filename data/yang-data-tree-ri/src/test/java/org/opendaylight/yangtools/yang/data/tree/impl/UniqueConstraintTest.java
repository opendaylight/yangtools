/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfErrorAware;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;
import org.opendaylight.yangtools.yang.data.tree.api.UniqueConstraintException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public class UniqueConstraintTest extends AbstractUniqueConstraintTest {
    private static final String NS = "foo";
    private static final String REV = "2016-05-17";
    private static final QName TASK_CONTAINER = QName.create(NS, REV, "task-container");
    private static final QName TASK = QName.create(NS, REV, "task");
    private static final QName TASK_ID = QName.create(NS, REV, "task-id");
    private static final QName MY_LEAF_1 = QName.create(NS, REV, "my-leaf-1");
    private static final QName MY_LEAF_2 = QName.create(NS, REV, "my-leaf-2");
    private static final QName MY_LEAF_3 = QName.create(NS, REV, "my-leaf-3");
    private static final QName MY_CONTAINER = QName.create(NS, REV, "my-container");

    @Test
    public void switchEntriesTest() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(TEST_MODEL, true);
        writeMapEntry(inMemoryDataTree, "1", "l1", "l2", "l3");
        writeMapEntry(inMemoryDataTree, "2", "l2", "l3", "l4");

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        final MapEntryNode mapEntry1 = createMapEntry("1", "l2", "l3", "l4");
        final MapEntryNode mapEntry2 = createMapEntry("2", "l1", "l2", "l3");

        //switch values of map entries
        modificationTree.write(
                YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK)
                        .node(NodeIdentifierWithPredicates.of(TASK, TASK_ID, "1")), mapEntry1);
        modificationTree.write(
                YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK)
                        .node(NodeIdentifierWithPredicates.of(TASK, TASK_ID, "2")), mapEntry2);

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void mapTest() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(TEST_MODEL, true);


        verifyException(assertThrows(UniqueValidationFailedException.class,
            () -> writeMap(inMemoryDataTree, true)),
            "(foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=",
            "}] violates unique constraint on [l1, l2] of ",
            "(foo?revision=2016-05-17)my-leaf-1",
            "(foo?revision=2016-05-17)my-leaf-2]");

        writeMap(inMemoryDataTree, false);
        verifyException(assertThrows(UniqueConstraintException.class,
            () -> writeMapEntry(inMemoryDataTree, "4", "l1", "l2", "l30")),
            "(foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=",
            "}] violates unique constraint on [l1, l2] of ",
            "(foo?revision=2016-05-17)my-leaf-1",
            "(foo?revision=2016-05-17)my-leaf-2");
    }

    @Test
    public void mapEntryTest() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(TEST_MODEL, true);
        writeAndRemoveMapEntries(inMemoryDataTree, true);
        writeAndRemoveMapEntries(inMemoryDataTree, false);
    }

    private static void writeAndRemoveMapEntries(final InMemoryDataTree inMemoryDataTree, final boolean clear)
            throws DataValidationFailedException {
        writeMapEntry(inMemoryDataTree, "1", "l1", "l2", "l3");
        writeMapEntry(inMemoryDataTree, "2", "l2", "l3", "l4");
        writeMapEntry(inMemoryDataTree, "3", "l3", "l4", "l5");
        writeMapEntry(inMemoryDataTree, "2", "l2", "l3", "l6");
        writeMapEntry(inMemoryDataTree, "10", "l2", "l10", "l4");
        verifyException(assertThrows(UniqueConstraintException.class,
            () -> writeMapEntry(inMemoryDataTree, "4", "l1", "l5", "l3")),
            "(foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=",
                    "}] violates unique constraint on [l1, l3] of ",
                    "(foo?revision=2016-05-17)my-container, my-leaf-3",
                    "(foo?revision=2016-05-17)my-leaf-1");
        writeMapEntry(inMemoryDataTree, "4", "l4", "l5", "l6");
        verifyException(assertThrows(UniqueConstraintException.class,
            () -> writeMapEntry(inMemoryDataTree, "5", "l3", "l4", "l7")),
            "(foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=",
            "}] violates unique constraint on [l3, l4] of ",
            "(foo?revision=2016-05-17)my-leaf-1",
            "(foo?revision=2016-05-17)my-leaf-2");
        removeMapEntry(inMemoryDataTree, taskEntryKey("3"));
        writeMapEntry(inMemoryDataTree, "5", "l3", "l4", "l7");
        writeMapEntry(inMemoryDataTree, "5", "l3", "l4", "l7");
        verifyException(assertThrows(UniqueConstraintException.class,
            () -> writeMapEntry(inMemoryDataTree, "6", "l3", "l4", "l11")),
            "(foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=",
            "}] violates unique constraint on [l3, l4] of ",
            "(foo?revision=2016-05-17)my-leaf-1",
            "(foo?revision=2016-05-17)my-leaf-2");

        if (clear) {
            removeMapEntry(inMemoryDataTree, taskEntryKey("1"));
            removeMapEntry(inMemoryDataTree, taskEntryKey("2"));
            removeMapEntry(inMemoryDataTree, taskEntryKey("4"));
            removeMapEntry(inMemoryDataTree, taskEntryKey("5"));
            removeMapEntry(inMemoryDataTree, taskEntryKey("10"));
        }
    }

    private static void verifyException(final Exception ex, final String expectedStart,
            final String... expectedLeaves) {
        verifyExceptionMessage(expectedStart, ex.getMessage(), expectedLeaves);
        assertThat(ex, instanceOf(YangNetconfErrorAware.class));
        final List<YangNetconfError> errors = ((YangNetconfErrorAware) ex).getNetconfErrors();
        assertEquals(1, errors.size());
        final YangNetconfError error = errors.get(0);
        assertEquals(ErrorSeverity.ERROR, error.severity());
        assertEquals(ErrorType.APPLICATION, error.type());
        assertEquals(ErrorTag.OPERATION_FAILED, error.tag());
        assertEquals("data-not-unique", error.appTag());
    }

    private static void verifyExceptionMessage(final String expectedStart, final String message,
            final String... leafs) {
        assertThat(message, startsWith(expectedStart));
        for (final String leaf : leafs) {
            assertThat(message, containsString(leaf));
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
                        withUniqueViolation ? createMapEntry("3", "l1", "l2", "l10") : createMapEntry("3", "l3", "l4",
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
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(TASK, TASK_ID, taskIdValue))
                .withChild(ImmutableNodes.leafNode(TASK_ID, taskIdValue))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_1, myLeaf1Value))
                .withChild(ImmutableNodes.leafNode(MY_LEAF_2, myLeaf2Value))
                .withChild(
                        Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(MY_CONTAINER))
                                .withChild(ImmutableNodes.leafNode(MY_LEAF_3, myLeaf3Value)).build()).build();
    }

    private static NodeIdentifierWithPredicates taskEntryKey(final String taskId) {
        return NodeIdentifierWithPredicates.of(TASK, TASK_ID, taskId);
    }

    @Test
    public void disabledUniqueIndexTest() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(TEST_MODEL, false);

        writeMapEntry(inMemoryDataTree, "1", "l1", "l2", "l3");
        writeMapEntry(inMemoryDataTree, "2", "l2", "l3", "l4");
        writeMapEntry(inMemoryDataTree, "3", "l3", "l4", "l5");
        writeMapEntry(inMemoryDataTree, "2", "l2", "l3", "l6");
        writeMapEntry(inMemoryDataTree, "10", "l2", "l10", "l4");
        writeMapEntry(inMemoryDataTree, "4", "l1", "l5", "l3");
        writeMapEntry(inMemoryDataTree, "4", "l4", "l5", "l6");
        writeMapEntry(inMemoryDataTree, "5", "l3", "l4", "l7");
        removeMapEntry(inMemoryDataTree, taskEntryKey("3"));
        writeMapEntry(inMemoryDataTree, "5", "l3", "l4", "l7");
        writeMapEntry(inMemoryDataTree, "5", "l3", "l4", "l7");
        writeMapEntry(inMemoryDataTree, "6", "l3", "l4", "l7");
    }

    private static InMemoryDataTree initDataTree(final EffectiveModelContext schemaContext, final boolean uniqueIndex)
            throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) new InMemoryDataTreeFactory().create(
            new DataTreeConfiguration.Builder(TreeType.CONFIGURATION).setUniqueIndexes(uniqueIndex).build());
        inMemoryDataTree.setEffectiveModelContext(schemaContext);

        final MapNode taskNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(TASK)).build();
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK), taskNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
        return inMemoryDataTree;
    }

    private static InMemoryDataTree emptyDataTree(final EffectiveModelContext schemaContext,
            final boolean uniqueIndex) {
        final InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) new InMemoryDataTreeFactory().create(
            new DataTreeConfiguration.Builder(TreeType.CONFIGURATION).setUniqueIndexes(uniqueIndex).build());
        inMemoryDataTree.setEffectiveModelContext(schemaContext);

        return inMemoryDataTree;
    }
}
