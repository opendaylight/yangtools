/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug5968MergeTest {
    private static final String NS = "foo";
    private static final String REV = "2016-07-28";
    private static final QName ROOT = QName.create(NS, REV, "root");
    private static final QName MY_LIST = QName.create(NS, REV, "my-list");
    private static final QName LIST_ID = QName.create(NS, REV, "list-id");
    private static final QName MANDATORY_LEAF = QName.create(NS, REV, "mandatory-leaf");
    private static final QName COMMON_LEAF = QName.create(NS, REV, "common-leaf");
    private static SchemaContext SCHEMA_CONTEXT;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = TestModel.createTestContext("/bug5968/foo.yang");
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    private static DataTree initDataTree(final SchemaContext schemaContext, final boolean withMapNode)
            throws DataValidationFailedException {
        final DataTree inMemoryDataTree = new InMemoryDataTreeFactory().create(
                DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);

        final DataContainerNodeBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT));
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT),
                withMapNode ? root.withChild(
                        Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(MY_LIST)).build()).build() : root
                        .build());
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        return inMemoryDataTree;
    }

    private static DataTree emptyDataTree(final SchemaContext schemaContext)
            throws DataValidationFailedException {
        return new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);
    }

    @Test
    public void mergeInvalidContainerTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);

        final MapNode myList = createMap(true);
        final DataContainerNodeBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT)).withChild(myList);

        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.merge(YangInstanceIdentifier.of(ROOT), root.build());

        try {
            modificationTree.ready();
            inMemoryDataTree.validate(modificationTree);
            final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
            fail("Should fail due to missing mandatory leaf.");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                    "Node (foo?revision=2016-07-28)my-list[{(foo?revision=2016-07-28)list-id=1}] is missing mandatory "
                            + "descendant /(foo?revision=2016-07-28)mandatory-leaf", e.getMessage());
        }
    }

    @Test
    public void mergeInvalidMapTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        mergeMap(modificationTree, true);

        try {
            modificationTree.ready();
            inMemoryDataTree.validate(modificationTree);
            final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
            fail("Should fail due to missing mandatory leaf.");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                    "Node (foo?revision=2016-07-28)my-list[{(foo?revision=2016-07-28)list-id=1}] is missing mandatory "
                            + "descendant /(foo?revision=2016-07-28)mandatory-leaf", e.getMessage());
        }
    }

    @Test
    public void mergeInvalidMapEntryTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = initDataTree(SCHEMA_CONTEXT, true);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        mergeMapEntry(modificationTree, "1", null, "common-value");

        try {
            modificationTree.ready();
            inMemoryDataTree.validate(modificationTree);
            final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
            fail("Should fail due to missing mandatory leaf.");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                    "Node (foo?revision=2016-07-28)my-list[{(foo?revision=2016-07-28)list-id=1}] is missing mandatory "
                            + "descendant /(foo?revision=2016-07-28)mandatory-leaf", e.getMessage());
        }
    }

    private static void mergeMap(final DataTreeModification modificationTree,
            final boolean mandatoryDataMissing) throws DataValidationFailedException {
        final MapNode myList = createMap(mandatoryDataMissing);
        modificationTree.merge(YangInstanceIdentifier.of(ROOT).node(MY_LIST), myList);
    }

    private static MapNode createMap(final boolean mandatoryDataMissing) throws DataValidationFailedException {
        return Builders
                .mapBuilder()
                .withNodeIdentifier(new NodeIdentifier(MY_LIST))
                .withChild(
                        mandatoryDataMissing ? createMapEntry("1", "common-value") : createMapEntry("1",
                                "mandatory-value", "common-value")).build();
    }

    private static void mergeMapEntry(final DataTreeModification modificationTree, final Object listIdValue,
            final Object mandatoryLeafValue, final Object commonLeafValue) throws DataValidationFailedException {
        final MapEntryNode taskEntryNode = mandatoryLeafValue == null ? createMapEntry(listIdValue, commonLeafValue)
                : createMapEntry(listIdValue, mandatoryLeafValue, commonLeafValue);

        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue))),
                taskEntryNode);
    }

    private static MapEntryNode createMapEntry(final Object listIdValue, final Object mandatoryLeafValue,
            final Object commonLeafValue) throws DataValidationFailedException {
        return Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue)))
                .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue))
                .withChild(ImmutableNodes.leafNode(MANDATORY_LEAF, mandatoryLeafValue))
                .withChild(ImmutableNodes.leafNode(COMMON_LEAF, commonLeafValue)).build();
    }

    private static MapEntryNode createMapEntry(final Object listIdValue, final Object commonLeafValue)
            throws DataValidationFailedException {
        return Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue)))
                .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue))
                .withChild(ImmutableNodes.leafNode(COMMON_LEAF, commonLeafValue)).build();
    }

    private static MapEntryNode createMapEntryM(final Object listIdValue, final Object mandatoryLeafValue)
            throws DataValidationFailedException {
        return Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue)))
                .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue))
                .withChild(ImmutableNodes.leafNode(MANDATORY_LEAF, mandatoryLeafValue)).build();
    }

    @Test
    public void mergeValidContainerTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);

        final MapNode myList = createMap(false);
        final DataContainerNodeBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT)).withChild(myList);

        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.merge(YangInstanceIdentifier.of(ROOT), root.build());
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void mergeValidMapTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        mergeMap(modificationTree, false);

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void mergeValidMapEntryTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = initDataTree(SCHEMA_CONTEXT, true);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        mergeMapEntry(modificationTree, "1", "mandatory-value", "common-value");

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void validMultiStepsMergeTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.merge(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT).node(MY_LIST), createMapBuilder().build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntry("1", "mandatory-value", "common-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void invalidMultiStepsMergeTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.merge(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT).node(MY_LIST), createMapBuilder().build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntry("1", "common-value"));

        try {
            modificationTree.ready();
            inMemoryDataTree.validate(modificationTree);
            final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
            fail("Should fail due to missing mandatory leaf.");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                    "Node (foo?revision=2016-07-28)my-list[{(foo?revision=2016-07-28)list-id=1}] is missing mandatory "
                            + "descendant /(foo?revision=2016-07-28)mandatory-leaf", e.getMessage());
        }
    }

    private static DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> createEmptyMapEntryBuilder(
            final Object listIdValue) throws DataValidationFailedException {
        return Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue)))
                .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue));
    }

    private static CollectionNodeBuilder<MapEntryNode, MapNode> createMapBuilder() {
        return Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(MY_LIST));
    }

    private static DataContainerNodeBuilder<NodeIdentifier, ContainerNode> createContainerBuilder() {
        return Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(ROOT));
    }

    @Test
    public void validMultiStepsWriteAndMergeTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT).node(MY_LIST), createMapBuilder().build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntry("1", "mandatory-value", "common-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void invalidMultiStepsWriteAndMergeTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT).node(MY_LIST), createMapBuilder().build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntry("1", "common-value"));

        try {
            modificationTree.ready();
            inMemoryDataTree.validate(modificationTree);
            final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
            fail("Should fail due to missing mandatory leaf.");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                    "Node (foo?revision=2016-07-28)my-list[{(foo?revision=2016-07-28)list-id=1}] is missing mandatory "
                            + "descendant /(foo?revision=2016-07-28)mandatory-leaf", e.getMessage());
        }
    }

    @Test
    public void validMapEntryMultiCommitMergeTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT).node(MY_LIST), createMapBuilder().build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntryM("1", "mandatory-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        final DataTreeModification modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree2.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntry("1", "common-value"));
        modificationTree2.ready();
        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);
    }

    @Test
    public void invalidMapEntryMultiCommitMergeTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT).node(MY_LIST), createMapBuilder().build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntryM("1", "mandatory-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        final DataTreeModification modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree2.write(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntry("1", "common-value"));
        modificationTree2.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntryM("1", "mandatory-value"));
        modificationTree2.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "2"))),
                createMapEntry("2", "common-value"));
        try {
            modificationTree2.ready();
            inMemoryDataTree.validate(modificationTree2);
            final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
            inMemoryDataTree.commit(prepare2);
            fail("Should fail due to missing mandatory leaf.");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                    "Node (foo?revision=2016-07-28)my-list[{(foo?revision=2016-07-28)list-id=2}] is missing mandatory "
                            + "descendant /(foo?revision=2016-07-28)mandatory-leaf", e.getMessage());
        }
    }

    /*
     * This test consists of two transactions (i.e. data tree modifications) on
     * empty data tree. The first one writes mandatory data and second one
     * writes common data without any mandatory data.
     */
    @Test
    public void validMapEntryMultiCommitMergeTest2() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        final DataTreeModification modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(YangInstanceIdentifier.of(ROOT), createContainerBuilder().build());
        modificationTree.merge(YangInstanceIdentifier.of(ROOT).node(MY_LIST), createMapBuilder().build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createEmptyMapEntryBuilder("1").build());
        modificationTree.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntryM("1", "mandatory-value"));

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        modificationTree2.merge(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, "1"))),
                createMapEntry("1", "common-value"));
        modificationTree2.ready();
        inMemoryDataTree.validate(modificationTree2);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(modificationTree2);
        inMemoryDataTree.commit(prepare2);
    }
}
