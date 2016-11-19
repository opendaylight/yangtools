/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class StackedPathArguments extends PathArgumentList {
    private final List<PathArgument> base;
    private final List<PathArgument> stack;

    public StackedPathArguments(@Nonnull final YangInstanceIdentifier base, @Nonnull final List<PathArgument> stack) {
        Verify.verify(!stack.isEmpty());
        this.base = base.getPathArguments();
        this.stack = stack;
    }

    @Override
    public int size() {
        return stack.size() + base.size();
    }

    @Override
    public boolean contains(final Object o) {
        final PathArgument srch = (PathArgument) Preconditions.checkNotNull(o);
        return stack.contains(srch) || base.contains(srch);
    }

    @Override
    public PathArgument get(final int index) {
        if (index < base.size()) {
            return base.get(index);
        }
        return stack.get(index - base.size());
    }

    @Override
    public int indexOf(final Object o) {
        final PathArgument srch = (PathArgument) Preconditions.checkNotNull(o);

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
    public int lastIndexOf(final Object o) {
        final PathArgument srch = (PathArgument) Preconditions.checkNotNull(o);

        final int ret = stack.lastIndexOf(srch);
        if (ret != -1) {
            return base.size() + ret;
        }

        return base.lastIndexOf(srch);
    }

    @Override
    public UnmodifiableIterator<PathArgument> iterator() {
        return new IteratorImpl(base, stack);
    }

    private static final class IteratorImpl extends UnmodifiableIterator<PathArgument> {
        private final Iterator<PathArgument> stack;
        private final Iterator<PathArgument> base;

        IteratorImpl(final Iterable<PathArgument> base, final Iterable<PathArgument> stack) {
            this.base = base.iterator();
            this.stack = stack.iterator();
        }

        @Override
        public boolean hasNext() {
            return stack.hasNext();
        }

        @Override
        public PathArgument next() {
            if (base.hasNext()) {
                return base.next();
            }
            return stack.next();
        }
    }
}
