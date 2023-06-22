/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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

class LeafrefResolutionTest {
    @Test
    void testLeafRefRelativeSelfReference() {
        final var schemaContext = YangParserTestUtils.parseYangResource("/leafref-relative-invalid.yang");
        final var iae = assertThrows(IllegalArgumentException.class,
            () -> DefaultBindingGenerator.generateFor(schemaContext));
        assertEquals(
            "Circular leafref chain detected at leaf (urn:xml:ns:yang:lrr?revision=2015-02-25)neighbor-id",
            iae.getMessage());
    }

    @Test
    void testLeafRefAbsoluteSelfReference() {
        final var schemaContext = YangParserTestUtils.parseYangResource("/leafref-absolute-invalid.yang");
        final var iae = assertThrows(IllegalArgumentException.class,
            () -> DefaultBindingGenerator.generateFor(schemaContext));
        assertEquals(
            "Circular leafref chain detected at leaf (urn:xml:ns:yang:lra?revision=2015-02-25)neighbor-id",
            iae.getMessage());
    }

    @Test
    void testLeafRefRelativeAndAbsoluteValidReference() {
        final var types = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/leafref-valid.yang"));
        assertEquals(2, types.size());

        final var neighborMethods = types.stream()
            .filter(type -> type.getName().equals("Neighbor"))
            .findFirst()
            .orElseThrow()
            .getMethodDefinitions();
        assertEquals(10, neighborMethods.size());

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
