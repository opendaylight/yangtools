/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.ImmutableOffsetMap.Unordered;

/**
 * Serialization proxy for {@link Unordered}.
 */
final class UIOMv1 extends IOMv1<Unordered<?, ?>> {
    @java.io.Serial
    private static final long serialVersionUID = 1;

    @SuppressWarnings("checkstyle:RedundantModifier")
    public UIOMv1() {
        // For Externalizable
    }

    UIOMv1(final @NonNull Unordered<?, ?> map) {
        super(map);
    }

    @Override
    Unordered<?, ?> createInstance(final ImmutableList<Object> keys, final Object[] values) {
        final var newOffsets = OffsetMapCache.unorderedOffsets(keys);
        return new Unordered<>(newOffsets, OffsetMapCache.adjustedArray(newOffsets, keys, values));
    }
}
