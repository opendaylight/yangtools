/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.SchemaValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;

// TODO: expand these tests to catch some more obscure cases
class ConfigStatementValidationTest extends AbstractTestModelTest {
    private static final Short ONE_ID = 1;
    private static final Short TWO_ID = 2;

    private static final YangInstanceIdentifier OUTER_LIST_1_PATH = YangInstanceIdentifier
            .builder(TestModel.OUTER_LIST_PATH).nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, ONE_ID)
            .build();

    private static final YangInstanceIdentifier OUTER_LIST_2_PATH = YangInstanceIdentifier
            .builder(TestModel.OUTER_LIST_PATH).nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, TWO_ID)
            .build();

    private static final MapEntryNode INNER_FOO_ENTRY_NODE = mapEntry(TestModel.INNER_LIST_QNAME,
            TestModel.NAME_QNAME, "foo");

    private static final MapEntryNode INNER_BAR_ENTRY_NODE =
            mapEntryBuilder(QName.create(TestModel.TEST_QNAME, "inner-list2"), TestModel.NAME_QNAME, "foo")
                .withChild(leafNode(TestModel.VALUE_QNAME, "value")).build();

    private static final MapEntryNode FOO_NODE = mapEntryBuilder(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, ONE_ID)
            .withChild(mapNodeBuilder(TestModel.INNER_LIST_QNAME).withChild(INNER_FOO_ENTRY_NODE)
                    .build())
            .build();

    private static final MapEntryNode BAR_NODE = mapEntryBuilder(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, TWO_ID)
            .withChild(mapNodeBuilder(TestModel.INNER_LIST_QNAME).withChild(INNER_BAR_ENTRY_NODE)
                    .build())
            .build();

    private static ContainerNode createFooTestContainerNode() {
        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(mapNodeBuilder(TestModel.OUTER_LIST_QNAME).withChild(FOO_NODE).build()).build();
    }

    private static ContainerNode createBarTestContainerNode() {
        return Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(mapNodeBuilder(TestModel.OUTER_LIST_QNAME).withChild(BAR_NODE).build()).build();
    }

    @Test
    void testOnPathFail() {
        assertThrows(SchemaValidationFailedException.class, () -> {
            final var inMemoryDataTree = new InMemoryDataTreeFactory().create(
                DataTreeConfiguration.DEFAULT_CONFIGURATION, SCHEMA_CONTEXT);
            final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
            final var ii = OUTER_LIST_1_PATH.node(new NodeIdentifier(TestModel.INNER_LIST_QNAME))
                .node(INNER_FOO_ENTRY_NODE.name());
            modificationTree.write(ii, INNER_FOO_ENTRY_NODE);

            inMemoryDataTree.validate(modificationTree);
            final var prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
        });
    }

    @Test
    void testOnDataFail() {
        assertThrows(SchemaValidationFailedException.class, () -> {
            final var inMemoryDataTree = new InMemoryDataTreeFactory().create(
                DataTreeConfiguration.DEFAULT_CONFIGURATION, SCHEMA_CONTEXT);
            final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
            modificationTree.write(TestModel.TEST_PATH, createFooTestContainerNode());
            modificationTree.ready();
            inMemoryDataTree.validate(modificationTree);
            final var prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
        });
    }

    @Test
    void testOnDataLeafFail() {
        assertThrows(SchemaValidationFailedException.class, () -> {
            final var inMemoryDataTree = new InMemoryDataTreeFactory().create(
                DataTreeConfiguration.DEFAULT_CONFIGURATION, SCHEMA_CONTEXT);
            final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
            modificationTree.write(TestModel.TEST_PATH, createBarTestContainerNode());
            modificationTree.ready();
            inMemoryDataTree.validate(modificationTree);
            final var prepare = inMemoryDataTree.prepare(modificationTree);
            inMemoryDataTree.commit(prepare);
        });
    }

    @Test
    void testOnPathCaseLeafFail() {
        assertThrows(SchemaValidationFailedException.class, () -> {
            final var inMemoryDataTree = new InMemoryDataTreeFactory().create(
                DataTreeConfiguration.DEFAULT_CONFIGURATION, SCHEMA_CONTEXT);
            final var choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));
            final var case2ContId = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "case2-cont"));
            final var ii = TestModel.TEST_PATH.node(choice1Id).node(case2ContId);
            final var case2Cont = Builders.containerBuilder().withNodeIdentifier(case2ContId)
                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case2-leaf1"), "leaf-value")).build();

            final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
            modificationTree.write(ii, case2Cont);
            modificationTree.ready();
        });
    }

    @Test
    void testOnDataCaseLeafFail() {
        assertThrows(SchemaValidationFailedException.class, () -> {
            final var inMemoryDataTree = new InMemoryDataTreeFactory().create(
                DataTreeConfiguration.DEFAULT_CONFIGURATION, SCHEMA_CONTEXT);
            final var choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));
            final var ii = TestModel.TEST_PATH.node(choice1Id);
            final var choice1 = Builders.choiceBuilder().withNodeIdentifier(choice1Id)
                .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case1-leaf1"), "leaf-value")).build();

            final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
            modificationTree.write(ii, choice1);

            modificationTree.ready();
        });
    }
}
