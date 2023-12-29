/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug5830Test {
    private static final String NS = "foo";
    private static final String REV = "2016-05-17";
    private static final QName TASK_CONTAINER = QName.create(NS, REV, "task-container");
    private static final QName TASK = QName.create(NS, REV, "task");
    private static final QName TASK_ID = QName.create(NS, REV, "task-id");
    private static final QName TASK_DATA = QName.create(NS, REV, "task-data");
    private static final QName OTHER_DATA = QName.create(NS, REV, "other-data");
    private static final QName MANDATORY_DATA = QName.create(NS, REV, "mandatory-data");
    private static final QName TASK_MANDATORY_LEAF = QName.create(NS, REV, "task-mandatory-leaf");
    private static final QName NON_PRESENCE_CONTAINER = QName.create(NS, REV, "non-presence-container");
    private static final QName NON_PRESENCE_CONTAINER_2 = QName.create(NS, REV, "non-presence-container-2");
    private static final QName PRESENCE_CONTAINER_2 = QName.create(NS, REV, "presence-container-2");
    private static final QName MANDATORY_LEAF_2 = QName.create(NS, REV, "mandatory-leaf-2");

    private static DataTree initDataTree(final EffectiveModelContext schemaContext)
            throws DataValidationFailedException {
        final var inMemoryDataTree = new InMemoryDataTreeFactory().create(
                DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);

        final var taskNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(TASK)).build();
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(TASK_CONTAINER, TASK), taskNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
        return inMemoryDataTree;
    }

    @Test
    void testMandatoryNodes() throws DataValidationFailedException {
        testPresenceContainer();
        testNonPresenceContainer();
        testMultipleContainers();
    }

    private static void testPresenceContainer() throws DataValidationFailedException {
        final var schemaContext = YangParserTestUtils.parseYang("""
            module foo-presence {
              yang-version 1;
              namespace "foo";
              prefix foo;

              revision 2016-05-17 {
                description "test";
              }

              container task-container {
                list task {
                  key "task-id";
                  leaf task-id {
                    type string;
                  }
                  leaf task-mandatory-leaf {
                    type string;
                    mandatory true;
                  }

                  container task-data {
                    presence "Task data";
                    leaf mandatory-data {
                      type string;
                      mandatory true;
                    }
                    leaf other-data {
                      type string;
                    }
                  }
                }
              }
            }""");
        assertNotNull(schemaContext, "Schema context must not be null.");

        testContainerIsNotPresent(schemaContext);
        final var ex = assertThrows(IllegalArgumentException.class, () -> testContainerIsPresent(schemaContext));
        assertEquals(
            "Node (foo?revision=2016-05-17)task-data is missing mandatory descendant /(foo?revision=2016-05-17)"
                + "mandatory-data", ex.getMessage());
        testMandatoryDataLeafIsPresent(schemaContext);
    }

    private static void testNonPresenceContainer() throws DataValidationFailedException {
        final var schemaContext = YangParserTestUtils.parseYang("""
            module foo-non-presence {
              yang-version 1;
              namespace "foo";
              prefix foo;

              revision 2016-05-17 {
                description "test";
              }

              container task-container {
                list task {
                  key "task-id";

                  leaf task-id {
                    type string;
                  }
                  leaf task-mandatory-leaf {
                    type string;
                    mandatory true;
                  }

                  container task-data {
                    leaf mandatory-data {
                      type string;
                      mandatory true;
                    }
                    leaf other-data {
                      type string;
                    }
                  }
                }
              }
            }""");
        assertNotNull(schemaContext, "Schema context must not be null.");

        try {
            testContainerIsNotPresent(schemaContext);
            fail("Should fail due to missing mandatory node.");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Node (foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=123}] is missing mandatory "
                            + "descendant /(foo?revision=2016-05-17)task-data/mandatory-data", e.getMessage());
        }

        try {
            testContainerIsPresent(schemaContext);
            fail("Should fail due to missing mandatory node.");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Node (foo?revision=2016-05-17)task[{(foo?revision=2016-05-17)task-id=123}] is missing mandatory "
                            + "descendant /(foo?revision=2016-05-17)task-data/mandatory-data", e.getMessage());
        }
        testMandatoryDataLeafIsPresent(schemaContext);
    }

    private static void testMultipleContainers() throws DataValidationFailedException {
        final var schemaContext = YangParserTestUtils.parseYang("""
            module foo-multiple {
              yang-version 1;
              namespace "foo";
              prefix foo;

              revision 2016-05-17 {
                description "test";
              }

              container task-container {
                list task {
                  key "task-id";

                  leaf task-id {
                    type string;
                  }
                  leaf task-mandatory-leaf {
                    type string;
                    mandatory true;
                  }

                  container task-data {
                    presence "Task data";

                    leaf mandatory-data {
                      type string;
                      mandatory true;
                    }
                    leaf other-data {
                      type string;
                    }

                    container non-presence-container {

                      container presence-container {
                        presence "presence container";

                        leaf mandatory-leaf {
                          mandatory true;
                          type string;
                        }
                      }

                      container non-presence-container-2 {
                        leaf mandatory-leaf-2 {
                          mandatory true;
                          type string;
                        }
                      }

                      container presence-container-2 {
                        presence "presence container";

                        leaf mandatory-leaf-3 {
                          mandatory true;
                          type string;
                        }
                      }
                    }
                  }
                }
              }
            }""");
        assertNotNull(schemaContext, "Schema context must not be null.");

        testContainerIsNotPresent(schemaContext);

        try {
            testContainerIsPresent(schemaContext);
            fail("Should fail due to missing mandatory node under present presence container.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith(
                    "Node (foo?revision=2016-05-17)task-data is missing mandatory descendant"));
        }

        try {
            testMandatoryDataLeafIsPresent(schemaContext);
            fail("Should fail due to missing mandatory node under present presence container.");
        } catch (IllegalArgumentException e) {
            assertEquals("""
                Node (foo?revision=2016-05-17)task-data is missing mandatory descendant \
                /(foo?revision=2016-05-17)non-presence-container/\
                non-presence-container-2/mandatory-leaf-2""", e.getMessage());
        }

        testMandatoryLeaf2IsPresent(schemaContext, false);

        try {
            testMandatoryLeaf2IsPresent(schemaContext, true);
            fail("Should fail due to missing mandatory node under present presence container.");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Node (foo?revision=2016-05-17)presence-container-2 is missing mandatory "
                            + "descendant /(foo?revision=2016-05-17)mandatory-leaf-3", e.getMessage());
        }
    }

    private static void testContainerIsNotPresent(final EffectiveModelContext schemaContext)
            throws DataValidationFailedException {
        final var inMemoryDataTree = initDataTree(schemaContext);
        final var taskEntryNode = Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(TASK, TASK_ID, "123"))
                .withChild(ImmutableNodes.leafNode(TASK_ID, "123"))
                .withChild(ImmutableNodes.leafNode(TASK_MANDATORY_LEAF, "mandatory data"))
                .build();

        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
            YangInstanceIdentifier.of(TASK_CONTAINER, TASK).node(NodeIdentifierWithPredicates.of(TASK, TASK_ID, "123")),
            taskEntryNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    private static void testContainerIsPresent(final EffectiveModelContext schemaContext)
            throws DataValidationFailedException {
        final var inMemoryDataTree = initDataTree(schemaContext);

        final var taskEntryNode = Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(TASK, TASK_ID, "123"))
                .withChild(ImmutableNodes.leafNode(TASK_ID, "123"))
                .withChild(ImmutableNodes.leafNode(TASK_MANDATORY_LEAF, "mandatory data"))
                .withChild(createTaskDataContainer(false)).build();

        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
            YangInstanceIdentifier.of(TASK_CONTAINER, TASK).node(NodeIdentifierWithPredicates.of(TASK, TASK_ID, "123")),
            taskEntryNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    private static void testMandatoryDataLeafIsPresent(final EffectiveModelContext schemaContext)
            throws DataValidationFailedException {
        final var inMemoryDataTree = initDataTree(schemaContext);

        final var taskEntryNode = Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(TASK, TASK_ID, "123"))
                .withChild(ImmutableNodes.leafNode(TASK_ID, "123"))
                .withChild(ImmutableNodes.leafNode(TASK_MANDATORY_LEAF, "mandatory data"))
                .withChild(createTaskDataContainer(true)).build();

        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
            YangInstanceIdentifier.of(TASK_CONTAINER, TASK).node(NodeIdentifierWithPredicates.of(TASK, TASK_ID, "123")),
            taskEntryNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    private static void testMandatoryLeaf2IsPresent(final EffectiveModelContext schemaContext,
            final boolean withPresenceContianer) throws DataValidationFailedException {
        final var inMemoryDataTree = initDataTree(schemaContext);

        final var taskEntryNode = Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(TASK, TASK_ID, "123"))
                .withChild(ImmutableNodes.leafNode(TASK_ID, "123"))
                .withChild(ImmutableNodes.leafNode(TASK_MANDATORY_LEAF, "mandatory data"))
                .withChild(createTaskDataMultipleContainer(withPresenceContianer))
                .build();

        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
            YangInstanceIdentifier.of(TASK_CONTAINER, TASK).node(NodeIdentifierWithPredicates.of(TASK, TASK_ID, "123")),
            taskEntryNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    private static DataContainerChild createTaskDataContainer(final boolean withMandatoryNode) {
        final var taskDataBuilder = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TASK_DATA))
                .withChild(ImmutableNodes.leafNode(OTHER_DATA, "foo"));
        if (withMandatoryNode) {
            taskDataBuilder.withChild(ImmutableNodes.leafNode(MANDATORY_DATA, "mandatory-data-value"));
        }
        return taskDataBuilder.build();
    }

    private static DataContainerChild createTaskDataMultipleContainer(final boolean withPresenceContianer) {
        final var nonPresenceContainerBuilder = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(NON_PRESENCE_CONTAINER))
            .withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(NON_PRESENCE_CONTAINER_2))
                .withChild(ImmutableNodes.leafNode(MANDATORY_LEAF_2, "mandatory leaf data 2")).build());

        if (withPresenceContianer) {
            nonPresenceContainerBuilder.withChild(Builders.containerBuilder()
                    .withNodeIdentifier(new NodeIdentifier(PRESENCE_CONTAINER_2)).build());
        }

        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TASK_DATA))
            .withChild(ImmutableNodes.leafNode(OTHER_DATA, "foo"))
            .withChild(ImmutableNodes.leafNode(MANDATORY_DATA, "mandatory-data-value"))
            .withChild(nonPresenceContainerBuilder.build())
            .build();
    }
}
