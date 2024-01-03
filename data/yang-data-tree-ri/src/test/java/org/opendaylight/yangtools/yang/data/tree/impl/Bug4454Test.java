/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug4454Test {

    private static final QName MASTER_CONTAINER_QNAME = QName
            .create("urn:opendaylight:params:xml:ns:yang:list-constraints-validation-test-model", "2015-02-02",
                    "master-container");
    private static final QName MIN_MAX_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-list");
    private static final QName MIN_MAX_LEAF_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-leaf-list");
    private static final QName MIN_MAX_LIST_QNAME_NO_MINMAX = QName
            .create(MASTER_CONTAINER_QNAME, "min-max-list-no-minmax");
    private static final QName MIN_MAX_KEY_LEAF_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-key-leaf");
    private static final QName MIN_MAX_VALUE_LEAF_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-value-leaf");
    private static final QName PRESENCE_QNAME = QName.create(MASTER_CONTAINER_QNAME, "presence");

    private static final YangInstanceIdentifier MASTER_CONTAINER_PATH =
        YangInstanceIdentifier.of(MASTER_CONTAINER_QNAME);
    private static final YangInstanceIdentifier MIN_MAX_LIST_PATH =
        YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH).node(MIN_MAX_LIST_QNAME).build();
    private static final YangInstanceIdentifier PRESENCE_PATH = YangInstanceIdentifier.of(PRESENCE_QNAME);
    private static final YangInstanceIdentifier PRESENCE_MIN_MAX_LIST_PATH = PRESENCE_PATH.node(MIN_MAX_LIST_QNAME);
    private static final YangInstanceIdentifier MIN_MAX_LIST_NO_MINMAX_PATH =
        YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH).node(MIN_MAX_LIST_QNAME_NO_MINMAX).build();
    private static final YangInstanceIdentifier MIN_MAX_LEAF_LIST_PATH =
        YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH).node(MIN_MAX_LEAF_LIST_QNAME).build();

    private final MapEntryNode fooEntryNodeWithValue = ImmutableNodes.newMapEntryBuilder()
        .withNodeIdentifier(NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "foo"))
        .withChild(ImmutableNodes.leafNode(MIN_MAX_VALUE_LEAF_QNAME, "footest"))
        .build();
    private final MapEntryNode bazEntryNodeWithValue = ImmutableNodes.newMapEntryBuilder()
        .withNodeIdentifier(NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "baz"))
        .withChild(ImmutableNodes.leafNode(MIN_MAX_VALUE_LEAF_QNAME, "baztest"))
        .build();
    private final MapEntryNode fooEntryNode = ImmutableNodes.newMapEntryBuilder()
        .withNodeIdentifier(NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "foo"))
        .withChild(ImmutableNodes.leafNode(MIN_MAX_KEY_LEAF_QNAME, "foo"))
        .build();
    private final MapEntryNode barEntryNode = ImmutableNodes.newMapEntryBuilder()
        .withNodeIdentifier(NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "bar"))
        .withChild(ImmutableNodes.leafNode(MIN_MAX_KEY_LEAF_QNAME, "bar"))
        .build();
    private final MapEntryNode bazEntryNode = ImmutableNodes.newMapEntryBuilder()
        .withNodeIdentifier(NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "baz"))
        .withChild(ImmutableNodes.leafNode(MIN_MAX_KEY_LEAF_QNAME, "baz"))
        .build();
    private final SystemMapNode mapNodeBazFuzWithNodes = ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LIST_QNAME))
            .withChild(bazEntryNode).withChild(bazEntryNodeWithValue).withChild(fooEntryNode)
            .build();
    private final SystemMapNode mapNodeFooWithNodes = ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LIST_QNAME))
            .withChild(fooEntryNode).withChild(fooEntryNodeWithValue).withChild(barEntryNode).withChild(bazEntryNode)
            .build();
    private final SystemMapNode mapNodeBar = ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LIST_QNAME))
            .withChild(barEntryNode).build();
    private final SystemMapNode mapNodeBaz = ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LIST_QNAME))
            .withChild(bazEntryNode).build();

    private static EffectiveModelContext schemaContext;

    private DataTree inMemoryDataTree;

    @BeforeAll
    static void beforeClass() {
        schemaContext = YangParserTestUtils.parseYang("""
            module Bug4454Test {
              yang-version 1;
              namespace "urn:opendaylight:params:xml:ns:yang:list-constraints-validation-test-model";
              prefix "list-constraints-validation";

              revision "2015-02-02" {
                description "Initial revision.";
              }

              container master-container {
                list min-max-list {
                  min-elements 1;
                  max-elements 3;
                  key "min-max-key-leaf";
                  leaf min-max-key-leaf {
                    type string;
                  }
                  leaf min-max-value-leaf {
                    type string;
                  }
                }

                list min-max-list-no-minmax {
                  key "min-max-key-leaf";
                  leaf min-max-key-leaf {
                    type string;
                  }
                }

                leaf-list min-max-leaf-list {
                  min-elements 0;
                  max-elements 10;
                  type string;
                }
              }

              container presence {
                presence "anchor point";

                list min-max-list {
                  min-elements 2;
                  max-elements 3;

                  key "min-max-key-leaf";

                  leaf min-max-key-leaf {
                    type string;
                  }
                }
              }
            }""");
    }

    @AfterAll
    static void afterClass() {
        schemaContext = null;
    }

    @BeforeEach
    void prepare() throws DataValidationFailedException {
        inMemoryDataTree =  new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL,
            schemaContext);
        final var initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();
        final var modificationTree = initialDataTreeSnapshot.newModification();

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(MASTER_CONTAINER_QNAME))
            .build());
        modificationTree.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(modificationTree));
    }

    @Test
    void minMaxListDeleteWriteTest() throws DataValidationFailedException {
        final var modificationTree1 = inMemoryDataTree.takeSnapshot().newModification();

        final var key = new HashMap<QName, Object>();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "foo");

        var mapEntryPath2 = NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME , key);

        final var minMaxLeafFoo = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPath2).build();

        key.clear();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "NON-EXISTING-LEAF");

        mapEntryPath2 = NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME, key);

        final var minMaxLeafNel = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPath2).build();

        final var keyTemp = new HashMap<QName, Object>();
        keyTemp.put(MIN_MAX_KEY_LEAF_QNAME, "baz");

        var mapEntryPathTest = NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME , keyTemp);

        final var pathToBaz = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPathTest).node(MIN_MAX_VALUE_LEAF_QNAME).build();

        keyTemp.clear();
        keyTemp.put(MIN_MAX_KEY_LEAF_QNAME, "bar");

        mapEntryPathTest = NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME , keyTemp);

        final var pathToBar = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPathTest).node(MIN_MAX_VALUE_LEAF_QNAME).build();

        keyTemp.clear();
        keyTemp.put(MIN_MAX_KEY_LEAF_QNAME, "foo");

        final var mapEntryPathTestKey = NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME,
            keyTemp);

        final var pathToKeyFoo = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPathTestKey).node(MIN_MAX_KEY_LEAF_QNAME).build();

        final var newNode = ImmutableNodes.leafNode(MIN_MAX_VALUE_LEAF_QNAME, "test");
        final var newNode1 = ImmutableNodes.leafNode(MIN_MAX_VALUE_LEAF_QNAME, "test1");
        final var newNode2 = ImmutableNodes.leafNode(MIN_MAX_VALUE_LEAF_QNAME, "test2");
        final var newNodekey = ImmutableNodes.leafNode(MIN_MAX_KEY_LEAF_QNAME, "foo");

        assertFalse(inMemoryDataTree.toString().contains("list"));

        final var snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        var minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_PATH);
        assertFalse(minMaxListRead.isPresent());

        modificationTree1.write(MIN_MAX_LIST_PATH, mapNodeFooWithNodes);
        modificationTree1.write(MIN_MAX_LIST_PATH, mapNodeFooWithNodes);
        modificationTree1.write(MIN_MAX_LIST_PATH, mapNodeFooWithNodes);
        modificationTree1.merge(MIN_MAX_LIST_PATH, mapNodeBar);
        modificationTree1.merge(MIN_MAX_LIST_PATH, mapNodeBaz);
        modificationTree1.write(pathToKeyFoo, newNodekey);
        modificationTree1.write(pathToBaz, newNode2);
        modificationTree1.write(pathToBaz, newNode1);
        modificationTree1.write(pathToBaz, newNode);
        modificationTree1.delete(minMaxLeafFoo);
        modificationTree1.delete(minMaxLeafNel);

        modificationTree1.ready();
        inMemoryDataTree.validate(modificationTree1);
        final var prepare = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare);

        final var test = inMemoryDataTree.takeSnapshot();
        testLoop(test, "bar", "test");

        final var tempMod = test.newModification();
        tempMod.write(pathToBaz, newNode2);
        tempMod.write(pathToBaz, newNode1);
        tempMod.merge(pathToBaz, newNode2);
        tempMod.write(pathToBaz, newNode1);

        tempMod.ready();
        inMemoryDataTree.validate(tempMod);
        final var prepare1 = inMemoryDataTree.prepare(tempMod);
        inMemoryDataTree.commit(prepare1);

        final var test1 = inMemoryDataTree.takeSnapshot();
        testLoop(test1, "bar", "test1");

        final var tempMod1 = test1.newModification();
        tempMod1.write(MIN_MAX_LIST_PATH, mapNodeFooWithNodes);

        tempMod1.ready();
        inMemoryDataTree.validate(tempMod1);
        final var prepare2 = inMemoryDataTree.prepare(tempMod1);
        inMemoryDataTree.commit(prepare2);

        final var test2 = inMemoryDataTree.takeSnapshot();
        minMaxListRead = test2.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertEquals(3, ((NormalizedNodeContainer<?>) minMaxListRead.orElseThrow()).size());

        final var tempMod2 = test2.newModification();
        tempMod2.write(MIN_MAX_LIST_PATH, mapNodeBaz);
        tempMod2.write(pathToBaz, newNode2);

        tempMod2.ready();
        inMemoryDataTree.validate(tempMod2);
        final var prepare3 = inMemoryDataTree.prepare(tempMod2);
        inMemoryDataTree.commit(prepare3);

        final var test3 = inMemoryDataTree.takeSnapshot();
        minMaxListRead = test3.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertEquals(1, ((NormalizedNodeContainer<?>) minMaxListRead.orElseThrow()).size());
        assertTrue(minMaxListRead.orElseThrow().body().toString().contains("test2"));

        final var tempMod3 = test3.newModification();
        tempMod3.merge(MIN_MAX_LIST_PATH, mapNodeBar);
        tempMod3.merge(pathToBar, newNode1);

        tempMod3.ready();
        inMemoryDataTree.validate(tempMod3);
        final var prepare4 = inMemoryDataTree.prepare(tempMod3);
        inMemoryDataTree.commit(prepare4);

        final var test4 = inMemoryDataTree.takeSnapshot();
        testLoop(test4, "test1", "test2");
    }

    @Test
    void minMaxLeafListPass() throws DataValidationFailedException {
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        final var barPath = new NodeWithValue<>(MIN_MAX_LIST_QNAME, "bar");
        final var gooPath = new NodeWithValue<>(MIN_MAX_LIST_QNAME, "goo");

        final var barLeafSetEntry = ImmutableNodes.leafSetEntry(barPath);
        final var gooLeafSetEntry = ImmutableNodes.leafSetEntry(gooPath);

        final var fooLeafSetNode = ImmutableNodes.newSystemLeafSetBuilder()
                .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LEAF_LIST_QNAME))
                .withChildValue("foo")
                .build();

        modificationTree.write(MIN_MAX_LEAF_LIST_PATH, fooLeafSetNode);
        modificationTree.write(MIN_MAX_LEAF_LIST_PATH.node(barPath), barLeafSetEntry);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final var prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        final var test1 = inMemoryDataTree.takeSnapshot();

        final var tempMod1 = test1.newModification();
        tempMod1.write(MIN_MAX_LEAF_LIST_PATH.node(gooPath), gooLeafSetEntry);
        tempMod1.write(MIN_MAX_LEAF_LIST_PATH.node(barPath), barLeafSetEntry);
        tempMod1.ready();

        inMemoryDataTree.validate(tempMod1);
        final var prepare2 = inMemoryDataTree.prepare(tempMod1);
        inMemoryDataTree.commit(prepare2);

        final var snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        final var masterContainer = snapshotAfterCommit.readNode(MASTER_CONTAINER_PATH);
        assertTrue(masterContainer.isPresent());
        final var leafList = ((DistinctNodeContainer) masterContainer.orElseThrow())
                .findChildByArg(new NodeIdentifier(MIN_MAX_LEAF_LIST_QNAME));
        assertTrue(leafList.isPresent());
        assertEquals(3, ((Optional<NormalizedNodeContainer<?>>) leafList).orElseThrow().size());
    }

    @Test
    void minMaxListDeleteTest() throws DataValidationFailedException {
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();


        var mapEntryPath2 = NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME,
            MIN_MAX_KEY_LEAF_QNAME, "foo");

        final var minMaxLeafFoo = MASTER_CONTAINER_PATH
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPath2);

        mapEntryPath2 = NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "bar");

        final var minMaxLeafBar = MASTER_CONTAINER_PATH
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPath2);

        mapEntryPath2 = NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "baz");

        final var minMaxLeafBaz = MASTER_CONTAINER_PATH
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPath2);

        modificationTree.write(MIN_MAX_LIST_PATH, mapNodeFooWithNodes);
        modificationTree.merge(MIN_MAX_LIST_PATH, mapNodeBar);
        modificationTree.merge(MIN_MAX_LIST_PATH, mapNodeBaz);
        modificationTree.delete(minMaxLeafFoo);
        modificationTree.delete(minMaxLeafBar);
        modificationTree.delete(minMaxLeafBaz);

        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        // Empty list should have disappeared, along with the container, as we are not enforcing root
        final var data = inMemoryDataTree.takeSnapshot().readNode(YangInstanceIdentifier.of()).orElseThrow();
        assertInstanceOf(ContainerNode.class, data);
        assertEquals(0, ((ContainerNode) data).size());
    }

    @Test
    void minMaxListDeleteExceptionTest() throws DataValidationFailedException {
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        var mapEntryPath2 = NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME,
            MIN_MAX_KEY_LEAF_QNAME, "foo");

        final var minMaxLeafFoo = PRESENCE_PATH.node(MIN_MAX_LIST_QNAME).node(mapEntryPath2);

        mapEntryPath2 = NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "bar");

        final var minMaxLeafBar = PRESENCE_PATH.node(MIN_MAX_LIST_QNAME).node(mapEntryPath2);

        mapEntryPath2 = NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "baz");

        final var minMaxLeafBaz = PRESENCE_PATH.node(MIN_MAX_LIST_QNAME).node(mapEntryPath2);

        modificationTree.write(PRESENCE_PATH,
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(PRESENCE_QNAME)).build());
        modificationTree.write(PRESENCE_MIN_MAX_LIST_PATH, mapNodeFooWithNodes);
        modificationTree.merge(PRESENCE_MIN_MAX_LIST_PATH, mapNodeBar);
        modificationTree.merge(PRESENCE_MIN_MAX_LIST_PATH, mapNodeBaz);
        modificationTree.delete(minMaxLeafFoo);
        modificationTree.delete(minMaxLeafBar);
        modificationTree.delete(minMaxLeafBaz);

        try {
            // Unlike minMaxListDeleteTest(), presence container enforces the list to be present
            modificationTree.ready();
            fail("Should have failed with IAE");
        } catch (IllegalArgumentException e) {
            assertEquals("""
                Node (urn:opendaylight:params:xml:ns:yang:list-constraints-validation-test-model?revision=2015-02-02)\
                presence is missing mandatory descendant /(urn:opendaylight:params:xml:ns:yang:list-constraints-\
                validation-test-model?revision=2015-02-02)min-max-list""", e.getMessage());
        }
    }

    @Test
    void minMaxListNoMinMaxDeleteTest() throws DataValidationFailedException {
        final var fooEntryNoMinMaxNode = ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(
                NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME_NO_MINMAX, MIN_MAX_KEY_LEAF_QNAME, "foo"))
            .withChild(ImmutableNodes.leafNode(MIN_MAX_KEY_LEAF_QNAME, "foo"))
            .build();
        final var mapNode1 = ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LIST_QNAME_NO_MINMAX))
                .withChild(fooEntryNoMinMaxNode).build();

        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        final var key = new HashMap<QName, Object>();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "foo");

        var mapEntryPath2 = NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME_NO_MINMAX, key);

        final var minMaxLeafFoo = MASTER_CONTAINER_PATH
                .node(MIN_MAX_LIST_QNAME_NO_MINMAX).node(mapEntryPath2);

        key.clear();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "non-existing-leaf");

        mapEntryPath2 = NodeIdentifierWithPredicates.of(MIN_MAX_LIST_QNAME_NO_MINMAX, key);

        final var minMaxLeafNel = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME_NO_MINMAX).node(mapEntryPath2).build();

        modificationTree.write(MIN_MAX_LIST_NO_MINMAX_PATH, mapNode1);
        modificationTree.delete(minMaxLeafFoo);
        modificationTree.delete(minMaxLeafNel);

        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        final var snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        final var minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_NO_MINMAX_PATH);

        // Empty list should have disappeared
        assertFalse(minMaxListRead.isPresent());
    }

    private static void testLoop(final DataTreeSnapshot snapshot, final String first, final String second) {
        final var minMaxListRead = snapshot.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertEquals(2, ((NormalizedNodeContainer<?>) minMaxListRead.orElseThrow()).size());

        for (var collectionChild : (Collection<?>) minMaxListRead.orElseThrow().body()) {
            if (collectionChild.toString().contains(first)) {
                assertTrue(collectionChild.toString().contains(first));
            } else {
                assertTrue(collectionChild.toString().contains(second));
            }
        }
    }
}
