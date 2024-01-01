/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

class CaseExclusionTest {
    private static EffectiveModelContext SCHEMA_CONTEXT;

    private DataTree inMemoryDataTree;

    @BeforeAll
    static void beforeClass() {
        SCHEMA_CONTEXT = TestModel.createTestContext("/case-exclusion-test.yang");
    }

    @AfterAll
    static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @BeforeEach
    void before() {
        inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION,
            SCHEMA_CONTEXT);
    }

    @Test
    void testCorrectCaseWrite() throws DataValidationFailedException {
        final var choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));

        final var container = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(choice1Id)
                .withChild(ImmutableNodes.leafNode(QName.create(TestModel.TEST_QNAME, "case1-leaf1"), "leaf-value"))
                .build())
            .build();
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, container);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    void testCaseExclusion() {
        assertThrows(IllegalArgumentException.class, () -> {
            final var choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));
            final var container = ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(ImmutableNodes.newChoiceBuilder()
                    .withNodeIdentifier(choice1Id)
                    .withChild(ImmutableNodes.leafNode(QName.create(TestModel.TEST_QNAME, "case1-leaf1"), "leaf-value"))
                    .withChild(ImmutableNodes.newContainerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "case2-cont")))
                        .build())
                    .build())
                .build();

            try {
                final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
                modificationTree.write(TestModel.TEST_PATH, container);
                modificationTree.ready();

                inMemoryDataTree.validate(modificationTree);
                final var prepare = inMemoryDataTree.prepare(modificationTree);
                inMemoryDataTree.commit(prepare);
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("implies non-presence of child"));
                throw e;
            }
        });
    }

    @Test
    void testCaseExclusionOnChoiceWrite() {
        assertThrows(IllegalArgumentException.class, () -> {
            // Container write
            final var container = ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME)).build();

            final var modificationTree1 = inMemoryDataTree.takeSnapshot().newModification();
            modificationTree1.write(TestModel.TEST_PATH, container);
            modificationTree1.ready();

            inMemoryDataTree.validate(modificationTree1);
            final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
            inMemoryDataTree.commit(prepare1);

            // Choice write
            final var choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));
            final var choice = ImmutableNodes.newChoiceBuilder().withNodeIdentifier(choice1Id)
                .withChild(ImmutableNodes.leafNode(QName.create(TestModel.TEST_QNAME, "case1-leaf1"), "leaf-value"))
                .withChild(ImmutableNodes.newContainerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "case2-cont")))
                    .build())
                .build();

            try {
                final var modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
                modificationTree2.write(TestModel.TEST_PATH.node(choice1Id), choice);
                modificationTree2.ready();

                inMemoryDataTree.validate(modificationTree2);

                final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
                inMemoryDataTree.commit(prepare2);
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("implies non-presence of child"));
                throw e;
            }
        });
    }
}
