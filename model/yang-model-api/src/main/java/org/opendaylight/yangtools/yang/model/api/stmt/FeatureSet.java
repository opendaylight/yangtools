/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * Set of features.
 */
public abstract sealed class FeatureSet implements Immutable {
    private static final class Explicit extends FeatureSet {
        private static final @NonNull Explicit EMPTY = new Explicit(ImmutableSet.of());

        private final ImmutableSet<QName> features;

        Explicit(final ImmutableSet<QName> features) {
            this.features = requireNonNull(features);
        }

        @Override
        public boolean contains(final QName qname) {
            return features.contains(qname);
        }

        @Override
        public int hashCode() {
            return features.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return this == obj || obj instanceof Explicit other && features.equals(other.features);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("features", features).toString();
        }
    }

    private static final class Sparse extends FeatureSet {
        // Note: not a ImmutableSetMultimap because we need to distinguish non-presence vs. empty Set
        private final ImmutableMap<QNameModule, ImmutableSet<String>> featuresByModule;

        Sparse(final Map<QNameModule, ImmutableSet<String>> featuresByModule) {
            this.featuresByModule = ImmutableMap.copyOf(featuresByModule);
        }

        @Override
        public boolean contains(final QName qname) {
            final var sets = featuresByModule.get(qname.getModule());
            return sets == null || sets.contains(qname.getLocalName());
        }

        @Override
        public int hashCode() {
            return featuresByModule.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return this == obj || obj instanceof Sparse other && featuresByModule.equals(other.featuresByModule);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("features", featuresByModule).toString();
        }
    }

    public static final class Builder {
        // Note: Tree{Map,Set} so we sort keys/entries by their natural ordering -- which also prevents nulls
        private final TreeMap<QNameModule, TreeSet<String>> moduleFeatures = new TreeMap<>();

        public @NonNull Builder addModuleFeatures(final QNameModule module, final Collection<String> names) {
            moduleFeatures.computeIfAbsent(module, ignored -> new TreeSet<>()).addAll(names);
            return this;
        }

        public @NonNull FeatureSet build() {
            return new Sparse(Maps.transformValues(moduleFeatures, ImmutableSet::copyOf));
        }
    }

    public static @NonNull FeatureSet of() {
        return Explicit.EMPTY;
    }

    public static @NonNull FeatureSet of(final QName... features) {
        return of(ImmutableSet.copyOf(features));
    }

    public static @NonNull FeatureSet of(final Set<QName> features) {
        return of(ImmutableSet.copyOf(features));
    }

    private static @NonNull FeatureSet of(final ImmutableSet<QName> features) {
        return features.isEmpty() ? of() : new Explicit(features);
    }

    public static @NonNull Builder builder() {
        return new Builder();
    }

    public abstract boolean contains(@NonNull QName qname);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
