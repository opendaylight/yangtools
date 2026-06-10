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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

@NonNullByDefault
class YT1896Test {
    private static final Unqualified FOO = Unqualified.of("foo");
    private static final Unqualified FOO_SUB = Unqualified.of("foo-sub");

    // module bar {
    //   namespace "urn:bar";
    //   prefix bar;
    // }
    private static final SourceInfo.Module BAR_INFO = SourceInfo.Module.builder()
        .setName(Unqualified.of("bar"))
        .setNamespace(XMLNamespace.of("urn:bar"))
        .setPrefix(Unqualified.of("bar"))
        .build();
    //  module foo {
    //    namespace "urn:foo";
    //    prefix foo;
    //
    //    include foo-sub;
    // }
    private static final SourceInfo.Module FOO_INFO = SourceInfo.Module.builder()
        .setName(FOO)
        .setNamespace(XMLNamespace.of("foo"))
        .setPrefix(FOO)
        .addInclude(new Include(FOO_SUB, null))
        .build();

    private static final SourceInfo.Submodule FOO_SUB_INFO = SourceInfo.Submodule.builder()
        .setName(FOO_SUB)
        .setBelongsTo(new BelongsTo(FOO, FOO, null))
        .build();

    @Test
    @Disabled("FIXME: YANGTOOLS-1896: fix the issue and enable this test")
    void unreferencedConsistent() throws Exception {
        assertOnlyBarResolved(FOO_INFO, FOO_SUB_INFO);
    }

    @Test
    void unreferencedWithoutSubmodule() throws Exception {
        assertOnlyBarResolved(FOO_INFO);
    }

    @Test
    @Disabled("FIXME: YANGTOOLS-1896: fix the issue and enable this test")
    void unreferencedWithoutModule() throws Exception {
        assertOnlyBarResolved(FOO_SUB_INFO);
    }

    private static void assertOnlyBarResolved(final SourceInfo... libraryInfos) throws Exception {
        // main source: a module with no dependencies
        final var barRef = BAR_INFO.newRef();

        // resolution should succeed and should return only barRef
        final var resolved = SourceLinkageResolver.resolveInvolvedSources(Set.of(barRef),
            Arrays.stream(libraryInfos).map(SourceInfo::newRef).collect(Collectors.toUnmodifiableSet()));
        assertEquals(1, resolved.size());
        assertSame(barRef, resolved.getFirst().infoRef());
    }
}

