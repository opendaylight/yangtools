/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import java.util.AbstractList;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Immutable;

@Beta
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
