/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertNotNull;

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
                TreeType.CONFIGURATION);
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
                TreeType.CONFIGURATION);
        inMemoryDataTree.setSchemaContext(schemaContext);

        return inMemoryDataTree;
    }

    // @Test
    // public void testUniqueConstraint() throws ReactorException,
    // DataValidationFailedException {
    // mapEntryTest();
    // //mapTest();
    // }

    @Test
    public void mapTest() throws ReactorException, DataValidationFailedException {
        final SchemaContext schemaContext = TestModel.createTestContext("/bug4955/foo.yang");
        assertNotNull("Schema context must not be null.", schemaContext);

        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        writeMap(inMemoryDataTree);
        System.out.println(inMemoryDataTree);
        writeMapEntry(inMemoryDataTree, "4", "l1", "l2", "l3");
        System.out.println(inMemoryDataTree);

    }

    @Test
    public void mapEntryTest() throws ReactorException, DataValidationFailedException {
        final SchemaContext schemaContext = TestModel.createTestContext("/bug4955/foo.yang");
        assertNotNull("Schema context must not be null.", schemaContext);

        final InMemoryDataTree inMemoryDataTree = initDataTree(schemaContext);
        writeMapEntry(inMemoryDataTree, "1", "l1", "l2", "l3");
        System.out.println(inMemoryDataTree);
        writeMapEntry(inMemoryDataTree, "2", "l2", "l3", "l4");
        System.out.println(inMemoryDataTree);
        writeMapEntry(inMemoryDataTree, "3", "l3", "l4", "l5");
        System.out.println(inMemoryDataTree);
        writeMapEntry(inMemoryDataTree, "2", "l2", "l3", "l6");
        System.out.println(inMemoryDataTree);
        writeMapEntry(inMemoryDataTree, "4", "l1", "l2", "l3");
        System.out.println(inMemoryDataTree);

    }

    private static void writeMap(final InMemoryDataTree inMemoryDataTree)
            throws DataValidationFailedException {
        final MapNode taskNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(TASK))
                .withChild(createMapEntry("1", "l1", "l2", "l3"))
                .withChild(createMapEntry("2", "l2", "l3", "l4"))
                .withChild(createMapEntry("3", "l1", "l2", "l3")).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
                YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK),
                taskNode);
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
