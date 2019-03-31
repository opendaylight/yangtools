/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.data.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadata;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Immutable implementation of {@link NormalizedMetadata}.
 */
@Beta
public class ImmutableNormalizedMetadata implements NormalizedMetadata {
    private static final class Container extends ImmutableNormalizedMetadata {
        private final ImmutableMap<PathArgument, NormalizedMetadata> children;

        Container(final PathArgument identifier, final Map<QName, Object> annotations,
                final Map<PathArgument, ImmutableNormalizedMetadata> children) {
            super(identifier, annotations);
            this.children = ImmutableMap.copyOf(children);
        }

        @Override
        public ImmutableMap<PathArgument, NormalizedMetadata> getChildren() {
            return children;
        }
    }

    private final @NonNull PathArgument identifier;
    private final @NonNull ImmutableMap<QName, Object> annotations;

    ImmutableNormalizedMetadata(final PathArgument identifier, final Map<QName, Object> annotations) {
        this.identifier = requireNonNull(identifier);
        this.annotations = ImmutableMap.copyOf(annotations);
    }

    /**
     * Return a new {@link Builder}.
     *
     * @return A new Builder.
     */
    public static final @NonNull Builder builder() {
        return new Builder();
    }

    @Override
    public final PathArgument getIdentifier() {
        return identifier;
    }

    @Override
    public final ImmutableMap<QName, Object> getAnnotations() {
        return annotations;
    }

    /**
     * {@link org.opendaylight.yangtools.concepts.Builder} of {@link ImmutableNormalizedMetadata} instances.
     */
    public static final class Builder
            implements org.opendaylight.yangtools.concepts.Builder<ImmutableNormalizedMetadata> {
        private final Map<PathArgument, ImmutableNormalizedMetadata> children = new HashMap<>();
        private final Map<QName, Object> annotations = new HashMap<>();
        private PathArgument identifier;

        Builder() {
            // Hidden to prevent instantiation
        }

        @SuppressWarnings("checkstyle:hiddenField")
        public Builder withIdentifier(final PathArgument identifier) {
            this.identifier = requireNonNull(identifier);
            return this;
        }

        public Builder withAnnotation(final QName type, final Object value) {
            annotations.put(requireNonNull(type, "type"), requireNonNull(value, "value"));
            return this;
        }

        @SuppressWarnings("checkstyle:hiddenField")
        public Builder withAnnotations(final Map<QName, Object> annotations) {
            annotations.forEach(this::withAnnotation);
            return this;
        }

        public Builder withChild(final ImmutableNormalizedMetadata child) {
            children.put(child.getIdentifier(), child);
            return this;
        }

        @SuppressWarnings("checkstyle:hiddenField")
        public Builder withChildren(final Collection<ImmutableNormalizedMetadata> children) {
            children.forEach(this::withChild);
            return this;
        }

        @Override
        public ImmutableNormalizedMetadata build() {
            final PathArgument id = identifier;
            checkArgument(id != null, "Identifier has not been set");
            return children.isEmpty() ? new ImmutableNormalizedMetadata(id, annotations)
                    : new Container(id, annotations, children);
        }
    }
}
