/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.openconfigver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class OpenconfigVersionMultipleImportTest {

    @Test
    public void multipleInvalidDeprecatedTest() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/openconfig-version/multiple/multiple-invalid-deprecated",
                    StatementParserMode.OPENCONFIG_VER_MODE);
            fail("Test should fail due to invalid openconfig version");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage()
                    .startsWith("Unable to find module compatible with requested import [bar(1.0.0)]."));
        }
    }

    @Test
    public void multipleInvalidNosufficientTest() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/openconfig-version/multiple/multiple-invalid-nosufficient",
                    StatementParserMode.OPENCONFIG_VER_MODE);
            fail("Test should fail due to invalid openconfig version");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage()
                    .startsWith("Unable to find module compatible with requested import [bar(2.5.5)]."));
        }
    }

    @Test
    public void multipleValidDefaultsTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/multiple/multiple-valid-defaults",
                StatementParserMode.OPENCONFIG_VER_MODE);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("http://openconfig.net/yang/openconfig-ext"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getOpenconfigVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getOpenconfigVersion());
        Module bar = findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("0.9.5"), bar.getOpenconfigVersion());
    }

    @Test
    public void multipleValidSpecifiedTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/multiple/multiple-valid-specified",
                StatementParserMode.OPENCONFIG_VER_MODE);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("http://openconfig.net/yang/openconfig-ext"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getOpenconfigVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getOpenconfigVersion());
        Module bar = findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("5.5.6"), bar.getOpenconfigVersion());
    }

    private static Module findImportedModule(final SchemaContext context, final Module rootModule,
            final String importedModuleName) {
        ModuleImport requestedModuleImport = null;
        Set<ModuleImport> rootImports = rootModule.getImports();
        for (ModuleImport moduleImport : rootImports) {
            if (moduleImport.getModuleName().equals(importedModuleName)) {
                requestedModuleImport = moduleImport;
                break;
            }
        }

        Module importedModule = context.findModuleByName(requestedModuleImport.getModuleName(),
                requestedModuleImport.getRevision());
        return importedModule;
    }
}
