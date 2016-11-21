/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug5968MergeTest {
    private static final String NS = "foo";
    private static final String REV = "2016-07-28";
    private static final QName ROOT = QName.create(NS, REV, "root");
    private static final QName MY_LIST = QName.create(NS, REV, "my-list");
    private static final QName LIST_ID = QName.create(NS, REV, "list-id");
    private static final QName MANDATORY_LEAF = QName.create(NS, REV, "mandatory-leaf");
    private static final QName COMMON_LEAF = QName.create(NS, REV, "common-leaf");
    private SchemaContext schemaContext;

    @Before
    public void init() throws ReactorException {
        this.schemaContext = TestModel.createTestContext("/bug5968/foo.yang");
        assertNotNull("Schema context must not be null.", this.schemaContext);
    }

    private static InMemoryDataTree initDataTree(final SchemaContext schemaContext, final boolean withMapNode)
            throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(
                DataTreeConfiguration.DEFAULT_CONFIGURATION);
        inMemoryDataTree.setSchemaContext(schemaContext);

        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT));
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
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

    private static InMemoryDataTree emptyDataTree(final SchemaContext schemaContext)
            throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(
                DataTreeConfiguration.DEFAULT_CONFIGURATION);
        inMemoryDataTree.setSchemaContext(schemaContext);

        return inMemoryDataTree;
    }

    @Test
    public void mergeInvalidContainerTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);

        final MapNode myList = createMap(true);
        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT)).withChild(myList);

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
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
    public void mergeInvalidMapTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
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
    public void mergeInvalidMapEntryTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(schemaContext, true);
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

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

    private static void mergeMap(final InMemoryDataTreeModification modificationTree, final boolean mandatoryDataMissing)
            throws DataValidationFailedException {
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

    private static void mergeMapEntry(final InMemoryDataTreeModification modificationTree, final Object listIdValue,
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
    public void mergeValidContainerTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);

        final MapNode myList = createMap(false);
        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT)).withChild(myList);

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.merge(YangInstanceIdentifier.of(ROOT), root.build());
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void mergeValidMapTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        mergeMap(modificationTree, false);

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void mergeValidMapEntryTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(schemaContext, true);
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        mergeMapEntry(modificationTree, "1", "mandatory-value", "common-value");

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void validMultiStepsMergeTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

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
    public void invalidMultiStepsMergeTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

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

    private static DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> createEmptyMapEntryBuilder(
            final Object listIdValue) throws DataValidationFailedException {
        return Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue)))
                .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue));
    }

    private static CollectionNodeBuilder<MapEntryNode, MapNode> createMapBuilder() throws DataValidationFailedException {
        return Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(MY_LIST));
    }

    private static DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> createContainerBuilder()
            throws DataValidationFailedException {
        return Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(ROOT));
    }

    @Test
    public void validMultiStepsWriteAndMergeTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

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
    public void invalidMultiStepsWriteAndMergeTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

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
    public void validMapEntryMultiCommitMergeTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

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

        final InMemoryDataTreeModification modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
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
    public void invalidMapEntryMultiCommitMergeTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

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

        final InMemoryDataTreeModification modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();
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

    @Test
    /*
     * This test consists of two transactions (i.e. data tree modifications) on
     * empty data tree. The first one writes mandatory data and second one
     * writes common data without any mandatory data.
     */
    public void validMapEntryMultiCommitMergeTest2() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        final InMemoryDataTreeModification modificationTree2 = inMemoryDataTree.takeSnapshot().newModification();

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
