/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Status;

public class StringTypeTest {

    @Test
    public void testMethodsOfStringType() {
        final StringType stringType = StringType.getInstance();
        final StringType anotherStringType = stringType;

        assertNotNull("Object of type StringType shouldn't be null.", stringType);
        assertNull("Base type should be null.", stringType.getBaseType());
        assertTrue("Units should be empty.", stringType.getUnits().isEmpty());
        assertTrue("Default value should be empty string.", String.valueOf(stringType.getDefaultValue()).isEmpty());
        assertNotNull("QName shouldn't be null.", stringType.getQName());
        assertTrue("Reference should be empty.", stringType.getReference().isEmpty());
        assertTrue("Description should be empty.", stringType.getDescription().isEmpty());
        assertNotNull("Schema path shouldn't be null.", stringType.getPath());
        assertEquals("Status should be current.", Status.CURRENT, stringType.getStatus());
        assertFalse("Length constraints shouldn't be empty.", stringType.getLengthConstraints().isEmpty());
        assertTrue("Pattern constraints should be empty.", stringType.getPatternConstraints().isEmpty());
        assertTrue("Unknown schema nodes should be empty.", stringType.getUnknownSchemaNodes().isEmpty());
        assertEquals("Hash codes should be equals.", stringType.hashCode(), anotherStringType.hashCode());
        assertTrue("Objects should be equals.", stringType.equals(anotherStringType));
        assertFalse("Objects shouldn't be equals.", stringType.equals("test"));
        assertFalse("Objects shouldn't be equals.", stringType.equals(null));
        assertNotNull("String representation shouldn't be null.", stringType.toString());
    }
}
