/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableMultimap;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangIRSourceInfoExtractor;

class DependencyResolverTest {
    @Test
    public void testModulesWithoutRevisionAndImport() throws Exception {
        final var resolved = resolveResources("/no-revision/imported.yang", "/no-revision/imported@2012-12-12.yang",
            "/no-revision/top@2012-10-10.yang");
        assertThat(resolved.resolvedSources()).containsExactlyInAnyOrder(
            new SourceIdentifier("top", "2012-10-10"),
            new SourceIdentifier("imported"),
            new SourceIdentifier("imported", "2012-12-12"));
        assertEquals(List.of(), resolved.unresolvedSources());
        assertEquals(ImmutableMultimap.of(), resolved.unsatisfiedImports());
    }

    @Test
    public void testSubmoduleNoModule() throws Exception {
        // Subfoo does not have parent in reactor
        final var resolved = resolveResources("/model/subfoo.yang", "/model/bar.yang", "/model/baz.yang");
        assertThat(resolved.resolvedSources()).containsExactlyInAnyOrder(
            new SourceIdentifier("bar", "2013-07-03"),
            new SourceIdentifier("baz", "2013-02-27"));
        assertThat(resolved.unresolvedSources()).containsExactlyInAnyOrder(
            new SourceIdentifier("subfoo", "2013-02-27"));

        assertEquals(ImmutableMultimap.of(
            new SourceIdentifier("subfoo", "2013-02-27"), new BelongsTo(Unqualified.of("foo"), Unqualified.of("f"))),
            resolved.unsatisfiedImports());
    }

    @Test
    public void testSubmodule() throws Exception {
        final var resolved = resolveResources("/model/subfoo.yang", "/model/foo.yang", "/model/bar.yang",
            "/model/baz.yang");
        assertThat(resolved.resolvedSources()).containsExactlyInAnyOrder(
            new SourceIdentifier("bar", "2013-07-03"),
            new SourceIdentifier("baz", "2013-02-27"));
        assertThat(resolved.unresolvedSources()).containsExactlyInAnyOrder(
            new SourceIdentifier("foo", "2013-02-27"),
            new SourceIdentifier("subfoo", "2013-02-27"));
        assertEquals(ImmutableMultimap.of(), resolved.unsatisfiedImports());
    }

    private static RevisionDependencyResolver resolveResources(final String... resourceNames) throws Exception {
        final var map = new HashMap<SourceIdentifier, SourceInfo>();
        for (var resourceName : resourceNames) {
            final var info = YangIRSourceInfoExtractor.forYangText(
                YangTextSource.forResource(DependencyResolverTest.class, resourceName));
            map.put(info.sourceId(), info);
        }
        return new RevisionDependencyResolver(map);
    }
}
