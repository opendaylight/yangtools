/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
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
    private static EffectiveModelContext MODEL_CONTEXT;

    @BeforeAll
    static void beforeAll() {
        MODEL_CONTEXT = YangParserTestUtils.parseYang("""
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
    static void afterAll() {
        MODEL_CONTEXT = null;
    }

    private static DataTree initDataTree(final EffectiveModelContext modelContext, final boolean withMapNode)
            throws Exception {
        final var inMemoryDataTree = new ReferenceDataTreeFactory().create(
                DataTreeConfiguration.DEFAULT_CONFIGURATION, modelContext);

        final var root = ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT));
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.merge(YangInstanceIdentifier.of(ROOT),
            withMapNode ? root.withChild(
                ImmutableNodes.newSystemMapBuilder().withNodeIdentifier(new NodeIdentifier(MY_LIST)).build()).build()
                : root.build());
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        return inMemoryDataTree;
    }

    private static DataTree emptyDataTree(final EffectiveModelContext modelContext) {
        return new ReferenceDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, modelContext);
    }

    @Test
    void mergeInvalidContainerTest() throws Exception {
        final var inMemoryDataTree = emptyDataTree(MODEL_CONTEXT);

        final var myList = createMap(true);
        final var root = ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT)).withChild(myList);

        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.merge(YangInstanceIdentifier.of(ROOT), root.build());

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var ex = assertThrows(IllegalArgumentException.class, () -> inMemoryDataTree.prepare(modificationTree));
        assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=1}] is missing "
            + "mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", ex.getMessage());
    }

    @Test
    void mergeInvalidMapTest() throws Exception {
        final var inMemoryDataTree = emptyDataTree(MODEL_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        mergeMap(modificationTree, true);

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var ex = assertThrows(IllegalArgumentException.class, () -> inMemoryDataTree.prepare(modificationTree));
        assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=1}] is missing "
            + "mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", ex.getMessage());
    }

    @Test
    void mergeInvalidMapEntryTest() throws Exception {
        final var inMemoryDataTree = initDataTree(MODEL_CONTEXT, true);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        mergeMapEntry(modificationTree, "1", null, "common-value");

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var ex = assertThrows(IllegalArgumentException.class, () -> inMemoryDataTree.prepare(modificationTree));
        assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=1}] is missing "
            + "mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", ex.getMessage());
    }

    private static void mergeMap(final DataTreeModification modificationTree, final boolean mandatoryDataMissing) {
        modificationTree.merge(YangInstanceIdentifier.of(ROOT, MY_LIST), createMap(mandatoryDataMissing));
    }

    private static SystemMapNode createMap(final boolean mandatoryDataMissing) {
        return ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(MY_LIST))
            .withChild(mandatoryDataMissing ? createMapEntry("1", "common-value")
                : createMapEntry("1", "mandatory-value", "common-value"))
            .build();
    }

    private static void mergeMapEntry(final DataTreeModification modificationTree, final Object listIdValue,
            final Object mandatoryLeafValue, final Object commonLeafValue) {
        final var taskEntryNode = mandatoryLeafValue == null ? createMapEntry(listIdValue, commonLeafValue)
                : createMapEntry(listIdValue, mandatoryLeafValue, commonLeafValue);

        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST)
                .node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, listIdValue)),
            taskEntryNode);
    }

    private static MapEntryNode createMapEntry(final Object listIdValue, final Object mandatoryLeafValue,
            final Object commonLeafValue) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, listIdValue))
            .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue))
            .withChild(ImmutableNodes.leafNode(MANDATORY_LEAF, mandatoryLeafValue))
            .withChild(ImmutableNodes.leafNode(COMMON_LEAF, commonLeafValue))
            .build();
    }

    private static MapEntryNode createMapEntry(final Object listIdValue, final Object commonLeafValue) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, listIdValue))
            .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue))
            .withChild(ImmutableNodes.leafNode(COMMON_LEAF, commonLeafValue))
            .build();
    }

    private static MapEntryNode createMapEntryM(final Object listIdValue, final Object mandatoryLeafValue) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, listIdValue))
            .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue))
            .withChild(ImmutableNodes.leafNode(MANDATORY_LEAF, mandatoryLeafValue))
            .build();
    }

    @Test
    void mergeValidContainerTest() throws Exception {
        final var inMemoryDataTree = emptyDataTree(MODEL_CONTEXT);

        final var myList = createMap(false);
        final var root = ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT)).withChild(myList);

        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.merge(YangInstanceIdentifier.of(ROOT), root.build());
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    void mergeValidMapTest() throws Exception {
        final var inMemoryDataTree = emptyDataTree(MODEL_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        mergeMap(modificationTree, false);

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    void mergeValidMapEntryTest() throws Exception {
        final var inMemoryDataTree = initDataTree(MODEL_CONTEXT, true);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        mergeMapEntry(modificationTree, "1", "mandatory-value", "common-value");

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    void validMultiStepsMergeTest() throws Exception {
        final var inMemoryDataTree = emptyDataTree(MODEL_CONTEXT);
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
    void invalidMultiStepsMergeTest() throws Exception {
        final var inMemoryDataTree = emptyDataTree(MODEL_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.merge(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT, MY_LIST), createMapBuilder().build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createMapEntry("1", "common-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var ex = assertThrows(IllegalArgumentException.class, () -> inMemoryDataTree.prepare(modificationTree));
        assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=1}] is missing "
            + "mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", ex.getMessage());
    }

    private static DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> createEmptyMapEntryBuilder(
            final Object listIdValue) {
        return ImmutableNodes.newMapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, listIdValue))
                .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue));
    }

    private static CollectionNodeBuilder<MapEntryNode, SystemMapNode> createMapBuilder() {
        return ImmutableNodes.newSystemMapBuilder().withNodeIdentifier(new NodeIdentifier(MY_LIST));
    }

    private static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> createContainerBuilder() {
        return ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(ROOT));
    }

    @Test
    void validMultiStepsWriteAndMergeTest() throws Exception {
        final var inMemoryDataTree = emptyDataTree(MODEL_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
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
    void invalidMultiStepsWriteAndMergeTest() {
        final var inMemoryDataTree = emptyDataTree(MODEL_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT, MY_LIST), createMapBuilder().build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createMapEntry("1", "common-value"));

        final var ex = assertThrows(IllegalArgumentException.class, modificationTree::ready);
        assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=1}] is missing "
            + "mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", ex.getMessage());
    }

    @Test
    void validMapEntryMultiCommitMergeTest() throws Exception {
        final var inMemoryDataTree = emptyDataTree(MODEL_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT, MY_LIST), createMapBuilder().build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createMapEntryM("1", "mandatory-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        final var modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree2.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createMapEntry("1", "common-value"));
        modificationTree2.ready();
        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);
    }

    @Test
    void invalidMapEntryMultiCommitMergeTest() throws Exception {
        final var inMemoryDataTree = emptyDataTree(MODEL_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT, MY_LIST), createMapBuilder().build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createMapEntryM("1", "mandatory-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        final var modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree2.write(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createMapEntry("1", "common-value"));
        modificationTree2.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createMapEntryM("1", "mandatory-value"));
        modificationTree2.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "2")),
            createMapEntry("2", "common-value"));
        modificationTree2.ready();
        inMemoryDataTree.validate(modificationTree2);
        final var ex = assertThrows(IllegalArgumentException.class, () -> inMemoryDataTree.prepare(modificationTree2));
        assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=2}] is missing "
            + "mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", ex.getMessage());
    }

    /*
     * This test consists of two transactions (i.e. data tree modifications) on
     * empty data tree. The first one writes mandatory data and second one
     * writes common data without any mandatory data.
     */
    @Test
    void validMapEntryMultiCommitMergeTest2() throws Exception {
        final var inMemoryDataTree = emptyDataTree(MODEL_CONTEXT);
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        final var modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT, MY_LIST), createMapBuilder().build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createMapEntryM("1", "mandatory-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        modificationTree2.merge(
            YangInstanceIdentifier.of(ROOT, MY_LIST).node(NodeIdentifierWithPredicates.of(MY_LIST, LIST_ID, "1")),
            createMapEntry("1", "common-value"));
        modificationTree2.ready();
        inMemoryDataTree.validate(modificationTree2);
        final var prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);
    }
}
