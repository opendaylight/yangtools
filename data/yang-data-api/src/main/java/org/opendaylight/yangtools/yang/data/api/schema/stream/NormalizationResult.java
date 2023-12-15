/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMetadata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMountpoints;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedTuple;

/**
 * The result of a {@link NormalizedNodeStreamWriter} stream, i.e. of a normalization operation. It really is just an
 * implementation of {@link NormalizedTuple}.
 */
public record NormalizationResult<T extends NormalizedNode>(
        @NonNull T data,
        @Nullable NormalizedMetadata metadata,
        @Nullable NormalizedMountpoints mountPoints) implements NormalizedTuple<T> {
    public NormalizationResult {
        requireNonNull(data);
    }

    public NormalizationResult(final @NonNull T data) {
        this(data, null, null);
    }

    public NormalizationResult(final @NonNull T data, final @Nullable NormalizedMetadata metadata) {
        this(data, metadata, null);
    }

    public NormalizationResult(final @NonNull T data, final @Nullable NormalizedMountpoints mountPoints) {
        this(data, null, mountPoints);
    }
}
