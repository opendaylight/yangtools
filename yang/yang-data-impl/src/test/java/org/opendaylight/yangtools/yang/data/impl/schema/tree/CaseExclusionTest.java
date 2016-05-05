/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class CaseExclusionTest {

    private SchemaContext schemaContext;

    @Before
    public void prepare() throws ReactorException {
        schemaContext = TestModel.createTestContext("/case-exclusion-test.yang");
        assertNotNull("Schema context must not be null.", schemaContext);
    }

    private InMemoryDataTree initDataTree() {
        InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(
                TreeType.CONFIGURATION);
        inMemoryDataTree.setSchemaContext(schemaContext);
        return inMemoryDataTree;
    }

    @Test
    public void testCorrectCaseWrite() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree();
        final NodeIdentifier choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));

        final ContainerNode container = Builders
                .containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(
                        Builders.choiceBuilder().withNodeIdentifier(choice1Id)
                                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case1-leaf1"), "leaf-value"))
                                .build()).build();
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, container);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCaseExclusion() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree();
        final NodeIdentifier choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));

        final ContainerNode container = Builders
                .containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(
                        Builders.choiceBuilder()
                                .withNodeIdentifier(choice1Id)
                                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case1-leaf1"), "leaf-value"))
                                .withChild(
                                        ImmutableNodes.containerNode(QName.create(TestModel.TEST_QNAME, "case2-cont")))
                                .build()).build();
        try {
            final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
            modificationTree.write(TestModel.TEST_PATH, container);
            modificationTree.ready();

            inMemoryDataTree.validate(modificationTree);
            final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("implies non-presence of child"));
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCaseExclusionOnChoiceWrite() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree();
        // Container write
        final ContainerNode container = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME)).build();

        final InMemoryDataTreeModification modificationTree1 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree1.write(TestModel.TEST_PATH, container);
        modificationTree1.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        // Choice write
        final NodeIdentifier choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));
        final ChoiceNode choice = Builders.choiceBuilder().withNodeIdentifier(choice1Id)
                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case1-leaf1"), "leaf-value"))
                .withChild(ImmutableNodes.containerNode(QName.create(TestModel.TEST_QNAME, "case2-cont"))).build();

        try {
            final InMemoryDataTreeModification modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
            modificationTree2.write(TestModel.TEST_PATH.node(choice1Id), choice);
            modificationTree2.ready();

            inMemoryDataTree.validate(modificationTree2);

            final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("implies non-presence of child"));
            throw e;
        }
    }
}
