/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class DataTreeTransactionTest {
    private DataTree tree;

    @Before
    public void setUp() throws ReactorException {
        tree = InMemoryDataTreeFactory.getInstance().create(TreeType.OPERATIONAL);
        tree.setSchemaContext(TestModel.createTestContext());
    }

    @Test
    public void testSealedValidate() throws DataValidationFailedException {
        final DataTreeModification mod = tree.takeSnapshot().newModification();
        mod.ready();
        tree.validate(mod);
    }

    @Test
    public void testSealedPrepare() {
        final DataTreeModification mod = tree.takeSnapshot().newModification();
        mod.ready();
        tree.prepare(mod);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsealedValidate() throws DataValidationFailedException {
        final DataTreeModification mod = tree.takeSnapshot().newModification();
        tree.validate(mod);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnsealedPrepare() {
        final DataTreeModification mod = tree.takeSnapshot().newModification();
        tree.prepare(mod);
    }
}
