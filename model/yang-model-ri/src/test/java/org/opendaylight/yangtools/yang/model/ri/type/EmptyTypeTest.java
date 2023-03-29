/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;

class EmptyTypeTest {
    @Test
    void canCreateEmptyType() {
        EmptyTypeDefinition emptyType = BaseTypes.emptyType();

        assertEquals(TypeDefinitions.EMPTY, emptyType.getQName(), "QName");
        assertNull(emptyType.getBaseType(), "BaseType");
        assertEquals(Optional.empty(), emptyType.getDefaultValue(), "DefaultValue");
        assertEquals(Status.CURRENT, emptyType.getStatus(), "Status");
        assertFalse(emptyType.getReference().isPresent());
        assertEquals(Optional.empty(), emptyType.getUnits(), "Units");
        assertFalse(emptyType.getDescription().isPresent());
        assertEquals(Collections.emptyList(), emptyType.getUnknownSchemaNodes(), "UnknownSchemaNodes");
        assertTrue(emptyType.toString().contains("empty"), "toString");
    }
}
