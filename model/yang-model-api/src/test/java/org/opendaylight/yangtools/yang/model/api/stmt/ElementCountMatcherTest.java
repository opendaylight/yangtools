/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsArgument.Bounded;

class ElementCountMatcherTest {
    private final @NonNull MinElementsArgument minOne = MinElementsArgument.of(1);
    private final @NonNull Bounded maxOne = MaxElementsArgument.of(1);

    @Test
    void atLeastZero() {
        assertNull(ElementCountMatcher.atLeast(MinElementsArgument.of(0)));
    }

    @Test
    void atLeastOne() {
        assertSame(minOne, ElementCountMatcher.atLeast(minOne));
    }

    @Test
    void atMostUnbounded() {
        assertNull(ElementCountMatcher.atMost(MaxElementsArgument.of()));
    }

    @Test
    void atMostOne() {
        assertSame(maxOne, ElementCountMatcher.atMost(maxOne));
    }

    @Test
    void ofNullable() {
        assertNull(ElementCountMatcher.ofNullable(null, null));
        assertNull(ElementCountMatcher.ofNullable(null, MaxElementsArgument.of()));
        assertNull(ElementCountMatcher.ofNullable(MinElementsArgument.of(0), MaxElementsArgument.of()));
        assertNull(ElementCountMatcher.ofNullable(MinElementsArgument.of(0), null));

        assertSame(minOne, ElementCountMatcher.ofNullable(minOne, null));
        assertSame(minOne, ElementCountMatcher.ofNullable(minOne, MaxElementsArgument.of()));
        assertSame(maxOne, ElementCountMatcher.ofNullable(null, maxOne));
        assertSame(maxOne, ElementCountMatcher.ofNullable(MinElementsArgument.of(0), maxOne));

        final var range = assertInstanceOf(ElementCountRange.class, ElementCountMatcher.ofNullable(minOne, maxOne));
        assertNotNull(range.matches(0));
        assertNull(range.matches(1));
        assertNotNull(range.matches(2));
    }
}
