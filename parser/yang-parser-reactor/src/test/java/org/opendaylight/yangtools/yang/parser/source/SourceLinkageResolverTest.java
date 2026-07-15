/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

@NonNullByDefault
class SourceLinkageResolverTest {
    private static final Unqualified FOO = Unqualified.of("foo");
    private static final Unqualified FOO_SUB = Unqualified.of("foo-sub");
    private static final Unqualified BAR = Unqualified.of("bar");
    private static final Unqualified BAZ = Unqualified.of("baz");

    // module foo {
    //   namespace "urn:foo";
    //   prefix foo;
    //
    //   include foo-sub;
    // }
    private static final SourceInfo.Module FOO_INFO = SourceInfo.Module.builder()
        .setName(FOO)
        .setNamespace(XMLNamespace.of("foo"))
        .setPrefix(FOO)
        .addInclude(new Include(FOO_SUB, null))
        .build();
    // submodule foo-sub {
    //   belongs-to foo {
    //     prefix foo;
    //   }
    // }
    private static final SourceInfo.Submodule FOO_SUB_INFO = SourceInfo.Submodule.builder()
        .setName(FOO_SUB)
        .setBelongsTo(new BelongsTo(FOO, FOO, null))
        .build();
    // module bar {
    //   namespace "urn:bar";
    //   prefix bar;
    // }
    private static final SourceInfo.Module BAR_INFO = SourceInfo.Module.builder()
        .setName(BAR)
        .setNamespace(XMLNamespace.of("urn:bar"))
        .setPrefix(BAR)
        .build();
    // module baz {
    //   namespace "urn:baz";
    //   prefix baz;
    //   import foo {
    //     prefix foo;
    //   }
    // }
    private static final SourceInfo.Module BAZ_INFO = SourceInfo.Module.builder()
        .setName(BAZ)
        .setNamespace(XMLNamespace.of("urn:baz"))
        .setPrefix(BAZ)
        .addImport(new Import(FOO, FOO, null, null))
        .build();

    @Test
    void emptySourcesResolvesToEmpty() throws Exception {
        assertEquals(List.of(), SourceLinkageResolver.resolveInvolvedSources(Set.of(), Set.of()));
        assertEquals(List.of(), SourceLinkageResolver.resolveInvolvedSources(Set.of(), Set.of(BAZ_INFO.newRef())));
    }

    @Test
    void unreferencedConsistent() throws Exception {
        assertOnlyBarResolved(FOO_INFO, FOO_SUB_INFO);
    }

    @Test
    void unreferencedWithoutSubmodule() throws Exception {
        assertOnlyBarResolved(FOO_INFO);
    }

    @Test
    void unreferencedWithoutModule() throws Exception {
        assertOnlyBarResolved(FOO_SUB_INFO);
    }

    // FIXME: inline the above three test cases into a parameterized test once we can enable the test cases
    private static void assertOnlyBarResolved(final SourceInfo... libraryInfos) throws Exception {
        // main source: a module with no dependencies
        final var barRef = BAR_INFO.newRef();

        // resolution should succeed and should return only barRef
        final var resolved = SourceLinkageResolver.resolveInvolvedSources(Set.of(barRef),
            Arrays.stream(libraryInfos).map(SourceInfo::newRef).collect(Collectors.toUnmodifiableSet()));
        assertEquals(1, resolved.size());
        assertSame(barRef, resolved.getFirst().infoRef());
    }

    @Test
    void referencedWithSubmodule() throws Exception {
        final var fooRef = FOO_INFO.newRef();
        final var fooSubRef = FOO_SUB_INFO.newRef();
        final var bazRef = BAZ_INFO.newRef();

        final var resolved = SourceLinkageResolver.resolveInvolvedSources(Set.of(bazRef),
            Set.of(fooSubRef, fooRef));
        assertEquals(3, resolved.size());
        assertSame(fooSubRef, resolved.get(0).infoRef());
        assertSame(fooRef, resolved.get(1).infoRef());
        assertSame(bazRef, resolved.get(2).infoRef());
    }

    @Test
    void wildcardImportResolvesToLatest() throws Exception {
        final var noRevRef = SourceInfo.Module.builder()
            .setName(FOO)
            .setNamespace(XMLNamespace.of("foo"))
            .setPrefix(FOO)
            .build()
            .newRef();
        final var revRef = SourceInfo.Module.builder()
            .setName(FOO)
            .setNamespace(XMLNamespace.of("foo"))
            .setPrefix(FOO)
            .addRevision(Revision.of("2026-07-12"))
            .build()
            .newRef();
        final var bazRef = BAZ_INFO.newRef();

        // We have two revisions of 'foo' and 'baz': we should resolve all three and baz should be importing
        // the one with revision
        final var resolved = SourceLinkageResolver.resolveInvolvedSources(ImmutableSet.of(bazRef, noRevRef, revRef),
            Set.of());
        assertEquals(3, resolved.size());
        assertSame(revRef, resolved.get(2).infoRef());
        assertSame(noRevRef, resolved.get(1).infoRef());
        final var baz = resolved.get(0);
        assertSame(bazRef, baz.infoRef());

        final var bazImports = baz.imports();
        assertEquals(1, bazImports.size());
        assertSame(revRef.ref(), bazImports.getFirst().sourceRef());
    }
}
