/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Test leafref resolution when the leaf is from a grouping.
 */
class Mdsal182Test {
    @Test
    void testOneUpLeafref() {
        final var types = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal-182/good-leafref.yang"));
        assertEquals(6, types.size());
    }

    @Test
    void testTwoUpLeafref() {
        final var types = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal-182/grouping-leafref.yang"));
        assertEquals(4, types.size());
    }
}
