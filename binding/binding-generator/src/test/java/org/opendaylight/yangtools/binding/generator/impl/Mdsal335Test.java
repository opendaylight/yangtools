/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal335Test {
    @Test
    void mdsal335Test() {
        final var generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal335.yang"));
        assertNotNull(generateTypes);
        assertEquals(3, generateTypes.size());

        final var gen = assertInstanceOf(ScalarTypeObjectArchetype.class, generateTypes.stream()
            .filter(type -> type.canonicalName()
                .equals("org.opendaylight.yang.gen.v1.mdsal335.norev.Ipv4AddressNoZone"))
            .findFirst().orElseThrow());
        final var restrictions = gen.getRestrictions();
        assertFalse(restrictions.isEmpty());
        final var patterns = restrictions.getPatternConstraints();
        assertEquals(1, patterns.size());
        assertEquals("[0-9\\.]*", patterns.getFirst().getRegularExpressionString());
    }
}
