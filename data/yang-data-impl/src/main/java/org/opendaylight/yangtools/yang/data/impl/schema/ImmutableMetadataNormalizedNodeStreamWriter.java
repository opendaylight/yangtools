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
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMetadata;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MetadataExtension;
import org.opendaylight.yangtools.yang.data.util.ImmutableNormalizedMetadata;
import org.opendaylight.yangtools.yang.data.util.ImmutableNormalizedMetadata.Builder;

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
        final Builder metaBuilder;
        final NormalizedNodeBuilder dataBuilder;

        State(final NormalizedNodeBuilder dataBuilder, final Builder metadataBuilder) {
            this.dataBuilder = requireNonNull(dataBuilder);
            metaBuilder = requireNonNull(metadataBuilder);
        }

        public NormalizedNodeBuilder getDataBuilder() {
            return dataBuilder;
        }

        public Builder getMetaBuilder() {
            return metaBuilder;
        }
    }

    private final Deque<Builder> builders = new ArrayDeque<>();
    private final NormalizationResultHolder holder;

    protected ImmutableMetadataNormalizedNodeStreamWriter(final State state) {
        super(state.getDataBuilder());
        builders.push(state.getMetaBuilder());
        holder = null;
    }

    protected ImmutableMetadataNormalizedNodeStreamWriter(final NormalizationResultHolder holder) {
        super(holder);
        this.holder = requireNonNull(holder);
    }

    @Override
    public final ClassToInstanceMap<Extension> getExtensions() {
        return ImmutableClassToInstanceMap.of(MetadataExtension.class, this);
    }

    @Override
    public final void metadata(final ImmutableMap<QName, Object> metadata) throws IOException {
        final Builder current = builders.peek();
        checkState(current != null, "Attempted to emit metadata when no metadata is open");
        current.withAnnotations(metadata);
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
        builders.push(ImmutableNormalizedMetadata.builder().withIdentifier(identifier));
    }

    @Override
    public final void endNode() {
        super.endNode();
        final ImmutableNormalizedMetadata metadata = builders.pop().build();
        final Builder current = builders.peek();
        if (current != null) {
            if (!metadata.getAnnotations().isEmpty() || !metadata.getChildren().isEmpty()) {
                current.withChild(metadata);
            }
        } else {
            // All done
            holder.setMetadata(metadata);
        }
    }
}
