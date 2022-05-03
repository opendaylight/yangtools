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

import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.api.ImportResolutionMode;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

@Deprecated(since = "8.0.4", forRemoval = true)
public class OpenconfigVersionPositionTest {
    private static final YangParserConfiguration SEMVER = YangParserConfiguration.builder()
        .importResolutionMode(ImportResolutionMode.OPENCONFIG_SEMVER)
        .build();

    @Test
    public void positionHeadTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/position/position-head", SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module bar = context.findModules(XMLNamespace.of("bar")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion().get());
    }

    @Test
    public void positionMiddleTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/position/position-middle", SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module bar = context.findModules(XMLNamespace.of("bar")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion().get());
    }

    @Test
    public void positiontailTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/position/position-tail", SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module bar = context.findModules(XMLNamespace.of("bar")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.2"), bar.getSemanticVersion().get());
    }
}
