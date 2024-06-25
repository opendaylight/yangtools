/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.AbstractIterator;
import java.util.Iterator;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An {@link Iterator} which concatenates another {@link Iterator} with a subsequent element.
 */
final class AppendIterator<T> extends AbstractIterator<T> {
    private @Nullable Iterator<? extends T> others;
    private @Nullable T last;

    AppendIterator(final Iterator<? extends T> others, final T last) {
        this.others = requireNonNull(others);
        this.last = requireNonNull(last);
    }

    @Override
    protected T computeNext() {
        final var it = others;
        if (it != null) {
            final var ret = it.next();
            if (!it.hasNext()) {
                others = null;
            }
            return ret;
        }

        final var ret = last;
        if (ret != null) {
            last = null;
            return ret;
        }
        return endOfData();
    }
}