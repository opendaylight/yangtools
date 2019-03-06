package org.opendaylight.yangtools.util;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterators;
import java.util.Collection;
import java.util.Iterator;
import org.opendaylight.yangtools.concepts.Immutable;

public final class PathComponentsTowardRoot<P extends ImmutablePath<P, C>, C extends Comparable<C> & Immutable,
        F extends AbstractFixedImmutablePath<P, C>, S extends AbstractStackedImmutablePath<P, C, F, S>>
        extends AbstractPathComponents<C> {
    private final AbstractStackedImmutablePath<P, C, F, S> identifier;

    private int size;
    private volatile boolean haveSize;

    PathComponentsTowardRoot(final AbstractStackedImmutablePath<P, C, F, S> identifier) {
        this.identifier = requireNonNull(identifier);
    }

    private static int calculateSize(final ImmutablePath<?, ?> parent) {
        verify(parent instanceof AbstractImmutablePath);
        AbstractImmutablePath<?, ?> current = (AbstractImmutablePath<?, ?>) parent;
        for (int i = 1;; ++i) {
            final Collection<?> args = current.tryPathTowardsRoot();
            if (args != null) {
                return i + args.size();
            }

            verify(current instanceof AbstractStackedImmutablePath);
            current = (AbstractImmutablePath<?, ?>) current.getParent();
        }
    }

    @Override
    public int size() {
        int ret = size;
        if (!haveSize) {
            ret = calculateSize(identifier.getParent());
            size = ret;
            haveSize = true;
        }

        return ret;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean contains(final Object o) {
        final C srch = (C) requireNonNull(o);
        return Iterators.contains(iterator(), srch);
    }

    @Override
    public C get(final int index) {
        return Iterators.get(iterator(), index);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int indexOf(final Object o) {
        final C srch = (C) requireNonNull(o);
        return super.indexOf(srch);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int lastIndexOf(final Object o) {
        final C srch = (C) requireNonNull(o);

        int ret = -1;
        final Iterator<C> it = iterator();
        for (int i = 0; it.hasNext(); ++i) {
            if (srch.equals(it.next())) {
                ret = i;
            }
        }

        return ret;
    }

    @Override
    public Iterator<C> iterator() {
        return new IteratorImpl<>(identifier);
    }

    private static final class IteratorImpl<P extends ImmutablePath<P, C>, C extends Comparable<C> & Immutable>
            implements Iterator<C> {
        private AbstractStackedImmutablePath<P, C, ?, ?> identifier;
        private Iterator<C> tail;

        IteratorImpl(final AbstractStackedImmutablePath<P, C, ?, ?> identifier) {
            this.identifier = requireNonNull(identifier);
        }

        @Override
        public boolean hasNext() {
            return tail == null || tail.hasNext();
        }

        @Override
        public C next() {
            if (tail != null) {
                return tail.next();
            }

            final C ret = identifier.getLastComponent();
            final P next = identifier.getParent();
            verify(next instanceof AbstractImmutablePath);
            final Iterable<C> args = ((AbstractImmutablePath<P, C>)next).tryPathTowardsRoot();
            if (args != null) {
                tail = args.iterator();
                identifier = null;
            } else {
                verify(next instanceof AbstractStackedImmutablePath);
                identifier = (AbstractStackedImmutablePath<P, C, ?, ?>) next;
            }

            return ret;
        }
    }

}
