/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;

class DataTreeTransactionTest extends AbstractTestModelTest {
    private DataTree tree;
    private InMemoryDataTreeModification mod;

    @BeforeEach
    void setUp() {
        tree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, SCHEMA_CONTEXT);
        mod = assertInstanceOf(InMemoryDataTreeModification.class, tree.takeSnapshot().newModification());
        assertEquals("Open", mod.acquireState().toString());
    }

    @Test
    void testSealedValidate() throws DataValidationFailedException {
        mod.ready();
        assertEquals("Noop", mod.acquireState().toString());
        tree.validate(mod);
    }

    @Test
    void testSealedPrepare() throws DataValidationFailedException {
        mod.ready();
        assertEquals("Noop", mod.acquireState().toString());
        assertInstanceOf(NoopDataTreeCandidate.class, tree.prepare(mod));
    }

    @Test
    void testUnsealedValidate() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> tree.validate(mod));
        assertEquals("Attempted to validate modification in state Open", ex.getMessage());
    }

    @Test
    void testUnsealedPrepare() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> tree.prepare(mod));
        assertEquals("Attempted to prepare modification in state Open", ex.getMessage());
    }
}
