/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class Decimal64Test {

    @Test
    public void canCreateDecimal64() {
        Integer fractionDig = 2;
        Decimal64 decimal64 = Decimal64.create(SchemaPath.ROOT, fractionDig);

        assertEquals("Status should be CURRENT", Status.CURRENT, decimal64.getStatus());

        assertEquals("Default value should be null", null, decimal64.getDefaultValue());

        assertEquals("Should be empty list", Collections.EMPTY_LIST, decimal64.getUnknownSchemaNodes());

        assertEquals("Should be null", null, decimal64.getBaseType());

        assertNotEquals("Description is not null", null, decimal64.getDescription());

        assertNotEquals("Reference is not null", null, decimal64.getReference());

        assertEquals("Should be empty string", "", decimal64.getUnits());

        assertTrue("Should contain factionDigits=2", decimal64.toString().contains("fractionDigits="+fractionDig));

        assertEquals("Should get farctionDig", fractionDig, decimal64.getFractionDigits());

        assertEquals("Should be empty list",
                Collections.EMPTY_LIST, decimal64.getPath().getPathFromRoot());

        assertEquals("Should be DECIMAL64_QNAME", BaseTypes.DECIMAL64_QNAME, decimal64.getQName());

        assertTrue("Should contain max", decimal64.getRangeConstraints().toString().contains("max=92233720368547758.07"));
        assertTrue("Should contain min", decimal64.getRangeConstraints().toString().contains("min=-92233720368547758.08"));

        Decimal64 decimal641 = decimal64;
        assertTrue("Hash code of decimal64 and decimal641 should be equal",
                    decimal64.hashCode() == decimal641.hashCode());

        assertFalse("Decimal64 shouldn't equal to null", decimal64.equals(null));
        assertEquals("Decimal64 should equals to itself", decimal64, decimal64);
        assertFalse("Decimal64 shouldn't equal to object of other type", decimal64.equals("str"));
    }
}