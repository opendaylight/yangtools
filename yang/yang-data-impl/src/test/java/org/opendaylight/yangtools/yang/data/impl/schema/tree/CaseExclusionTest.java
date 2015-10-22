/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class CaseExclusionTest {

    private InMemoryDataTree inMemoryDataTree;
    private SchemaContext schemaContext;

    @Before
    public void prepare() {
        schemaContext = TestModel.createTestContext();
        assertNotNull("Schema context must not be null.", schemaContext);
        inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create();
        inMemoryDataTree.setSchemaContext(schemaContext);
    }

    @Test(expected=RuntimeException.class)
    public void testCaseExclusion() throws DataValidationFailedException {
        final NodeIdentifier choice1Id = new NodeIdentifier(QName.create(TestModel.TEST_QNAME, "choice1"));

        final ContainerNode container = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(Builders.containerBuilder().withNodeIdentifier(choice1Id)
                    .withChild(leafNode(QName.create(TestModel.TEST_QNAME, "case1-leaf1"), "leaf-value"))
                    .withChild(ImmutableNodes.containerNode(QName.create(TestModel.TEST_QNAME, "case2-cont"))).build())
                .build();

        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(TestModel.TEST_PATH, container);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }
}
