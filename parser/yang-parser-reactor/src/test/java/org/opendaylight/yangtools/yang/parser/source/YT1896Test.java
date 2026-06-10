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

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

class YT1896Test {
    @Test
    void unreferencedSubmodulesAreIgnored() throws Exception {
        // main source: a module with no dependencies
        final var barRef = SourceInfo.Module.builder()
            .setName(Unqualified.of("bar"))
            .setNamespace(XMLNamespace.of("urn:bar"))
            .setYangVersion(YangVersion.VERSION_1_1)
            .setPrefix(Unqualified.of("bar"))
            .build()
            .newRef();

        // lib sources: a module and its submodule
        final var foo = Unqualified.of("foo");
        final var fooSub = Unqualified.of("foo-sub");
        final var fooRef = SourceInfo.Module.builder()
            .setName(foo)
            .setNamespace(XMLNamespace.of("foo"))
            .setYangVersion(YangVersion.VERSION_1_1)
            .setPrefix(foo)
            .addInclude(new Include(fooSub, null))
            .build()
            .newRef();
        final var fooSubRef = SourceInfo.Submodule.builder()
            .setName(fooSub)
            .setBelongsTo(new BelongsTo(foo, foo, null))
            .setYangVersion(YangVersion.VERSION_1_1)
            .build()
            .newRef();

        // resolution should succeed and should return only barRef
        final var resolved = SourceLinkageResolver.resolveInvolvedSources(Set.of(barRef), Set.of(fooRef, fooSubRef));
        assertEquals(1, resolved.size());
        assertSame(barRef, resolved.getFirst().infoRef());
    }
}
