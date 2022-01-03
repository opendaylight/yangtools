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

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.api.ImportResolutionMode;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class OpenconfigVersionDefaultsTest {
    private static final YangParserConfiguration SEMVER = YangParserConfiguration.builder()
        .importResolutionMode(ImportResolutionMode.OPENCONFIG_SEMVER)
        .build();

    @Test
    public void defaultsTest() throws Exception {
        final var context = StmtTestUtils.parseYangSources("/openconfig-version/defaults/defaults", SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module bar = context.findModules(XMLNamespace.of("bar")).iterator().next();

        assertEquals(Optional.empty(), foo.getSemanticVersion());
        assertEquals(Optional.empty(), bar.getSemanticVersion());
    }

    @Test
    public void defaultMajorValidTest() throws Exception {
        final var context = StmtTestUtils.parseYangSources("/openconfig-version/defaults/default-major-valid",
            SEMVER);
        assertNotNull(context);

        Module foo = context.findModules(XMLNamespace.of("foo")).iterator().next();
        Module bar = context.findModules(XMLNamespace.of("bar")).iterator().next();

        assertEquals(Optional.empty(), foo.getSemanticVersion());
        assertEquals(SemVer.valueOf("0.99.99"), bar.getSemanticVersion().get());
    }

    @Test
    public void defaultMajorInvalidTest() throws Exception {
        final var ex = assertThrows(ReactorException.class,
            () -> StmtTestUtils.parseYangSources("/openconfig-version/defaults/default-major-invalid", SEMVER));
        assertThat(ex.getCause().getMessage(),
            startsWith("Unable to find module compatible with requested import [bar(0.0.1)]."));
    }
}
