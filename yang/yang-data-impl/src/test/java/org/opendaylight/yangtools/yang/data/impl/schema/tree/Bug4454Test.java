/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class Bug4454Test {
    private static final String CONSTRAINTS_VALIDATION_TEST_YANG = "/Bug4454Test.yang";

    private InMemoryDataTree inMemoryDataTree;

    private static final QName MASTER_CONTAINER_QNAME = QName
            .create("urn:opendaylight:params:xml:ns:yang:list-constraints-validation-test-model", "2015-02-02",
                    "master-container");
    private static final QName MIN_MAX_LIST_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-list");
    private static final QName MIN_MAX_LIST_QNAME_NO_MINMAX = QName
            .create(MASTER_CONTAINER_QNAME, "min-max-list-no-minmax");
    private static final QName MIN_MAX_KEY_LEAF_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-key-leaf");
    private static final QName MIN_MAX_VALUE_LEAF_QNAME = QName.create(MASTER_CONTAINER_QNAME, "min-max-value-leaf");

    private static final YangInstanceIdentifier MASTER_CONTAINER_PATH = YangInstanceIdentifier
            .of(MASTER_CONTAINER_QNAME);
    private static final YangInstanceIdentifier MIN_MAX_LIST_PATH = YangInstanceIdentifier
            .builder(MASTER_CONTAINER_PATH)
            .node(MIN_MAX_LIST_QNAME).build();
    private static final YangInstanceIdentifier MIN_MAX_LIST_NO_MINMAX_PATH = YangInstanceIdentifier
            .builder(MASTER_CONTAINER_PATH)
            .node(MIN_MAX_LIST_QNAME_NO_MINMAX).build();

    private final MapEntryNode fooEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME,
            "foo");
    private final MapEntryNode barEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME,
            "bar");
    private final MapEntryNode bazEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME, MIN_MAX_KEY_LEAF_QNAME,
            "baz");
    private final MapNode mapNodeFoo = ImmutableNodes.mapNodeBuilder()
            .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(MIN_MAX_LIST_QNAME))
            .withChild(fooEntryNode).build();
    private final MapNode mapNodeBar = ImmutableNodes.mapNodeBuilder()
            .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(MIN_MAX_LIST_QNAME))
            .withChild(barEntryNode).build();
    private final MapNode mapNodeBaz = ImmutableNodes.mapNodeBuilder()
            .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(MIN_MAX_LIST_QNAME))
            .withChild(bazEntryNode).build();

    @Before
    public void prepare() throws IOException, YangSyntaxErrorException {
        SchemaContext schemaContext = createTestContext();
        assertNotNull("Schema context must not be null.", schemaContext);
        inMemoryDataTree =  (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
        final InMemoryDataTreeSnapshot initialDataTreeSnapshot = inMemoryDataTree.takeSnapshot();
        final DataTreeModification modificationTree = initialDataTreeSnapshot.newModification();

        modificationTree.write(MASTER_CONTAINER_PATH, ImmutableNodes.containerNode(MASTER_CONTAINER_QNAME));
        modificationTree.ready();
        inMemoryDataTree.commit(inMemoryDataTree.prepare(modificationTree));
    }

    private static ByteSource getInputStream() {
        return Resources.asByteSource(TestModel.class.getResource(CONSTRAINTS_VALIDATION_TEST_YANG));
    }

    public static SchemaContext createTestContext() throws IOException, YangSyntaxErrorException {
        final YangParserImpl parser = new YangParserImpl();
        return parser.parseSources(Collections.singletonList(getInputStream()));
    }

    @Test
    public void minMaxListDeleteWriteTest() throws DataValidationFailedException {
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        Map<QName, Object> key = new HashMap<>();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "foo");

        YangInstanceIdentifier.NodeIdentifierWithPredicates mapEntryPath2 = new YangInstanceIdentifier
                .NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME , key);

        YangInstanceIdentifier MIN_MAX_LEAF_FOO = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPath2).build();

        key.clear();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "NON-EXISTING-LEAF");

        mapEntryPath2 = new YangInstanceIdentifier.NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME, key);

        YangInstanceIdentifier MIN_MAX_LEAF_NEL = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH).node
                (MIN_MAX_LIST_QNAME).node(mapEntryPath2).build();

        final Map<QName, Object> keyTemp = new HashMap<>();
        keyTemp.put(MIN_MAX_KEY_LEAF_QNAME, "baz");

        final YangInstanceIdentifier.NodeIdentifierWithPredicates mapEntryPathTest = new YangInstanceIdentifier
                .NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME , keyTemp);

        final YangInstanceIdentifier pathToBaz = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPathTest).node(MIN_MAX_VALUE_LEAF_QNAME).build();

        keyTemp.clear();
        keyTemp.put(MIN_MAX_KEY_LEAF_QNAME, "foo");

        final YangInstanceIdentifier.NodeIdentifierWithPredicates mapEntryPathTestKey = new YangInstanceIdentifier
                .NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME , keyTemp);

        final YangInstanceIdentifier pathToFoo = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPathTestKey).node(MIN_MAX_KEY_LEAF_QNAME).build();

        final LeafNode<String> newNode = ImmutableNodes.leafNode(MIN_MAX_VALUE_LEAF_QNAME, "test");
        final LeafNode<String> newNode1 = ImmutableNodes.leafNode(MIN_MAX_VALUE_LEAF_QNAME, "test1");
        final LeafNode<String> newNode2 = ImmutableNodes.leafNode(MIN_MAX_VALUE_LEAF_QNAME, "test2");
        final LeafNode<String> newNodekey = ImmutableNodes.leafNode(MIN_MAX_KEY_LEAF_QNAME, "foo");

        modificationTree.write(MIN_MAX_LIST_PATH, mapNodeFoo);
        modificationTree.write(MIN_MAX_LIST_PATH, mapNodeFoo);
        modificationTree.write(MIN_MAX_LIST_PATH, mapNodeFoo);
        modificationTree.merge(MIN_MAX_LIST_PATH, mapNodeBar);
        modificationTree.merge(MIN_MAX_LIST_PATH, mapNodeBaz);
        modificationTree.write(pathToFoo, newNodekey);
        modificationTree.write(pathToBaz, newNode);
        modificationTree.write(pathToBaz, newNode1);
        modificationTree.write(pathToBaz, newNode2);
        modificationTree.delete(MIN_MAX_LEAF_FOO);
        modificationTree.delete(MIN_MAX_LEAF_NEL);

        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        final InMemoryDataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        final Optional<NormalizedNode<?, ?>> minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertTrue(((NormalizedNodeContainer<?, ?, ?>) minMaxListRead.get()).getValue().size() == 2);
    }

    @Test(expected=DataValidationFailedException.class)
    public void minMaxListDeleteExceptionTest() throws DataValidationFailedException {
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        Map<QName, Object> key = new HashMap<>();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "foo");

        YangInstanceIdentifier.NodeIdentifierWithPredicates mapEntryPath2 = new YangInstanceIdentifier
                .NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME , key);

        YangInstanceIdentifier MIN_MAX_LEAF_FOO = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH)
                .node(MIN_MAX_LIST_QNAME).node(mapEntryPath2).build();

        key.clear();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "bar");

        mapEntryPath2 = new YangInstanceIdentifier.NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME, key);

        YangInstanceIdentifier MIN_MAX_LEAF_BAR = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH).node
                (MIN_MAX_LIST_QNAME)
                .node(mapEntryPath2).build();

        key.clear();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "bar");

        mapEntryPath2 = new YangInstanceIdentifier.NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME, key);

        YangInstanceIdentifier MIN_MAX_LEAF_BAZ = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH).node
                (MIN_MAX_LIST_QNAME)
                .node(mapEntryPath2).build();

        modificationTree.write(MIN_MAX_LIST_PATH, mapNodeFoo);
        modificationTree.merge(MIN_MAX_LIST_PATH, mapNodeBar);
        modificationTree.merge(MIN_MAX_LIST_PATH, mapNodeBaz);
        modificationTree.delete(MIN_MAX_LEAF_FOO);
        modificationTree.delete(MIN_MAX_LEAF_BAR);
        modificationTree.delete(MIN_MAX_LEAF_BAZ);

        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    @Test
    public void minMaxListNoMinMaxDeleteTest() throws DataValidationFailedException {
        final MapEntryNode fooEntryNode = ImmutableNodes.mapEntry(MIN_MAX_LIST_QNAME_NO_MINMAX, MIN_MAX_KEY_LEAF_QNAME
                , "foo");
        final MapNode mapNode1 = ImmutableNodes.mapNodeBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(MIN_MAX_LIST_QNAME_NO_MINMAX))
                .withChild(fooEntryNode).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        Map<QName, Object> key = new HashMap<>();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "foo");

        YangInstanceIdentifier.NodeIdentifierWithPredicates mapEntryPath2 = new YangInstanceIdentifier
                .NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME_NO_MINMAX , key);

        YangInstanceIdentifier MIN_MAX_LEAF_FOO = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH).node
                (MIN_MAX_LIST_QNAME_NO_MINMAX).node(mapEntryPath2).build();

        key.clear();
        key.put(MIN_MAX_KEY_LEAF_QNAME, "non-existing-leaf");

        mapEntryPath2 = new YangInstanceIdentifier.NodeIdentifierWithPredicates(MIN_MAX_LIST_QNAME_NO_MINMAX, key);

        YangInstanceIdentifier MIN_MAX_LEAF_NEL = YangInstanceIdentifier.builder(MASTER_CONTAINER_PATH).node
                (MIN_MAX_LIST_QNAME_NO_MINMAX)
                .node(mapEntryPath2).build();

        modificationTree.write(MIN_MAX_LIST_NO_MINMAX_PATH, mapNode1);
        modificationTree.delete(MIN_MAX_LEAF_FOO);
        modificationTree.delete(MIN_MAX_LEAF_NEL);

        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        final InMemoryDataTreeSnapshot snapshotAfterCommit = inMemoryDataTree.takeSnapshot();
        final Optional<NormalizedNode<?, ?>> minMaxListRead = snapshotAfterCommit.readNode(MIN_MAX_LIST_NO_MINMAX_PATH);
        assertTrue(minMaxListRead.isPresent());
        assertTrue(((NormalizedNodeContainer<?, ?, ?>) minMaxListRead.get()).getValue().size() == 0);
    }
}
