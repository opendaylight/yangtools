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
import org.opendaylight.yangtools.util.ImmutableOffsetMap.Ordered;

/**
 * Serialization proxy for {@link Ordered}.
 */
final class OIOMv1 extends IOMv1<Ordered<?, ?>> {
    @java.io.Serial
    private static final long serialVersionUID = 1;

    @SuppressWarnings("checkstyle:RedundantModifier")
    public OIOMv1() {
        // For Externalizable
    }

    OIOMv1(final @NonNull Ordered<?, ?> map) {
        super(map);
    }

    @Override
    Ordered<?, ?> createInstance(final ImmutableList<Object> keys, final Object[] values) {
        return new Ordered<>(OffsetMapCache.orderedOffsets(keys), values);
    }
}
