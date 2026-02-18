/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.MaterializedSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceTransformer;

/**
 * A single registered source. Allows instantiating {@link SourceInfo} and {@link StatementStreamSource} to further
 * process the source.
 */
@NonNullByDefault
public final class BuildSource<S extends SourceRepresentation & MaterializedSourceRepresentation<S, ?>> {
    /**
     * A stage in {@link BuildSource} lifecycle.
     */
    sealed interface Stage permits NeedTransform, Materialized, ReactorSource {
        /**
         * {@return the stage SourceIdentifier}
         */
        SourceIdentifier sourceId();
    }

    /**
     * A {@link Stage} when we have only a reference to a source and a transformer to {@link Materialized}.
     */
    private record NeedTransform<
            I extends SourceRepresentation,
            O extends SourceRepresentation & MaterializedSourceRepresentation<O, ?>>(
            SourceTransformer<I, O> transformer,
            I input,
            StatementStreamSource.Support<O> streamSupport) implements Stage {
        NeedTransform {
            requireNonNull(transformer);
            requireNonNull(input);
            requireNonNull(streamSupport);
        }

        @Override
        public SourceIdentifier sourceId() {
            return input.sourceId();
        }

        Materialized<O> toMaterialized() throws IOException, SourceSyntaxException {
            final var source = transformer.transformSource(input);
            return new Materialized<>(source, streamSupport);
        }
    }

    /**
     * A {@link Stage} when we have acquired a {@link MaterializedSourceRepresentation}.
     *
     * @param <S> the {@link MaterializedSourceRepresentation}
     */
    private record Materialized<S extends MaterializedSourceRepresentation<?, ?>>(
            S source,
            StatementStreamSource.Support<S> streamSupport) implements Stage {
        Materialized {
            requireNonNull(source);
            requireNonNull(streamSupport);
        }

        @Override
        public SourceIdentifier sourceId() {
            return source.sourceId();
        }

        ReactorSource toReactorSource() throws SourceSyntaxException {
            final var sourceInfo = source.extractSourceInfo();
            return new ReactorSourceImpl(sourceInfo.newRef(),
                streamSupport.newFactory(source, sourceInfo.yangVersion()));
        }
    }

    private Stage stage;

    private BuildSource(final Stage stage) {
        this.stage = requireNonNull(stage);
    }

    public static <S extends SourceRepresentation & MaterializedSourceRepresentation<S, ?>>
            BuildSource<S> ofMaterialized(final S source, final StatementStreamSource.Support<S> streamSupport) {
        return new BuildSource<>(new Materialized<>(source, streamSupport));
    }

    public static <S extends SourceRepresentation & MaterializedSourceRepresentation<S, ?>,
            I extends SourceRepresentation> BuildSource<S> ofTransformed(final SourceTransformer<I, S> transformer,
                final I input, final StatementStreamSource.Support<S> streamSupport) {
        return new BuildSource<>(new NeedTransform<>(transformer, input, streamSupport));
    }

    public SourceIdentifier sourceId() {
        return stage.sourceId();
    }

    // TODO: we really would like this to be 'ensureReactorSource' continuation, but for that we need to peel out
    //       source-specific context access.
    public ReactorSource ensureReactorSource() throws IOException, SourceSyntaxException {
        return switch (stage) {
            case NeedTransform<?, ?> needTransform -> ensureReactorSource(needTransform);
            case Materialized<?> materialized -> ensureReactorSource(materialized);
            case ReactorSource reactorSource -> reactorSource;
        };
    }

    private ReactorSource ensureReactorSource(final Materialized<?> materialized) throws SourceSyntaxException {
        final var reactorSource = materialized.toReactorSource();
        stage = reactorSource;
        return reactorSource;
    }

    private ReactorSource ensureReactorSource(final NeedTransform<?, ?> needTransform)
            throws IOException, SourceSyntaxException {
        final var materialized = needTransform.toMaterialized();
        stage = materialized;
        return ensureReactorSource(materialized);
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
