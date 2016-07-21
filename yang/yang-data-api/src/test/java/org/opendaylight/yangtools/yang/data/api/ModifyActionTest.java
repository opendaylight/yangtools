/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ModifyActionTest {

    @Test
    public void testModifyAction() {
        final ModifyAction modifyAction = ModifyAction.fromXmlValue("create");
        assertEquals(ModifyAction.CREATE, modifyAction);
        assertEquals(ModifyAction.DELETE, ModifyAction.fromXmlValue("delete"));
        assertEquals(ModifyAction.MERGE, ModifyAction.fromXmlValue("merge"));
        assertEquals(ModifyAction.NONE, ModifyAction.fromXmlValue("none"));
        assertEquals(ModifyAction.REMOVE, ModifyAction.fromXmlValue("remove"));
        assertEquals(ModifyAction.REPLACE, ModifyAction.fromXmlValue("replace"));
        assertFalse(modifyAction.isAsDefaultPermitted());
        assertTrue(modifyAction.isOnElementPermitted());

        try {
            ModifyAction.fromXmlValue("exception call");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
