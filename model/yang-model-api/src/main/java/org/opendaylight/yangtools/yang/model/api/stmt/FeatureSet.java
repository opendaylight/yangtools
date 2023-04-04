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
 * A set of features.
 *
 * <p>
 * The semantics of {@link #contains(QName)} is a bit funky, depending on implementation:
 * <ol>
 *   <li>explicit implementation, returned from {@link #of()}, {@link #of(Set)} et al., delegates to the underlying
 *       Set's {@link Set#contains(Object)}, while on the other hand</li>
 *   <li>sparse implementation, constructed via {@link #builder()} and {@link Builder#build()}, carves the features into
 *       well-known {@code module} namespaces, expressed as {@link QNameModule} for which we have an explicit
 *       enumeration of supported features. All other {@code module} namespaces are treated as if there was no
 *       specification of supported features -- e.g. all features from those namespaces are deemed to be present
 *       in the instance.</li>
 * </ol>
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

    /**
     * A builder for a sparse FeatureSet. The semantics is such that for features which belong to a namespace which
     * has been explicitly mention, only the specified features are supported. For namespaces not mentioned, all
     * features are reported as present.
     */
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

    /**
     * Return an empty {@link FeatureSet}.
     *
     * @return An empty {@link FeatureSet}
     */
    public static @NonNull FeatureSet of() {
        return Explicit.EMPTY;
    }

    /**
     * Return a {@link FeatureSet} containing specified features.
     *
     * @return A {@link FeatureSet}
     * @throws NullPointerException if {@code features} is or contains {@code null}
     */
    public static @NonNull FeatureSet of(final QName... features) {
        return of(ImmutableSet.copyOf(features));
    }

    /**
     * Return a {@link FeatureSet} containing specified features.
     *
     * @return A {@link FeatureSet}
     * @throws NullPointerException if {@code features} is or contains {@code null}
     */
    public static @NonNull FeatureSet of(final Set<QName> features) {
        return of(ImmutableSet.copyOf(features));
    }

    private static @NonNull FeatureSet of(final ImmutableSet<QName> features) {
        return features.isEmpty() ? of() : new Explicit(features);
    }

    public static @NonNull Builder builder() {
        return new Builder();
    }

    /**
     * Determine whether a particular {@code feature}, as identified by its {@link QName} is part of this set.
     *
     * @param qname Feature QName
     * @return {@code true} if this set contains the feature
     * @throws NullPointerException if {@code qname} is {@code null}
     */
    public abstract boolean contains(@NonNull QName qname);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
