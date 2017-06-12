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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.model.util.type.BaseTypes.binaryType;

import java.util.Collections;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;

public class BinaryTypeTest {

    @Test
    public void canCreateBinaryType() {
        BinaryTypeDefinition binType = binaryType();
        BinaryTypeDefinition binType1 = binaryType();

        assertEquals(0, binType.getLengthConstraints().size());
        assertNull(binType.getDefaultValue());
        assertEquals("CURRENT", Status.CURRENT, binType.getStatus());
        assertEquals("Base type is null", null, binType.getBaseType());
        assertEquals("getQName gives BINARY_QNAME", BaseTypes.BINARY_QNAME, binType.getQName());
        assertNull("Units should be null", binType.getUnits());
        assertEquals("getPath gives List of BINARY_QNAME",
                Collections.singletonList(BaseTypes.BINARY_QNAME), binType.getPath().getPathFromRoot());

        assertTrue("binType1 should equal to binType",
                binType.equals(binType1) && binType1.equals(binType));
        assertTrue("Hash code of binType and binType1 should be equal",
                binType.hashCode() == binType1.hashCode());
        assertEquals("binType should equals to itself", binType, binType);
        assertFalse("binType shouldn't equal to null", binType.equals(null));
        assertFalse("binType shouldn't equal to object of other type", binType.equals("str"));
    }

}
