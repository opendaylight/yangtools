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
import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class OpenconfigVersionBorderCaseTest {

    @Test
    public void borderCaseValidMajorTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/border-case/border-case-valid-major",
                StatementParserMode.OPENCONFIG_VER_MODE);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("http://openconfig.net/yang/openconfig-ext"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getOpenconfigVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getOpenconfigVersion());
        Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("5.5.5"), bar.getOpenconfigVersion());
    }

    @Test
    public void borderCaseValidMinorTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/border-case/border-case-valid-minor",
                StatementParserMode.OPENCONFIG_VER_MODE);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("http://openconfig.net/yang/openconfig-ext"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getOpenconfigVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getOpenconfigVersion());
        Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("5.6.5"), bar.getOpenconfigVersion());
    }

    @Test
    public void borderCaseValidPatchTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/border-case/border-case-valid-patch",
                StatementParserMode.OPENCONFIG_VER_MODE);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("http://openconfig.net/yang/openconfig-ext"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getOpenconfigVersion());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getOpenconfigVersion());
        Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("5.5.6"), bar.getOpenconfigVersion());
    }

    @Test
    public void borderCaseInvalidMajorTest() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/openconfig-version/border-case/border-case-invalid-major",
                    StatementParserMode.OPENCONFIG_VER_MODE);
            fail("Test should fail due to invalid openconfig version");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage()
                    .startsWith("Unable to find module compatible with requested import [bar(5.5.5)]."));
        }
    }

    @Test
    public void borderCaseInvalidMinorTest() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/openconfig-version/border-case/border-case-invalid-minor",
                    StatementParserMode.OPENCONFIG_VER_MODE);
            fail("Test should fail due to invalid openconfig version");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage()
                    .startsWith("Unable to find module compatible with requested import [bar(5.5.5)]."));
        }
    }

    @Test
    public void borderCaseInvalidPatchTest() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/openconfig-version/border-case/border-case-invalid-patch",
                    StatementParserMode.OPENCONFIG_VER_MODE);
            fail("Test should fail due to invalid openconfig version");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage()
                    .startsWith("Unable to find module compatible with requested import [bar(5.5.5)]."));
        }
    }
}
