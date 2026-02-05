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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.MaterializedSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
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
    sealed interface Stage permits Analyzed, NeedTransform, Materialized, SourceSpecificContext {
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
            BuildGlobalContext global,
            SourceTransformer<I, O> transformer,
            I input,
            StatementStreamSource.Factory<O> streamFactory) implements Stage {
        NeedTransform {
            requireNonNull(global);
            requireNonNull(transformer);
            requireNonNull(input);
            requireNonNull(streamFactory);
        }

        @Override
        public SourceIdentifier sourceId() {
            return input.sourceId();
        }

        Materialized<O> toMaterialized() throws IOException, SourceSyntaxException {
            final var source = transformer.transformSource(input);
            return new Materialized<>(global, source, streamFactory);
        }
    }

    /**
     * A {@link Stage} when we have acquired a {@link MaterializedSourceRepresentation}.
     *
     * @param <S> the {@link MaterializedSourceRepresentation}
     */
    private record Materialized<S extends MaterializedSourceRepresentation<?, ?>>(
            BuildGlobalContext global,
            S source,
            StatementStreamSource.Factory<S> streamFactory) implements Stage {
        Materialized {
            requireNonNull(global);
            requireNonNull(source);
            requireNonNull(streamFactory);
        }

        @Override
        public SourceIdentifier sourceId() {
            return source.sourceId();
        }

        Analyzed<S> toAnalyzed() throws SourceSyntaxException {
            return new Analyzed<>(global, source, source.extractSourceInfo(), streamFactory);
        }
    }

    /**
     * A {@link Stage} when we have acquired {@link SourceInfo} from the source representation.
     *
     * @param <S> the {@link MaterializedSourceRepresentation}
     */
    private record Analyzed<S extends MaterializedSourceRepresentation<?, ?>>(
            BuildGlobalContext global,
            S source,
            SourceInfo sourceInfo,
            StatementStreamSource.Factory<S> streamFactory) implements Stage {
        Analyzed {
            requireNonNull(global);
            requireNonNull(source);
            requireNonNull(sourceInfo);
            requireNonNull(streamFactory);
        }

        @Override
        public SourceIdentifier sourceId() {
            // Note: unlike source.sourceId(), this is guaranteed to be canonical
            return sourceInfo.sourceId();
        }

        SourceSpecificContext toSourceContext() {
            return new SourceSpecificContext(global, sourceInfo,
                streamFactory.newStreamSource(source, sourceInfo.yangVersion()));
        }
    }

    private Stage stage;

    BuildSource(final BuildGlobalContext global, final S source, final StatementStreamSource.Factory<S> streamFactory) {
        stage = new Materialized<>(global, source, streamFactory);
    }

    <I extends SourceRepresentation> BuildSource(final BuildGlobalContext global,
            final SourceTransformer<I, S> transformer, final I input,
            final StatementStreamSource.Factory<S> streamFactory) {
        stage = new NeedTransform<>(global, transformer, input, streamFactory);
    }

    SourceIdentifier sourceId() {
        return stage.sourceId();
    }

    SourceInfo ensureSourceInfo() throws IOException, SourceSyntaxException {
        return switch (stage) {
            case NeedTransform<?, ?> needTransform -> ensureSourceInfo(needTransform);
            case Materialized<?> materialized -> ensureSourceInfo(materialized);
            case Analyzed<?> analyzed -> analyzed.sourceInfo;
            case SourceSpecificContext sourceContext -> sourceContext.sourceInfo();
        };
    }

    private SourceInfo ensureSourceInfo(final Materialized<?> materialized) throws SourceSyntaxException {
        final var analyzed = materialized.toAnalyzed();
        stage = analyzed;
        return analyzed.sourceInfo;
    }

    private SourceInfo ensureSourceInfo(final NeedTransform<?, ?> needTransform)
            throws IOException, SourceSyntaxException {
        final var materialized = needTransform.toMaterialized();
        stage = materialized;
        return ensureSourceInfo(materialized);
    }

    SourceSpecificContext ensureSourceContext() throws IOException, SourceSyntaxException {
        return switch (stage) {
            case NeedTransform<?, ?> needTransform -> ensureSourceContext(needTransform);
            case Materialized<?> materialized -> ensureSourceContext(materialized);
            case Analyzed<?> analyzed -> ensureSourceContext(analyzed);
            case SourceSpecificContext sourceContext -> sourceContext;
        };
    }

    private SourceSpecificContext ensureSourceContext(final Analyzed<?> analyzed) {
        final var sourceContext = analyzed.toSourceContext();
        stage = sourceContext;
        return sourceContext;
    }

    private SourceSpecificContext ensureSourceContext(final Materialized<?> materialized) throws SourceSyntaxException {
        final var analyzed = materialized.toAnalyzed();
        stage = analyzed;
        return ensureSourceContext(analyzed);
    }

    private SourceSpecificContext ensureSourceContext(final NeedTransform<?, ?> needTransform)
            throws IOException, SourceSyntaxException {
        final var materialized = needTransform.toMaterialized();
        stage = materialized;
        return ensureSourceContext(materialized);
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