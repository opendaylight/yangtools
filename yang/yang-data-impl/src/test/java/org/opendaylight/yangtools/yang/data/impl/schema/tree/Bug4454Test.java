/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug4454Test {

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

    private static final YangInstanceIdentifier MASTER_CONTAINER_PATH = YangInstanceIdentifier
            .of(MASTER_CONTAINER_QNAME);
    private static final YangInstanceIdentifier MIN_MAX_LIST_PATH = YangInstanceIdentifier
            .builder(MASTER_CONTAINER_PATH)
            .node(MIN_MAX_LIST_QNAME).build();
    private static final YangInstanceIdentifier PRESENCE_PATH = YangInstanceIdentifier.of(PRESENCE_QNAME);
    private static final YangInstanceIdentifier PRESENCE_MIN_MAX_LIST_PATH = PRESENCE_PATH.node(MIN_MAX_LIST_QNAME);
    private static final YangInstanceIdentifier MIN_MAX_LIST_NO_MINMAX_PATH = YangInstanceIdentifier
            .builder(MASTER_CONTAINER_PATH)
            .node(MIN_MAX_LIST_QNAME_NO_MINMAX).build();
    private static final YangInstanceIdentifier MIN_MAX_LEAF_LIST_PATH = YangInstanceIdentifier
            .builder(MASTER_CONTAINER_PATH).node(MIN_MAX_LEAF_LIST_QNAME).build();

    private static final Map<QName, Object> FOO_PREDICATES = ImmutableMap.of(MIN_MAX_KEY_LEAF_QNAME, "foo");
    private static final Map<QName, Object> BAZ_PREDICATES = ImmutableMap.of(MIN_MAX_KEY_LEAF_QNAME, "baz");

    private final MapEntryNode fooEntryNodeWithValue = ImmutableMapEntryNodeBuilder.create().withNodeIdentifier(
        new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME, FOO_PREDICATES))
            .withChild(ImmutableNodes.leafNode(MIN_MAX_VALUE_LEAF_QNAME, "footest")).build();
    private final MapEntryNode bazEntryNodeWithValue = ImmutableMapEntryNodeBuilder.create().withNodeIdentifier(
        new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME, BAZ_PREDICATES))
            .withChild(ImmutableNodes.leafNode(MIN_MAX_VALUE_LEAF_QNAME, "baztest")).build();
    private final MapEntryNode fooEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME,
            "foo");
    private final MapEntryNode barEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME,
            "bar");
    private final MapEntryNode bazEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME,
            "baz");
    private final MapNode mapNodeBazFuzWithNodes = ImmutableNodes.mapNodeBuilder()
            .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LIST_QNAME))
            .withChild(bazEntryNode).withChild(bazEntryNodeWithValue).withChild(fooEntryNode)
            .build();
    private final MapNode mapNodeFooWithNodes = ImmutableNodes.mapNodeBuilder()
            .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LIST_QNAME))
            .withChild(fooEntryNode).withChild(fooEntryNodeWithValue).withChild(barEntryNode).withChild(bazEntryNode)
            .build();
    private final MapNode mapNodeBar = ImmutableNodes.mapNodeBuilder()
            .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LIST_QNAME))
            .withChild(barEntryNode).build();
    private final MapNode mapNodeBaz = ImmutableNodes.mapNodeBuilder()
            .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LIST_QNAME))
            .withChild(bazEntryNode).build();

    private static SchemaContext schemaContext;

    private DataTree inMemoryDataTree;

    @BeforeClass
    public static void beforeClass() {
        schemaContext = YangParserTestUtils.parseYangResource("/bug-4454-test.yang");
    }

    @AfterClass
    public static void afterClass() {
        schemaContext = null;
    }

    @Before
    public void prepare() {
        inMemoryDataTree =  new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL,
            schemaContext);
        final DataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();
        final DataTreeModification modificationTree = initialDataTreeSnapshot.newModification();

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(modificationTree));
    }

    @Test
    public void minMaxListDeleteWriteTest() throws DataValidationFailedException {
        final DataTreeModification modificationTree1 = inMemoryDataTree.takeSnapshot().newModification();

        Map<QName, Object> key = new HashMap<>();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "foo");

        NodeIdentifierWithPredicates mapEntryPath2 = new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME , key);

        final YangInstanceIdentifier minMaxLeafFoo = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPath2).build();

        key.clear();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "NON-EXISTING-LEAF");

        mapEntryPath2 = new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME, key);

        final YangInstanceIdentifier minMaxLeafNel = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPath2).build();

        final Map<QName, Object> keyTemp = new HashMap<>();
        keyTemp.put(MIN_MAX_KEY_LEAF_QNAME, "baz");

        NodeIdentifierWithPredicates mapEntryPathTest = new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME , keyTemp);

        final YangInstanceIdentifier pathToBaz = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPathTest).node(MIN_MAX_VALUE_LEAF_QNAME).build();

        keyTemp.clear();
        keyTemp.put(MIN_MAX_KEY_LEAF_QNAME, "bar");

        mapEntryPathTest = new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME , keyTemp);

        final YangInstanceIdentifier pathToBar = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPathTest).node(MIN_MAX_VALUE_LEAF_QNAME).build();

        keyTemp.clear();
        keyTemp.put(MIN_MAX_KEY_LEAF_QNAME, "foo");

        final NodeIdentifierWithPredicates mapEntryPathTestKey = new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME,
            keyTemp);

        final YangInstanceIdentifier pathToKeyFoo = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPathTestKey).node(MIN_MAX_KEY_LEAF_QNAME).build();

        final LeafNode<String> newNode = ImmutableNodes.leafNode(MIN_MAX_VALUE_LEAF_QNAME, "test");
        final LeafNode<String> newNode1 = ImmutableNodes.leafNode(MIN_MAX_VALUE_LEAF_QNAME, "test1");
        final LeafNode<String> newNode2 = ImmutableNodes.leafNode(MIN_MAX_VALUE_LEAF_QNAME, "test2");
        final LeafNode<String> newNodekey = ImmutableNodes.leafNode(MIN_MAX_KEY_LEAF_QNAME, "foo");

        assertFalse(inMemoryDataTree.toString().contains("list"));

        DataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode<?, ?>> minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_PATH);
        assertTrue(!minMaxListRead.isPresent());

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
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree1);
        inMemoryDataTree.commit(prepare);

        DataTreeSnapshot test = inMemoryDataTree.takeSnapshot();
        testLoop(test, "bar", "test");

        DataTreeModification tempMod = test.newModification();
        tempMod.write(pathToBaz, newNode2);
        tempMod.write(pathToBaz, newNode1);
        tempMod.merge(pathToBaz, newNode2);
        tempMod.write(pathToBaz, newNode1);

        tempMod.ready();
        inMemoryDataTree.validate(tempMod);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(tempMod);
        inMemoryDataTree.commit(prepare1);

        DataTreeSnapshot test1 = inMemoryDataTree.takeSnapshot();
        testLoop(test1, "bar", "test1");

        DataTreeModification tempMod1 = test1.newModification();
        tempMod1.write(MIN_MAX_LIST_PATH, mapNodeFooWithNodes);

        tempMod1.ready();
        inMemoryDataTree.validate(tempMod1);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(tempMod1);
        inMemoryDataTree.commit(prepare2);

        DataTreeSnapshot test2 = inMemoryDataTree.takeSnapshot();
        minMaxListRead = test2.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertTrue(((NormalizedNodeContainer<?, ?, ?>) minMaxListRead.get()).getValue().size() == 3);

        DataTreeModification tempMod2 = test2.newModification();
        tempMod2.write(MIN_MAX_LIST_PATH, mapNodeBaz);
        tempMod2.write(pathToBaz, newNode2);

        tempMod2.ready();
        inMemoryDataTree.validate(tempMod2);
        final DataTreeCandidate prepare3 = inMemoryDataTree.prepare(tempMod2);
        inMemoryDataTree.commit(prepare3);

        DataTreeSnapshot test3 = inMemoryDataTree.takeSnapshot();
        minMaxListRead = test3.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertTrue(((NormalizedNodeContainer<?, ?, ?>) minMaxListRead.get()).getValue().size() == 1);
        assertTrue(minMaxListRead.get().getValue().toString().contains("test2"));

        DataTreeModification tempMod3 = test3.newModification();
        tempMod3.merge(MIN_MAX_LIST_PATH, mapNodeBar);
        tempMod3.merge(pathToBar, newNode1);

        tempMod3.ready();
        inMemoryDataTree.validate(tempMod3);
        final DataTreeCandidate prepare4 = inMemoryDataTree.prepare(tempMod3);
        inMemoryDataTree.commit(prepare4);

        DataTreeSnapshot test4 = inMemoryDataTree.takeSnapshot();
        testLoop(test4, "test1", "test2");
    }

    @Test
    public void minMaxLeafListPass() throws DataValidationFailedException {
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        final NodeWithValue<?> barPath = new NodeWithValue<>(MIN_MAX_LIST_QNAME, "bar");
        final NodeWithValue<?> gooPath = new NodeWithValue<>(MIN_MAX_LIST_QNAME, "goo");

        final LeafSetEntryNode<Object> barLeafSetEntry = ImmutableLeafSetEntryNodeBuilder.create()
                .withNodeIdentifier(barPath)
                .withValue("bar").build();
        final LeafSetEntryNode<Object> gooLeafSetEntry = ImmutableLeafSetEntryNodeBuilder.create()
                .withNodeIdentifier(gooPath)
                .withValue("goo").build();

        final LeafSetNode<Object> fooLeafSetNode = ImmutableLeafSetNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LEAF_LIST_QNAME))
                .withChildValue("foo").build();

        modificationTree.write(MIN_MAX_LEAF_LIST_PATH, fooLeafSetNode);
        modificationTree.write(MIN_MAX_LEAF_LIST_PATH.node(barPath), barLeafSetEntry);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        DataTreeSnapshot test1 = inMemoryDataTree.takeSnapshot();

        DataTreeModification tempMod1 = test1.newModification();
        tempMod1.write(MIN_MAX_LEAF_LIST_PATH.node(gooPath), gooLeafSetEntry);
        tempMod1.write(MIN_MAX_LEAF_LIST_PATH.node(barPath), barLeafSetEntry);
        tempMod1.ready();

        inMemoryDataTree.validate(tempMod1);
        final DataTreeCandidate prepare2 = inMemoryDataTree.prepare(tempMod1);
        inMemoryDataTree.commit(prepare2);

        final DataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        final Optional<NormalizedNode<?, ?>> masterContainer = snapshotAfterCommit.readNode(MASTER_CONTAINER_PATH);
        assertTrue(masterContainer.isPresent());
        final Optional<NormalizedNodeContainer<?, ?, ?>> leafList = ((NormalizedNodeContainer) masterContainer.get())
                .getChild(new NodeIdentifier(MIN_MAX_LEAF_LIST_QNAME));
        assertTrue(leafList.isPresent());
        assertTrue(leafList.get().getValue().size() == 3);
    }

    @Test
    public void minMaxListDeleteTest() throws DataValidationFailedException {
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();


        NodeIdentifierWithPredicates mapEntryPath2 = new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME,
            ImmutableMap.of(MIN_MAX_KEY_LEAF_QNAME, "foo"));

        final YangInstanceIdentifier minMaxLeafFoo = MASTER_CONTAINER_PATH
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPath2);

        mapEntryPath2 = new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME,
            ImmutableMap.of(MIN_MAX_KEY_LEAF_QNAME, "bar"));

        final YangInstanceIdentifier minMaxLeafBar = MASTER_CONTAINER_PATH
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPath2);

        mapEntryPath2 = new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME,
            ImmutableMap.of(MIN_MAX_KEY_LEAF_QNAME, "baz"));

        final YangInstanceIdentifier minMaxLeafBaz = MASTER_CONTAINER_PATH
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPath2);

        modificationTree.write(MIN_MAX_LIST_PATH, mapNodeFooWithNodes);
        modificationTree.merge(MIN_MAX_LIST_PATH, mapNodeBar);
        modificationTree.merge(MIN_MAX_LIST_PATH, mapNodeBaz);
        modificationTree.delete(minMaxLeafFoo);
        modificationTree.delete(minMaxLeafBar);
        modificationTree.delete(minMaxLeafBaz);

        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        // Empty list should have disappeared, along with the container, as we are not enforcing root
        final NormalizedNode<?, ?> data = inMemoryDataTree.takeSnapshot().readNode(YangInstanceIdentifier.EMPTY).get();
        assertTrue(data instanceof ContainerNode);
        assertEquals(0, ((ContainerNode) data).getValue().size());
    }

    @Test
    public void minMaxListDeleteExceptionTest() throws DataValidationFailedException {
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        NodeIdentifierWithPredicates mapEntryPath2 = new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME,
            ImmutableMap.of(MIN_MAX_KEY_LEAF_QNAME, "foo"));

        final YangInstanceIdentifier minMaxLeafFoo = PRESENCE_PATH.node(MIN_MAX_LIST_QNAME).node(mapEntryPath2);

        mapEntryPath2 = new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME,
            ImmutableMap.of(MIN_MAX_KEY_LEAF_QNAME, "bar"));

        final YangInstanceIdentifier minMaxLeafBar = PRESENCE_PATH.node(MIN_MAX_LIST_QNAME).node(mapEntryPath2);

        mapEntryPath2 = new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME,
            ImmutableMap.of(MIN_MAX_KEY_LEAF_QNAME, "baz"));

        final YangInstanceIdentifier minMaxLeafBaz = PRESENCE_PATH.node(MIN_MAX_LIST_QNAME).node(mapEntryPath2);

        modificationTree.write(PRESENCE_PATH, ImmutableNodes.containerNode(PRESENCE_QNAME));
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
            assertEquals("Node (urn:opendaylight:params:xml:ns:yang:list-constraints-validation-test-model?"
                    + "revision=2015-02-02)presence is missing mandatory descendant "
                    + "/(urn:opendaylight:params:xml:ns:yang:list-constraints-validation-test-model?"
                    + "revision=2015-02-02)min-max-list", e.getMessage());
        }
    }

    @Test
    public void minMaxListNoMinMaxDeleteTest() throws DataValidationFailedException {
        final MapEntryNode fooEntryNoMinMaxNode =
                ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME_NO_MINMAX, MIN_MAX_KEY_LEAF_QNAME, "foo");
        final MapNode mapNode1 = ImmutableNodes.mapNodeBuilder()
                .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LIST_QNAME_NO_MINMAX))
                .withChild(fooEntryNoMinMaxNode).build();

        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        Map<QName, Object> key = new HashMap<>();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "foo");

        NodeIdentifierWithPredicates mapEntryPath2 = new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME_NO_MINMAX,
            key);

        final YangInstanceIdentifier minMaxLeafFoo = MASTER_CONTAINER_PATH
                .node(MIN_MAX_LIST_QNAME_NO_MINMAX).node(mapEntryPath2);

        key.clear();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "non-existing-leaf");

        mapEntryPath2 = new NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME_NO_MINMAX, key);

        YangInstanceIdentifier minMaxLeafNel = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME_NO_MINMAX).node(mapEntryPath2).build();

        modificationTree.write(MIN_MAX_LIST_NO_MINMAX_PATH, mapNode1);
        modificationTree.delete(minMaxLeafFoo);
        modificationTree.delete(minMaxLeafNel);

        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        final DataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        final Optional<NormalizedNode<?, ?>> minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_NO_MINMAX_PATH);

        // Empty list should have disappeared
        assertFalse(minMaxListRead.isPresent());
    }

    private static void testLoop(final DataTreeSnapshot snapshot, final String first, final String second) {
        Optional<NormalizedNode<?, ?>> minMaxListRead = snapshot.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertTrue(((NormalizedNodeContainer<?, ?, ?>) minMaxListRead.get()).getValue().size() == 2);
        UnmodifiableCollection<?> collectionChildren = (UnmodifiableCollection<?>) minMaxListRead.get().getValue();

        for (Object collectionChild : collectionChildren) {
            if (collectionChild.toString().contains(first)) {
                assertTrue(collectionChild.toString().contains(first));
            } else {
                assertTrue(collectionChild.toString().contains(second));
            }
        }
    }
}
