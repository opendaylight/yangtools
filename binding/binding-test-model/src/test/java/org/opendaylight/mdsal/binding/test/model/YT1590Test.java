/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.yt1885.norev.Four;
import org.opendaylight.yang.gen.v1.yt1885.norev.Two;

class YT1590Test {
    @Test
    void restrictedBitsStrings() {
        final var two = new Two(true, false);
        assertEquals("one", two.stringValue());
        final var parsed = Two.ofStringValue("one");
        assertEquals(two, parsed);
        assertEquals("Two{one}", parsed.toString());
    }

    @Test
    void unrestrictedBitsStrings() {
        final var four = new Four(true, true, false, true);
        assertEquals("one two four", four.stringValue());
        final var parsed = Four.ofStringValue("one two four");
        assertEquals(four, parsed);
        assertEquals("Four{one, two, four}", parsed.toString());
    }

    @Test
    void ofStringValueIsLenient() {
        final var four = new Four(true, true, false, true);
        // spaces do not matter
        assertEquals(four, Four.ofStringValue(" one    two four  "));
        // order does not matter
        assertEquals(four, Four.ofStringValue("one four two"));
        // duplicates do not matter
        assertEquals(four, Four.ofStringValue("one four two one four two"));
    }

    @Test
    void ofStringValueRejectsBadIdentifier() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> Two.ofStringValue("-"));
        assertEquals("\"-\" is not a valid bit name", ex.getMessage());
    }

    @Test
    void ofStringValueRejectsBadBit() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> Two.ofStringValue("_"));
        assertEquals("_ is not one of [one, two]", ex.getMessage());
    }
}
