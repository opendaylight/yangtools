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

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug6491Test {

    @Test
    public void testBug6491SchemaContext() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6491/");
        assertNotNull(context);
        final Module module = context.findModuleByName("bar", SimpleDateFormatUtil.DEFAULT_DATE_REV);
        assertNotNull(module);
        final Set<ModuleImport> imports = module.getImports();
        assertNotNull(imports);
        assertEquals(1, imports.size());
    }
}