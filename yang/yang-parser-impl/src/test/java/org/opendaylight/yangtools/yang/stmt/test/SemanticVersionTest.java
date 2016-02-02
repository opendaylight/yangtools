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
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class SemanticVersionTest {
    @Test
    public void basicTest() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/semantic-version/basic");
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module bar = context.findModuleByNamespace(new URI("bar")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("urn:opendaylight:yang:extension:semantic-version"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion());
    }

    @Test
    public void basicTest2() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/semantic-version/basic-2");
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module bar = context.findModuleByNamespace(new URI("bar")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("urn:opendaylight:yang:extension:semantic-version"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion());
    }

    @Test
    public void basicTest3() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/semantic-version/basic-3");
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("urn:opendaylight:yang:extension:semantic-version"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion());
    }

    @Test
    public void basicImportTest1() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/semantic-version/basic-import-1");
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module bar = context.findModuleByNamespace(new URI("bar")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("urn:opendaylight:yang:extension:semantic-version"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion());
    }

    @Test
    public void basicImportErrTest1() {
        try {
            StmtTestUtils.parseYangSources("/semantic-version/basic-import-invalid-1");
            fail("Test should fail due to invalid semantic version");
        } catch (Exception e) {
            // :TODO verify Exception
        }
    }

    @Test
    public void basicImportErrTest2() {
        try {
            StmtTestUtils.parseYangSources("/semantic-version/basic-import-invalid-2");
            fail("Test should fail due to invalid semantic version");
        } catch (Exception e) {
            // :TODO verify Exception
        }
    }
}
