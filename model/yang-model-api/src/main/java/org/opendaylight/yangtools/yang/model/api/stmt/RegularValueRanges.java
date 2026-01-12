/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link ValueRanges} encompassing more than one {@link ValueRange}.
 */
@NonNullByDefault
record RegularValueRanges(List<ValueRange> ranges) implements ValueRanges {
    RegularValueRanges {
        requireNonNull(ranges);
    }

    @Override
    public int size() {
        return ranges.size();
    }

    @Override
    public Iterator<ValueRange> iterator() {
        return ranges.iterator();
    }

    @Override
    public List<ValueRange> asList() {
        return ranges;
    }
}
