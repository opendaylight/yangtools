/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
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
    private SchemaContext schemaContext;
    private QName taskContainer;
    private QName task;
    private QName taskId;
    private QName taskData;
    private QName otherData;
    private QName mandatoryData;

    @Before
    public void prepare() throws ReactorException {
        schemaContext = TestModel.createTestContext("/bug5830/foo.yang");
        assertNotNull("Schema context must not be null.", schemaContext);

        taskContainer = QName.create(NS, REV, "task-container");
        task = QName.create(NS, REV, "task");
        taskId = QName.create(NS, REV, "task-id");
        taskData = QName.create(NS, REV, "task-data");
        otherData = QName.create(NS, REV, "other-data");
        mandatoryData = QName.create(NS, REV, "mandatory-data");
    }

    private InMemoryDataTree initDataTree() {
        InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(
                TreeType.CONFIGURATION);
        inMemoryDataTree.setSchemaContext(schemaContext);
        return inMemoryDataTree;
    }

    @Test
    public void testCorrectMandatoryLeafWrite() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree();
        final NodeIdentifier taskIdentifier = new NodeIdentifier(task);

        final MapNode taskNode = Builders
                .mapBuilder()
                .withNodeIdentifier(taskIdentifier)
                .withChild(
                        Builders.mapEntryBuilder()
                                .withNodeIdentifier(
                                        new NodeIdentifierWithPredicates(task, ImmutableMap.of(taskId, "123")))
                                .withChild(ImmutableNodes.leafNode(taskId, "123")).build()).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(taskContainer).node(task), taskNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        System.out.println("Test1: "+inMemoryDataTree);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMandatoryLeafViolation() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree();
        final NodeIdentifier taskIdentifier = new NodeIdentifier(task);

        final MapNode taskNode = Builders
                .mapBuilder()
                .withNodeIdentifier(taskIdentifier)
                .withChild(
                        Builders.mapEntryBuilder()
                                .withNodeIdentifier(
                                        new NodeIdentifierWithPredicates(task, ImmutableMap.of(taskId, "123")))
                                .withChild(ImmutableNodes.leafNode(taskId, "123"))
                                .withChild(createTaskDataContainer(false)).build()).build();
        try {
            final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
            modificationTree.write(YangInstanceIdentifier.of(taskContainer).node(task), taskNode);
            modificationTree.ready();

            inMemoryDataTree.validate(modificationTree);
            final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);

            System.out.println("Missing mandatory data test: "+inMemoryDataTree);
        } catch (IllegalArgumentException e) {
            assertEquals("", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testMandatoryLeafInPresenceContainer() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree();
        final NodeIdentifier taskIdentifier = new NodeIdentifier(task);

        final MapNode taskNode = Builders
                .mapBuilder()
                .withNodeIdentifier(taskIdentifier)
                .withChild(
                        Builders.mapEntryBuilder()
                                .withNodeIdentifier(
                                        new NodeIdentifierWithPredicates(task, ImmutableMap.of(taskId, "123")))
                                .withChild(ImmutableNodes.leafNode(taskId, "123"))
                                .withChild(createTaskDataContainer(true)).build()).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(taskContainer).node(task), taskNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        System.out.println("Present mandatory data test: "+inMemoryDataTree);
    }

    private DataContainerChild<?, ?> createTaskDataContainer(boolean withMandatoryNode) {
        DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> taskDataBuilder = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(taskData)).withChild(ImmutableNodes.leafNode(otherData, "foo"));
        if (withMandatoryNode) {
            taskDataBuilder.withChild(ImmutableNodes.leafNode(mandatoryData, "mandatory-data-value"));
        }
        return taskDataBuilder.build();
    }
}
