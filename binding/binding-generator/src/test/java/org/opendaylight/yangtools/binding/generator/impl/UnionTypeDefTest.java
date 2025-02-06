/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class UnionTypeDefTest {
    @Test
    void unionTypeResolvingTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(
            UnionTypeDefTest.class, "/union-test-models/abstract-topology.yang", "/ietf-models/ietf-inet-types.yang"));
        assertEquals(29, genTypes.size());

        // TODO: implement test
    }

    @Test
    void unionTypedefLeafrefTest() {
        final var generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/bug8449.yang"));
        assertEquals(5, generateTypes.size());

        final var cont = generateTypes.stream()
            .filter(type -> type.getName().equals("Cont"))
            .findFirst()
            .orElseThrow();

        final var enclosedTypes = cont.getEnclosedTypes();
        assertEquals(1, enclosedTypes.size());

        final var refType = enclosedTypes.get(0);
        final var properties = refType.getProperties();
        assertEquals(1, properties.size());

        final var property = properties.get(0);
        assertEquals("stringRefValue", property.getName());
        assertEquals(Types.STRING, property.getReturnType());
    }
}
