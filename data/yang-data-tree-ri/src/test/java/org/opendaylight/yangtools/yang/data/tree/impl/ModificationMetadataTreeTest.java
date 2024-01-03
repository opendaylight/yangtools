/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/*
 * Schema structure of document is
 *
 * container root { 
 *      list list-a {
 *              key leaf-a;
 *              leaf leaf-a;
 *              choice choice-a {
 *                      case one {
 *                              leaf one;
 *                      }
 *                      case two-three {
 *                              leaf two;
 *                              leaf three;
 *                      }
 *              }
 *              list list-b {
 *                      key leaf-b;
 *                      leaf leaf-b;
 *              }
 *      }
 * }
 */
class ModificationMetadataTreeTest extends AbstractTestModelTest {

    private static final Short ONE_ID = 1;
    private static final Short TWO_ID = 2;
    private static final String TWO_ONE_NAME = "one";
    private static final String TWO_TWO_NAME = "two";

    private static final YangInstanceIdentifier OUTER_LIST_2_PATH =
            YangInstanceIdentifier.builder(TestModel.OUTER_LIST_PATH)
            .nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, TWO_ID)
            .build();

    private static final YangInstanceIdentifier TWO_TWO_PATH = YangInstanceIdentifier.builder(OUTER_LIST_2_PATH)
            .node(TestModel.INNER_LIST_QNAME)
            .nodeWithKey(TestModel.INNER_LIST_QNAME, TestModel.NAME_QNAME, TWO_TWO_NAME)
            .build();

    private static final YangInstanceIdentifier TWO_TWO_VALUE_PATH = YangInstanceIdentifier.builder(TWO_TWO_PATH)
            .node(TestModel.VALUE_QNAME)
            .build();

    private static final MapEntryNode BAR_NODE = ImmutableNodes.newMapEntryBuilder()
        .withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, TWO_ID))
        .withChild(ImmutableNodes.leafNode(TestModel.ID_QNAME, TWO_ID))
        .withChild(ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.INNER_LIST_QNAME))
            .withChild(ImmutableNodes.newMapEntryBuilder()
                .withNodeIdentifier(
                    NodeIdentifierWithPredicates.of(TestModel.INNER_LIST_QNAME, TestModel.NAME_QNAME, TWO_ONE_NAME))
                .withChild(ImmutableNodes.leafNode(TestModel.NAME_QNAME, TWO_ONE_NAME))
                .build())
            .withChild(ImmutableNodes.newMapEntryBuilder()
                .withNodeIdentifier(
                    NodeIdentifierWithPredicates.of(TestModel.INNER_LIST_QNAME,TestModel.NAME_QNAME, TWO_TWO_NAME))
                .withChild(ImmutableNodes.leafNode(TestModel.NAME_QNAME, TWO_TWO_NAME))
                .build())
            .build())
        .build();

    private RootApplyStrategy rootOper;

    @BeforeEach
    void prepare() throws ExcludedDataSchemaNodeException {
        rootOper = RootApplyStrategy.from(SchemaAwareApplyOperation.from(SCHEMA_CONTEXT,
            DataTreeConfiguration.DEFAULT_OPERATIONAL));
    }

    /**
     * Returns a test document.
     * <pre>
     * test
     *     outer-list
     *          id 1
     *     outer-list
     *          id 2
     *          inner-list
     *                  name "one"
     *          inner-list
     *                  name "two"
     *
     * </pre>
     *
     * @return a test document
     */
    public ContainerNode createDocumentOne() {
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(SchemaContext.NAME))
            .withChild(createTestContainer())
            .build();
    }

    private static ContainerNode createTestContainer() {
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
                .withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(
                        NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, ONE_ID))
                    .withChild(ImmutableNodes.leafNode(TestModel.ID_QNAME, ONE_ID))
                    .build())
                .withChild(BAR_NODE).build())
            .build();
    }

    @Test
    void basicReadWrites() {
        final var modificationTree = new InMemoryDataTreeModification(
            new InMemoryDataTreeSnapshot(SCHEMA_CONTEXT,
                TreeNode.of(createDocumentOne(), Version.initial()), rootOper), rootOper);
        final var originalBarNode = modificationTree.readNode(OUTER_LIST_2_PATH);
        assertTrue(originalBarNode.isPresent());
        assertSame(BAR_NODE, originalBarNode.orElseThrow());

        // writes node to /outer-list/1/inner_list/two/value
        modificationTree.write(TWO_TWO_VALUE_PATH, ImmutableNodes.leafNode(TestModel.VALUE_QNAME, "test"));

        // reads node to /outer-list/1/inner_list/two/value
        // and checks if node is already present
        final var barTwoCModified = modificationTree.readNode(TWO_TWO_VALUE_PATH);
        assertTrue(barTwoCModified.isPresent());
        assertEquals(ImmutableNodes.leafNode(TestModel.VALUE_QNAME, "test"), barTwoCModified.orElseThrow());

        // delete node to /outer-list/1/inner_list/two/value
        modificationTree.delete(TWO_TWO_VALUE_PATH);
        final var barTwoCAfterDelete = modificationTree.readNode(TWO_TWO_VALUE_PATH);
        assertFalse(barTwoCAfterDelete.isPresent());
    }


    public DataTreeModification createEmptyModificationTree() {
        /**
         * Creates empty Snapshot with associated schema context.
         */
        final var t = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL,
            SCHEMA_CONTEXT);

        /**
         *
         * Creates Mutable Data Tree based on provided snapshot and schema
         * context.
         *
         */
        return t.takeSnapshot().newModification();
    }

    @Test
    void createFromEmptyState() {

        final var modificationTree = createEmptyModificationTree();
        // Writes empty container node to /test
        modificationTree.write(TestModel.TEST_PATH,
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME)).build());

        // Writes empty list node to /test/outer-list
        modificationTree.write(TestModel.OUTER_LIST_PATH, ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
            .build());

        // Reads list node from /test/outer-list.
        final var potentialOuterList = modificationTree.readNode(TestModel.OUTER_LIST_PATH);
        assertFalse(potentialOuterList.isPresent());

        // Reads container node from /test and verifies that it contains test node.
        final var potentialTest = modificationTree.readNode(TestModel.TEST_PATH);
        assertPresentAndType(potentialTest, ContainerNode.class);
    }

    @Test
    void writeSubtreeReadChildren() {
        final var modificationTree = createEmptyModificationTree();
        modificationTree.write(TestModel.TEST_PATH, createTestContainer());
        final var potential = modificationTree.readNode(TWO_TWO_PATH);
        assertPresentAndType(potential, MapEntryNode.class);
    }

    @Test
    void writeSubtreeDeleteChildren() {
        final var modificationTree = createEmptyModificationTree();
        modificationTree.write(TestModel.TEST_PATH, createTestContainer());

        // We verify data are present
        final var potentialBeforeDelete = modificationTree.readNode(TWO_TWO_PATH);
        assertPresentAndType(potentialBeforeDelete, MapEntryNode.class);

        modificationTree.delete(TWO_TWO_PATH);
        final var potentialAfterDelete = modificationTree.readNode(TWO_TWO_PATH);
        assertFalse(potentialAfterDelete.isPresent());

    }

    private static <T> T assertPresentAndType(final Optional<?> potential, final Class<T> type) {
        assertNotNull(potential);
        assertTrue(potential.isPresent());
        assertTrue(type.isInstance(potential.orElseThrow()));
        return type.cast(potential.orElseThrow());
    }
}
