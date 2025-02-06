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

class Mdsal335Test {
    @Test
    void mdsal335Test() {
        final var generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal335.yang"));
        assertNotNull(generateTypes);
        assertEquals(3, generateTypes.size());

        final var gen = generateTypes.stream()
            .filter(type -> type.getFullyQualifiedName()
                .equals("org.opendaylight.yang.gen.v1.mdsal335.norev.Ipv4AddressNoZone"))
            .findFirst().orElseThrow();
        assertEquals(1, gen.getConstantDefinitions().size());
    }
}
