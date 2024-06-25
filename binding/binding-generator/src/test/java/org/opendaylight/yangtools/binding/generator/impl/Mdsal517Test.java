/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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

public class Mdsal517Test extends AbstractOpaqueTest {
    @Test
    public void augmentActionInputInGroupingTest() {
        final var types = DefaultBindingGenerator.generateFor(
                YangParserTestUtils.parseYangResourceDirectory("/mdsal-517"));
        assertNotNull(types);
        assertEquals(12, types.size());
    }
}
