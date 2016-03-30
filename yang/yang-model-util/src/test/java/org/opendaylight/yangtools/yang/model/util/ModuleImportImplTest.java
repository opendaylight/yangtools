/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;

public class ModuleImportImplTest {

    private ModuleImport module1, module2, module3, module4, module5;
    private int hash1, hash2;
    private Date now;

    @Before
    public void setup() {
        now = new Date();
        module1 = new ModuleImportImpl("myModule", now, "myPrefix");
        module2 = new ModuleImportImpl("foo", null, "prefix-foo");
        module3 = new ModuleImportImpl("myModule", null, "customPrefix");
        module4 = new ModuleImportImpl("myModule", now, "prefix");
        module5 = new ModuleImportImpl("myModule", now, "myPrefix");
        hash1 = module1.hashCode();
        hash2 = module2.hashCode();
    }

    @Test
    public void testModule() {
        assertNotNull(module1);
        assertTrue(module1.getModuleName().equals("myModule"));
        assertTrue(module1.getPrefix().equals("myPrefix"));
        assertTrue(module1.getRevision().equals(now));
        assertFalse(module1.equals(module2));
    }

    @Test
    public void testToString() {
        String toString = module1.toString();
        assertTrue(toString.contains("ModuleImport"));
    }

    @Test
    public void testHashCode() {
        assertTrue(!(hash1 == hash2));
    }

    @Test
    public void testEquals() {
        assertTrue(module1.equals(module1));
        assertFalse(module1.equals(module2));
        assertFalse(module1.equals(""));
        assertFalse(module2.equals(module1));
        assertFalse(module1.equals(null));
        assertFalse(module1.equals(module3));
        assertFalse(module3.equals(module1));
        assertFalse(module1.equals(module4));
        assertFalse(module4.equals(module1));
        assertTrue(module1.equals(module5));
    }

}
