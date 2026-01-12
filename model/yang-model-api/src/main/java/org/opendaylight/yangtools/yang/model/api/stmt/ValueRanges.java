/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * One or more {@link ValueRange}s.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface ValueRanges extends Iterable<ValueRange> permits RegularValueRanges, SingleValueRanges {
    /**
     * {@return the number of {@link ValueRange}} contained in this object
     */
    int size();

    /**
     * {@return an equivalent {@code List<ValueRange>}}
     */
    // FIXME: guarantee ordering and de-duplication
    List<ValueRange> asList();

    /**
     * Return a {@link ValueRanges} encompassing exactly one {@link ValueRange}..
     *
     * @param range the range
     * @return a {@link ValueRanges} instance
     */
    static ValueRanges of(final ValueRange range) {
        return new SingleValueRanges(range);
    }

    /**
     * Return a {@link ValueRanges} encompassing exactly one {@link ValueRange}..
     *
     * @param first the first {@link ValueRange}
     * @param others the subsequent {@link ValueRange}s
     * @return a {@link ValueRanges} instance
     */
    static ValueRanges of(final ValueRange first, final ValueRange... others) {
        return others.length == 0 ? of(first) : of(Stream.concat(Stream.of(first), Arrays.stream(others)).toList());
    }

    /**
     * Return a {@link ValueRanges} encompassing specified ranges.
     *
     * @param ranges the ranges
     * @return a {@link ValueRanges} instance
     * @throws IllegalArgumentException if {@code ranges} is empty
     */
    static ValueRanges of(final List<ValueRange> ranges) {
        // remove any duplicates and collect to an immutable list
        final var copy = ranges.stream().distinct().collect(Collectors.toUnmodifiableList());
        return switch (copy.size()) {
            case 0 -> throw new IllegalArgumentException("empty ranges");
            case 1 -> new SingleValueRanges(copy.getFirst());
            default -> new RegularValueRanges(copy);
        };
    }
}
