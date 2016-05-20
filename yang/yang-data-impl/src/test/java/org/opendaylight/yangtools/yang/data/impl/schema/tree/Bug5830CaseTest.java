/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug5830CaseTest {

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
        schemaContext = TestModel.createTestContext("/bug5830/foo-case.yang");
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
    public void testCaseMandatoryNodeMissing() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree();
        final ChoiceNode taskChoiceNode = createTaskChoice(false);

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(taskContainer).node(task), taskChoiceNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        System.out.println("Missing mandatory data test: " + inMemoryDataTree);
    }

    @Test
    public void testCaseMandatoryNodePresent() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree();
        final ChoiceNode taskChoiceNode = createTaskChoice(true);

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(taskContainer).node(task), taskChoiceNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        System.out.println("Present mandatory data test: " + inMemoryDataTree);
    }

    private ChoiceNode createTaskChoice(boolean withMandatoryNode) {
        DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> taskChoiceBuilder = Builders.choiceBuilder().withNodeIdentifier(new NodeIdentifier(task)).withChild(ImmutableNodes.leafNode(otherData, "foo"));
        if (withMandatoryNode) {
            taskChoiceBuilder.withChild(ImmutableNodes.leafNode(mandatoryData, "mandatory-data-value"));
        }
        return taskChoiceBuilder.build();
    }
}
