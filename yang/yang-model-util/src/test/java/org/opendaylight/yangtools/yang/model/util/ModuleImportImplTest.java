/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.*;

import java.util.Date;

import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.junit.Before;
import org.junit.Test;

public class ModuleImportImplTest {

    private ModuleImport module1, module2, module3, module4, module5;
    private int hash1, hash2;
    private Date now;

    @Before
    public void setup() {
        now = new Date();
        module1 = new ModuleImportImpl("myModule", now, "myPrefix");
        module2 = new ModuleImportImpl(null, null, null);
        module3 = new ModuleImportImpl("myModule", null, "customPrefix");
        module4 = new ModuleImportImpl("myModule", now, null);
        module5 = new ModuleImportImpl("myModule", now, "myPrefix");
        hash1 = module1.hashCode();
        hash2 = module2.hashCode();
    }

    @Test
    public void testModule() {
        assertNotNull(module1);
        assertEquals(module1.getModuleName(), "myModule");
        assertEquals(module1.getPrefix(), "myPrefix");
        assertEquals(module1.getRevision(), now);
        assertNotEquals(module1, module2);
    }

    @Test
    public void testToString() {
        String toString = module1.toString();
        assertTrue(toString.contains("ModuleImport"));
    }

    @Test
    public void testHashCode() {
        assertFalse(hash1 == hash2);
    }

    @Test
    public void testEquals() {
        assertEquals(module1, module1);
        assertNotEquals(module1, module2);
        assertNotEquals(module1, "");
        assertNotEquals(module2, module1);
        assertNotEquals(module1, null);
        assertNotEquals(module1, module3);
        assertNotEquals(module3, module1);
        assertNotEquals(module1, module4);
        assertNotEquals(module4, module1);
        assertEquals(module1, module5);
    }

}
