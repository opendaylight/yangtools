package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
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
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ListConstraintsValidationTest {
    private static final Logger LOG = LoggerFactory.getLogger(ListConstraintsValidationTest.class);

    private static final String CONSTRAINTS_VALIDATION_TEST_YANG = "/list-constraints-validation-test-model.yang";
    private SchemaContext schemaContext;
    private RootModificationApplyOperation rootOper;

    private static final QName MASTER_CONTAINER_QNAME = QName.create("urn:opendaylight:params:xml:ns:yang:list-constraints-validation-test-model", "2015-02-02",
            "master-container");
    private static final QName MIN_MAX_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-list");
    private static final QName MIN_MAX_KEY_LEAF_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-key-leaf");
    private static final QName UNBOUNDED_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "unbounded-list");
    private static final QName UNBOUNDED_KEY_LEAF_QNAME = QName.create(MASTER_CONTAINER_QNAME, "unbounded-key-leaf");
    private static final QName MIN_MAX_LEAF_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-leaf-list");
    private static final QName UNBOUNDED_LEAF_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "unbounded-leaf-list");
    private static final QName UNKEYED_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "unkeyed-list");
    private static final QName UNKEYED_LEAF_QNAME = QName.create(MASTER_CONTAINER_QNAME, "unkeyed-leaf");

    private static final YangInstanceIdentifier MASTER_CONTAINER_PATH = YangInstanceIdentifier.of(MASTER_CONTAINER_QNAME);
    private static final YangInstanceIdentifier MIN_MAX_LIST_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(MIN_MAX_LIST_QNAME).build();
    private static final YangInstanceIdentifier UNBOUNDED_LIST_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(UNBOUNDED_LIST_QNAME).build();
    private static final YangInstanceIdentifier MIN_MAX_LEAF_LIST_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(MIN_MAX_LEAF_LIST_QNAME).build();
    private static final YangInstanceIdentifier UNBOUNDED_LEAF_LIST_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(UNBOUNDED_LEAF_LIST_QNAME).build();
    private static final YangInstanceIdentifier UNKEYED_LIST_PATH = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
            .node(UNKEYED_LIST_QNAME).build();

    @Before
    public void prepare() {
        schemaContext = createTestContext();
        assertNotNull("Schema context must not be null.", schemaContext);
        rootOper = RootModificationApplyOperation.from(SchemaAwareApplyOperation.from(schemaContext));
    }

    public static final InputStream getDatastoreTestInputStream() {
        return getInputStream(CONSTRAINTS_VALIDATION_TEST_YANG);
    }

    private static InputStream getInputStream(final String resourceName) {
        return TestModel.class.getResourceAsStream(CONSTRAINTS_VALIDATION_TEST_YANG);
    }

    public static SchemaContext createTestContext() {
        YangParserImpl parser = new YangParserImpl();
        Set<Module> modules = parser.parseYangModelsFromStreams(Collections.singletonList(getDatastoreTestInputStream()));
        return parser.resolveSchemaContext(modules);
    }

    @Test
    public void minMaxListTestPass() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        final MapEntryNode fooEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "foo");
        final MapEntryNode barEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "bar");
        final MapNode mapNode1 = ImmutableNodes.mapNodeBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(MIN_MAX_LIST_QNAME))
                .withChild(fooEntryNode).build();
        final MapNode mapNode2 = ImmutableNodes.mapNodeBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(MIN_MAX_LIST_QNAME))
                .withChild(barEntryNode).build();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.merge(MIN_MAX_LIST_PATH, mapNode1);
        modificationTree.merge(MIN_MAX_LIST_PATH, mapNode2);
        // TODO: check why write and then merge on list commits only "bar" child

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        InMemoryDataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode<?, ?>> minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertTrue(((NormalizedNodeContainer) minMaxListRead.get()).getValue().size() == 2);
    }

    @Test
    public void minMaxListFail() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        final MapEntryNode fooEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "foo");
        final MapEntryNode barEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "bar");
        final MapEntryNode gooEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME, "goo");
        final MapNode mapNode = ImmutableNodes.mapNodeBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(MIN_MAX_LIST_QNAME))
                .withChild(fooEntryNode).build();

        final YangInstanceIdentifier fooPath = MIN_MAX_LIST_PATH.node(fooEntryNode.getIdentifier());
        final YangInstanceIdentifier barPath = MIN_MAX_LIST_PATH.node(barEntryNode.getIdentifier());
        final YangInstanceIdentifier gooPath = MIN_MAX_LIST_PATH.node(gooEntryNode.getIdentifier());

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(MIN_MAX_LIST_PATH, mapNode);
        modificationTree.merge(barPath, barEntryNode);
        modificationTree.write(gooPath, gooEntryNode);
        modificationTree.delete(gooPath);

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        InMemoryDataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode<?, ?>> minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertTrue(((NormalizedNodeContainer) minMaxListRead.get()).getValue().size() == 2);

        modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.write(gooPath, gooEntryNode);

        inMemoryDataTree.validate(modificationTree);
        prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertTrue(((NormalizedNodeContainer) minMaxListRead.get()).getValue().size() == 3);

        modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        modificationTree.delete(gooPath);
        modificationTree.delete(fooPath);

        try {
            inMemoryDataTree.validate(modificationTree);
            fail("Exception should have been thrown.");
        } catch (DataValidationFailedException e) {
            LOG.debug("DataValidationFailedException - '{}' was thrown as expected.", e);
        }
    }

    @Test
    public void minMaxLeafListPass() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        YangInstanceIdentifier.NodeWithValue fooPath = new YangInstanceIdentifier.NodeWithValue(MIN_MAX_LIST_QNAME, "foo");
        YangInstanceIdentifier.NodeWithValue barPath = new YangInstanceIdentifier.NodeWithValue(MIN_MAX_LIST_QNAME, "bar");
        YangInstanceIdentifier.NodeWithValue gooPath = new YangInstanceIdentifier.NodeWithValue(MIN_MAX_LIST_QNAME, "goo");

        LeafSetEntryNode<Object> barLeafSetEntry = ImmutableLeafSetEntryNodeBuilder.create()
                .withNodeIdentifier(barPath)
                .withValue("bar").build();
        LeafSetEntryNode<Object> gooLeafSetEntry = ImmutableLeafSetEntryNodeBuilder.create()
                .withNodeIdentifier(gooPath)
                .withValue("goo").build();

        LeafSetNode<Object> fooLeafSetNode = ImmutableLeafSetNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(MIN_MAX_LIST_QNAME))
                .withChildValue("foo").build();

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(MIN_MAX_LEAF_LIST_PATH, fooLeafSetNode);
        modificationTree.write(MIN_MAX_LEAF_LIST_PATH.node(barPath), barLeafSetEntry);
        modificationTree.merge(MIN_MAX_LEAF_LIST_PATH.node(gooPath), gooLeafSetEntry);
        modificationTree.delete(MIN_MAX_LEAF_LIST_PATH.node(gooPath));

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        InMemoryDataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode<?, ?>> masterContainer = snapshotAfterCommit.readNode(MASTER_CONTAINER_PATH);
        assertTrue(masterContainer.isPresent());
        Optional<NormalizedNodeContainer> leafList = ((NormalizedNodeContainer) masterContainer.get()).getChild(
                new YangInstanceIdentifier.NodeIdentifier(MIN_MAX_LIST_QNAME));
        assertTrue(leafList.isPresent());
        assertTrue(leafList.get().getValue().size() == 2);
    }

    @Test
    public void minMaxLeafListFail() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        YangInstanceIdentifier.NodeWithValue fooPath = new YangInstanceIdentifier.NodeWithValue(MIN_MAX_LIST_QNAME, "foo");
        YangInstanceIdentifier.NodeWithValue barPath = new YangInstanceIdentifier.NodeWithValue(MIN_MAX_LIST_QNAME, "bar");
        YangInstanceIdentifier.NodeWithValue gooPath = new YangInstanceIdentifier.NodeWithValue(MIN_MAX_LIST_QNAME, "goo");
        YangInstanceIdentifier.NodeWithValue fuuPath = new YangInstanceIdentifier.NodeWithValue(MIN_MAX_LIST_QNAME, "fuu");

        LeafSetEntryNode<Object> barLeafSetEntry = ImmutableLeafSetEntryNodeBuilder.create()
                .withNodeIdentifier(barPath)
                .withValue("bar").build();
        LeafSetEntryNode<Object> gooLeafSetEntry = ImmutableLeafSetEntryNodeBuilder.create()
                .withNodeIdentifier(gooPath)
                .withValue("goo").build();
        LeafSetEntryNode<Object> fuuLeafSetEntry = ImmutableLeafSetEntryNodeBuilder.create()
                .withNodeIdentifier(fuuPath)
                .withValue("fuu").build();

        LeafSetNode<Object> fooLeafSetNode = ImmutableLeafSetNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(MIN_MAX_LIST_QNAME))
                .withChildValue("foo").build();

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(MIN_MAX_LEAF_LIST_PATH, fooLeafSetNode);
        modificationTree.write(MIN_MAX_LEAF_LIST_PATH.node(barPath), barLeafSetEntry);
        modificationTree.merge(MIN_MAX_LEAF_LIST_PATH.node(gooPath), gooLeafSetEntry);
        modificationTree.write(MIN_MAX_LEAF_LIST_PATH.node(fuuPath), fuuLeafSetEntry);

        try {
            inMemoryDataTree.validate(modificationTree);
            fail("Exception should have been thrown.");
        } catch (DataValidationFailedException e) {
            LOG.debug("DataValidationFailedException - '{}' was thrown as expected.", e);
        }
    }

    @Test
    public void unkeyedListTestPass() throws DataValidationFailedException {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        final UnkeyedListEntryNode foo = ImmutableUnkeyedListEntryNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(UNKEYED_LEAF_QNAME))
                .withChild(ImmutableNodes.leafNode(UNKEYED_LEAF_QNAME, "foo")).build();
        List<UnkeyedListEntryNode> unkeyedEntries = new ArrayList<>();
        unkeyedEntries.add(foo);
        final UnkeyedListNode unkeyedListNode = ImmutableUnkeyedListNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(UNKEYED_LIST_QNAME))
                .withValue(unkeyedEntries).build();

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.merge(UNKEYED_LIST_PATH, unkeyedListNode);

        inMemoryDataTree.validate(modificationTree);
        DataTreeCandidate prepare1 = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare1);

        InMemoryDataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode<?, ?>> unkeyedListRead = snapshotAfterCommit.readNode(UNKEYED_LIST_PATH);
        assertTrue(unkeyedListRead.isPresent());
        assertTrue(((UnkeyedListNode) unkeyedListRead.get()).getSize() == 1);
    }

    @Test
    public void unkeyedListTestFail() {
        InMemoryDataTree inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();

        DataTreeModification modificationTree = new InMemoryDataTreeModification(initialDataTreeSnapshot,
                rootOper);

        final UnkeyedListEntryNode foo = ImmutableUnkeyedListEntryNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(UNKEYED_LEAF_QNAME))
                .withChild(ImmutableNodes.leafNode(UNKEYED_LEAF_QNAME, "foo")).build();
        final UnkeyedListEntryNode bar = ImmutableUnkeyedListEntryNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(UNKEYED_LEAF_QNAME))
                .withChild(ImmutableNodes.leafNode(UNKEYED_LEAF_QNAME, "bar")).build();
        List<UnkeyedListEntryNode> unkeyedEntries = new ArrayList<>();
        unkeyedEntries.add(foo);
        unkeyedEntries.add(bar);
        final UnkeyedListNode unkeyedListNode = ImmutableUnkeyedListNodeBuilder.create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(UNKEYED_LIST_QNAME))
                .withValue(unkeyedEntries).build();

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.write(UNKEYED_LIST_PATH, unkeyedListNode);

        try {
            inMemoryDataTree.validate(modificationTree);
            fail("Exception should have been thrown.");
        } catch (DataValidationFailedException e) {
            LOG.debug("DataValidationFailedException - '{}' was thrown as expected.", e);
        }
    }
}
