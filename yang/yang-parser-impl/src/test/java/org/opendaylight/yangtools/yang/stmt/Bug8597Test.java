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
import static org.junit.Assert.fail;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug8597Test {
    @Test
    public void test() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug8597");
        assertNotNull(context);

        final Module foo = context.findModuleByName("foo", QName.parseRevision("1970-01-01"));
        assertNotNull(foo);

        final Set<ModuleImport> imports = foo.getImports();

        for (final ModuleImport moduleImport : imports) {
            switch (moduleImport.getModuleName()) {
                case "bar":
                    assertEquals(QName.parseRevision("1970-01-01"), moduleImport.getRevision());
                    assertEquals("bar-ref", moduleImport.getReference());
                    assertEquals("bar-desc", moduleImport.getDescription());
                    break;
                case "baz":
                    assertEquals(QName.parseRevision("2010-10-10"), moduleImport.getRevision());
                    assertEquals("baz-ref", moduleImport.getReference());
                    assertEquals("baz-desc", moduleImport.getDescription());
                    break;
                default:
                    fail("Module 'foo' should only contains import of module 'bar' and 'baz'");
            }
        }
    }
}
