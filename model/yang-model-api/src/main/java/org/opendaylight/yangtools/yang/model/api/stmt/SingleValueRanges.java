/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link ValueRanges} encompassing a single {@link ValueRange}.
 */
// FIXME: JEP-401 when available
@NonNullByDefault
record SingleValueRanges(ValueRange range) implements ValueRanges {
    SingleValueRanges {
        requireNonNull(range);
    }

    @Override
    public Iterator<ValueRange> iterator() {
        return Iterators.singletonIterator(range);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public List<ValueRange> asList() {
        return List.of(range);
    }

    @Override
    public String toString() {
        return "[" + range + "]";
    }
}
