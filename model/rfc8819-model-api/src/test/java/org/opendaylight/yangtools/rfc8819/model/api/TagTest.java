/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;

class TagTest {
    @Test
    void testIetfTag() {
        final var tag = new Tag("ietf:first-tag");
        assertTrue(tag.hasPrefix(Prefix.IETF));
        assertFalse(tag.hasPrefix(Prefix.USER));
        assertFalse(tag.hasPrefix(Prefix.VENDOR));
    }

    @Test
    void testUserTag() {
        final var tag = new Tag("user:first-tag");
        assertFalse(tag.hasPrefix(Prefix.IETF));
        assertTrue(tag.hasPrefix(Prefix.USER));
        assertFalse(tag.hasPrefix(Prefix.VENDOR));
    }

    @Test
    void testVendorTag() {
        final var tag = new Tag("vendor:first-tag");
        assertFalse(tag.hasPrefix(Prefix.IETF));
        assertFalse(tag.hasPrefix(Prefix.USER));
        assertTrue(tag.hasPrefix(Prefix.VENDOR));
    }

    @Test
    void testIfIsTagInvalid() {
        assertIllegalArgumentException("");
        assertIllegalArgumentException("\n");
        assertIllegalArgumentException("\t");
        assertIllegalArgumentException("ietf:tag\ntag");
    }

    private static void assertIllegalArgumentException(final @NonNull String input) {
        final var e = assertThrows(IllegalArgumentException.class, () -> new Tag(input));
        assertEquals("Invalid tag value '" + input + "'", e.getMessage());
    }
}
