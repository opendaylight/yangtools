/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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

class Mdsal531Test extends AbstractOpaqueTest {
    @Test
    void augmentGroupingTest() {
        final var types = DefaultBindingGenerator.generateFor(
                YangParserTestUtils.parseYangResourceDirectory("/mdsal-531"));
        assertNotNull(types);
        assertEquals(10, types.size());
    }
}
