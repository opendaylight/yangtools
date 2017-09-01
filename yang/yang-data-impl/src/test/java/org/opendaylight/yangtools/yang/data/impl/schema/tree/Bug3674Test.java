/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

/**
 * BUG-3674: issuing a delete on a non-existent entry must be preserved in
 *           DataTreeModification, but should appear as UNMODIFIED in the
 *           resulting DataTreeCandidate.
 */
public class Bug3674Test {
    private DataTree tree;

    @Before
    public void setUp() throws ReactorException, IOException, YangSyntaxErrorException {
        tree = InMemoryDataTreeFactory.getInstance().create(TreeType.OPERATIONAL);
        tree.setSchemaContext(TestModel.createTestContext());

        // Create the top-level container
        final DataTreeModification mod = tree.takeSnapshot().newModification();
        mod.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        mod.ready();
        tree.commit(tree.prepare(mod));
    }

    @Test
    public void testDeleteOfNonExistingNode() {
        final DataTreeModification mod = tree.takeSnapshot().newModification();
        mod.delete(TestModel.OUTER_LIST_PATH);
        mod.ready();

        final DataTreeCandidate candidate = tree.prepare(mod);
        final DataTreeCandidateNode root = candidate.getRootNode();
        assertEquals(ModificationType.UNMODIFIED, root.getModificationType());
    }
}
