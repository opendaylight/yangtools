/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

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
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug5968Test {
    private static final String NS = "bug5968";
    private static final String REV = "2016-07-28";
    private static final QName ROOT = QName.create(NS, REV, "root");
    private static final QName MY_LIST = QName.create(NS, REV, "my-list");
    private static final QName LIST_ID = QName.create(NS, REV, "list-id");
    private static final QName MANDATORY_LEAF = QName.create(NS, REV, "mandatory-leaf");
    private static final QName COMMON_LEAF = QName.create(NS, REV, "common-leaf");
    private static final String BUG_5968_YANG = """
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
            }""";

    private static EffectiveModelContext SCHEMA_CONTEXT;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYang(BUG_5968_YANG);
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    private static DataTree initDataTree(final EffectiveModelContext schemaContext, final boolean withMapNode)
            throws DataValidationFailedException {
        final DataTree inMemoryDataTree = new InMemoryDataTreeFactory().create(
                DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);

        final DataContainerNodeBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
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

    private static DataTree emptyDataTree(final EffectiveModelContext schemaContext)
            throws DataValidationFailedException {
        return new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);
    }

    @Test
    public void writeInvalidContainerTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);

        final SystemMapNode myList = createMap(true);
        final DataContainerNodeBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
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
            assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=1}] is "
                + "missing mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", e.getMessage());
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
            assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=1}] is "
                + "missing mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", e.getMessage());
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
            assertEquals("Node (bug5968?revision=2016-07-28)my-list[{(bug5968?revision=2016-07-28)list-id=1}] is "
                + "missing mandatory descendant /(bug5968?revision=2016-07-28)mandatory-leaf", e.getMessage());
        }
    }

    private static void writeMap(final DataTreeModification modificationTree, final boolean mandatoryDataMissing) {
        modificationTree.write(YangInstanceIdentifier.of(ROOT).node(MY_LIST), createMap(mandatoryDataMissing));
    }

    private static SystemMapNode createMap(final boolean mandatoryDataMissing) {
        return Builders.mapBuilder()
            .withNodeIdentifier(new NodeIdentifier(MY_LIST))
            .withChild(mandatoryDataMissing ? createMapEntry("1", "common-value")
                : createMapEntry("1", "mandatory-value", "common-value"))
            .build();
    }

    private static void writeMapEntry(final DataTreeModification modificationTree, final Object listIdValue,
            final Object mandatoryLeafValue, final Object commonLeafValue) throws DataValidationFailedException {
        final MapEntryNode taskEntryNode = mandatoryLeafValue == null ? createMapEntry(listIdValue, commonLeafValue)
                : createMapEntry(listIdValue, mandatoryLeafValue, commonLeafValue);

        modificationTree.write(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue))),
                taskEntryNode);
    }

    private static MapEntryNode createMapEntry(final Object listIdValue, final Object mandatoryLeafValue,
            final Object commonLeafValue) {
        return Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue)))
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

    @Test
    public void writeValidContainerTest() throws DataValidationFailedException {
        final DataTree inMemoryDataTree = emptyDataTree(SCHEMA_CONTEXT);

        final SystemMapNode myList = createMap(false);
        final DataContainerNodeBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
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
