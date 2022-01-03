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

public class OpenconfigVersionImportTest {
    private static final YangParserConfiguration SEMVER = YangParserConfiguration.builder()
        .importResolutionMode(ImportResolutionMode.OPENCONFIG_SEMVER)
        .build();

    @Test
    public void importValidTest() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/openconfig-version/import/import-valid", SEMVER);
        assertNotNull(context);

        Module semVer = context.findModules(XMLNamespace.of("http://openconfig.net/yang/openconfig-ext"))
            .iterator().next();

        assertEquals(SemVer.valueOf("1.0.0"), semVer.getSemanticVersion().get());
    }

    @Test
    public void importInvalidDeprecatedTest1() {
        ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/openconfig-version/import/import-invalid-deprecated-1", SEMVER));
        assertThat(ex.getCause().getMessage(), startsWith(
            "Unable to find module compatible with requested import [openconfig-extensions(1.0.0)]."));
    }

    @Test
    public void importInvalidDeprecatedTest2() {
        ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/openconfig-version/import/import-invalid-deprecated-2", SEMVER));
        assertThat(ex.getCause().getMessage(), startsWith(
            "Unable to find module compatible with requested import [openconfig-extensions(0.9.9)]."));
    }

    @Test
    public void importInvalidNotsufficientTest1() {
        ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/openconfig-version/import/import-invalid-notsufficient-1", SEMVER));
        assertThat(ex.getCause().getMessage(), startsWith(
            "Unable to find module compatible with requested import [openconfig-extensions(2.0.0)]."));
    }

    @Test
    public void importInvalidNotsufficientTest2() {
        ReactorException ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/openconfig-version/import/import-invalid-notsufficient-2", SEMVER));
        assertThat(ex.getCause().getMessage(), startsWith(
            "Unable to find module compatible with requested import [openconfig-extensions(2.0.5)]."));
    }
}
