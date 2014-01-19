/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class Int8Test {

    @Test
    public void testInt8() {
        Int8 int8 = Int8.getInstance();
        Int8 int8Second = Int8.getInstance();
        assertTrue("The method 'getInstance()' has to return the same instance", int8 == int8Second);
        assertTrue("The method 'getBaseType()' is returning incorrect value", int8.getBaseType() == null);
        assertTrue("The method 'getDefaultType()' is returning incorrect value", int8.getDefaultValue() == null);
        assertEquals("The method 'toString()' is returning incorrect value",
                "type (urn:ietf:params:xml:ns:yang:1)int8", int8.toString());

    }

}
