/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.openconfigver.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;
import org.xml.sax.SAXException;

public class YinOpenconfigVersionTest {

    @Test
    public void basicTest() throws URISyntaxException, SAXException, IOException, ReactorException {
        SchemaContext context = StmtTestUtils.parseYinSources("/openconfig-version/yin-input/basic",
                StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(URI.create("foo")).iterator().next();
        Module bar = context.findModuleByNamespace(URI.create("bar")).iterator().next();
        Module semVer = context.findModuleByNamespace(URI.create("http://openconfig.net/yang/openconfig-ext"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion());
    }

    @Test
    public void basicImportTest1() throws URISyntaxException, SAXException, IOException, ReactorException {
        SchemaContext context = StmtTestUtils.parseYinSources("/openconfig-version/yin-input/basic-import",
                StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(URI.create("foo")).iterator().next();
        Module semVer = context.findModuleByNamespace(URI.create("http://openconfig.net/yang/openconfig-ext"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion());
        Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion());
    }

    @Test
    public void basicImportErrTest1() throws URISyntaxException, SAXException, IOException {
        try {
            StmtTestUtils.parseYinSources("/openconfig-version/yin-input/basic-import-invalid", StatementParserMode.SEMVER_MODE);
            fail("Test should fail due to invalid openconfig version");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage()
                    .startsWith("Unable to find module compatible with requested import [bar(0.1.2)]."));
        }
    }
}
