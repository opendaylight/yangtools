/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Collection;
import java.util.Iterator;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class StackedPathArguments extends PathArgumentCollection {
    private final Collection<StackedYangInstanceIdentifier> stack;
    private final Collection<PathArgument> base;

    public StackedPathArguments(final YangInstanceIdentifier base, final Collection<StackedYangInstanceIdentifier> stack) {
        this.base = base.getPathArguments();
        this.stack = Preconditions.checkNotNull(stack);
    }

    @Override
    public int size() {
        return stack.size() + base.size();
    }

    @Override
    public UnmodifiableIterator<PathArgument> iterator() {
        return new IteratorImpl(base, stack);
    }

    private static final class IteratorImpl extends UnmodifiableIterator<PathArgument> {
        private final Iterator<StackedYangInstanceIdentifier> stack;
        private final Iterator<PathArgument> base;

        IteratorImpl(final Iterable<PathArgument> base, final Iterable<StackedYangInstanceIdentifier> stack) {
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
            return stack.next().getLastPathArgument();
        }
    }
}
