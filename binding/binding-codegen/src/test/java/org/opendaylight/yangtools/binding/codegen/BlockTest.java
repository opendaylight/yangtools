/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BlockTest {
    @Test
    void emptyOneToBuilder() {
        final var bb = Block.builder();
        Block1.EMPTY.appendTo(bb);
        assertEquals("\n", bb.toRawString());
    }

    @Test
    void emptyTwoToBuilder() {
        final var bb = Block.builder();
        Block2.EMPTY.appendTo(bb);
        assertEquals("\n\n", bb.toRawString());
    }
}
