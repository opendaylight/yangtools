/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
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

public class Mdsal352Test {
    @Test
    public void mdsal352Test() {
        final var generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal352.yang"));
        assertNotNull(generateTypes);
        assertEquals(6, generateTypes.size());
    }
}
