/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal437Test extends AbstractOpaqueTest {
    @Test
    public void generateAnyxmlTest() {
        final var types = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource("/mdsal437.yang"));
        assertNotNull(types);
        assertEquals(7, types.size());

        assertOpaqueNode(types, "mdsal437", "", "Any");
        assertOpaqueNode(types, "mdsal437", ".cont", "Cont");
        assertOpaqueNode(types, "mdsal437", ".grp", "Grp");
    }
}
