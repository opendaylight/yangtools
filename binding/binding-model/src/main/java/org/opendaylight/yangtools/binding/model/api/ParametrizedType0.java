/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
record ParametrizedType0(Type getRawType) implements ParameterizedType {
    ParametrizedType0 {
        requireNonNull(getRawType);
    }

    @Override
    public List<Type> getActualTypeArguments() {
        return List.of();
    }

    @Override
    public final int hashCode() {
        return name().hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Type other && name().equals(other.name());
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(ParameterizedType.class).add("name", name()).toString();
    }
}
