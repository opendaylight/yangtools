package org.opendaylight.yangtools.util;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;
import org.opendaylight.yangtools.concepts.Immutable;

public final class PathComponentsFromRoot<C extends Comparable<C> & Immutable> extends AbstractPathComponents<C> {
    private final List<C> base;
    private final List<C> stack;

    public PathComponentsFromRoot(final ImmutablePath<?, C> base, final List<C> stack) {
        verify(!stack.isEmpty());
        this.base = base.getPathFromRoot();
        this.stack = stack;
    }

    @Override
    public int size() {
        return stack.size() + base.size();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean contains(final Object o) {
        final C srch = (C) requireNonNull(o);
        return stack.contains(srch) || base.contains(srch);
    }

    @Override
    public C get(final int index) {
        if (index < base.size()) {
            return base.get(index);
        }
        return stack.get(index - base.size());
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int indexOf(final Object o) {
        final C srch = (C) requireNonNull(o);

        int ret = base.indexOf(srch);
        if (ret == -1) {
            ret = stack.indexOf(srch);
            if (ret != -1) {
                return base.size() + ret;
            }
        }
        return ret;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int lastIndexOf(final Object o) {
        final C srch = (C) requireNonNull(o);

        final int ret = stack.lastIndexOf(srch);
        if (ret != -1) {
            return base.size() + ret;
        }

        return base.lastIndexOf(srch);
    }

    @Override
    public Iterator<C> iterator() {
        return new IteratorImpl<>(base, stack);
    }

    private static final class IteratorImpl<T> implements Iterator<T> {
        private final Iterator<T> stack;
        private final Iterator<T> base;

        IteratorImpl(final Iterable<T> base, final Iterable<T> stack) {
            this.base = base.iterator();
            this.stack = stack.iterator();
        }

        @Override
        public boolean hasNext() {
            return stack.hasNext();
        }

        @Override
        public T next() {
            if (base.hasNext()) {
                return base.next();
            }
            return stack.next();
        }
    }
}
