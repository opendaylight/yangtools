/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertFalse;

import com.google.common.base.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class EmptyRootTest {
    @Test
    public void testEmptyRoot() throws ReactorException, DataValidationFailedException {
        final DataTree tree = InMemoryDataTreeFactory.getInstance().create(
            DataTreeConfiguration.DEFAULT_OPERATIONAL, TestModel.createTestContext(), DataTreeFactory.absentRoot());

        final Optional<NormalizedNode<?, ?>> read = tree.takeSnapshot().readNode(YangInstanceIdentifier.EMPTY);
        assertFalse(read.isPresent());
    }
}
