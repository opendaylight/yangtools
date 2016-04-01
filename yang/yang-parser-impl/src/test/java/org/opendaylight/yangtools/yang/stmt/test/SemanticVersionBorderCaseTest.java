/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class SemanticVersionBorderCaseTest {

    @Test
    public void borderCaseValidMajorTest() throws SourceException, FileNotFoundException, ReactorException,
            URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/semantic-version/border-case/border-case-valid-major",
                true);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("urn:opendaylight:yang:extension:semantic-version"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion());
        Module bar = findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("5.5.5"), bar.getSemanticVersion());
    }

    @Test
    public void borderCaseValidMinorTest() throws SourceException, FileNotFoundException, ReactorException,
            URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/semantic-version/border-case/border-case-valid-minor",
                true);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("urn:opendaylight:yang:extension:semantic-version"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion());
        Module bar = findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("5.6.5"), bar.getSemanticVersion());
    }

    @Test
    public void borderCaseValidPatchTest() throws SourceException, FileNotFoundException, ReactorException,
            URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/semantic-version/border-case/border-case-valid-patch",
                true);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("urn:opendaylight:yang:extension:semantic-version"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion());
        Module bar = findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("5.5.6"), bar.getSemanticVersion());
    }

    @Test
    public void borderCaseInvalidMajorTest() throws SourceException, FileNotFoundException, ReactorException,
            URISyntaxException {
        try {
            StmtTestUtils.parseYangSources("/semantic-version/border-case/border-case-invalid-major", true);
            fail("Test should fail due to invalid semantic version");
        } catch (InferenceException e) {
            assertTrue(e.getMessage()
                    .startsWith("Unable to find module compatible with requested import [bar(5.5.5)]."));
        }
    }

    @Test
    public void borderCaseInvalidMinorTest() throws SourceException, FileNotFoundException, ReactorException,
            URISyntaxException {
        try {
            StmtTestUtils.parseYangSources("/semantic-version/border-case/border-case-invalid-minor", true);
            fail("Test should fail due to invalid semantic version");
        } catch (InferenceException e) {
            assertTrue(e.getMessage()
                    .startsWith("Unable to find module compatible with requested import [bar(5.5.5)]."));
        }
    }

    @Test
    public void borderCaseInvalidPatchTest() throws SourceException, FileNotFoundException, ReactorException,
            URISyntaxException {
        try {
            StmtTestUtils.parseYangSources("/semantic-version/border-case/border-case-invalid-patch", true);
            fail("Test should fail due to invalid semantic version");
        } catch (InferenceException e) {
            assertTrue(e.getMessage()
                    .startsWith("Unable to find module compatible with requested import [bar(5.5.5)]."));
        }
    }

    private static Module findImportedModule(SchemaContext context, Module rootModule, String importedModuleName) {
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
