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
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

@NonNullByDefault
final class SourceLinkageBuilder {
    private final HashSet<ReactorSource> sources = new HashSet<>();
    private final HashSet<BuildSource<?>> libSources = new HashSet<>();

    <S extends SourceRepresentation & MaterializedSourceRepresentation<S, ?>> void addSource(final S source,
            final StatementStreamSource.Support<S> streamSupport) throws IOException, SourceSyntaxException {
        final var buildSource = BuildSource.ofMaterialized(source, streamSupport);
        // eagerly initialize, so that any source-related problem is reported now rather than later
        sources.add(buildSource.ensureReactorSource());
    }

    <I extends SourceRepresentation, O extends SourceRepresentation & MaterializedSourceRepresentation<O, ?>>
            void addLibSource(final SourceTransformer<I, O> transformer, final I source,
                final StatementStreamSource.Support<O> streamSupport) {
        libSources.add(BuildSource.ofTransformed(transformer, source, streamSupport));
    }

    <S extends SourceRepresentation & MaterializedSourceRepresentation<S, ?>> void addLibSource(final S source,
            final StatementStreamSource.Support<S> streamSupport) {
        // library sources are lazily initialized
        libSources.add(BuildSource.ofMaterialized(source, streamSupport));
    }

    Map<ReactorSource, ResolvedSourceInfo> build() throws ReactorException, SourceSyntaxException {
        // FIXME: do not materialize libSources until needed
        final var libReactorSources = HashSet.<ReactorSource>newHashSet(libSources.size());
        for (final var buildSource : libSources) {
            final ReactorSource reactorSource;
            try {
                reactorSource = buildSource.ensureReactorSource();
            } catch (IOException e) {
                throw new SomeModifiersUnresolvedException(ModelProcessingPhase.INIT, buildSource.sourceId(), e);
            }
            libReactorSources.add(reactorSource);
        }

        return SourceLinkageResolver.resolveInvolvedSources(sources, libReactorSources);
    }
}
