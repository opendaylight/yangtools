/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

class MandatoryLeafTest {
    private static EffectiveModelContext SCHEMA_CONTEXT;

    @BeforeAll
    static void beforeClass() {
        SCHEMA_CONTEXT = TestModel.createTestContext("/mandatory-leaf-test.yang");
    }

    @AfterAll
    static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    private static DataTree initDataTree(final boolean enableValidation) {
        return new InMemoryDataTreeFactory().create(new DataTreeConfiguration.Builder(TreeType.CONFIGURATION)
            .setMandatoryNodesValidation(enableValidation)
            .build(), SCHEMA_CONTEXT);
    }

    @Test
    void testCorrectMandatoryLeafWrite() throws DataValidationFailedException {
        final var inMemoryDataTree = initDataTree(true);
        final var choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));

        final var container = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(choice1Id)
                .withChild(ImmutableNodes.newContainerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "case2-cont")))
                    .withChild(ImmutableNodes.leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf1"), "leaf-value"))
                    .withChild(ImmutableNodes.leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf2"),
                        "leaf-value2"))
                    .build())
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
    void testCorrectMandatoryLeafChoiceWrite() throws DataValidationFailedException {
        final var inMemoryDataTree = initDataTree(true);
        // Container write
        final var container = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build();

        final var modificationTree1 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree1.write(TestModel.TEST_PATH, container);
        modificationTree1.ready();

        inMemoryDataTree.validate(modificationTree1);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        // Choice write
        final var choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));
        final var choice = ImmutableNodes.newChoiceBuilder()
            .withNodeIdentifier(choice1Id)
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "case2-cont")))
                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf1"), "leaf-value"))
                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf2"), "leaf-value2"))
                .build())
            .build();

        final var modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree2.write(TestModel.TEST_PATH.node(choice1Id), choice);
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);
    }

    @Test
    void testMandatoryLeafViolation() {
        assertThrows(IllegalArgumentException.class, () -> {
            final var inMemoryDataTree = initDataTree(true);
            final var choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));

            final var container = ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(ImmutableNodes.newChoiceBuilder()
                    .withNodeIdentifier(choice1Id)
                    .withChild(ImmutableNodes.newContainerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "case2-cont")))
                        .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf2"), "leaf-value2"))
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
            } catch (final IllegalArgumentException e) {
                assertEquals("Node (urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test?"
                    + "revision=2014-03-13)choice1 is missing mandatory descendant /(urn:opendaylight:params:xml:ns:"
                    + "yang:controller:md:sal:dom:store:test?revision=2014-03-13)case2-cont/case2-leaf1",
                    e.getMessage());
                throw e;
            }
        });
    }

    @Test
    void testDisabledValidation() throws DataValidationFailedException {
        final var inMemoryDataTree = initDataTree(false);
        final var choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));

        final ContainerNode container = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(choice1Id)
                .withChild(ImmutableNodes.newContainerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "case2-cont")))
                    .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf2"), "leaf-value2"))
                    .build())
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
    void testMandatoryLeafViolationChoiceWrite() {
        assertThrows(IllegalArgumentException.class, () -> {
            final var inMemoryDataTree = initDataTree(true);
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
            final var choice = ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(choice1Id)
                .withChild(ImmutableNodes.newContainerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "case2-cont")))
                    .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf2"), "leaf-value2"))
                    .build())
                .build();

            try {
                final var modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
                modificationTree2.write(TestModel.TEST_PATH.node(choice1Id), choice);
                modificationTree2.ready();
                inMemoryDataTree.validate(modificationTree2);
                final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
                inMemoryDataTree.commit(prepare2);
            } catch (final IllegalArgumentException e) {
                assertEquals("Node (urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test?"
                    + "revision=2014-03-13)choice1 is missing mandatory descendant /(urn:opendaylight:params:xml:ns:"
                    + "yang:controller:md:sal:dom:store:test?revision=2014-03-13)case2-cont/case2-leaf1",
                    e.getMessage());
                throw e;
            }
        });
    }

    @Test
    void testDisabledValidationChoiceWrite() throws DataValidationFailedException {
        final var inMemoryDataTree = initDataTree(false);
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
        final var choice = ImmutableNodes.newChoiceBuilder()
            .withNodeIdentifier(choice1Id)
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "case2-cont")))
                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf2"), "leaf-value2"))
                .build())
            .build();

        final var modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree2.write(TestModel.TEST_PATH.node(choice1Id), choice);
        modificationTree2.ready();
        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);
    }
}
