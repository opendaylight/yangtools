/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertNotNull;

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
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug5968Test {
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
        modificationTree.write(
                YangInstanceIdentifier.of(ROOT),
                withMapNode ? root.withChild(
                        Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(MY_LIST)).build()).build() : root
                        .build());
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        System.out.println("Init data tree: " + inMemoryDataTree);

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
    public void writeContainerTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);

        final MapNode myList = createMap(true);
        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT)).withChild(myList);

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(ROOT), root.build());
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        System.out.println("Container write test: " + inMemoryDataTree);
    }

    @Test
    public void writeMapTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        writeMap(inMemoryDataTree, true);
        System.out.println("Map write test: " + inMemoryDataTree);
    }

    @Test
    public void writeMapEntryTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(schemaContext, true);
        writeMapEntry(inMemoryDataTree, "1", null, "common-value");
        System.out.println("MapEntry write test: " + inMemoryDataTree);
    }

    private static void writeMap(final InMemoryDataTree inMemoryDataTree, final boolean mandatoryDataMissing)
            throws DataValidationFailedException {

        final MapNode myList = createMap(mandatoryDataMissing);

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(ROOT).node(MY_LIST), myList);
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    private static MapNode createMap(final boolean mandatoryDataMissing) throws DataValidationFailedException {
        return Builders
                .mapBuilder()
                .withNodeIdentifier(new NodeIdentifier(MY_LIST))
                .withChild(
                        mandatoryDataMissing ? createMapEntry("1", "common-value") : createMapEntry("1",
                                "mandatory-value", "common-value")).build();
    }

    private static void writeMapEntry(final InMemoryDataTree inMemoryDataTree, final Object listIdValue,
            final Object mandatoryLeafValue, final Object commonLeafValue) throws DataValidationFailedException {
        final MapEntryNode taskEntryNode = mandatoryLeafValue == null ? createMapEntry(listIdValue, commonLeafValue)
                : createMapEntry(listIdValue, mandatoryLeafValue, commonLeafValue);

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
                YangInstanceIdentifier.of(ROOT).node(MY_LIST)
                        .node(new NodeIdentifierWithPredicates(MY_LIST, ImmutableMap.of(LIST_ID, listIdValue))),
                taskEntryNode);
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
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

    @Test
    public void writeContainerPositiveTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);

        final MapNode myList = createMap(false);
        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT)).withChild(myList);

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(ROOT), root.build());
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        System.out.println("Container write positive test: " + inMemoryDataTree);
    }

    @Test
    public void writeMapPositiveTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        writeMap(inMemoryDataTree, false);
        System.out.println("Map write positive test: " + inMemoryDataTree);
    }

    @Test
    public void writeMapEntryPositiveTest() throws ReactorException, DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(schemaContext, true);
        writeMapEntry(inMemoryDataTree, "1", "mandatory-value", "common-value");
        System.out.println("MapEntry write positive test: " + inMemoryDataTree);
    }
}
