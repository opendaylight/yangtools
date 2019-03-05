/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadataContainer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Immutable implementation of {@link NormalizedMetadataContainer}.
 */
@Beta
public final class ImmutableNormalizedMetadataContainer extends ImmutableNormalizedMetadata
        implements NormalizedMetadataContainer {
    private final @NonNull ImmutableMap<PathArgument, ImmutableNormalizedMetadata> children;

    ImmutableNormalizedMetadataContainer(final PathArgument identifier, final ImmutableMap<QName, Object> annotations,
        final ImmutableMap<PathArgument, ImmutableNormalizedMetadata> children) {
        super(identifier, annotations);
        this.children = requireNonNull(children);
    }

    /**
     * Return a new {@link Builder}.
     *
     * @return A new Builder.
     */
    public static @NonNull Builder builder() {
        return new Builder();
    }

    @Override
    public Optional<ImmutableNormalizedMetadata> getChild(final PathArgument child) {
        return Optional.ofNullable(children.get(requireNonNull(child)));
    }

    /**
     * Return all {@link ImmutableNormalizedMetadata} children of this object.
     *
     * @return Child map.
     */
    public @NonNull ImmutableMap<PathArgument, ImmutableNormalizedMetadata> getChildren() {
        return children;
    }

    /**
     * {@link org.opendaylight.yangtools.concepts.Builder} of {@link ImmutableNormalizedMetadataContainer} instances.
     */
    public static final class Builder extends ImmutableNormalizedMetadata.Builder {
        private final Map<PathArgument, ImmutableNormalizedMetadata> children = new HashMap<>();

        Builder() {
            // Hidden to prevent instantiation
        }

        @Override
        public Builder withIdentifier(final PathArgument identifier) {
            super.withIdentifier(identifier);
            return this;
        }

        @Override
        public Builder withAnnotation(final QName type, final Object value) {
            super.withAnnotation(type, value);
            return this;
        }

        @Override
        public Builder withAnnotations(final Map<QName, Object> annotations) {
            super.withAnnotations(annotations);
            return this;
        }

        public Builder withChild(final ImmutableNormalizedMetadata child) {
            children.put(child.getIdentifier(), child);
            return this;
        }

        public Builder withChildren(final Collection<ImmutableNormalizedMetadata> children) {
            children.forEach(this::withChild);
            return this;
        }

        @Override
        public ImmutableNormalizedMetadataContainer build() {
            return new ImmutableNormalizedMetadataContainer(getIdentifier(), getAnnotations(), ImmutableMap.copyOf(children));
        }
    }
}
