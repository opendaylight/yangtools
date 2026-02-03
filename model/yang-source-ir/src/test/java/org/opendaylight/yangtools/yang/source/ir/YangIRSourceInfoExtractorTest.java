/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.source.ir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.StringYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.source.ir.dagger.YangIRSourceModule;

class YangIRSourceInfoExtractorTest {
    @Test
    void testModuleWithNoImports() throws Exception {
        final var info = forResource("/ietf-inet-types.yang");
        assertEquals(new SourceIdentifier("ietf-inet-types", "2010-09-24"), info.sourceId());
        assertEquals(YangVersion.VERSION_1, info.yangVersion());
        assertEquals(Set.of(Revision.of("2010-09-24")), info.revisions());
        assertEquals(Set.of(), info.imports());
        assertEquals(Set.of(), info.includes());
    }

    @Test
    void testModuleWithImports() throws Exception {
        final var info = forResource("/m2@2013-09-30.yang");
        assertEquals(new SourceIdentifier("m2", "2013-09-30"), info.sourceId());
        assertEquals(YangVersion.VERSION_1, info.yangVersion());
        assertEquals(Set.of(Revision.of("2013-09-30")), info.revisions());

        final var imports = info.imports();
        assertEquals(2, imports.size());
        for (var imp : imports) {
            switch (imp.name().getLocalName()) {
                case "m4" -> {
                    assertEquals(Unqualified.of("m4"), imp.prefix());
                    assertNull(imp.revision());
                    final var sourceRef = imp.sourceRef();
                    assertNotNull(sourceRef);
                    assertEquals("m2:7:5", sourceRef.toString());
                }
                case "m5" -> {
                    assertEquals(Unqualified.of("m5"), imp.prefix());
                    assertNull(imp.revision());
                    final var sourceRef = imp.sourceRef();
                    assertNotNull(sourceRef);
                    assertEquals("m2:10:5", sourceRef.toString());
                }
                default -> throw new AssertionError();
            }
        }

        assertEquals(Set.of(), info.includes());
    }

    @Test
    void testModuleWithoutRevision() throws Exception {
        final var info = forResource("/module-without-revision.yang");
        assertEquals(new SourceIdentifier("module-without-revision"), info.sourceId());
        assertEquals(YangVersion.VERSION_1, info.yangVersion());
        assertEquals(Set.of(), info.revisions());

        final var imports = info.imports();
        assertThat(imports).hasSize(1).first().satisfies(imp -> {
            assertEquals(Unqualified.of("ietf-inet-types"), imp.name());
            assertEquals(Unqualified.of("inet"), imp.prefix());
            assertEquals(Revision.of("2010-09-24"), imp.revision());
            final var sourceRef = imp.sourceRef();
            assertNotNull(sourceRef);
            assertEquals("module-without-revision:7:5", sourceRef.toString());
        });

        assertEquals(Set.of(), info.includes());
    }

    @Test
    void testMalformedImport() {
        assertSSE("Missing argument to import [at dummy:4:3]", """
            module malformed-import {
              namespace "urn:test:foo";
              prefix aug;
              import;
            }""");
    }

    @Test
    void testMalformedImportRev() {
        assertSSE("Missing argument to revision-date [at dummy:4:16]", """
            module malformed-import-rev {
              namespace "urn:test:foo";
              prefix aug;
              import foo { revision-date; prefix foo; }
            }""");
    }

    @Test
    void testMalformedModule() {
        assertEquals("Root statement does not have an argument [at dummy:1:1]",
            assertThrows(SourceSyntaxException.class, () -> forText("""
                module {
                  namespace "urn:test:foo";
                  prefix aug;
                }""")).getMessage());
    }

    @Test
    void testMalformedModuleArg() {
        assertSSE("Invalid argument to module: String '0123' is not a valid identifier [at dummy:1:1]", """
            module 0123 {
              namespace "urn:test:foo";
              prefix aug;
            }""");
    }

    @Test
    void testMalformedRev() {
        assertSSE("Missing argument to revision [at dummy:5:3]", """
            module malformed-import {
              namespace "urn:test:opendaylight-mdsal45-aug";
              prefix aug;

              revision;
            }""");
    }

    @Test
    void testMalformedRevArg() {
        assertSSE("Invalid argument to revision: Text 'bad' could not be parsed at index 0 [at dummy:5:3]",
            """
            module malformed-rev-arg {
              namespace "urn:test:opendaylight-mdsal45-aug";
              prefix aug;

              revision bad;
            }""");
    }

    @Test
    void testYT827() throws Exception {
        // Latest revision needs to be picked up irrespective of ordering
        final var info = forText("""
            module foo {
              namespace foo;
              prefix foo;

              revision "2010-10-10";

              revision "2014-12-24";
            }""");
        assertEquals(new SourceIdentifier("foo", "2014-12-24"), info.sourceId());
        assertEquals(YangVersion.VERSION_1, info.yangVersion());
        assertEquals(Set.of(Revision.of("2010-10-10"), Revision.of("2014-12-24")), info.revisions());
        assertEquals(Set.of(), info.imports());
        assertEquals(Set.of(), info.includes());
    }

    private static void assertSSE(final String message, final String text) {
        assertEquals(message, assertThrows(SourceSyntaxException.class, () -> forText(text)).getMessage());
    }

    // Utility
    private static SourceInfo forResource(final String resourceName) throws Exception {
        return forText(new URLYangTextSource(YangIRSourceInfoExtractorTest.class.getResource(resourceName)));
    }

    private static SourceInfo forText(final String text) throws Exception {
        return forText(new StringYangTextSource(new SourceIdentifier("dummy"), text, "dummyHuman"));
    }

    @NonNullByDefault
    private static SourceInfo forText(final YangTextSource source) throws Exception {
        return YangIRSourceModule.provideTextToIR().transformSource(source).extractSourceInfo();
    }
}
