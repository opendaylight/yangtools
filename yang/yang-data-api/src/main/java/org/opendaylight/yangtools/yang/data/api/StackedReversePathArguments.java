/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class StackedReversePathArguments implements Iterable<PathArgument> {
    private final StackedYangInstanceIdentifier identifier;

    StackedReversePathArguments(final StackedYangInstanceIdentifier identifier) {
        this.identifier = Preconditions.checkNotNull(identifier);
    }

    @Override
    public Iterator<PathArgument> iterator() {
        return new IteratorImpl(identifier);
    }

    private static final class IteratorImpl extends UnmodifiableIterator<PathArgument> {
        private StackedYangInstanceIdentifier identifier;
        private Iterator<PathArgument> tail;

        IteratorImpl(final StackedYangInstanceIdentifier identifier) {
            this.identifier = Preconditions.checkNotNull(identifier);
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
            if (!(next instanceof StackedYangInstanceIdentifier)) {
                tail = next.getReversePathArguments().iterator();
                identifier = null;
            } else {
                identifier = (StackedYangInstanceIdentifier) next;
            }

            return ret;
        }
    }
}
