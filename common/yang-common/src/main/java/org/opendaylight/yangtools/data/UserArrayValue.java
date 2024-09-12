/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.data;

import com.google.common.collect.Iterators;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Ordering;

/**
 * An {@link Ordering#USER}-ordered {@link ArrayValue}.
 *
 * @param <E> type of individual elements
 */
@NonNullByDefault
public abstract non-sealed class UserArrayValue<E extends ScalarValue> extends ArrayValue<E> {
    @Override
    public final Ordering ordering() {
        return Ordering.USER;
    }

    @Override
    public final int hashCode() {
        int hash = 1;
        for (var obj : this) {
            hash = 31 * hash + obj.hashCode();
        }
        return hash;
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof UserArrayValue<?> other && size() == other.size()
            && Iterators.elementsEqual(iterator(), other.iterator());
    }
}