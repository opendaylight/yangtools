/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug9005Test {
    @Test
    public void test() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug9005");
        assertNotNull(context);

        final Module foo = context.findModuleByName("foo",
                SimpleDateFormatUtil.getRevisionFormat().parse("2017-07-07"));

        final Set<ModuleImport> imports = foo.getImports();
        assertEquals(1, imports.size());
        final ModuleImport imp1 = imports.iterator().next();
        assertEquals("bar-2", imp1.getModuleName());
        assertEquals("bar", imp1.getPrefix());
        assertEquals(SimpleDateFormatUtil.getRevisionFormat().parse("2000-01-02"), imp1.getRevision());

        final Set<Module> submodules = foo.getSubmodules();
        assertEquals(1, submodules.size());
        final Module submodule = submodules.iterator().next();
        final Set<ModuleImport> subImports = submodule.getImports();

        assertEquals(1, subImports.size());
        final ModuleImport subImp1 = subImports.iterator().next();
        assertEquals("bar-1", subImp1.getModuleName());
        assertEquals("bar", subImp1.getPrefix());
        assertEquals(SimpleDateFormatUtil.getRevisionFormat().parse("2000-01-01"), subImp1.getRevision());
    }
}
