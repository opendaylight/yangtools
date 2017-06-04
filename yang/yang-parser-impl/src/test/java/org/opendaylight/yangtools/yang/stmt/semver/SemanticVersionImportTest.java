/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.semver;

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

public class SemanticVersionImportTest {

    @Test
    public void importValidTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/semantic-version/import/import-valid",
                StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module semVer = context.findModuleByNamespace(new URI("urn:opendaylight:yang:extension:semantic-version"))
                .iterator().next();

        assertEquals(SemVer.valueOf("1.0.0"), semVer.getSemanticVersion());
    }

    @Test
    public void importInvalidDeprecatedTest1() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/semantic-version/import/import-invalid-deprecated-1",
                    StatementParserMode.SEMVER_MODE);
            fail("Test should fail due to invalid import of semantic-version module");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Unable to find module compatible with requested import " + "[semantic-version(1.0.0)]."));
        }
    }

    @Test
    public void importInvalidDeprecatedTest2() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/semantic-version/import/import-invalid-deprecated-2",
                    StatementParserMode.SEMVER_MODE);
            fail("Test should fail due to invalid import of semantic-version module");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Unable to find module compatible with requested import " + "[semantic-version(0.9.9)]."));
        }
    }

    @Test
    public void importInvalidNotsufficientTest1() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/semantic-version/import/import-invalid-notsufficient-1",
                    StatementParserMode.SEMVER_MODE);
            fail("Test should fail due to invalid import of semantic-version module");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Unable to find module compatible with requested import " + "[semantic-version(2.0.0)]."));
        }
    }

    @Test
    public void importInvalidNotsufficientTest2() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/semantic-version/import/import-invalid-notsufficient-2",
                    StatementParserMode.SEMVER_MODE);
            fail("Test should fail due to invalid import of semantic-version module");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Unable to find module compatible with requested import " + "[semantic-version(2.0.5)]."));
        }
    }
}
