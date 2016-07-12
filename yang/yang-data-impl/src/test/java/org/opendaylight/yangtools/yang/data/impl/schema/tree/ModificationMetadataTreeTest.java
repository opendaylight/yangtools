/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntry;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapEntryBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.mapNodeBuilder;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

/**
 *
 * Schema structure of document is
 *
 * <pre>
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
 * </pre>
 *
 */
public class ModificationMetadataTreeTest {

    private static final Short ONE_ID = 1;
    private static final Short TWO_ID = 2;
    private static final String TWO_ONE_NAME = "one";
    private static final String TWO_TWO_NAME = "two";

    private static final YangInstanceIdentifier OUTER_LIST_1_PATH = YangInstanceIdentifier.builder(TestModel.OUTER_LIST_PATH)
            .nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, ONE_ID) //
            .build();

    private static final YangInstanceIdentifier OUTER_LIST_2_PATH = YangInstanceIdentifier.builder(TestModel.OUTER_LIST_PATH)
            .nodeWithKey(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, TWO_ID) //
            .build();

    private static final YangInstanceIdentifier TWO_TWO_PATH = YangInstanceIdentifier.builder(OUTER_LIST_2_PATH)
            .node(TestModel.INNER_LIST_QNAME) //
            .nodeWithKey(TestModel.INNER_LIST_QNAME, TestModel.NAME_QNAME, TWO_TWO_NAME) //
            .build();

    private static final YangInstanceIdentifier TWO_TWO_VALUE_PATH = YangInstanceIdentifier.builder(TWO_TWO_PATH)
            .node(TestModel.VALUE_QNAME) //
            .build();

    private static final MapEntryNode BAR_NODE = mapEntryBuilder(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, TWO_ID) //
            .withChild(mapNodeBuilder(TestModel.INNER_LIST_QNAME) //
                    .withChild(mapEntry(TestModel.INNER_LIST_QNAME, TestModel.NAME_QNAME, TWO_ONE_NAME)) //
                    .withChild(mapEntry(TestModel.INNER_LIST_QNAME,TestModel.NAME_QNAME, TWO_TWO_NAME)) //
                    .build()) //
                    .build();

    private SchemaContext schemaContext;
    private RootModificationApplyOperation rootOper;

    @Before
    public void prepare() throws ReactorException {
        schemaContext = TestModel.createTestContext();
        assertNotNull("Schema context must not be null.", schemaContext);
        rootOper = RootModificationApplyOperation.from(SchemaAwareApplyOperation.from(schemaContext, DataTreeConfiguration.DEFAULT_OPERATIONAL));
    }

    /**
     * Returns a test document
     *
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
    public NormalizedNode<?, ?> createDocumentOne() {
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new NodeIdentifier(schemaContext.getQName()))
                .withChild(createTestContainer()).build();

    }

    private static ContainerNode createTestContainer() {
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(
                        mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
                        .withChild(mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, ONE_ID))
                        .withChild(BAR_NODE).build()).build();
    }

    @Test
    public void basicReadWrites() {
        final DataTreeModification modificationTree = new InMemoryDataTreeModification(new InMemoryDataTreeSnapshot(schemaContext,
                TreeNodeFactory.createTreeNodeRecursively(createDocumentOne(), Version.initial()), rootOper),
                rootOper);
        final Optional<NormalizedNode<?, ?>> originalBarNode = modificationTree.readNode(OUTER_LIST_2_PATH);
        assertTrue(originalBarNode.isPresent());
        assertSame(BAR_NODE, originalBarNode.get());

        // writes node to /outer-list/1/inner_list/two/value
        modificationTree.write(TWO_TWO_VALUE_PATH, ImmutableNodes.leafNode(TestModel.VALUE_QNAME, "test"));

        // reads node to /outer-list/1/inner_list/two/value
        // and checks if node is already present
        final Optional<NormalizedNode<?, ?>> barTwoCModified = modificationTree.readNode(TWO_TWO_VALUE_PATH);
        assertTrue(barTwoCModified.isPresent());
        assertEquals(ImmutableNodes.leafNode(TestModel.VALUE_QNAME, "test"), barTwoCModified.get());

        // delete node to /outer-list/1/inner_list/two/value
        modificationTree.delete(TWO_TWO_VALUE_PATH);
        final Optional<NormalizedNode<?, ?>> barTwoCAfterDelete = modificationTree.readNode(TWO_TWO_VALUE_PATH);
        assertFalse(barTwoCAfterDelete.isPresent());
    }


    public DataTreeModification createEmptyModificationTree() {
        /**
         * Creates empty Snapshot with associated schema context.
         */
        final DataTree t = InMemoryDataTreeFactory.getInstance().create(TreeType.OPERATIONAL);
        t.setSchemaContext(schemaContext);

        /**
         *
         * Creates Mutable Data Tree based on provided snapshot and schema
         * context.
         *
         */
        return t.takeSnapshot().newModification();
    }

    @Test
    public void createFromEmptyState() {

        final DataTreeModification modificationTree = createEmptyModificationTree();
        /**
         * Writes empty container node to /test
         *
         */
        modificationTree.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));

        /**
         * Writes empty list node to /test/outer-list
         */
        modificationTree
            .write(TestModel.OUTER_LIST_PATH, ImmutableNodes.mapNodeBuilder(TestModel.OUTER_LIST_QNAME).build());

        /**
         * Reads list node from /test/outer-list. Should not be present since parent list nodes are managed in
         * org.opendaylight.yangtools.yang.data.impl.schema.tree.StructuralModificationStrategyWrapper
         */
        final Optional<NormalizedNode<?, ?>> potentialOuterList = modificationTree.readNode(TestModel.OUTER_LIST_PATH);
        assertFalse(potentialOuterList.isPresent());

        /**
         * Reads container node from /test and verifies that it contains test
         * node
         */
        final Optional<NormalizedNode<?, ?>> potentialTest = modificationTree.readNode(TestModel.TEST_PATH);
        assertPresentAndType(potentialTest, ContainerNode.class);
    }

    @Test
    public void writeSubtreeReadChildren() {
        final DataTreeModification modificationTree = createEmptyModificationTree();
        modificationTree.write(TestModel.TEST_PATH, createTestContainer());
        final Optional<NormalizedNode<?, ?>> potential = modificationTree.readNode(TWO_TWO_PATH);
        assertPresentAndType(potential, MapEntryNode.class);
    }

    @Test
    public void writeSubtreeDeleteChildren() {
        final DataTreeModification modificationTree = createEmptyModificationTree();
        modificationTree.write(TestModel.TEST_PATH, createTestContainer());

        // We verify data are present
        final Optional<NormalizedNode<?, ?>> potentialBeforeDelete = modificationTree.readNode(TWO_TWO_PATH);
        assertPresentAndType(potentialBeforeDelete, MapEntryNode.class);

        modificationTree.delete(TWO_TWO_PATH);
        final Optional<NormalizedNode<?, ?>> potentialAfterDelete = modificationTree.readNode(TWO_TWO_PATH);
        assertFalse(potentialAfterDelete.isPresent());

    }

    private static <T> T assertPresentAndType(final Optional<?> potential, final Class<T> type) {
        assertNotNull(potential);
        assertTrue(potential.isPresent());
        assertTrue(type.isInstance(potential.get()));
        return type.cast(potential.get());
    }

}
