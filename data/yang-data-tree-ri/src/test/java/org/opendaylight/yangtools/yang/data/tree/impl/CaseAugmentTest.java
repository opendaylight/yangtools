/*
 * Copyright (c) 2017 All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class CaseAugmentTest {
    private static final QName CHOICE1_QNAME = QName.create(TestModel.TEST_QNAME, "choice1");
    private static final QName C1L1_QNAME = QName.create(TestModel.TEST_QNAME, "case1-leaf1");
    private static final QName C1L2_QNAME = QName.create(TestModel.TEST_QNAME, "case1-leaf2");
    private static final QName C1L3_QNAME = QName.create(TestModel.TEST_QNAME, "case1-leaf3");
    private static final QName C2L1_QNAME = QName.create(TestModel.TEST_QNAME, "case2-leaf1");
    private static final NodeIdentifier CHOICE_ID = new NodeIdentifier(CHOICE1_QNAME);
    private static final AugmentationIdentifier AUGMENT_ID = new AugmentationIdentifier(
        ImmutableSet.of(C1L2_QNAME, C1L3_QNAME));

    private static EffectiveModelContext SCHEMA_CONTEXT;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYang("""
            module case-augment-test {
                yang-version 1;
                namespace "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test";
                prefix "store-test";
                revision "2014-03-13" {
                    description "Initial revision.";
                }
                container test {
                    choice choice1 {
                        case case1 {
                            leaf case1-leaf1 {
                                type string;
                            }
                        }
                        case case2 {
                            leaf case2-leaf1 {
                                type string;
                            }
                        }
                    }
                }
                augment "/test/choice1/case1" {
                    leaf case1-leaf2 {
                        type string;
                    }
                    leaf case1-leaf3 {
                        type string;
                    }
                }
            }""");
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    private static DataTree initDataTree() {
        DataTree inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION,
            SCHEMA_CONTEXT);
        return inMemoryDataTree;
    }

    @Test
    public void testWriteAugment() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = initDataTree();

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
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, container);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void testWriteCase1All() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = initDataTree();

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
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, container);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteConflict() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = initDataTree();

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
            final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
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
