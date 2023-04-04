/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Set of features.
 */
// FIXME: 12.0.0: should have two implementations (Set-based and module/feature based via a builder). This should be
//                named 'FeatureSet' and live in yang-model-api, where it has a tie-in with IfFeatureExpr
@Beta
public abstract class FeatureSet implements Immutable {
    private static final class Explicit extends FeatureSet {
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

    public static @NonNull FeatureSet of() {
        return new Explicit(ImmutableSet.of());
    }

    public static @NonNull FeatureSet of(final QName... features) {
        return new Explicit(ImmutableSet.copyOf(features));
    }

    public static @NonNull FeatureSet of(final Set<QName> features) {
        return new Explicit(ImmutableSet.copyOf(features));
    }

    public abstract boolean contains(@NonNull QName qname);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
