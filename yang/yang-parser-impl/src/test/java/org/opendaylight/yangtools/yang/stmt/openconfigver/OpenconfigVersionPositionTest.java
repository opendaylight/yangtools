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

import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class OpenconfigVersionPositionTest {

    @Test
    public void positionHeadTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/position/position-head",
                StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module bar = context.findModuleByNamespace(new URI("bar")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("http://openconfig.net/yang/openconfig-ext"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion().get());
    }

    @Test
    public void positionMiddleTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/position/position-middle",
                StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module bar = context.findModuleByNamespace(new URI("bar")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("http://openconfig.net/yang/openconfig-ext"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion().get());
    }

    @Test
    public void positiontailTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/position/position-tail",
                StatementParserMode.SEMVER_MODE);
        assertNotNull(context);

        Module foo = context.findModuleByNamespace(new URI("foo")).iterator().next();
        Module bar = context.findModuleByNamespace(new URI("bar")).iterator().next();
        Module semVer = context.findModuleByNamespace(new URI("http://openconfig.net/yang/openconfig-ext"))
                .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion().get());
    }
}
