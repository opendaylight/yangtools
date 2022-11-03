/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangNetconfError;
import org.opendaylight.yangtools.yang.data.api.YangNetconfErrorAware;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class ListConstraintsValidation {
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

    private static EffectiveModelContext schemaContext;

    private DataTree inMemoryDataTree;

    @BeforeClass
    public static void beforeClass() {
        schemaContext = YangParserTestUtils.parseYangResource("/list-constraints-validation-test-model.yang");
    }

    @AfterClass
    public static void afterClass() {
        schemaContext = null;
    }

    @Before
    public void prepare() throws DataValidationFailedException {
        inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL,
            schemaContext);
        final DataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();
        final DataTreeModification modificationTree = initialDataTreeSnapshot.newModification();

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(modificationTree));
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

        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(MIN_MAX_LIST_PATH, mapNode1);
        modificationTree.merge(MIN_MAX_LIST_PATH, mapNode2);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        final DataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        final Optional<NormalizedNode> minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertEquals(2, ((NormalizedNodeContainer<?>) minMaxListRead.get()).size());
    }

    @Test(expected = DataValidationFailedException.class)
    public void minMaxListFail() throws DataValidationFailedException {
        DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

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

        DataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode> minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertEquals(2, ((NormalizedNodeContainer<?>) minMaxListRead.get()).size());

        modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(gooPath, gooEntryNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertEquals(3, ((NormalizedNodeContainer<?>) minMaxListRead.get()).size());

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

        modificationTree.write(MIN_MAX_LEAF_LIST_PATH, Builders.leafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LEAF_LIST_QNAME))
            .withChildValue("foo")
            .build());
        modificationTree.write(MIN_MAX_LEAF_LIST_PATH.node(barPath), Builders.leafSetEntryBuilder()
            .withNodeIdentifier(barPath)
            .withValue("bar")
            .build());
        modificationTree.merge(MIN_MAX_LEAF_LIST_PATH.node(gooPath), Builders.leafSetEntryBuilder()
            .withNodeIdentifier(gooPath)
            .withValue("goo")
            .build());
        modificationTree.delete(MIN_MAX_LEAF_LIST_PATH.node(gooPath));
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        final DataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        final Optional<NormalizedNode> masterContainer = snapshotAfterCommit.readNode(MASTER_CONTAINER_PATH);
        assertTrue(masterContainer.isPresent());
        final NormalizedNodeContainer<?> leafList =
            (NormalizedNodeContainer<?>) ((DistinctNodeContainer) masterContainer.get())
                .childByArg(new NodeIdentifier(MIN_MAX_LEAF_LIST_QNAME));
        assertNotNull(leafList);
        assertEquals(2, leafList.size());
    }

    @Test
    public void minMaxLeafListFail() {
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        final NodeWithValue<Object> barPath = new NodeWithValue<>(MIN_MAX_LIST_QNAME, "bar");
        final NodeWithValue<Object> gooPath = new NodeWithValue<>(MIN_MAX_LIST_QNAME, "goo");
        final NodeWithValue<Object> fuuPath = new NodeWithValue<>(MIN_MAX_LIST_QNAME, "fuu");

        modificationTree.write(MIN_MAX_LEAF_LIST_PATH, Builders.leafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(MIN_MAX_LEAF_LIST_QNAME))
            .withChildValue("foo")
            .build());
        modificationTree.write(MIN_MAX_LEAF_LIST_PATH.node(barPath), Builders.leafSetEntryBuilder()
            .withNodeIdentifier(barPath)
            .withValue("bar")
            .build());
        modificationTree.merge(MIN_MAX_LEAF_LIST_PATH.node(gooPath), Builders.leafSetEntryBuilder()
            .withNodeIdentifier(gooPath)
            .withValue("goo")
            .build());
        modificationTree.write(MIN_MAX_LEAF_LIST_PATH.node(fuuPath), Builders.leafSetEntryBuilder()
            .withNodeIdentifier(fuuPath)
            .withValue("fuu")
            .build());

        final MinMaxElementsValidationFailedException ex = assertThrows(MinMaxElementsValidationFailedException.class,
            () -> modificationTree.ready());
        assertEquals("(urn:opendaylight:params:xml:ns:yang:list-constraints-validation-test-model?"
            + "revision=2015-02-02)min-max-leaf-list has too many elements (4), can have at most 3",
            ex.getMessage());
        assertTooManyElements(ex);
    }

    @Test
    public void unkeyedListTestPass() throws DataValidationFailedException {
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        final UnkeyedListNode unkeyedListNode = Builders.unkeyedListBuilder()
            .withNodeIdentifier(new NodeIdentifier(UNKEYED_LIST_QNAME))
            .withValue(List.of(Builders.unkeyedListEntryBuilder()
                .withNodeIdentifier(new NodeIdentifier(UNKEYED_LEAF_QNAME))
                .withChild(ImmutableNodes.leafNode(UNKEYED_LEAF_QNAME, "foo"))
                .build()))
            .build();

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.merge(UNKEYED_LIST_PATH, unkeyedListNode);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        final DataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        final Optional<NormalizedNode> unkeyedListRead = snapshotAfterCommit.readNode(UNKEYED_LIST_PATH);
        assertTrue(unkeyedListRead.isPresent());
        assertEquals(1, ((UnkeyedListNode) unkeyedListRead.get()).size());
    }

    @Test
    public void unkeyedListTestFail() {
        final DataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        modificationTree.write(UNKEYED_LIST_PATH, Builders.unkeyedListBuilder()
            .withNodeIdentifier(new NodeIdentifier(UNKEYED_LIST_QNAME))
            .withValue(List.of(
                Builders.unkeyedListEntryBuilder()
                    .withNodeIdentifier(new NodeIdentifier(UNKEYED_LEAF_QNAME))
                    .withChild(ImmutableNodes.leafNode(UNKEYED_LEAF_QNAME, "foo"))
                    .build(),
                Builders.unkeyedListEntryBuilder()
                    .withNodeIdentifier(new NodeIdentifier(UNKEYED_LEAF_QNAME))
                    .withChild(ImmutableNodes.leafNode(UNKEYED_LEAF_QNAME, "bar"))
                    .build()))
            .build());
        final MinMaxElementsValidationFailedException ex = assertThrows(MinMaxElementsValidationFailedException.class,
            () -> modificationTree.ready());
        assertEquals("(urn:opendaylight:params:xml:ns:yang:list-constraints-validation-test-model?"
            + "revision=2015-02-02)unkeyed-list has too many elements (2), can have at most 1", ex.getMessage());
        assertTooManyElements(ex);
    }

    static void assertTooFewElements(final Exception ex) {
        assertOperationFailed(ex, "too-few-elements");
    }

    static void assertTooManyElements(final Exception ex) {
        assertOperationFailed(ex, "too-many-elements");
    }

    private static void assertOperationFailed(final Exception ex, final String expectedAppTag) {
        assertThat(ex, instanceOf(YangNetconfErrorAware.class));
        final List<YangNetconfError> errors = ((YangNetconfErrorAware) ex).getNetconfErrors();
        assertEquals(1, errors.size());
        final YangNetconfError error = errors.get(0);
        assertEquals(ErrorSeverity.ERROR, error.severity());
        assertEquals(ErrorType.APPLICATION, error.type());
        assertEquals(ErrorTag.OPERATION_FAILED, error.tag());
        assertEquals(expectedAppTag, error.appTag());
    }
}
