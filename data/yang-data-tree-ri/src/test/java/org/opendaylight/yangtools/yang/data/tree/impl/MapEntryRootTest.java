/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;

class MapEntryRootTest extends AbstractTestModelTest {
    @Test
    void testMapEntryRoot() {
        final var treeConfig = DataTreeConfiguration.builder(TreeType.OPERATIONAL).setRootPath(
            TestModel.TEST_PATH.node(TestModel.OUTER_LIST_QNAME).node(
                NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, (short) 12))).build();
        final var dataTree = new ReferenceDataTreeFactory().create(treeConfig, MODEL_CONTEXT);
        assertInstanceOf(InMemoryDataTree.class, dataTree);

        final var imdt = dataTree;
        final var mod = imdt.takeSnapshot().newModification();
        final var strategy = mod.getStrategy();
        assertInstanceOf(MapEntryModificationStrategy.class, strategy);
    }
}
