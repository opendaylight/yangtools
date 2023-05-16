/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMetadata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * The result of a {@link NormalizedNodeStreamWriter} stream, i.e. of a normalization operation. It contains the distict
 * parts of normalization: a mandatory {@link #data()} and optional {@link #metadata()}.
 */
@NonNullByDefault
public record NormalizationResult(NormalizedNode data, @Nullable NormalizedMetadata metadata) {
    public NormalizationResult {
        requireNonNull(data);
    }

    public NormalizationResult(final NormalizedNode data) {
        this(data, null);
    }
}
