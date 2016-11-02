/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import com.google.common.collect.UnmodifiableIterator;
import java.util.AbstractList;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

abstract class PathArgumentList extends AbstractList<PathArgument> {
    @Nonnull
    @Override
    public abstract UnmodifiableIterator<PathArgument> iterator();

    @Override
    public final boolean isEmpty() {
        return false;
    }

    @Override
    public final boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(@Nonnull final Collection<? extends PathArgument> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean removeAll(@Nonnull final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean retainAll(@Nonnull final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean addAll(final int index, final Collection<? extends PathArgument> c) {
        throw new UnsupportedOperationException();
    }
}
