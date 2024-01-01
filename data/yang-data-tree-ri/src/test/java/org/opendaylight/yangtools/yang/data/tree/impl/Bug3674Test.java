/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;

/**
 * BUG-3674: issuing a delete on a non-existent entry must be preserved in
 *           DataTreeModification, but should appear as UNMODIFIED in the
 *           resulting DataTreeCandidate.
 */
class Bug3674Test extends AbstractTestModelTest {
    private DataTree tree;

    @Test
    void testDeleteOfNonExistingNode() throws DataValidationFailedException {
        tree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, SCHEMA_CONTEXT);

        // Create the top-level container
        final var mod = tree.takeSnapshot().newModification();
        mod.write(TestModel.TEST_PATH,
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME)).build());
        mod.ready();
        tree.commit(tree.prepare(mod));

        final var mod2 = tree.takeSnapshot().newModification();
        mod2.delete(TestModel.OUTER_LIST_PATH);
        mod2.ready();

        final var candidate = tree.prepare(mod2);
        final var root = candidate.getRootNode();
        assertEquals(ModificationType.UNMODIFIED, root.modificationType());
    }
}
