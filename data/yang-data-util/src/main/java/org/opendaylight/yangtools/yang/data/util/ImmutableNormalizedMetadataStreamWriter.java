/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMetadata;
import org.opendaylight.yangtools.yang.data.api.schema.stream.StreamWriterMetadataExtension;
import org.opendaylight.yangtools.yang.data.util.ImmutableNormalizedMetadata.Builder;

/**
 * A simple {@link StreamWriterMetadataExtension} implementation, which takes care of building
 * {@link NormalizedMetadata} based on additional nesting instructions.
 */
@Beta
public final class ImmutableNormalizedMetadataStreamWriter implements StreamWriterMetadataExtension {
    private final Deque<Builder> builders = new ArrayDeque<>();

    private ImmutableNormalizedMetadata result;

    public void enter(final PathArgument identifier) {
        checkNotDone();
        builders.push(ImmutableNormalizedMetadata.builder().withIdentifier(identifier));
    }

    public void exit() {
        checkNotDone();
        final ImmutableNormalizedMetadata metadata = builders.pop().build();
        final Builder parent = builders.peek();
        if (parent != null) {
            if (!metadata.getAnnotations().isEmpty()) {
                parent.withChild(metadata);
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
        builders.peek().withAnnotations(metadata);
    }

    private void checkNotDone() {
        checkState(result != null, "Metadata already completed");
    }
}
