/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModifiedNodeDoesNotExistException;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public class ErrorReportingTest extends AbstractTestModelTest {

    private DataTree tree;

    @Before
    public void setup() {
        tree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, SCHEMA_CONTEXT);
    }

    @Test
    public void writeWithoutParentExisting() {
        DataTreeModification modification = tree.takeSnapshot().newModification();
        // We write node without creating parent
        modification.write(TestModel.OUTER_LIST_PATH, ImmutableNodes.mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
            .build());
        modification.ready();
        try {
            tree.validate(modification);
            fail("ModifiedNodeDoesNotExistException should be raised");
        } catch (ModifiedNodeDoesNotExistException e) {
            assertEquals(TestModel.TEST_PATH, e.getPath());
        } catch (DataValidationFailedException e) {
            fail("ModifiedNodeDoesNotExistException expected");
        }
    }

    @Test
    public void parentConcurrentlyDeletedExisting() {
        DataTreeModification initial = tree.takeSnapshot().newModification();
        // We write node without creating parent
        initial.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        initial.ready();
        // We commit transaction
        tree.commit(tree.prepare(initial));

        final DataTreeModification writeTx = tree.takeSnapshot().newModification();
        final DataTreeModification deleteTx = tree.takeSnapshot().newModification();
        deleteTx.delete(TestModel.TEST_PATH);
        deleteTx.ready();
        // We commit delete modification
        tree.commit(tree.prepare(deleteTx));

        writeTx.write(TestModel.OUTER_LIST_PATH, ImmutableNodes.mapNodeBuilder(TestModel.OUTER_LIST_QNAME).build());
        writeTx.ready();
        try {
            tree.validate(writeTx);
            fail("ConflictingModificationAppliedException should be raised");
        } catch (ConflictingModificationAppliedException e) {
            assertEquals(TestModel.TEST_PATH, e.getPath());
        } catch (DataValidationFailedException e) {
            fail("ConflictingModificationAppliedException expected");
        }
    }
}
