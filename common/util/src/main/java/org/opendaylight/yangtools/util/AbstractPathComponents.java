package org.opendaylight.yangtools.util;

import java.util.AbstractList;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Immutable;

public abstract class AbstractPathComponents<C extends Comparable<C> & Immutable> extends AbstractList<C> {
    @Override
    public final boolean isEmpty() {
        return false;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean addAll(@Nonnull final Collection<? extends C> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean addAll(final int index, final Collection<? extends C> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean removeAll(@Nonnull final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean retainAll(@Nonnull final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }
}
