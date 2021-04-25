/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;

public class BinaryTypeTest {
    @Test
    public void canCreateBinaryType() {
        final BinaryTypeDefinition binType = BaseTypes.binaryType();
        final BinaryTypeDefinition binType1 = BaseTypes.binaryType();

        assertFalse(binType.getLengthConstraint().isPresent());
        assertEquals(Optional.empty(), binType.getDefaultValue());
        assertEquals("CURRENT", Status.CURRENT, binType.getStatus());
        assertEquals("Base type is null", null, binType.getBaseType());
        assertEquals("getQName gives BINARY_QNAME", TypeDefinitions.BINARY, binType.getQName());
        assertEquals(Optional.empty(), binType.getUnits());

        assertTrue("binType1 should equal to binType", binType.equals(binType1) && binType1.equals(binType));
        assertTrue("Hash code of binType and binType1 should be equal",
                binType.hashCode() == binType1.hashCode());
        assertEquals("binType should equals to itself", binType, binType);
        assertFalse("binType shouldn't equal to null", binType.equals(null));
        assertFalse("binType shouldn't equal to object of other type", binType.equals("str"));
    }
}
