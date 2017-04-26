/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug8291Test {
    private static final String NS = "foo";
    private static final String REV = "1970-01-01";
    private static final QName ROOT = QName.create(NS, REV, "root");
    private static final QName OUTER_LIST = QName.create(NS, REV, "outer-list");
    private static final QName OUTER_LIST_ID = QName.create(NS, REV, "id");
    private static final QName INNER_LIST = QName.create(NS, REV, "inner-list");
    private SchemaContext schemaContext;

    @Before
    public void init() throws ReactorException {
        this.schemaContext = TestModel.createTestContext("/bug8291/foo.yang");
        assertNotNull("Schema context must not be null.", this.schemaContext);
    }

    private static InMemoryDataTree initDataTree(final SchemaContext schemaContext)
            throws DataValidationFailedException {
        final DataTreeConfiguration config = new DataTreeConfiguration.Builder(TreeType.CONFIGURATION).setRootPath(
                YangInstanceIdentifier.of(ROOT).node(OUTER_LIST)).build();
        final InMemoryDataTree inMemoryDataTree = (InMemoryDataTree) InMemoryDataTreeFactory.getInstance().create(
                config, schemaContext);
        return inMemoryDataTree;
    }

    @Test
    public void test() throws DataValidationFailedException {
        final InMemoryDataTree inMemoryDataTree = initDataTree(schemaContext);
        writeOuterListMapEntry(inMemoryDataTree);
        writeInnerList(inMemoryDataTree);
    }

    private void writeInnerList(final InMemoryDataTree inMemoryDataTree) throws DataValidationFailedException {
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
                YangInstanceIdentifier.create(
                        new NodeIdentifierWithPredicates(OUTER_LIST, ImmutableMap.of(OUTER_LIST_ID, 1))).node(
                        INNER_LIST), Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(INNER_LIST)).build());

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    private static void writeOuterListMapEntry(final InMemoryDataTree inMemoryDataTree)
            throws DataValidationFailedException {
        final InMemoryDataTreeModification modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        final MapEntryNode outerListMapEntry = Builders.mapEntryBuilder()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(OUTER_LIST, ImmutableMap.of(OUTER_LIST_ID, 1)))
                .withChild(ImmutableNodes.leafNode(OUTER_LIST_ID, 1)).build();

        modificationTree.write(YangInstanceIdentifier.create(new NodeIdentifierWithPredicates(OUTER_LIST, ImmutableMap
                .of(OUTER_LIST_ID, 1))), outerListMapEntry);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final DataTreeCandidate prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }
}
