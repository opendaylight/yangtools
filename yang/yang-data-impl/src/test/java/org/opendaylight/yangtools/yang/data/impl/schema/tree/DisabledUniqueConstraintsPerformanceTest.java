/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class DisabledUniqueConstraintsPerformanceTest {
    private static final int INITIAL_WRITE_COUNT = 100000;
    private static final int SECOND_WRITE_COUNT = 100;

    @Test
    public void mapEntryTest() throws ReactorException, DataValidationFailedException {
        final SchemaContext schemaContext = TestModel.createTestContext("/bug4955/foo.yang");
        assertNotNull("Schema context must not be null.", schemaContext);

        final InMemoryDataTree inMemoryDataTree = UniqueConstraintPerformanceTestUtils.initDataTree(schemaContext,
                false, INITIAL_WRITE_COUNT);
        UniqueConstraintPerformanceTestUtils.writeMapEntriesInSequence(inMemoryDataTree, SECOND_WRITE_COUNT,
                INITIAL_WRITE_COUNT);
    }
}
