/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;

public class MapEntryRootTest extends AbstractTestModelTest {
    @Test
    public void testMapEntryRoot() {
        final DataTreeConfiguration treeConfig = DataTreeConfiguration.builder(TreeType.OPERATIONAL).setRootPath(
            TestModel.TEST_PATH.node(TestModel.OUTER_LIST_QNAME).node(
                NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, (short) 12))).build();
        final DataTree dataTree = new InMemoryDataTreeFactory().create(treeConfig, SCHEMA_CONTEXT);
        assertTrue(dataTree instanceof InMemoryDataTree);

        final InMemoryDataTree imdt = (InMemoryDataTree) dataTree;
        final InMemoryDataTreeModification mod = imdt.takeSnapshot().newModification();
        final ModificationApplyOperation strategy = mod.getStrategy();
        assertThat(strategy, instanceOf(MapEntryModificationStrategy.class));
    }
}
