/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;

/**
 * BUG-3674: issuing a delete on a non-existent entry must be preserved in
 *           DataTreeModification, but should appear as UNMODIFIED in the
 *           resulting DataTreeCandidate.
 */
public class Bug3674Test extends AbstractTestModelTest {
    private DataTree tree;

    @Before
    public void setUp() throws DataValidationFailedException {
        tree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, SCHEMA_CONTEXT);

        // Create the top-level container
        final DataTreeModification mod = tree.takeSnapshot().newModification();
        mod.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        mod.ready();
        tree.commit(tree.prepare(mod));
    }

    @Test
    public void testDeleteOfNonExistingNode() throws DataValidationFailedException {
        final DataTreeModification mod = tree.takeSnapshot().newModification();
        mod.delete(TestModel.OUTER_LIST_PATH);
        mod.ready();

        final DataTreeCandidate candidate = tree.prepare(mod);
        final DataTreeCandidateNode root = candidate.getRootNode();
        assertEquals(ModificationType.UNMODIFIED, root.getModificationType());
    }
}
