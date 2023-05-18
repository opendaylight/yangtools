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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMetadata;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MetadataExtension;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedMetadata.Builder;

/**
 * A simple {@link MetadataExtension} implementation, which takes care of building {@link NormalizedMetadata} based on
 * additional nesting instructions.
 */
@Beta
public final class ImmutableMetadataExtension implements MetadataExtension {
    @NonNullByDefault
    private record BuilderEntry(PathArgument identifier, Builder builder) {
        BuilderEntry {
            requireNonNull(identifier);
            requireNonNull(builder);
        }
    }

    private final Deque<BuilderEntry> builders = new ArrayDeque<>();

    private ImmutableNormalizedMetadata result;

    public void enter(final PathArgument identifier) {
        checkNotDone();
        builders.push(new BuilderEntry(identifier, ImmutableNormalizedMetadata.builder()));
    }

    public void exit() {
        checkNotDone();

        final var last = builders.pop();
        final var metadata = last.builder.build();
        final var current = builders.peek();
        if (current != null) {
            if (!metadata.getAnnotations().isEmpty() || !metadata.getChildren().isEmpty()) {
                current.builder().withChild(last.identifier, metadata);
            }
        } else {
            result = metadata;
        }
    }

    public Optional<NormalizedMetadata> getResult() {
        checkState(result != null, "Metadata tree has not been completely built");
        return result.getAnnotations().isEmpty() ? Optional.empty() : Optional.of(result);
    }

    @Override
    public void metadata(final ImmutableMap<QName, Object> metadata) throws IOException {
        builders.peek().builder.withAnnotations(metadata);
    }

    private void checkNotDone() {
        checkState(result != null, "Metadata already completed");
    }
}
