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
    private final @NonNull PathArgument identifier;
    private final @NonNull ImmutableMap<QName, Object> annotations;

    ImmutableNormalizedMetadata(final PathArgument identifier, final ImmutableMap<QName, Object> annotations) {
        this.identifier = requireNonNull(identifier);
        this.annotations = requireNonNull(annotations);
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
    public static class Builder implements org.opendaylight.yangtools.concepts.Builder<ImmutableNormalizedMetadata> {
        private final Map<QName, Object> annotations = new HashMap<>();
        private PathArgument identifier;

        Builder() {
            // Hidden to prevent instantiation
        }

        public Builder withIdentifier(final PathArgument identifier) {
            this.identifier = requireNonNull(identifier);
            return this;
        }

        public Builder withAnnotation(final QName type, final Object value) {
            annotations.put(requireNonNull(type, "type"), requireNonNull(value, "value"));
            return this;
        }

        public Builder withAnnotations(final Map<QName, Object> annotations) {
            annotations.forEach(this::withAnnotation);
            return this;
        }

        @Override
        public ImmutableNormalizedMetadata build() {
            return new ImmutableNormalizedMetadata(identifier(), annotations());
        }

        final ImmutableMap<QName, Object> annotations() {
            return ImmutableMap.copyOf(annotations);
        }

        final @NonNull PathArgument identifier() {
            final PathArgument local = identifier;
            checkArgument(local != null, "Identifier has not been set");
            return local;
        }
    }
}
