/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.Mutable;

@Beta
// can assume non-threadsafety
public interface MutableIndexedList<I extends Identifier, T extends Identifiable<I>>
        extends IndexedList<I, T>, Mutable {

    // Note: isolated
    @NonNull ImmutableIndexedList<I, T> toImmutable();

    void replaceItem(int offset, T item);

    void insertItem(T item, int offset);

    default int insertItem(final T item) {
        final int ret = size();
        insertItem(item, ret);
        return ret;
    }

    default @Nullable T removeItem(final @NonNull I identifier) {
        final int offset = getItemOffset(identifier);
        return offset == -1 ? null : remove(offset);
    }
}
