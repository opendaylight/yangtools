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
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.api.ImportResolutionMode;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class OpenconfigVersionBorderCaseTest {
    private static final YangParserConfiguration SEMVER = YangParserConfiguration.builder()
        .importResolutionMode(ImportResolutionMode.OPENCONFIG_SEMVER)
        .build();

    @Test
    public void borderCaseValidMajorTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources(
            "/openconfig-version/border-case/border-case-valid-major", SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("5.5.5"), bar.getSemanticVersion().get());
    }

    @Test
    public void borderCaseValidMinorTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources(
            "/openconfig-version/border-case/border-case-valid-minor", SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("5.6.5"), bar.getSemanticVersion().get());
    }

    @Test
    public void borderCaseValidPatchTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources(
            "/openconfig-version/border-case/border-case-valid-patch", SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("0.0.1"), semVer.getSemanticVersion().get());
        assertEquals(SemVer.valueOf("0.1.1"), foo.getSemanticVersion().get());
        Module bar = StmtTestUtils.findImportedModule(context, foo, "bar");
        assertEquals(SemVer.valueOf("5.5.6"), bar.getSemanticVersion().get());
    }

    @Test
    public void borderCaseInvalidMajorTest() throws Exception {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/openconfig-version/border-case/border-case-invalid-major", SEMVER));
        assertThat(ex.getCause().getMessage(),
            startsWith("Unable to find module compatible with requested import [bar(5.5.5)]."));
    }

    @Test
    public void borderCaseInvalidMinorTest() {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/openconfig-version/border-case/border-case-invalid-minor", SEMVER));
        assertThat(ex.getCause().getMessage(),
            startsWith("Unable to find module compatible with requested import [bar(5.5.5)]."));
    }

    @Test
    public void borderCaseInvalidPatchTest() throws Exception {
        final ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/openconfig-version/border-case/border-case-invalid-patch", SEMVER));
        assertThat(ex.getCause().getMessage(),
            startsWith("Unable to find module compatible with requested import [bar(5.5.5)]."));
    }
}
