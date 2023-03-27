/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Set of features. This is nominally a {@link Set} due to API pre-existing API contracts. Implementations are not
 * required to faithfully implement {@link Set#equals(Object)} and {@link Set#hashCode()}.
 */
// FIXME: 12.0.0: publish to a common place to share with yang-parser-reactor, this being a @FunctionalInterface with
//                only contains(QName)
@Beta
public abstract class SupportedFeatureSet extends AbstractSet<QName> implements Immutable {
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean contains(final Object o) {
        return o instanceof QName qname && contains(qname);
    }

    public abstract boolean contains(@NonNull QName qname);

    @Override
    public abstract String toString();

    @Deprecated
    @Override
    public final Iterator<QName> iterator() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final int size() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final <T> T[] toArray(final T[] a) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean add(final QName e) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean addAll(final Collection<? extends QName> c) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @SuppressFBWarnings(value = "EQ_UNUSUAL", justification = "Always unsupported")
    public final boolean equals(final Object obj) {
        throw new UnsupportedOperationException();
    }
}
