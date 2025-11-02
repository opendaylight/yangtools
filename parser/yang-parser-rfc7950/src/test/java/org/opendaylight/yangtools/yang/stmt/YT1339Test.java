/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.YangVersion;

/**
 * Tests for {@code MUST NOT} statements around include/import interop of RFC6020 and RFC7950, as per
 * <a href="https://www.rfc-editor.org/rfc/rfc7950#section-12">RFC7950 section 12</a>.
 */
class YT1339Test extends AbstractYangTest {
    @Test
    void testInclude() {
        // A YANG version 1.1 module MUST NOT include a YANG version 1 submodule,
        assertFailedInclude("old-sub", YangVersion.VERSION_1, YangVersion.VERSION_1_1);
        // ... and a YANG version 1 module MUST NOT include a YANG version 1.1 submodule
        assertFailedInclude("new-sub", YangVersion.VERSION_1_1, YangVersion.VERSION_1);
    }

    @Test
    void testImportNewByRev() {
        // A YANG version 1 module or submodule MUST NOT import a YANG version 1.1 module by revision.
        assertFailedImport("import-rev");
        assertFailedImport("import-rev-sub");
    }

    @Test
    void testImportOldByRev() {
        // A YANG version 1.1 module or submodule MAY import a YANG version 1 module by revision.
        assertEffectiveModelDir("/bugs/YT1339/import");
    }

    @Test
    void testImportNoRev() {
        // no language forbidding imports without revision
        assertEffectiveModelDir("/bugs/YT1339/import-norev");
    }

    private static void assertFailedImport(final String subdir) {
        assertThat(assertYangVersionLinkageException(subdir))
            .startsWith("Cannot import by revision version [1.1] module [new] at");
    }

    private static void assertFailedInclude(final String subdir, final YangVersion subVer, final YangVersion modVer) {
        assertThat(assertYangVersionLinkageException(subdir))
            .startsWith("Cannot include a version [" + subVer + "] submodule")
            .contains("in a version [" + modVer + "] module");
    }

    private static String assertYangVersionLinkageException(final String subdir) {
        return assertExceptionDir("/bugs/YT1339/" + subdir, IllegalStateException.class).getMessage();
    }
}
