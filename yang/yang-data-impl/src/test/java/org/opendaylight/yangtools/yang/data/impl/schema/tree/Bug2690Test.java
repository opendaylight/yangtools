/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug2690Test {
    private static final String ODL_DATASTORE_TEST_YANG = "/odl-datastore-test.yang";
    private SchemaContext schemaContext;
    private InMemoryDataTree inMemoryDataTree;

    @Before
    public void prepare() throws ReactorException {
        schemaContext = createTestContext();
        assertNotNull("Schema context must not be null.", schemaContext);
        inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(TreeType.OPERATIONAL);
        inMemoryDataTree.setSchemaContext(schemaContext);
    }

    public static SchemaContext createTestContext() throws ReactorException {
        return YangParserTestUtils.parseYangResource(ODL_DATASTORE_TEST_YANG);
    }

    @Test
    public void testWriteMerge1() throws DataValidationFailedException {
        final MapEntryNode fooEntryNode = ImmutableNodes.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1);
        final MapEntryNode barEntryNode = ImmutableNodes.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 2);
        final MapNode mapNode1 = ImmutableNodes.mapNodeBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TestModel.OUTER_LIST_QNAME))
                .withChild(fooEntryNode).build();
        final MapNode mapNode2 = ImmutableNodes.mapNodeBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TestModel.OUTER_LIST_QNAME))
                .withChild(barEntryNode).build();

        final ContainerNode cont1 = Builders.containerBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(mapNode1).build();

        final ContainerNode cont2 = Builders.containerBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(mapNode2).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, cont1);
        modificationTree.merge(TestModel.TEST_PATH, cont2);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        final InMemoryDataTreeSnapshot snapshotAfterTx = inMemoryDataTree.takeSnapshot();
        final InMemoryDataTreeModification modificationAfterTx = snapshotAfterTx.newModification();
        final Optional<NormalizedNode<?, ?>> readNode = modificationAfterTx.readNode(TestModel.OUTER_LIST_PATH);
        assertTrue(readNode.isPresent());
        assertEquals(2, ((NormalizedNodeContainer<?,?,?>)readNode.get()).getValue().size());
    }
}
