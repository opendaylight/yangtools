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
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.parser.source.StatementStreamSource;

/**
 * A single registered source. Allows instantiating {@link SourceInfo} and {@link StatementStreamSource} to further
 * process the source.
 *
 * @param extractor the {@link SourceInfoExtractor}
 * @param streamSupplier the {@link StatementStreamSource} supplier
 */
final class BuildSource<S extends SourceRepresentation & SourceInfo.Extractor> {
    /**
     * A stage in {@link BuildSource} lifecycle.
     */
    sealed interface Stage permits SourceSpecificContext, Uninitialized {
        // Just a marker
    }

    @NonNullByDefault
    private record Uninitialized<S extends SourceRepresentation & SourceInfo.Extractor>(
            BuildGlobalContext global, S source, Function<S, StatementStreamSource> streamFactory) implements Stage {
        Uninitialized {
            requireNonNull(global);
            requireNonNull(source);
            requireNonNull(streamFactory);
        }

        SourceSpecificContext initialize() throws SourceSyntaxException {
            final var sourceInfo = source.extractSourceInfo();
            return new SourceSpecificContext(global, sourceInfo.yangVersion(), streamFactory.apply(source));
        }
    }

    private @NonNull Stage stage;

    BuildSource(final BuildGlobalContext global, final S source,
            final Function<S, StatementStreamSource> streamFactory) {
        stage = new Uninitialized<>(global, source, streamFactory);
    }

    SourceSpecificContext getSourceContext() throws SourceSyntaxException {
        return switch (stage) {
            case Uninitialized<?> unitialized -> {
                final var context = unitialized.initialize();
                stage = context;
                yield context;
            }
            case SourceSpecificContext initialized -> initialized;
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