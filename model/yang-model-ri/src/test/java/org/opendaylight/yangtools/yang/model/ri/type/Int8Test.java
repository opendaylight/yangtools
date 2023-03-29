/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;

class Int8Test {
    @Test
    void testInt8() {
        Int8TypeDefinition int8 = BaseTypes.int8Type();
        Int8TypeDefinition int8Second = BaseTypes.int8Type();
        assertSame(int8, int8Second, "The method 'getInstance()' has to return the same instance");
        assertNull(int8.getBaseType(), "The method 'getBaseType()' is returning incorrect value");
        assertEquals(Optional.empty(),
            int8.getDefaultValue(),
            "The method 'getDefaultType()' is returning incorrect value");
    }
}
