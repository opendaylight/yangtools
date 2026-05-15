/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.yt1885.norev.Four;
import org.opendaylight.yang.gen.v1.yt1885.norev.Two;

class YT1885Test {
    @Test
    void restrictedBitsAreFalse() {
        final var two = new Two(true, false);
        assertTrue(two.getOne());
        assertFalse(two.getTwo());
        assertFalse(two.getThree());
        assertFalse(two.getFour());
    }

    @Test
    void restrictedBitsAreRejected() {
        final var four = new Four(false, false, true, false);
        final var ex = assertThrows(IllegalArgumentException.class, () -> new Two(four));
        assertEquals("Invalid bit: three", ex.getMessage());
    }

    @Test
    void unrestrictedBitsAreCopied() {
        assertEquals(new Two(false, true), new Two(new Four(false, false, false, true)));
        assertEquals(new Two(true, false), new Two(new Four(false, true, false, false)));
    }
}
