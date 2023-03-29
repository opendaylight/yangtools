/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;

class BinaryTypeTest {
    @Test
    void canCreateBinaryType() {
        final var binType = BaseTypes.binaryType();
        final var binType1 = BaseTypes.binaryType();

        assertEquals(Optional.empty(), binType.getLengthConstraint());
        assertEquals(Optional.empty(), binType.getDefaultValue());
        assertEquals(Status.CURRENT, binType.getStatus(), "CURRENT");
        assertNull(binType.getBaseType(), "Base type is null");
        assertEquals(TypeDefinitions.BINARY, binType.getQName(), "getQName gives BINARY_QNAME");
        assertEquals(Optional.empty(), binType.getUnits());

        assertTrue(binType.equals(binType1) && binType1.equals(binType), "binType1 should equal to binType");
        assertEquals(binType.hashCode(), binType1.hashCode(), "Hash code of binType and binType1 should be equal");
        assertEquals(binType, binType, "binType should equals to itself");
        assertNotEquals(null, binType, "binType shouldn't equal to null");
        assertNotEquals("str", binType, "binType shouldn't equal to object of other type");
    }
}
