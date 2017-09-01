/*
 * Copyright (c) 2017 All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class CaseAugmentTest {

    private SchemaContext schemaContext;
    private static final QName CHOICE1_QNAME = QName.create(TestModel.TEST_QNAME, "choice1");
    private static final QName C1L1_QNAME = QName.create(TestModel.TEST_QNAME, "case1-leaf1");
    private static final QName C1L2_QNAME = QName.create(TestModel.TEST_QNAME, "case1-leaf2");
    private static final QName C1L3_QNAME = QName.create(TestModel.TEST_QNAME, "case1-leaf3");
    private static final QName C2L1_QNAME = QName.create(TestModel.TEST_QNAME, "case2-leaf1");
    private static final NodeIdentifier CHOICE_ID = new NodeIdentifier(CHOICE1_QNAME);
    private static final AugmentationIdentifier AUGMENT_ID = new AugmentationIdentifier(
        ImmutableSet.of(C1L2_QNAME, C1L3_QNAME));

    @Before
    public void prepare() throws ReactorException {
        schemaContext = TestModel.createTestContext("/case-augment-test.yang");
        assertNotNull("Schema context must not be null.", schemaContext);
    }

    private InMemoryDataTree initDataTree() {
        InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(
                TreeType.CONFIGURATION);
        inMemoryDataTree.setSchemaContext(schemaContext);
        return inMemoryDataTree;
    }

    @Test
    public void testWriteAugment() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree();

        AugmentationNode augmentationNode = Builders.augmentationBuilder()
                .withNodeIdentifier(AUGMENT_ID)
                .withChild(leafNode(C1L2_QNAME, "leaf-value"))
                .build();

        final ContainerNode container = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(
                        Builders.choiceBuilder().withNodeIdentifier(CHOICE_ID)
                                .withChild(augmentationNode)
                                .build()).build();
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, container);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void testWriteCase1All() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree();

        AugmentationNode augmentationNode = Builders.augmentationBuilder()
                .withNodeIdentifier(AUGMENT_ID)
                .withChild(leafNode(C1L2_QNAME, "leaf-value"))
                .withChild(leafNode(C1L3_QNAME, "leaf-value"))
                .build();

        final ContainerNode container = Builders
                .containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(
                        Builders.choiceBuilder().withNodeIdentifier(CHOICE_ID)
                                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case1-leaf1"), "leaf-value"))
                                .withChild(augmentationNode)
                                .build()).build();
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, container);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteConflict() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree();

        AugmentationNode augmentationNode = Builders.augmentationBuilder()
                .withNodeIdentifier(AUGMENT_ID)
                .withChild(leafNode(C1L2_QNAME, "leaf-value"))
                .build();

        final ContainerNode container = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(
                        Builders.choiceBuilder().withNodeIdentifier(CHOICE_ID)
                                .withChild(augmentationNode)
                                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf1"), "leaf-value"))
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

}
