/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ServiceLoader;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;

class YangIRSourceInfoExtractorTest {
    private static final YangTextToIRSourceTransformer TRANSFORMER =
        ServiceLoader.load(YangTextToIRSourceTransformer.class).findFirst().orElseThrow();

    @Test
    void testModuleWithNoImports() throws Exception {
        final var info = forResource("/ietf/ietf-inet-types@2010-09-24.yang");
        assertEquals(new SourceIdentifier("ietf-inet-types", "2010-09-24"), info.sourceId());
        assertEquals(YangVersion.VERSION_1, info.yangVersion());
        assertEquals(Set.of(Revision.of("2010-09-24")), info.revisions());
        assertEquals(Set.of(), info.imports());
        assertEquals(Set.of(), info.includes());
    }

    @Test
    void testModuleWithImports() throws Exception {
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
    void testModuleWithoutRevision() throws Exception {
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
    void testYangtools827() throws Exception {
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
        final var ex = assertEE("/depinfo-malformed/malformed-import.yang");
        assertEquals("Missing argument to import [at malformed-import:4:5]", ex.getMessage());
    }

    @Test
    void testMalformedImportRev() {
        final var ex = assertEE("/depinfo-malformed/malformed-import-rev.yang");
        assertEquals("Missing argument to revision-date [at malformed-import-rev:4:18]", ex.getMessage());
    }

    @Test
    void testMalformedModule() {
        final var ex = assertEE("/depinfo-malformed/malformed-module.yang");
        assertEquals("Missing argument to module [at malformed-module:1:1]", ex.getMessage());
    }

    @Test
    void testMalformedModuleArg() {
        final var ex = assertEE("/depinfo-malformed/malformed-module-arg.yang");
        assertEquals(
            "Invalid argument to module: String '0123' is not a valid identifier [at malformed-module-arg:1:1]",
            ex.getMessage());
    }

    @Test
    void testMalformedRev() {
        final var ex = assertEE("/depinfo-malformed/malformed-rev.yang");
        assertEquals("Missing argument to revision [at malformed-rev:5:5]", ex.getMessage());
    }

    @Test
    void testMalformedRevArg() {
        final var ex = assertEE("/depinfo-malformed/malformed-rev-arg.yang");
        assertEquals(
            "Invalid argument to revision: Text 'bad' could not be parsed at index 0 [at malformed-rev-arg:5:5]",
            ex.getMessage());
    }

    private static ExtractorException assertEE(final String resourceName) {
        return assertThrows(ExtractorException.class, () -> forResource(resourceName));
    }

    // Utility
    private static SourceInfo forResource(final String resourceName) throws Exception {
        return TRANSFORMER
            .transformSource(new URLYangTextSource(YangIRSourceInfoExtractorTest.class.getResource(resourceName)))
            .extractSourceInfo();
    }
}
