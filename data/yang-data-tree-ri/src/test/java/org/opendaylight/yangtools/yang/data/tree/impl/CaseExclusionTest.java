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
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

class CaseExclusionTest {
    private static EffectiveModelContext MODEL_CONTEXT;

    private DataTree dataTree;

    @BeforeAll
    static void beforeAll() {
        MODEL_CONTEXT = TestModel.createTestContext("/case-exclusion-test.yang");
    }

    @AfterAll
    static void afterAll() {
        MODEL_CONTEXT = null;
    }

    @BeforeEach
    void beforeEach() {
        dataTree = new ReferenceDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, MODEL_CONTEXT);
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
        final var modificationTree = dataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, container);
        modificationTree.ready();

        dataTree.validate(modificationTree);
        final var prepare = dataTree.prepare(modificationTree);
        dataTree.commit(prepare);
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
                final var modificationTree = dataTree.takeSnapshot().newModification();
                modificationTree.write(TestModel.TEST_PATH, container);
                modificationTree.ready();

                dataTree.validate(modificationTree);
                final var prepare = dataTree.prepare(modificationTree);
                dataTree.commit(prepare);
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

            final var modificationTree1 = dataTree.takeSnapshot().newModification();
            modificationTree1.write(TestModel.TEST_PATH, container);
            modificationTree1.ready();

            dataTree.validate(modificationTree1);
            final var prepare1 = dataTree.prepare(modificationTree1);
            dataTree.commit(prepare1);

            // Choice write
            final var choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));
            final var choice = ImmutableNodes.newChoiceBuilder().withNodeIdentifier(choice1Id)
                .withChild(ImmutableNodes.leafNode(QName.create(TestModel.TEST_QNAME, "case1-leaf1"), "leaf-value"))
                .withChild(ImmutableNodes.newContainerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "case2-cont")))
                    .build())
                .build();

            try {
                final var modificationTree2 = dataTree.takeSnapshot().newModification();
                modificationTree2.write(TestModel.TEST_PATH.node(choice1Id), choice);
                modificationTree2.ready();

                dataTree.validate(modificationTree2);

                final var prepare2 = dataTree.prepare(modificationTree2);
                dataTree.commit(prepare2);
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("implies non-presence of child"));
                throw e;
            }
        });
    }
}
