/*
 * Copyright (c) 2017 All rights reserved.
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
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class CaseAugmentTest {
    private static final QName CHOICE1_QNAME = QName.create(TestModel.TEST_QNAME, "choice1");
    private static final QName C1L2_QNAME = QName.create(TestModel.TEST_QNAME, "case1-leaf2");
    private static final QName C1L3_QNAME = QName.create(TestModel.TEST_QNAME, "case1-leaf3");
    private static final NodeIdentifier CHOICE_ID = new NodeIdentifier(CHOICE1_QNAME);

    private static EffectiveModelContext SCHEMA_CONTEXT;

    @BeforeAll
    static void beforeClass() {
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

    @AfterAll
    static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    private static DataTree initDataTree() {
        return new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION,
            SCHEMA_CONTEXT);
    }

    @Test
    void testWriteAugment() throws DataValidationFailedException {
        final var inMemoryDataTree = initDataTree();

        final var container = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(CHOICE_ID)
                .withChild(ImmutableNodes.leafNode(C1L2_QNAME, "leaf-value"))
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
    void testWriteCase1All() throws DataValidationFailedException {
        final var inMemoryDataTree = initDataTree();

        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(CHOICE_ID)
                .withChild(ImmutableNodes.leafNode(QName.create(TestModel.TEST_QNAME, "case1-leaf1"), "leaf-value"))
                .withChild(ImmutableNodes.leafNode(C1L2_QNAME, "leaf-value"))
                .withChild(ImmutableNodes.leafNode(C1L3_QNAME, "leaf-value"))
                .build())
            .build());
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    void testWriteConflict() throws DataValidationFailedException {
        final var modificationTree = initDataTree().takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(CHOICE_ID)
                .withChild(ImmutableNodes.leafNode(C1L2_QNAME, "leaf-value"))
                .withChild(ImmutableNodes.leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf1"), "leaf-value"))
                .build())
            .build());

        final var e = assertThrows(IllegalArgumentException.class, modificationTree::ready);
        assertTrue(e.getMessage().contains(" implies non-presence of child "));
    }
}
