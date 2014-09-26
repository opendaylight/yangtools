/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;

public class ModuleImportImplTest {

    @Test
    public void testMethodsOfModuleImportImpl() throws ParseException {
        final SimpleDateFormat dateFormat = SimpleDateFormatUtil.getRevisionFormat();
        final String stringDate = "2014-09-26";
        final Date revision = dateFormat.parse(stringDate);
        final ModuleImportImpl moduleImportImpl = new ModuleImportImpl("test-module", revision, "test");
        final ModuleImportImpl moduleImportImpl2 = new ModuleImportImpl("test-module2", revision, "test");
        final ModuleImportImpl moduleImportImpl3 = new ModuleImportImpl("test-module", revision, "test");
        final ModuleImportImpl moduleImportImpl4 = moduleImportImpl;
        final ModuleImportImpl moduleImportImpl5 = new ModuleImportImpl(null, revision, "test");
        final ModuleImportImpl moduleImportImpl6 = new ModuleImportImpl("test-module", null, "test");
        final ModuleImportImpl moduleImportImpl7 = new ModuleImportImpl("test-module", revision, null);

        assertNotNull("Object 'moduleImportImpl' shouldn't be null.", moduleImportImpl);
        assertEquals("Object 'moduleImportImpl' should has moduleName 'test-module'.", "test-module", moduleImportImpl.getModuleName());
        assertEquals("Object 'moduleImportImpl' should has revision '2014-09-26'.", revision, moduleImportImpl.getRevision());
        assertEquals("Object 'moduleImportImpl' should has prefix 'test'.", "test", moduleImportImpl.getPrefix());
        assertNotNull("String representation of 'moduleImportImpl' object shouldn't be null.", moduleImportImpl.toString());

        assertNotEquals("Objects shouldn't have equals hash codes.", moduleImportImpl.hashCode(), moduleImportImpl2.hashCode());

        assertEquals("Objects should be equals.", moduleImportImpl, moduleImportImpl3);
        assertEquals("Objects should be equals.", moduleImportImpl, moduleImportImpl3);
        assertEquals("Objects should be equals.", moduleImportImpl, moduleImportImpl4);
        assertNotEquals("Objects shouldn't be equals.", moduleImportImpl, moduleImportImpl2);
        assertNotEquals("Objects shouldn't be equals.", moduleImportImpl, "test");
        assertNotEquals("Objects shouldn't be equals.", moduleImportImpl, null);
        assertNotEquals("Objects shouldn't be equals.", moduleImportImpl5, moduleImportImpl);
        assertNotEquals("Objects shouldn't be equals.", moduleImportImpl6, moduleImportImpl);
        assertNotEquals("Objects shouldn't be equals.", moduleImportImpl7, moduleImportImpl);
    }
}
