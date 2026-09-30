/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.VerifyException;
import org.junit.jupiter.api.Test;

class BlockBuilderTest {
    // Note: behavior selected by environment variable
    private final BlockBuilder bb = new BlockBuilder();

    @Test
    void buildIndented() {
        bb.oB();
        final var ex = assertThrows(VerifyException.class, bb::build);
        assertEquals("leftover indentation depth 1", ex.getMessage());
    }

    @Test
    void buildIndentedTwice() {
        bb.oB().oB();
        final var ex = assertThrows(VerifyException.class, bb::build);
        assertEquals("leftover indentation depth 2", ex.getMessage());
    }

    @Test
    void buildIndentedReopen() {
        bb.oB().cb().oB();
        final var ex = assertThrows(VerifyException.class, bb::build);
        assertEquals("leftover indentation depth 1", ex.getMessage());
    }
}
