/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterables;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An {@link Iterable} which concatenates another {@link Iterable} with a subsequent element.
 */
@NonNullByDefault
record AppendIterable<T>(Iterable<? extends T> others, T last) implements Iterable<T> {
    AppendIterable {
        requireNonNull(others);
        requireNonNull(last);
    }

    @Override
    public Iterator<T> iterator() {
        return new AppendIterator<>(others.iterator(), last);
    }

    @Override
    public String toString() {
        return Iterables.toString(this);
    }
}
