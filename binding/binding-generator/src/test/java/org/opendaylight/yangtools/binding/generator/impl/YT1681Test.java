/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1681Test {
    @Test
    void testLeafRefCircularReference() {
        // Test that leafref cycle is detected and exception is thrown
        final var modelContext =
            YangParserTestUtils.parseYangResource("/yt1681/leafref-relative-circular.yang");
        final var iae = assertThrows(IllegalArgumentException.class,
            () -> DefaultBindingGenerator.generateFor(modelContext));
        assertEquals(
            "Circular leafref chain detected at leaf (urn:xml:ns:yang:lrc?revision=2023-06-22)neighbor3-id",
            iae.getMessage());
    }

    @Test
    void testLeafRefValidCircularReference() {
        // Test if valid leafref chain is resolved correctly
        final var types = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/yt1681/leafref-valid-chain.yang"));
        assertEquals(2, types.size());

        final var neighborMethods = types.stream()
            .filter(type -> type.getName().equals("Neighbor"))
            .findFirst()
            .orElseThrow()
            .getMethodDefinitions();
        assertEquals(14, neighborMethods.size());

        final var getNeighborId = neighborMethods.stream()
            .filter(method -> method.getName().equals("getNeighborId"))
            .findFirst()
            .orElseThrow();
        assertEquals(Types.STRING, getNeighborId.getReturnType());

        final var getNeighbor2Id = neighborMethods.stream()
            .filter(method -> method.getName().equals("getNeighbor2Id"))
            .findFirst()
            .orElseThrow();
        assertEquals(Types.STRING, getNeighbor2Id.getReturnType());
    }
}
