/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class ListConstraintsValidation {
    private static final String CONSTRAINTS_VALIDATION_TEST_YANG = "/list-constraints-validation-test-model.yang";
    private static final QName MASTER_CONTAINER_QNAME = QName.create(
            "urn:opendaylight:params:xml:ns:yang:list-constraints-validation-test-model", "2015-02-02",
            "master-container");
    private static final QName MIN_MAX_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-list");
    private static final QName MIN_MAX_KEY_LEAF_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-key-leaf");
    private static final QName UNBOUNDED_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "unbounded-list");
    private static final QName UNBOUNDED_KEY_LEAF_QNAME = QName.create(MASTER_CONTAINER_QNAME, "unbounded-key-leaf");
    private static final QName MIN_MAX_LEAF_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-leaf-list");
    private static final QName UNBOUNDED_LEAF_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "unbounded-leaf-list");
    private static final QName UNKEYED_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "unkeyed-list");
    private static final QName UNKEYED_LEAF_QNAME = QName.create(MASTER_CONTAINER_QNAME, "unkeyed-leaf");

    private static final YangInstanceIdentifier MASTER_CONTAINER_PATH = YangInstanceIdentifier
            .of(MASTER_CONTAINER_QNAME);
    private static final YangInstanceIdentifier MIN_MAX_LIST_PATH = YangInstanceIdentifier
            .builder(MASTER_CONTAINER_PATH).node(MIN_MAX_LIST_QNAME).build();
    private static final YangInstanceIdentifier UNBOUNDED_LIST_PATH = YangInstanceIdentifier
            .builder(MASTER_CONTAINER_PATH).node(UNBOUNDED_LIST_QNAME).build();
    private static final YangInstanceIdentifier MIN_MAX_LEAF_LIST_PATH = YangInstanceIdentifier
            .builder(MASTER_CONTAINER_PATH).node(MIN_MAX_LEAF_LIST_QNAME).build();
    private static final YangInstanceIdentifier UNBOUNDED_LEAF_LIST_PATH = YangInstanceIdentifier
            .builder(MASTER_CONTAINER_PATH).node(UNBOUNDED_LEAF_LIST_QNAME).build();
    private static final YangInstanceIdentifier UNKEYED_LIST_PATH = YangInstanceIdentifier
            .builder(MASTER_CONTAINER_PATH).node(UNKEYED_LIST_QNAME).build();

    private SchemaContext schemaContext;
    private InMemoryDataTree inMemoryDataTree;

    @Before
    public void prepare() throws ReactorException {
        schemaContext = createTestContext();
        assertNotNull("Schema context must not be null.", schemaContext);
        inMemoryDataTree =  (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(TreeType.OPERATIONAL);
        inMemoryDataTree.setSchemaContext(schemaContext);
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();
        final DataTreeModification modificationTree = initialDataTreeSnapshot.newModification();

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(modificationTree));
    }

    public static InputStream getDatastoreTestInputStream() {
        return getInputStream(CONSTRAINTS_VALIDATION_TEST_YANG);
    }

    private static InputStream getInputStream(final String resourceName) {
        return TestModel.class.getResourceAsStream(CONSTRAINTS_VALIDATION_TEST_YANG);
    }

    public static SchemaContext createTestContext() throws ReactorException {
        return YangParserTestUtils.parseYangStreams(Collections.singletonList(getDatastoreTestInputStream()));
    }

    @Test
    public void minMaxListTestPass() throws DataValidationFailedException {

        final MapEntryNode fooEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "foo");
        final MapEntryNode barEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "bar");
        final MapNode mapNode1 = ImmutableNodes.mapNodeBuilder()
                .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LIST_QNAME))
                .withChild(fooEntryNode).build();
        final MapNode mapNode2 = ImmutableNodes.mapNodeBuilder()
                .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LIST_QNAME))
                .withChild(barEntryNode).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(MIN_MAX_LIST_PATH, mapNode1);
        modificationTree.merge(MIN_MAX_LIST_PATH, mapNode2);
        // TODO: check why write and then merge on list commits only "bar" child
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        final InMemoryDataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        final Optional<NormalizedNode<?, ?>> minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertTrue(((NormalizedNodeContainer<?, ?, ?>) minMaxListRead.get()).getValue().size() == 2);
    }

    @Test(expected = DataValidationFailedException.class)
    public void minMaxListFail() throws DataValidationFailedException {
        InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        final MapEntryNode fooEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "foo");
        final MapEntryNode barEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "bar");
        final MapEntryNode gooEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "goo");
        final MapNode mapNode = ImmutableNodes.mapNodeBuilder()
                .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LIST_QNAME))
                .withChild(fooEntryNode).build();

        final YangInstanceIdentifier fooPath = MIN_MAX_LIST_PATH.node(fooEntryNode.getIdentifier());
        final YangInstanceIdentifier barPath = MIN_MAX_LIST_PATH.node(barEntryNode.getIdentifier());
        final YangInstanceIdentifier gooPath = MIN_MAX_LIST_PATH.node(gooEntryNode.getIdentifier());

        modificationTree.write(MIN_MAX_LIST_PATH, mapNode);
        modificationTree.merge(barPath, barEntryNode);
        modificationTree.write(gooPath, gooEntryNode);
        modificationTree.delete(gooPath);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        InMemoryDataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode<?, ?>> minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertTrue(((NormalizedNodeContainer<?, ?, ?>) minMaxListRead.get()).getValue().size() == 2);

        modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(gooPath, gooEntryNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertTrue(((NormalizedNodeContainer<?, ?, ?>) minMaxListRead.get()).getValue().size() == 3);

        modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.delete(gooPath);
        modificationTree.delete(fooPath);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
    }

    @Test
    public void minMaxLeafListPass() throws DataValidationFailedException {
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        final NodeWithValue<Object> barPath = new NodeWithValue<>(MIN_MAX_LIST_QNAME, "bar");
        final NodeWithValue<Object> gooPath = new NodeWithValue<>(MIN_MAX_LIST_QNAME, "goo");

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
        modificationTree.merge(MIN_MAX_LEAF_LIST_PATH.node(gooPath), gooLeafSetEntry);
        modificationTree.delete(MIN_MAX_LEAF_LIST_PATH.node(gooPath));
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        final InMemoryDataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        final Optional<NormalizedNode<?, ?>> masterContainer = snapshotAfterCommit.readNode(MASTER_CONTAINER_PATH);
        assertTrue(masterContainer.isPresent());
        final Optional<NormalizedNodeContainer<?, ?, ?>> leafList = ((NormalizedNodeContainer) masterContainer.get())
                .getChild(new NodeIdentifier(MIN_MAX_LEAF_LIST_QNAME));
        assertTrue(leafList.isPresent());
        assertTrue(leafList.get().getValue().size() == 2);
    }

    @Test(expected = DataValidationFailedException.class)
    public void minMaxLeafListFail() throws DataValidationFailedException {
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        final NodeWithValue<Object> fooPath = new NodeWithValue<>(MIN_MAX_LIST_QNAME, "foo");
        final NodeWithValue<Object> barPath = new NodeWithValue<>(MIN_MAX_LIST_QNAME, "bar");
        final NodeWithValue<Object> gooPath = new NodeWithValue<>(MIN_MAX_LIST_QNAME, "goo");
        final NodeWithValue<Object> fuuPath = new NodeWithValue<>(MIN_MAX_LIST_QNAME, "fuu");

        final LeafSetEntryNode<Object> barLeafSetEntry = ImmutableLeafSetEntryNodeBuilder.create()
                .withNodeIdentifier(barPath)
                .withValue("bar").build();
        final LeafSetEntryNode<Object> gooLeafSetEntry = ImmutableLeafSetEntryNodeBuilder.create()
                .withNodeIdentifier(gooPath)
                .withValue("goo").build();
        final LeafSetEntryNode<Object> fuuLeafSetEntry = ImmutableLeafSetEntryNodeBuilder.create()
                .withNodeIdentifier(fuuPath)
                .withValue("fuu").build();

        final LeafSetNode<Object> fooLeafSetNode = ImmutableLeafSetNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LEAF_LIST_QNAME))
                .withChildValue("foo").build();

        modificationTree.write(MIN_MAX_LEAF_LIST_PATH, fooLeafSetNode);
        modificationTree.write(MIN_MAX_LEAF_LIST_PATH.node(barPath), barLeafSetEntry);
        modificationTree.merge(MIN_MAX_LEAF_LIST_PATH.node(gooPath), gooLeafSetEntry);
        modificationTree.write(MIN_MAX_LEAF_LIST_PATH.node(fuuPath), fuuLeafSetEntry);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
    }

    @Test
    public void unkeyedListTestPass() throws DataValidationFailedException {
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        final UnkeyedListEntryNode foo = ImmutableUnkeyedListEntryNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(UNKEYED_LEAF_QNAME))
                .withChild(ImmutableNodes.leafNode(UNKEYED_LEAF_QNAME, "foo")).build();
        final List<UnkeyedListEntryNode> unkeyedEntries = new ArrayList<>();
        unkeyedEntries.add(foo);
        final UnkeyedListNode unkeyedListNode = ImmutableUnkeyedListNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(UNKEYED_LIST_QNAME))
                .withValue(unkeyedEntries).build();

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.merge(UNKEYED_LIST_PATH, unkeyedListNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        final InMemoryDataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        final Optional<NormalizedNode<?, ?>> unkeyedListRead = snapshotAfterCommit.readNode(UNKEYED_LIST_PATH);
        assertTrue(unkeyedListRead.isPresent());
        assertTrue(((UnkeyedListNode) unkeyedListRead.get()).getSize() == 1);
    }

    @Test(expected = DataValidationFailedException.class)
    public void unkeyedListTestFail() throws DataValidationFailedException {
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        final UnkeyedListEntryNode foo = ImmutableUnkeyedListEntryNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(UNKEYED_LEAF_QNAME))
                .withChild(ImmutableNodes.leafNode(UNKEYED_LEAF_QNAME, "foo")).build();
        final UnkeyedListEntryNode bar = ImmutableUnkeyedListEntryNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(UNKEYED_LEAF_QNAME))
                .withChild(ImmutableNodes.leafNode(UNKEYED_LEAF_QNAME, "bar")).build();
        final List<UnkeyedListEntryNode> unkeyedEntries = new ArrayList<>();
        unkeyedEntries.add(foo);
        unkeyedEntries.add(bar);
        final UnkeyedListNode unkeyedListNode = ImmutableUnkeyedListNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(UNKEYED_LIST_QNAME))
                .withValue(unkeyedEntries).build();

        modificationTree.write(UNKEYED_LIST_PATH, unkeyedListNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
    }
}
