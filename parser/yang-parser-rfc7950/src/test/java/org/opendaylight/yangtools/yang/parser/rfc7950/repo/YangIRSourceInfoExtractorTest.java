/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

class YangIRSourceInfoExtractorTest {
    @Test
    void testModuleWithNoImports() {
        final var info = forResource("/ietf/ietf-inet-types@2010-09-24.yang");
        assertEquals(new SourceIdentifier("ietf-inet-types", "2010-09-24"), info.sourceId());
        assertEquals(YangVersion.VERSION_1, info.yangVersion());
        assertEquals(Set.of(Revision.of("2010-09-24")), info.revisions());
        assertEquals(Set.of(), info.imports());
        assertEquals(Set.of(), info.includes());
    }

    @Test
    void testModuleWithImports() {
        final var info = forResource("/parse-methods/dependencies/m2@2013-09-30.yang");
        assertEquals(new SourceIdentifier("m2", "2013-09-30"), info.sourceId());
        assertEquals(YangVersion.VERSION_1, info.yangVersion());
        assertEquals(Set.of(Revision.of("2013-09-30")), info.revisions());
        assertEquals(Set.of(
            new Import(Unqualified.of("m4"), Unqualified.of("m4")),
            new Import(Unqualified.of("m5"), Unqualified.of("m5"))), info.imports());
        assertEquals(Set.of(), info.includes());
    }

    @Test
    void testModuleWithoutRevision() {
        final var info = forResource("/no-revision/module-without-revision.yang");
        assertEquals(new SourceIdentifier("module-without-revision"), info.sourceId());
        assertEquals(YangVersion.VERSION_1, info.yangVersion());
        assertEquals(Set.of(), info.revisions());
        assertEquals(Set.of(
            new Import(Unqualified.of("ietf-inet-types"), Unqualified.of("inet"), Revision.of("2010-09-24"))),
            info.imports());
        assertEquals(Set.of(), info.includes());
    }

    @Test
    void testYangtools827() {
        // Latest revision needs to be picked up irrespective of ordering
        final var info = forResource("/bugs/YT827/foo.yang");
        assertEquals(new SourceIdentifier("foo", "2014-12-24"), info.sourceId());
        assertEquals(YangVersion.VERSION_1, info.yangVersion());
        assertEquals(Set.of(Revision.of("2010-10-10"), Revision.of("2014-12-24")), info.revisions());
        assertEquals(Set.of(), info.imports());
        assertEquals(Set.of(), info.includes());
    }

    @Test
    void testMalformedImport() {
        final var ex = assertIAE("/depinfo-malformed/malformed-import.yang");
        assertEquals("Missing import argument at malformed-import:4:5", ex.getMessage());
    }

    @Test
    void testMalformedImportRev() {
        final var ex = assertIAE("/depinfo-malformed/malformed-import-rev.yang");
        assertEquals("Missing revision date argument at malformed-import-rev:4:18", ex.getMessage());
    }

    @Test
    void testMalformedModule() {
        final var ex = assertIAE("/depinfo-malformed/malformed-module.yang");
        assertEquals("Missing module/submodule argument at malformed-module:1:1", ex.getMessage());
    }

    @Test
    void testMalformedRev() {
        final var ex = assertIAE("/depinfo-malformed/malformed-rev.yang");
        assertEquals("Missing revision argument at malformed-rev:5:5", ex.getMessage());
    }

    private static IllegalArgumentException assertIAE(final String resourceName) {
        return assertThrows(IllegalArgumentException.class, () -> forResource(resourceName));
    }

    // Utility
    private static SourceInfo forResource(final String resourceName) {
        final var source = StmtTestUtils.sourceForResource(resourceName);
        final var info = YangIRSourceInfoExtractor.forIR(source.rootStatement(), source.getIdentifier());
        assertNotNull(info);
        return info;
    }
}
