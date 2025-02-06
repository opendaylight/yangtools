/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal302Test {
    @Test
    void testRpcUsesAugment() {
        final var types = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource("/mdsal302.yang"));
        assertNotNull(types);
        assertEquals(15, types.size());
    }
}
