/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.ModifiedNodeDoesNotExistException;

class ErrorReportingTest extends AbstractTestModelTest {
    private DataTree dataTree;

    @BeforeEach
    void beforeEach() {
        dataTree = new ReferenceDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, MODEL_CONTEXT);
    }

    @Test
    void writeWithoutParentExisting() {
        final var modification = dataTree.takeSnapshot().newModification();
        // We write node without creating parent
        modification.write(TestModel.OUTER_LIST_PATH, ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
            .build());
        modification.ready();

        final var ex = assertThrows(ModifiedNodeDoesNotExistException.class, () -> dataTree.validate(modification));
        assertEquals(TestModel.TEST_PATH, ex.getPath());
    }

    @Test
    void parentConcurrentlyDeletedExisting() throws Exception {
        final var initial = dataTree.takeSnapshot().newModification();
        // We write node without creating parent
        initial.write(TestModel.TEST_PATH,
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME)).build());
        initial.ready();
        // We commit transaction
        dataTree.commit(dataTree.prepare(initial));

        final var writeTx = dataTree.takeSnapshot().newModification();
        final var deleteTx = dataTree.takeSnapshot().newModification();
        deleteTx.delete(TestModel.TEST_PATH);
        deleteTx.ready();
        // We commit delete modification
        dataTree.commit(dataTree.prepare(deleteTx));

        writeTx.write(TestModel.OUTER_LIST_PATH, ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
            .build());
        writeTx.ready();

        final var ex = assertThrows(ConflictingModificationAppliedException.class, () -> dataTree.validate(writeTx));
        assertEquals(TestModel.TEST_PATH, ex.getPath());
    }
}
