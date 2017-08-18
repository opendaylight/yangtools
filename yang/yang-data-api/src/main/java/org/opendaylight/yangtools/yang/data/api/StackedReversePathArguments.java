/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Collection;
import java.util.Iterator;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class StackedReversePathArguments extends PathArgumentList {
    private final StackedYangInstanceIdentifier identifier;
    private int size;
    private volatile boolean haveSize;

    StackedReversePathArguments(final StackedYangInstanceIdentifier identifier) {
        this.identifier = requireNonNull(identifier);
    }

    private static int calculateSize(final YangInstanceIdentifier parent) {
        YangInstanceIdentifier current = parent;
        for (int i = 1;; ++i) {
            final Collection<PathArgument> args = current.tryReversePathArguments();
            if (args != null) {
                return i + args.size();
            }

            verify(current instanceof StackedYangInstanceIdentifier);
            current = current.getParent();
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
        final PathArgument srch = (PathArgument) requireNonNull(o);
        return Iterators.contains(iterator(), srch);
    }

    @Override
    public PathArgument get(final int index) {
        return Iterators.get(iterator(), index);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int indexOf(final Object o) {
        final PathArgument srch = (PathArgument) requireNonNull(o);
        return super.indexOf(srch);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int lastIndexOf(final Object o) {
        final PathArgument srch = (PathArgument) requireNonNull(o);

        int ret = -1;
        final Iterator<PathArgument> it = iterator();
        for (int i = 0; it.hasNext(); ++i) {
            if (srch.equals(it.next())) {
                ret = i;
            }
        }

        return ret;
    }

    @Nonnull
    @Override
    public UnmodifiableIterator<PathArgument> iterator() {
        return new IteratorImpl(identifier);
    }

    private static final class IteratorImpl extends UnmodifiableIterator<PathArgument> {
        private StackedYangInstanceIdentifier identifier;
        private Iterator<PathArgument> tail;

        IteratorImpl(final StackedYangInstanceIdentifier identifier) {
            this.identifier = requireNonNull(identifier);
        }

        @Override
        public boolean hasNext() {
            return tail == null || tail.hasNext();
        }

        @Override
        public PathArgument next() {
            if (tail != null) {
                return tail.next();
            }

            final PathArgument ret = identifier.getLastPathArgument();
            final YangInstanceIdentifier next = identifier.getParent();
            final Iterable<PathArgument> args = next.tryReversePathArguments();
            if (args != null) {
                tail = args.iterator();
                identifier = null;
            } else {
                verify(next instanceof StackedYangInstanceIdentifier);
                identifier = (StackedYangInstanceIdentifier) next;
            }

            return ret;
        }
    }
}
