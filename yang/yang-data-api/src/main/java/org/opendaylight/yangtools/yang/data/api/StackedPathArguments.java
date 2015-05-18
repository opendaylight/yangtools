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
import java.util.List;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class StackedPathArguments implements Iterable<PathArgument> {
    private final List<StackedYangInstanceIdentifier> stack;
    private final YangInstanceIdentifier base;

    public StackedPathArguments(final YangInstanceIdentifier base, final List<StackedYangInstanceIdentifier> stack) {
        this.base = Preconditions.checkNotNull(base);
        this.stack = Preconditions.checkNotNull(stack);
    }

    @Override
    public Iterator<PathArgument> iterator() {
        return new IteratorImpl(base, stack);
    }

    private static final class IteratorImpl extends UnmodifiableIterator<PathArgument> {
        private final Iterator<StackedYangInstanceIdentifier> stack;
        private final Iterator<PathArgument> base;

        IteratorImpl(final YangInstanceIdentifier base, final Collection<StackedYangInstanceIdentifier> stack) {
            this.base = base.getPathArguments().iterator();
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
