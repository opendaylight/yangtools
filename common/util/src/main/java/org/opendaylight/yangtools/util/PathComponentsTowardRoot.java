/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterators;
import java.util.Collection;
import java.util.Iterator;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.LinearPath;

@Beta
public final class PathComponentsTowardRoot<P extends LinearPath<P, C>, C extends Comparable<C> & Immutable,
        F extends AbstractFixedLinearPath<P, C>, S extends AbstractStackedLinearPath<P, C, F, S>>
        extends AbstractPathComponents<C> {
    private final AbstractStackedLinearPath<P, C, F, S> identifier;

    private int size;
    private volatile boolean haveSize;

    PathComponentsTowardRoot(final AbstractStackedLinearPath<P, C, F, S> identifier) {
        this.identifier = requireNonNull(identifier);
    }

    private static int calculateSize(final LinearPath<?, ?> parent) {
        verify(parent instanceof AbstractLinearPath);
        AbstractLinearPath<?, ?> current = (AbstractLinearPath<?, ?>) parent;
        for (int i = 1;; ++i) {
            final Collection<?> args = current.tryPathTowardsRoot();
            if (args != null) {
                return i + args.size();
            }

            verify(current instanceof AbstractStackedLinearPath);
            current = (AbstractLinearPath<?, ?>) current.getParent();
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

    private static final class IteratorImpl<P extends LinearPath<P, C>, C extends Comparable<C> & Immutable>
            implements Iterator<C> {
        private AbstractStackedLinearPath<P, C, ?, ?> identifier;
        private Iterator<C> tail;

        IteratorImpl(final AbstractStackedLinearPath<P, C, ?, ?> identifier) {
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
            verify(next instanceof AbstractLinearPath);
            final Iterable<C> args = ((AbstractLinearPath<P, C>)next).tryPathTowardsRoot();
            if (args != null) {
                tail = args.iterator();
                identifier = null;
            } else {
                verify(next instanceof AbstractStackedLinearPath);
                identifier = (AbstractStackedLinearPath<P, C, ?, ?>) next;
            }

            return ret;
        }
    }

}
