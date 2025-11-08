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
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;

class DataTreeTransactionTest extends AbstractTestModelTest {
    private DataTree tree;
    private DataTreeModification mod;

    @BeforeEach
    void beforeEach() {
        tree = new ReferenceDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, MODEL_CONTEXT);
        mod = tree.takeSnapshot().newModification();
    }

    @Test
    void testSealedValidate() throws DataValidationFailedException {
        mod.ready();
        tree.validate(mod);
    }

    @Test
    void testSealedPrepare() throws DataValidationFailedException {
        mod.ready();
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
