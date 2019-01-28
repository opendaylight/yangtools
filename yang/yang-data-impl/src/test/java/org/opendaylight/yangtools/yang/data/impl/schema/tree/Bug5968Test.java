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
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug5968Test {
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

        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT));
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
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
    public void writeInvalidContainerTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);

        final MapNode myList = createMap(true);
        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT)).withChild(myList);

        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(ROOT), root.build());

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
    public void writeInvalidMapTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        writeMap(modificationTree, true);

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
    public void writeInvalidMapEntryTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = initDataTree(SCHEMA_CONTEXT, true);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        writeMapEntry(modificationTree, "1", null, "common-value");

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

    private static void writeMap(final DataTreeModification modificationTree, final boolean mandatoryDataMissing) {
        final MapNode myList = createMap(mandatoryDataMissing);
        modificationTree.write(YangInstanceIdentifier.of(ROOT).node(MY_LIST), myList);
    }

    private static MapNode createMap(final boolean mandatoryDataMissing) {
        return Builders
                .mapBuilder()
                .withNodeIdentifier(new NodeIdentifier(MY_LIST))
                .withChild(
                        mandatoryDataMissing ? createMapEntry("1", "common-value") : createMapEntry("1",
                                "mandatory-value", "common-value")).build();
    }

    private static void writeMapEntry(final DataTreeModification modificationTree, final Object listIdValue,
            final Object mandatoryLeafValue, final Object commonLeafValue) throws DataValidationFailedException {
        final MapEntryNode taskEntryNode = mandatoryLeafValue == null ? createMapEntry(listIdValue, commonLeafValue)
                : createMapEntry(listIdValue, mandatoryLeafValue, commonLeafValue);

        modificationTree.write(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue))),
                taskEntryNode);
    }

    private static MapEntryNode createMapEntry(final Object listIdValue, final Object mandatoryLeafValue,
            final Object commonLeafValue) {
        return Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue)))
                .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue))
                .withChild(ImmutableNodes.leafNode(MANDATORY_LEAF, mandatoryLeafValue))
                .withChild(ImmutableNodes.leafNode(COMMON_LEAF, commonLeafValue)).build();
    }

    private static MapEntryNode createMapEntry(final Object listIdValue, final Object commonLeafValue) {
        return Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue)))
                .withChild(ImmutableNodes.leafNode(LIST_ID, listIdValue))
                .withChild(ImmutableNodes.leafNode(COMMON_LEAF, commonLeafValue)).build();
    }

    @Test
    public void writeValidContainerTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);

        final MapNode myList = createMap(false);
        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT)).withChild(myList);

        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(ROOT), root.build());
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void writeValidMapTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        writeMap(modificationTree, false);

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void writeValidMapEntryTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = initDataTree(SCHEMA_CONTEXT, true);
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        writeMapEntry(modificationTree, "1", "mandatory-value", "common-value");

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }
}
