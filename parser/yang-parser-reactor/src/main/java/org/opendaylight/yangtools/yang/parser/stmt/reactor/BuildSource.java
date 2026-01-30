/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.io.IOException;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.MaterializedSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Extractor;
import org.opendaylight.yangtools.yang.model.spi.source.SourceTransformer;
import org.opendaylight.yangtools.yang.parser.source.StatementStreamSource;

/**
 * A single registered source. Allows instantiating {@link SourceInfo} and {@link StatementStreamSource} to further
 * process the source.
 *
 * @param extractor the {@link SourceInfoExtractor}
 * @param streamSupplier the {@link StatementStreamSource} supplier
 */
@NonNullByDefault
final class BuildSource<S extends SourceRepresentation & MaterializedSourceRepresentation<S, ?>> {
    /**
     * A stage in {@link BuildSource} lifecycle.
     */
    sealed interface Stage permits NeedTransform, Materialized, SourceSpecificContext {
        // Just a marker
    }

    /**
     * A {@link Stage} when we have only a reference to a source and a transformer to {@link Materialized}.
     */
    private record NeedTransform<
            I extends SourceRepresentation,
            O extends SourceRepresentation & MaterializedSourceRepresentation<O, ?>>(
            BuildGlobalContext global,
            SourceTransformer<I, O> transformer,
            I input,
            Function<O, StatementStreamSource> streamFactory) implements Stage {
        NeedTransform {
            requireNonNull(global);
            requireNonNull(transformer);
            requireNonNull(input);
            requireNonNull(streamFactory);
        }

        Materialized<O> toMaterialized() throws IOException, SourceSyntaxException {
            final var source = transformer.transformSource(input);
            return new Materialized<>(global, source, streamFactory);
        }
    }

    /**
     * A {@link Stage} when we have acquired a representation that is a {@link Extractor}.
     *
     * @param <S> the {@link SourceRepresentation}
     */
    private record Materialized<S extends SourceRepresentation & SourceInfo.Extractor>(
            BuildGlobalContext global, S source, Function<S, StatementStreamSource> streamFactory) implements Stage {
        Materialized {
            requireNonNull(global);
            requireNonNull(source);
            requireNonNull(streamFactory);
        }

        SourceSpecificContext toSourceContext() throws SourceSyntaxException {
            final var sourceInfo = source.extractSourceInfo();
            return new SourceSpecificContext(global, sourceInfo.yangVersion(), streamFactory.apply(source));
        }
    }

    private Stage stage;

    BuildSource(final BuildGlobalContext global, final S source,
            final Function<S, StatementStreamSource> streamFactory) {
        stage = new Materialized<>(global, source, streamFactory);
    }

    <I extends SourceRepresentation> BuildSource(final BuildGlobalContext global,
            final SourceTransformer<I, S> transformer, final I input,
            final Function<S, StatementStreamSource> streamFactory) {
        stage = new NeedTransform<>(global, transformer, input, streamFactory);
    }

    SourceSpecificContext getSourceContext() throws IOException, SourceSyntaxException {
        return switch (stage) {
            case NeedTransform<?, ?> needTransform -> {
                // Note: two stores to free references as soon as possible
                final var materialized = needTransform.toMaterialized();
                stage = materialized;
                final var context = materialized.toSourceContext();
                stage = context;
                yield context;
            }
            case Materialized<?> materialized -> {
                final var context = materialized.toSourceContext();
                stage = context;
                yield context;
            }
            case SourceSpecificContext sourceContext -> sourceContext;
        };
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("stage", stage).toString();
    }
}