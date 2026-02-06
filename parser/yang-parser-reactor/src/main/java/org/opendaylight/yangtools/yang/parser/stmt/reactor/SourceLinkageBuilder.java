/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.MaterializedSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceTransformer;
import org.opendaylight.yangtools.yang.parser.source.BuildSource;
import org.opendaylight.yangtools.yang.parser.source.ReactorSource;
import org.opendaylight.yangtools.yang.parser.source.ResolvedSourceInfo;
import org.opendaylight.yangtools.yang.parser.source.SourceLinkageResolver;
import org.opendaylight.yangtools.yang.parser.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

@NonNullByDefault
final class SourceLinkageBuilder {
    private final HashSet<BuildSource<?>> sources = new HashSet<>();
    private final HashSet<BuildSource<?>> libSources = new HashSet<>();

    <S extends SourceRepresentation & MaterializedSourceRepresentation<S, ?>> void addSource(final S source,
            final StatementStreamSource.Factory<S> streamFactory) throws IOException, SourceSyntaxException {
        final var buildSource = BuildSource.ofMaterialized(source, streamFactory);
        // eagerly initialize, so that any source-related problem is reported now rather than later
        buildSource.ensureReactorSource();
        sources.add(buildSource);
    }

    <I extends SourceRepresentation, O extends SourceRepresentation & MaterializedSourceRepresentation<O, ?>>
            void addLibSource(final SourceTransformer<I, O> transformer, final I source,
                final StatementStreamSource.Factory<O> streamFactory) {
        libSources.add(BuildSource.ofTransformed(transformer, source, streamFactory));
    }

    <S extends SourceRepresentation & MaterializedSourceRepresentation<S, ?>> void addLibSource(final S source,
            final StatementStreamSource.Factory<S> streamFactory) {
        // library sources are lazily initialized
        libSources.add(BuildSource.ofMaterialized(source, streamFactory));
    }

    Map<ReactorSource<?>, ResolvedSourceInfo> build() throws ReactorException, SourceSyntaxException {
        return SourceLinkageResolver.resolveInvolvedSources(sources, libSources);
    }
}
