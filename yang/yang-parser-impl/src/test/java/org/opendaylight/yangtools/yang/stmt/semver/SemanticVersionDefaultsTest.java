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

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class SemanticVersionDefaultsTest {

    @Test
    public void defaultsTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/semantic-version/defaults/defaults",
                StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module bar = context.findModuleByNamespace(new URI("bar")).iterator().next();

        assertEquals(SemVer.valueOf("0.0.0"), foo.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.0.0"), bar.getSemanticVersion());
    }

    @Test
    public void defaultMajorValidTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/semantic-version/defaults/default-major-valid",
                StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module bar = context.findModuleByNamespace(new URI("bar")).iterator().next();

        assertEquals(SemVer.valueOf("0.0.0"), foo.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.99.99"), bar.getSemanticVersion());
    }

    @Test
    public void defaultMajorInvalidTest() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/semantic-version/defaults/default-major-invalid",
                StatementParserMode.SEMVER_MODE);
            fail("Test should fail due to invalid semantic version");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage()
                    .startsWith("Unable to find module compatible with requested import [bar(0.0.0)]."));
        }
    }
}
