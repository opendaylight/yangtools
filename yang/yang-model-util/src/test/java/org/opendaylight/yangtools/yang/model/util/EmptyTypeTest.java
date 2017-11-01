/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.model.util.type.BaseTypes.emptyType;

import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

public class EmptyTypeTest {

    @Test
    public void canCreateEmptyType() {
        EmptyTypeDefinition emptyType = emptyType();

        assertEquals("QName", BaseTypes.EMPTY_QNAME, emptyType.getQName());
        assertEquals("Path", Collections.singletonList(BaseTypes.EMPTY_QNAME),
                emptyType.getPath().getPathFromRoot());
        assertEquals("BaseType", null, emptyType.getBaseType());
        assertEquals("DefaultValue", Optional.empty(), emptyType.getDefaultValue());
        assertEquals("Status", Status.CURRENT, emptyType.getStatus());
        assertFalse(emptyType.getReference().isPresent());
        assertEquals("Units", Optional.empty(), emptyType.getUnits());
        assertFalse(emptyType.getDescription().isPresent());
        assertEquals("UnknownSchemaNodes", Collections.EMPTY_LIST, emptyType.getUnknownSchemaNodes());
        assertTrue("toString", emptyType.toString().contains("empty"));
    }
}
