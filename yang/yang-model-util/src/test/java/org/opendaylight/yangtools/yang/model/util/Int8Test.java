/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

public class Int8Test {

    @Test
    public void testInt8() {
        IntegerTypeDefinition int8 = BaseTypes.int8Type();
        IntegerTypeDefinition int8Second = BaseTypes.int8Type();
        assertTrue("The method 'getInstance()' has to return the same instance", int8 == int8Second);
        assertTrue("The method 'getBaseType()' is returning incorrect value", int8.getBaseType() == null);
        assertTrue("The method 'getDefaultType()' is returning incorrect value", int8.getDefaultValue() == null);
    }
}
