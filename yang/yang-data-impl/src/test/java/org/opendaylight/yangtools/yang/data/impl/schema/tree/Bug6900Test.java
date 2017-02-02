/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug6900Test {
    private static final String NS = "foo";
    private static final String REV = "1970-01-01";
    private static final QName ROOT = QName.create(NS, REV, "root");
    private static final QName LEAF_LIST = QName.create(NS, REV, "my-leaf-list");
    private SchemaContext schemaContext;

    @Before
    public void init() throws ReactorException {
        this.schemaContext = TestModel.createTestContext("/bug6900/foo.yang");
        assertNotNull("Schema context must not be null.", this.schemaContext);
    }

    private static InMemoryDataTree emptyDataTree(final SchemaContext schemaContext)
            throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(
                DataTreeConfiguration.DEFAULT_OPERATIONAL);
        inMemoryDataTree.setSchemaContext(schemaContext);
        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> root = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT));

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(ROOT), root.build());
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        return inMemoryDataTree;
    }

    @Test
    public void test() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = emptyDataTree(schemaContext);
        System.out.println("INIT: \n\n"+inMemoryDataTree);

        final LeafSetNode<Object> leafList = Builders.leafSetBuilder().withNodeIdentifier(new NodeIdentifier(LEAF_LIST))
        .withChild(createLeafListEntry("SAME VALUE"))
        .withChild(createLeafListEntry("SAME VALUE"))
        .withChild(createLeafListEntry("SAME VALUE")).build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(YangInstanceIdentifier.of(ROOT).node(LEAF_LIST), leafList);
        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);

        System.out.println("AFTER: \n\n"+inMemoryDataTree);
    }

    private static LeafSetEntryNode<Object> createLeafListEntry(final String value)
            throws DataValidationFailedException {
        return Builders.leafSetEntryBuilder().withNodeIdentifier(new NodeWithValue<>(LEAF_LIST, value))
                .withValue(value).build();
    }
}
