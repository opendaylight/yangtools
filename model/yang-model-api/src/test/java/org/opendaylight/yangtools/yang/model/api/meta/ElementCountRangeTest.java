/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.meta.ElementCountMatcher.TooFewElements;
import org.opendaylight.yangtools.yang.model.api.meta.ElementCountMatcher.TooManyElements;
import org.opendaylight.yangtools.yang.model.api.meta.ElementCountMatcher.Violation;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsArgument;

class ElementCountRangeTest {
    private final ElementCountRange range = new ElementCountRange(MinElementsArgument.of(2), MaxElementsArgument.of(5));

    @Test
    void testMatching() {
        assertNull(range.matches(2));
        assertNull(range.matches(2L));
        assertNull(range.matches(BigInteger.TWO));

        assertNull(range.matches(5));
        assertNull(range.matches(5L));
        assertNull(range.matches(BigInteger.valueOf(5L)));

        assertEquals("2", assertTooFew(range.matches(0)));
        assertEquals("2", assertTooFew(range.matches(0L)));
        assertEquals("2", assertTooFew(range.matches(BigInteger.ONE)));

        assertEquals("5", assertTooMany(range.matches(6)));
        assertEquals("5", assertTooMany(range.matches(7L)));
        assertEquals("5", assertTooMany(range.matches(BigInteger.TEN)));
    }

    @Test
    void toStringWorks() {
        assertEquals("[2..5]", range.toString());
    }

    private static String assertTooFew(final Violation violation) {
        return assertInstanceOf(TooFewElements.class, violation).minElements().toString();
    }

    private static String assertTooMany(final Violation violation) {
        return assertInstanceOf(TooManyElements.class, violation).maxElements().toString();
    }
}
