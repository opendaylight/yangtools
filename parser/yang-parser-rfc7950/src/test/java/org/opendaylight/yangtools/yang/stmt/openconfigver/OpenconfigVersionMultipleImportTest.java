/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.openconfigver;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.api.ImportResolutionMode;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class OpenconfigVersionMultipleImportTest {
    private static final YangParserConfiguration SEMVER = YangParserConfiguration.builder()
        .importResolutionMode(ImportResolutionMode.OPENCONFIG_SEMVER)
        .build();

    @Test
    public void multipleInvalidDeprecatedTest() {
        ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/openconfig-version/multiple/multiple-invalid-deprecated", SEMVER));
        assertThat(ex.getCause().getMessage(),
            startsWith("Unable to find module compatible with requested import [bar(1.0.0)]."));
    }

    @Test
    public void multipleInvalidNosufficientTest() {
        ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/openconfig-version/multiple/multiple-invalid-nosufficient", SEMVER));
        assertThat(ex.getCause().getMessage(),
            startsWith("Unable to find module compatible with requested import [bar(2.5.5)]."));
    }

    @Test
    public void multipleValidDefaultsTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/multiple/multiple-valid-defaults",
            SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        Module bar = findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("0.9.5"), bar.getSemanticVersion().get());
    }

    @Test
    public void multipleValidSpecifiedTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/multiple/multiple-valid-specified",
            SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        Module bar = findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("5.5.6"), bar.getSemanticVersion().get());
    }

    private static Module findImportedModule(final SchemaContext context, final Module rootModule,
            final String importedModuleName) {
        ModuleImport requestedModuleImport = null;
        for (ModuleImport moduleImport : rootModule.getImports()) {
            if (moduleImport.getModuleName().equals(importedModuleName)) {
                requestedModuleImport = moduleImport;
                break;
            }
        }

        return context.findModule(requestedModuleImport.getModuleName(), requestedModuleImport.getRevision()).get();
    }
}
