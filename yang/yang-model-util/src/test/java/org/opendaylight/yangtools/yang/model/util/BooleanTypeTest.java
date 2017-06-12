/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.model.util.type.BaseTypes.booleanType;

import java.util.Collections;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;

public class BooleanTypeTest {

    @Test
    public void canCreateBooleanType() {
        final BooleanTypeDefinition boolType = booleanType();
        final String stringBoolType = boolType.toString();

        assertEquals("getPath gives List of BOOLEAN_QNAME",
                Collections.singletonList(BaseTypes.BOOLEAN_QNAME), boolType.getPath().getPathFromRoot());
        assertEquals("getQName gives BOOLEAN_QNAME", BaseTypes.BOOLEAN_QNAME, boolType.getQName());
        assertNull(boolType.getDescription());

        final String strPath = boolType.getPath().toString();
        assertTrue("Should contain string of getPath", stringBoolType.contains(strPath));
        assertNull("Should be null", boolType.getUnits());
        assertEquals("Base type is null", null, boolType.getBaseType());
        assertNull("Default value is null", boolType.getDefaultValue());
        assertEquals("Status CURRENT", Status.CURRENT, boolType.getStatus());
        assertEquals("Should contain empty list", Collections.EMPTY_LIST, boolType.getUnknownSchemaNodes());
    }
}
