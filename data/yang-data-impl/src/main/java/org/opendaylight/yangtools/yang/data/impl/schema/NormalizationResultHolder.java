/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMetadata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMountpoints;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationResult;

/**
 * Client-allocated result holder for {@link ImmutableNormalizedNodeStreamWriter} operation. The operation can result
 * in multiple distinct, but related items, to be produced -- as captured by {@link NormalizationResult}.
 */
public final class NormalizationResultHolder implements Mutable {
    private NormalizedNode data;
    private NormalizedMetadata metadata;
    private NormalizedMountpoints mountPoints;

    public @Nullable NormalizationResult<?> result() {
        final var localData = data;
        return localData == null ? null : new NormalizationResult<>(localData, metadata, mountPoints);
    }

    public @NonNull NormalizationResult<?> getResult() {
        final var localData = data;
        if (localData == null) {
            throw new IllegalStateException("Holder " + this + " has not been completed");
        }
        return new NormalizationResult<>(localData, metadata, mountPoints);
    }

    void setData(final NormalizedNode data) {
        if (this.data != null) {
            throw new ResultAlreadySetException("Normalized Node result was already set.", this.data);
        }
        this.data = requireNonNull(data);
    }

    void setMetadata(final NormalizedMetadata metadata) {
        this.metadata = metadata;
    }

    void setMountPoints(final NormalizedMountpoints mountPoints) {
        this.mountPoints = mountPoints;
    }

    void reset() {
        data = null;
        metadata = null;
        mountPoints = null;
    }
}
