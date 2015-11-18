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

import java.sql.Types;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;

public class StringTypeTest {

    private StringType string;
    private int hash;

    @Before
    public void setup() {
        string = StringType.getInstance();
        hash = string.hashCode();
    }

    @Test
    public void testGetBaseTypeShouldReturnNull() {
        assertTrue(string.getBaseType() == null);
    }

    @Test
    public void testGetters() {
        assertEquals(string.getUnits(), "");
        assertNull(string.getDefaultValue());
        assertEquals(string.getDescription(), "");
        assertEquals(string.getReference(), "");
        assertEquals(string.getQName(), BaseTypes.STRING_QNAME);
        assertEquals(string.getStatus(), Status.CURRENT);

        SchemaPath path = SchemaPath.create(true, string.getQName());
        assertEquals(string.getPath(), path);

        assertNotNull(string.getLengthConstraints());
        assertNotNull(string.getPatternConstraints());
        assertNotNull(string.getUnknownSchemaNodes());
    }

    @Test
    public void testToString() {
        String toString = string.toString();
        assertTrue(toString.contains("StringType"));
    }

    @Test
    public void testEquals() {
        assertTrue(string.equals(string));
        assertFalse(string.equals(null));
        assertFalse(string.equals(Types.DOUBLE));
    }
}
