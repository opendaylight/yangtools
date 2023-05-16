/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMountPoint;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMountpoints;

/**
 * Immutable implementation of {@link NormalizedMountpoints}.
 */
public sealed class ImmutableNormalizedMountpoints implements NormalizedMountpoints {
    private static final class Container extends ImmutableNormalizedMountpoints {
        private final @NonNull ImmutableMap<PathArgument, NormalizedMountpoints> children;

        Container(final NormalizedMountPoint mountPoint,
                final Map<PathArgument, NormalizedMountpoints> children) {
            super(mountPoint);
            this.children = ImmutableMap.copyOf(children);
        }

        @Override
        public Map<PathArgument, NormalizedMountpoints> getChildren() {
            return children;
        }
    }

    private final NormalizedMountPoint mountPoint;

    private ImmutableNormalizedMountpoints(final NormalizedMountPoint mountPoint) {
        this.mountPoint = mountPoint;
    }

    @Override
    public NormalizedMountPoint mountPoint() {
        return mountPoint;
    }

    /**
     * Return a new {@link Builder}.
     *
     * @return A new Builder.
     */
    public static @NonNull Builder builder() {
        return new Builder();
    }

    /**
     * A Builder of {@link ImmutableNormalizedMountpoints} instances.
     */
    public static final class Builder implements Mutable {
        private final Map<PathArgument, NormalizedMountpoints> children = new HashMap<>();
        private NormalizedMountPoint mountPoint;

        Builder() {
            // Hidden to prevent instantiation
        }

        public @NonNull Builder withChild(final PathArgument identifier, final NormalizedMountpoints child) {
            children.put(requireNonNull(identifier), requireNonNull(child));
            return this;
        }

        @SuppressWarnings("checkstyle:hiddenField")
        public @NonNull Builder withChildren(final Map<PathArgument, NormalizedMountpoints> children) {
            children.forEach(this::withChild);
            return this;
        }

        @SuppressWarnings("checkstyle:hiddenField")
        public @NonNull Builder withMountPoint(final NormalizedMountPoint mountPoint) {
            this.mountPoint = requireNonNull(mountPoint);
            return this;
        }

        /**
         * Return an {@link ImmutableNormalizedMountpoints} view of this builder's state.
         *
         * @return An ImmutableNormalizedMountPoints instace
         * @throws IllegalStateException if this builder does not have enough state
         */
        public @NonNull ImmutableNormalizedMountpoints build() {
            return children.isEmpty() ? new ImmutableNormalizedMountpoints(mountPoint)
                : new Container(mountPoint, children);
        }
    }
}
