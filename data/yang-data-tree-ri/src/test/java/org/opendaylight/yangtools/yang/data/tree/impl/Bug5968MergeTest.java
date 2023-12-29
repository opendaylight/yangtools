/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug5968MergeTest {
    private static final String NS = "bug5968";
    private static final String REV = "2016-07-28";
    private static final QName ROOT = QName.create(NS, REV, "root");
    private static final QName MY_LIST = QName.create(NS, REV, "my-list");
    private static final QName LIST_ID = QName.create(NS, REV, "list-id");
    private static final QName MANDATORY_LEAF = QName.create(NS, REV, "mandatory-leaf");
    private static final QName COMMON_LEAF = QName.create(NS, REV, "common-leaf");
    private static EffectiveModelContext SCHEMA_CONTEXT;

    @BeforeAll
    static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYang("""
            module bug5968 {
              yang-version 1;
              namespace bug5968;
              prefix bug5968;

              revision 2016-07-28 {
                description "test";
              }

              container root {
                list my-list {
                  key "list-id";
                  leaf list-id {
                    type string;
                  }
                  leaf mandatory-leaf {
                    type string;
                    mandatory true;
                  }
                  leaf common-leaf {
                    type string;
                  }
                }
              }
            }""");
    }

    @AfterAll
    static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    private static DataTree initDataTree(final EffectiveModelContext schemaContext, final boolean withMapNode)
            throws DataValidationFailedException {
        final var inMemoryDataTree = new InMemoryDataTreeFactory().create(
                DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);

        final var root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT));
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.merge(YangInstanceIdentifier.of(ROOT),
            withMapNode ? root.withChild(
                Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(MY_LIST)).build()).build()
                : root.build());
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        return inMemoryDataTree;
    }

    private static DataTree emptyDataTree(final EffectiveModelContext schemaContext) {
        return new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);
    }

    @Test
    void mergeInvalidContainerTest() throws DataValidationFailedException {
        final var inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);

        final var myList = createMap(true);
        final var root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT)).withChild(myList);

        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.merge(YangInstanceIdentifier.of(ROOT), root.build());

        try {
            modificationTree.ready();
            inMemoryDataTree.validate(modificationTree);
            final var prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
            fail("Should fail due to missing mandatory leaf.");
        } catch (final IllegalArgumentException e) {
            assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=1}] is "
                + "missing mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", e.getMessage());
        }
    }

    @Test
    void mergeInvalidMapTest() throws DataValidationFailedException {
        final var inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        mergeMap(modificationTree, true);

        try {
            modificationTree.ready();
            inMemoryDataTree.validate(modificationTree);
            final var prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
            fail("Should fail due to missing mandatory leaf.");
        } catch (final IllegalArgumentException e) {
            assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=1}] is "
                + "missing mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", e.getMessage());
        }
    }

    @Test
    void mergeInvalidMapEntryTest() throws DataValidationFailedException {
        final var inMemoryDataTree = initDataTree(SCHEMA_CONTEXT, true);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        mergeMapEntry(modificationTree, "1", null, "common-value");

        try {
            modificationTree.ready();
            inMemoryDataTree.validate(modificationTree);
            final var prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
            fail("Should fail due to missing mandatory leaf.");
        } catch (final IllegalArgumentException e) {
            assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=1}] is "
                + "missing mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", e.getMessage());
        }
    }

    private static void mergeMap(final DataTreeModification modificationTree,
            final boolean mandatoryDataMissing) throws DataValidationFailedException {
        modificationTree.merge(YangInstanceIdentifier.of(ROOT, MY_LIST), createMap(mandatoryDataMissing));
    }

    private static SystemMapNode createMap(final boolean mandatoryDataMissing) throws DataValidationFailedException {
        return Builders.mapBuilder()
            .withNodeIdentifier(new NodeIdentifier(MY_LIST))
            .withChild(mandatoryDataMissing ? createMapEntry("1", "common-value")
                : createMapEntry("1", "mandatory-value", "common-value"))
            .build();
    }

    private static void mergeMapEntry(final DataTreeModification modificationTree, final Object listIdValue,
            final Object mandatoryLeafValue, final Object commonLeafValue) throws DataValidationFailedException {
        final var taskEntryNode = mandatoryLeafValue == null ? createMapEntry(listIdValue, commonLeafValue)
                : createMapEntry(listIdValue, mandatoryLeafValue, commonLeafValue);

        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST)
                .node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, listIdValue)),
            taskEntryNode);
    }

    private static MapEntryNode createMapEntry(final Object listIdValue, final Object mandatoryLeafValue,
            final Object commonLeafValue) {
        return Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, listIdValue))
                .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue))
                .withChild(ImmutableNodes.leafNode(MANDATORY_LEAF, mandatoryLeafValue))
                .withChild(ImmutableNodes.leafNode(COMMON_LEAF, commonLeafValue)).build();
    }

    private static MapEntryNode createMapEntry(final Object listIdValue, final Object commonLeafValue) {
        return Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue)))
                .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue))
                .withChild(ImmutableNodes.leafNode(COMMON_LEAF, commonLeafValue)).build();
    }

    private static MapEntryNode createMapEntryM(final Object listIdValue, final Object mandatoryLeafValue) {
        return Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue)))
                .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue))
                .withChild(ImmutableNodes.leafNode(MANDATORY_LEAF, mandatoryLeafValue)).build();
    }

    @Test
    void mergeValidContainerTest() throws DataValidationFailedException {
        final var inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);

        final var myList = createMap(false);
        final var root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT)).withChild(myList);

        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.merge(YangInstanceIdentifier.of(ROOT), root.build());
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    void mergeValidMapTest() throws DataValidationFailedException {
        final var inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        mergeMap(modificationTree, false);

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    void mergeValidMapEntryTest() throws DataValidationFailedException {
        final var inMemoryDataTree = initDataTree(SCHEMA_CONTEXT, true);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        mergeMapEntry(modificationTree, "1", "mandatory-value", "common-value");

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    void validMultiStepsMergeTest() throws DataValidationFailedException {
        final var inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.merge(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT, MY_LIST), createMapBuilder().build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createMapEntry("1", "mandatory-value", "common-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    void invalidMultiStepsMergeTest() throws DataValidationFailedException {
        final var inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.merge(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT, MY_LIST), createMapBuilder().build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createMapEntry("1", "common-value"));

        try {
            modificationTree.ready();
            inMemoryDataTree.validate(modificationTree);
            final var prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
            fail("Should fail due to missing mandatory leaf.");
        } catch (final IllegalArgumentException e) {
            assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=1}] is "
                + "missing mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", e.getMessage());
        }
    }

    private static DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> createEmptyMapEntryBuilder(
            final Object listIdValue) {
        return Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue)))
                .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue));
    }

    private static CollectionNodeBuilder<MapEntryNode, SystemMapNode> createMapBuilder() {
        return Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(MY_LIST));
    }

    private static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> createContainerBuilder() {
        return Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(ROOT));
    }

    @Test
    void validMultiStepsWriteAndMergeTest() throws DataValidationFailedException {
        final var inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT).node(MY_LIST), createMapBuilder().build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntry("1", "mandatory-value", "common-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    void invalidMultiStepsWriteAndMergeTest() throws DataValidationFailedException {
        final var inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT).node(MY_LIST), createMapBuilder().build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntry("1", "common-value"));

        try {
            modificationTree.ready();
            inMemoryDataTree.validate(modificationTree);
            final var prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
            fail("Should fail due to missing mandatory leaf.");
        } catch (final IllegalArgumentException e) {
            assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=1}] is "
                + "missing mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", e.getMessage());
        }
    }

    @Test
    void validMapEntryMultiCommitMergeTest() throws DataValidationFailedException {
        final var inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT).node(MY_LIST), createMapBuilder().build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntryM("1", "mandatory-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        final var modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree2.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntry("1", "common-value"));
        modificationTree2.ready();
        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);
    }

    @Test
    void invalidMapEntryMultiCommitMergeTest() throws DataValidationFailedException {
        final var inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT).node(MY_LIST), createMapBuilder().build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntryM("1", "mandatory-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        final var modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree2.write(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntry("1", "common-value"));
        modificationTree2.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntryM("1", "mandatory-value"));
        modificationTree2.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "2"))),
                createMapEntry("2", "common-value"));
        try {
            modificationTree2.ready();
            inMemoryDataTree.validate(modificationTree2);
            final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
            fail("Should fail due to missing mandatory leaf.");
        } catch (final IllegalArgumentException e) {
            assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=2}] is "
                + "missing mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", e.getMessage());
        }
    }

    /*
     * This test consists of two transactions (i.e. data tree modifications) on
     * empty data tree. The first one writes mandatory data and second one
     * writes common data without any mandatory data.
     */
    @Test
    void validMapEntryMultiCommitMergeTest2() throws DataValidationFailedException {
        final var inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        final var modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT).node(MY_LIST), createMapBuilder().build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntryM("1", "mandatory-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        modificationTree2.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntry("1", "common-value"));
        modificationTree2.ready();
        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);
    }
}
