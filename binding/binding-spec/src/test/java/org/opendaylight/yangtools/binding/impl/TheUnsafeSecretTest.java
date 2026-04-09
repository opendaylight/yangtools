/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.binding.impl.TheUnsafeSecret.INSTANCE;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.lib.CodeHelpers;

class TheUnsafeSecretTest {
    @Test
    void trivials() {
        assertNotNull(INSTANCE);

        final var first = new TheUnsafeSecret();
        final var second = new TheUnsafeSecret();
        assertNotEquals(INSTANCE, first);
        assertEquals(first, first);
        assertNotEquals(first, second);
    }

    @Test
    void codeHelpersIntegration() {
        final var alien = new TheUnsafeSecret();
        final var ex = assertThrows(LinkageError.class, () -> CodeHelpers.verifySecret(alien));
        assertEquals("UnsafeSecret mismatch: expecting " + INSTANCE + ", got " + alien, ex.getMessage());
    }
}
