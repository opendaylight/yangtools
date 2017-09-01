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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug5830Test {
    private static final String NS = "foo";
    private static final String REV = "2016-05-17";
    private static final QName TASK_CONTAINER = QName.create(NS, REV, "task-container");
    private static final QName TASK = QName.create(NS, REV, "task");
    private static final QName TASK_ID = QName.create(NS, REV, "task-id");
    private static final QName TASK_DATA = QName.create(NS, REV, "task-data");
    private static final QName OTHER_DATA = QName.create(NS, REV, "other-data");
    private static final QName MANDATORY_DATA = QName.create(NS, REV, "mandatory-data");
    private static final QName TASK_MANDATORY_LEAF = QName.create(NS, REV, "task-mandatory-leaf");
    private static final QName NON_PRESENCE_CONTAINER = QName.create(NS, REV, "non-presence-container");
    private static final QName NON_PRESENCE_CONTAINER_2 = QName.create(NS, REV, "non-presence-container-2");
    private static final QName PRESENCE_CONTAINER_2 = QName.create(NS, REV, "presence-container-2");
    private static final QName MANDATORY_LEAF_2 = QName.create(NS, REV, "mandatory-leaf-2");

    private static InMemoryDataTree initDataTree(final SchemaContext schemaContext)
            throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(
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

    @Test
    public void testMandatoryNodes() throws ReactorException, DataValidationFailedException {
        testPresenceContainer();
        testNonPresenceContainer();
        testMultipleContainers();
    }

    private static void testPresenceContainer() throws ReactorException, DataValidationFailedException {
        final SchemaContext schemaContext = TestModel.createTestContext("/bug5830/foo-presence.yang");
        assertNotNull("Schema context must not be null.", schemaContext);

        testContainerIsNotPresent(schemaContext);
        try {
            testContainerIsPresent(schemaContext);
            fail("Should fail due to missing mandatory node under present presence container.");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Node (foo?revision=2016-05-17)task-data is missing mandatory descendant /(foo?revision=2016-05-17)"
                            + "mandatory-data", e.getMessage());
        }
        testMandatoryDataLeafIsPresent(schemaContext);
    }

    private static void testNonPresenceContainer() throws ReactorException, DataValidationFailedException {
        final SchemaContext schemaContext = TestModel.createTestContext("/bug5830/foo-non-presence.yang");
        assertNotNull("Schema context must not be null.", schemaContext);

        try {
            testContainerIsNotPresent(schemaContext);
            fail("Should fail due to missing mandatory node.");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Node (foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=123}] is missing mandatory "
                            + "descendant /(foo?revision=2016-05-17)task-data/mandatory-data", e.getMessage());
        }

        try {
            testContainerIsPresent(schemaContext);
            fail("Should fail due to missing mandatory node.");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Node (foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=123}] is missing mandatory "
                            + "descendant /(foo?revision=2016-05-17)task-data/mandatory-data", e.getMessage());
        }
        testMandatoryDataLeafIsPresent(schemaContext);
    }

    private static void testMultipleContainers() throws ReactorException, DataValidationFailedException {
        final SchemaContext schemaContext = TestModel.createTestContext("/bug5830/foo-multiple.yang");
        assertNotNull("Schema context must not be null.", schemaContext);

        testContainerIsNotPresent(schemaContext);

        try {
            testContainerIsPresent(schemaContext);
            fail("Should fail due to missing mandatory node under present presence container.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith(
                    "Node (foo?revision=2016-05-17)task-data is missing mandatory descendant"));
        }

        try {
            testMandatoryDataLeafIsPresent(schemaContext);
            fail("Should fail due to missing mandatory node under present presence container.");
        } catch (IllegalArgumentException e) {
            assertEquals("Node (foo?revision=2016-05-17)task-data "
                    + "is missing mandatory descendant /(foo?revision=2016-05-17)non-presence-container/"
                    + "non-presence-container-2/mandatory-leaf-2", e.getMessage());
        }

        testMandatoryLeaf2IsPresent(schemaContext, false);

        try {
            testMandatoryLeaf2IsPresent(schemaContext, true);
            fail("Should fail due to missing mandatory node under present presence container.");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Node (foo?revision=2016-05-17)presence-container-2 is missing mandatory "
                            + "descendant /(foo?revision=2016-05-17)mandatory-leaf-3", e.getMessage());
        }
    }

    private static void testContainerIsNotPresent(final SchemaContext schemaContext)
            throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(schemaContext);
        final MapEntryNode taskEntryNode = Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, "123")))
                .withChild(ImmutableNodes.leafNode(TASK_ID, "123"))
                .withChild(ImmutableNodes.leafNode(TASK_MANDATORY_LEAF, "mandatory data")).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
                YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK)
                        .node(new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, "123"))), taskEntryNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    private static void testContainerIsPresent(final SchemaContext schemaContext) throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(schemaContext);

        final MapEntryNode taskEntryNode = Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, "123")))
                .withChild(ImmutableNodes.leafNode(TASK_ID, "123"))
                .withChild(ImmutableNodes.leafNode(TASK_MANDATORY_LEAF, "mandatory data"))
                .withChild(createTaskDataContainer(false)).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
                YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK)
                        .node(new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, "123"))), taskEntryNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    private static void testMandatoryDataLeafIsPresent(final SchemaContext schemaContext)
            throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(schemaContext);

        final MapEntryNode taskEntryNode = Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, "123")))
                .withChild(ImmutableNodes.leafNode(TASK_ID, "123"))
                .withChild(ImmutableNodes.leafNode(TASK_MANDATORY_LEAF, "mandatory data"))
                .withChild(createTaskDataContainer(true)).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
                YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK)
                        .node(new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, "123"))), taskEntryNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    private static void testMandatoryLeaf2IsPresent(final SchemaContext schemaContext, final boolean withPresenceContianer)
            throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(schemaContext);

        final MapEntryNode taskEntryNode = Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, "123")))
                .withChild(ImmutableNodes.leafNode(TASK_ID, "123"))
                .withChild(ImmutableNodes.leafNode(TASK_MANDATORY_LEAF, "mandatory data"))
                .withChild(createTaskDataMultipleContainer(withPresenceContianer)).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
                YangInstanceIdentifier.of(TASK_CONTAINER).node(TASK)
                        .node(new NodeIdentifierWithPredicates(TASK, ImmutableMap.of(TASK_ID, "123"))), taskEntryNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    private static DataContainerChild<?, ?> createTaskDataContainer(final boolean withMandatoryNode) {
        DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> taskDataBuilder = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TASK_DATA))
                .withChild(ImmutableNodes.leafNode(OTHER_DATA, "foo"));
        if (withMandatoryNode) {
            taskDataBuilder.withChild(ImmutableNodes.leafNode(MANDATORY_DATA, "mandatory-data-value"));
        }
        return taskDataBuilder.build();
    }

    private static DataContainerChild<?, ?> createTaskDataMultipleContainer(final boolean withPresenceContianer) {
        DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> nonPresenceContainerBuilder = Builders
                .containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(NON_PRESENCE_CONTAINER))
                .withChild(
                        Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(NON_PRESENCE_CONTAINER_2))
                                .withChild(ImmutableNodes.leafNode(MANDATORY_LEAF_2, "mandatory leaf data 2")).build());

        if (withPresenceContianer) {
            nonPresenceContainerBuilder.withChild(Builders.containerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(PRESENCE_CONTAINER_2)).build());
        }

        DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> taskDataBuilder = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TASK_DATA))
                .withChild(ImmutableNodes.leafNode(OTHER_DATA, "foo"));
        taskDataBuilder.withChild(ImmutableNodes.leafNode(MANDATORY_DATA, "mandatory-data-value"));
        taskDataBuilder.withChild(nonPresenceContainerBuilder.build());

        return taskDataBuilder.build();
    }
}
