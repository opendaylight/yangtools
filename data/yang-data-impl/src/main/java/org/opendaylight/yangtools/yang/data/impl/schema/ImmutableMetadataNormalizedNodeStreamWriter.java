/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMetadata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MetadataExtension;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedMetadata.Builder;

/**
 * A {@link NormalizedMetadata}-aware {@link ImmutableMetadataNormalizedNodeStreamWriter}. It advertizes the
 * {@link MetadataExtension} extension.
 */
@Beta
public class ImmutableMetadataNormalizedNodeStreamWriter extends ImmutableNormalizedNodeStreamWriter
        implements MetadataExtension {
    /**
     * Snapshot of currently-open data- and metadatastate.
     */
    public static final class State {
        final BuilderEntry metaBuilder;
        final NormalizedNode.Builder dataBuilder;

        State(final NormalizedNode.Builder dataBuilder, final BuilderEntry metadataBuilder) {
            this.dataBuilder = requireNonNull(dataBuilder);
            metaBuilder = requireNonNull(metadataBuilder);
        }

        public NormalizedNode.Builder getDataBuilder() {
            return dataBuilder;
        }

        public Builder getMetaBuilder() {
            return metaBuilder.builder;
        }
    }

    @NonNullByDefault
    private record BuilderEntry(PathArgument identifier, Builder builder) {
        BuilderEntry {
            requireNonNull(identifier);
            requireNonNull(builder);
        }
    }

    private final Deque<BuilderEntry> builders = new ArrayDeque<>();
    private final NormalizationResultHolder holder;

    protected ImmutableMetadataNormalizedNodeStreamWriter(final State state) {
        super(state.dataBuilder);
        builders.push(state.metaBuilder);
        holder = null;
    }

    protected ImmutableMetadataNormalizedNodeStreamWriter(final NormalizationResultHolder holder) {
        super(holder);
        this.holder = requireNonNull(holder);
    }

    @Override
    public final List<MetadataExtension> supportedExtensions() {
        return List.of(this);
    }

    @Override
    public final void metadata(final ImmutableMap<QName, Object> metadata) throws IOException {
        final var current = builders.peek();
        checkState(current != null, "Attempted to emit metadata when no metadata is open");
        current.builder.withAnnotations(metadata);
    }

    /**
     * Remove the currently-open builders for data and metadata from the stack.
     *
     * @return Builder state.
     */
    protected final @NonNull State popState() {
        return new State(popBuilder(), builders.pop());
    }

    @Override
    @SuppressWarnings("rawtypes")
    final void enter(final PathArgument identifier, final NormalizedNodeBuilder next) {
        super.enter(identifier, next);
        builders.push(new BuilderEntry(identifier, ImmutableNormalizedMetadata.builder()));
    }

    @Override
    public final void endNode() {
        super.endNode();

        final var last = builders.pop();
        final var metadata = last.builder.build();
        final var current = builders.peek();
        if (current != null) {
            if (!metadata.getAnnotations().isEmpty() || !metadata.getChildren().isEmpty()) {
                current.builder.withChild(last.identifier, metadata);
            }
        } else {
            // All done
            holder.setMetadata(metadata);
        }
    }
}
