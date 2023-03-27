/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * Set of features. This is nominally a {@link Set} due to API pre-existing API contracts. This class needs to be used
 * <b>very carefully</b> because its {@link #hashCode()} and {@link #equals(Object)} contracts do not conform to
 * the specification laid out by {@link Set} and it cannot enumerate its individual component {@link QName}s -- thus
 * breaking reflexivity requirement of {@link #equals(Object)}.
 *
 * <p>
 * The semantics of {@link #contains(Object)} is a bit funky, but reflects the default of supporting all encountered
 * features without enumerating them. The map supplied to the constructor enumerates all {@code module} namespaces,
 * expressed as {@link QNameModule} for which we have an explicit enumeration of supported features. All other
 * {@code module} namespaces are treated as if there was no specification of supported features -- e.g. all features
 * from those namespaces are deemed to be present in the instance.
 */
// FIXME: 12.0.0: this should only have 'boolean contains(QName)', with two implementations (Set-based and
//                module/feature based via a builder). This shouldlive in yang-model-api, where it has a tie-in
//                with IfFeatureExpr
@Beta
public final class FeatureSet extends AbstractSet<QName> implements Immutable {
    // Note: not a ImmutableSetMultimap because we need to distinguish non-presence vs. empty Set
    private final ImmutableMap<QNameModule, ImmutableSet<String>> featuresByModule;

    public FeatureSet(final ImmutableMap<QNameModule, ImmutableSet<String>> featuresByModule) {
        this.featuresByModule = requireNonNull(featuresByModule);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean contains(final Object o) {
        if (o instanceof QName qname) {
            final var features = featuresByModule.get(qname.getModule());
            return features == null || features.contains(qname.getLocalName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return featuresByModule.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof FeatureSet other && featuresByModule.equals(other.featuresByModule);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("features", featuresByModule).toString();
    }

    @Deprecated
    @Override
    public Iterator<QName> iterator() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public <T> T[] toArray(final T[] a) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean add(final QName e) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean addAll(final Collection<? extends QName> c) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
