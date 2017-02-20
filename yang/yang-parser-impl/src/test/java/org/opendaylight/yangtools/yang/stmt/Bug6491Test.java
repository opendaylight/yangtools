/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil.DEFAULT_DATE_REV;
import static org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil.getRevisionFormat;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug6491Test {
    private Date date;

    @Before
    public void setup() throws ParseException {
        date = getRevisionFormat().parse("2016-01-01");
    }

    @Test
    public void tetststs() throws Exception {
        testRevision("withoutRevision", DEFAULT_DATE_REV, DEFAULT_DATE_REV);
        testRevision("withRevision", date, date);
        testRevision("importedModuleRevisionOnly", DEFAULT_DATE_REV, date);
        testRevision("moduleRevisionOnly", date, DEFAULT_DATE_REV);
    }

    private static void testRevision(final String path, final Date moduleRevision, final Date importedRevision)
            throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6491/".concat(path));
        assertNotNull(context);
        final Module module = context.findModuleByName("bar", moduleRevision);
        assertNotNull(module);
        final Set<ModuleImport> imports = module.getImports();
        assertNotNull(imports);
        assertEquals(1, imports.size());
        assertEquals(importedRevision, imports.iterator().next().getRevision());
    }
}