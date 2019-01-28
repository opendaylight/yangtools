/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class MandatoryLeafTest {

    private static SchemaContext SCHEMA_CONTEXT;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = TestModel.createTestContext("/mandatory-leaf-test.yang");
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    private static DataTree initDataTree(final boolean enableValidation) {
        return new InMemoryDataTreeFactory().create(
                new DataTreeConfiguration.Builder(TreeType.CONFIGURATION).setMandatoryNodesValidation(enableValidation)
                        .build(), SCHEMA_CONTEXT);
    }

    @Test
    public void testCorrectMandatoryLeafWrite() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = initDataTree(true);
        final NodeIdentifier choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));

        final ContainerNode container = Builders
                .containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(
                        Builders.choiceBuilder()
                                .withNodeIdentifier(choice1Id)
                                .withChild(
                                        Builders.containerBuilder()
                                                .withNodeIdentifier(
                                                        new NodeIdentifier(QName.create(TestModel.TEST_QNAME,
                                                                "case2-cont")))
                                                .withChild(
                                                        leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf1"),
                                                                "leaf-value"))
                                                .withChild(
                                                        leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf2"),
                                                                "leaf-value2")).build()).build()).build();

        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, container);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void testCorrectMandatoryLeafChoiceWrite() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = initDataTree(true);
        // Container write
        final ContainerNode container = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME)).build();

        final DataTreeModification modificationTree1 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree1.write(TestModel.TEST_PATH, container);
        modificationTree1.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        // Choice write
        final NodeIdentifier choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));
        final ChoiceNode choice = Builders
                .choiceBuilder()
                .withNodeIdentifier(choice1Id)
                .withChild(
                        Builders.containerBuilder()
                                .withNodeIdentifier(
                                        new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "case2-cont")))
                                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf1"), "leaf-value"))
                                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf2"), "leaf-value2"))
                                .build()).build();

        final DataTreeModification modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree2.write(TestModel.TEST_PATH.node(choice1Id), choice);
        modificationTree2.ready();

        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMandatoryLeafViolation() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = initDataTree(true);
        final NodeIdentifier choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));

        final ContainerNode container = Builders
                .containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(
                        Builders.choiceBuilder()
                                .withNodeIdentifier(choice1Id)
                                .withChild(
                                        Builders.containerBuilder()
                                                .withNodeIdentifier(
                                                        new NodeIdentifier(QName.create(TestModel.TEST_QNAME,
                                                                "case2-cont")))
                                                .withChild(
                                                        leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf2"),
                                                                "leaf-value2")).build()).build()).build();
        try {
            final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
            modificationTree.write(TestModel.TEST_PATH, container);
            modificationTree.ready();

            inMemoryDataTree.validate(modificationTree);
            final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
        } catch (final IllegalArgumentException e) {
            assertEquals("Node (urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test?"
                    + "revision=2014-03-13)choice1 is missing mandatory descendant /(urn:opendaylight:params:xml:ns:"
                    + "yang:controller:md:sal:dom:store:test?revision=2014-03-13)case2-cont/case2-leaf1",
                    e.getMessage());
            throw e;
        }
    }

    @Test
    public void testDisabledValidation() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = initDataTree(false);
        final NodeIdentifier choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));

        final ContainerNode container = Builders
                .containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(
                        Builders.choiceBuilder()
                                .withNodeIdentifier(choice1Id)
                                .withChild(
                                        Builders.containerBuilder()
                                                .withNodeIdentifier(
                                                        new NodeIdentifier(QName.create(TestModel.TEST_QNAME,
                                                                "case2-cont")))
                                                .withChild(
                                                        leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf2"),
                                                                "leaf-value2")).build()).build()).build();
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, container);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMandatoryLeafViolationChoiceWrite() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = initDataTree(true);
        // Container write
        final ContainerNode container = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME)).build();

        final DataTreeModification modificationTree1 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree1.write(TestModel.TEST_PATH, container);
        modificationTree1.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        // Choice write
        final NodeIdentifier choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));
        final ChoiceNode choice = Builders
                .choiceBuilder()
                .withNodeIdentifier(choice1Id)
                .withChild(
                        Builders.containerBuilder()
                                .withNodeIdentifier(
                                        new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "case2-cont")))
                                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf2"), "leaf-value2"))
                                .build()).build();

        try {
            final DataTreeModification modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
            modificationTree2.write(TestModel.TEST_PATH.node(choice1Id), choice);
            modificationTree2.ready();
            inMemoryDataTree.validate(modificationTree2);
            final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
        } catch (final IllegalArgumentException e) {
            assertEquals("Node (urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test?"
                    + "revision=2014-03-13)choice1 is missing mandatory descendant /(urn:opendaylight:params:xml:ns:"
                    + "yang:controller:md:sal:dom:store:test?revision=2014-03-13)case2-cont/case2-leaf1",
                    e.getMessage());
            throw e;
        }
    }

    @Test
    public void testDisabledValidationChoiceWrite() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = initDataTree(false);
        // Container write
        final ContainerNode container = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME)).build();

        final DataTreeModification modificationTree1 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree1.write(TestModel.TEST_PATH, container);
        modificationTree1.ready();

        inMemoryDataTree.validate(modificationTree1);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare1);

        // Choice write
        final NodeIdentifier choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));
        final ChoiceNode choice = Builders
                .choiceBuilder()
                .withNodeIdentifier(choice1Id)
                .withChild(
                        Builders.containerBuilder()
                                .withNodeIdentifier(
                                        new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "case2-cont")))
                                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf2"), "leaf-value2"))
                                .build()).build();

        final DataTreeModification modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree2.write(TestModel.TEST_PATH.node(choice1Id), choice);
        modificationTree2.ready();
        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);
    }
}
