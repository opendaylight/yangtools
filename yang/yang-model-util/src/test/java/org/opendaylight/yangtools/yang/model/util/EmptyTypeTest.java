/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Status;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class EmptyTypeTest {

    @Test
    public void canCreateEmptyType() {
        EmptyType emptyType = EmptyType.getInstance();

        assertEquals("QName", BaseTypes.EMPTY_QNAME, emptyType.getQName());
        assertEquals("Path", Collections.singletonList(BaseTypes.EMPTY_QNAME),
                emptyType.getPath().getPathFromRoot());
        assertEquals("BaseType", null, emptyType.getBaseType());
        assertEquals("DefaultValue", null, emptyType.getDefaultValue());
        assertEquals("Status", Status.CURRENT, emptyType.getStatus());
        assertTrue("Reference", emptyType.getReference().contains("rfc6020"));
        assertEquals("Units", null, emptyType.getUnits());
        assertNotEquals("Description is not null", null, emptyType.getDescription());
        assertEquals("UnknownSchemaNodes", Collections.EMPTY_LIST, emptyType.getUnknownSchemaNodes());
        assertTrue("toString", emptyType.toString().contains("empty"));
    }
}