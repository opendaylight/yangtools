/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class LeafrefResolutionTest {
    @Test
    public void testLeafRefRelativeSelfReference() {
        final EffectiveModelContext schemaContext =
            YangParserTestUtils.parseYangResource("/leafref-relative-invalid.yang");
        final IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
            () -> DefaultBindingGenerator.generateFor(schemaContext));
        assertEquals(
            "Effective model contains self-referencing leaf (urn:xml:ns:yang:lrr?revision=2015-02-25)neighbor-id",
            iae.getMessage());
    }

    @Test
    public void testLeafRefAbsoluteSelfReference() {
        final EffectiveModelContext schemaContext =
            YangParserTestUtils.parseYangResource("/leafref-absolute-invalid.yang");
        final IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
            () -> DefaultBindingGenerator.generateFor(schemaContext));
        assertEquals(
            "Effective model contains self-referencing leaf (urn:xml:ns:yang:lra?revision=2015-02-25)neighbor-id",
            iae.getMessage());
    }

    @Test
    public void testLeafRefRelativeAndAbsoluteValidReference() {
        final List<GeneratedType> types = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/leafref-valid.yang"));
        assertEquals(2, types.size());

        final List<MethodSignature> neighborMethods = types.stream()
            .filter(type -> type.getName().equals("Neighbor"))
            .findFirst()
            .orElseThrow()
            .getMethodDefinitions();
        assertEquals(10, neighborMethods.size());

        final MethodSignature getNeighborId = neighborMethods.stream()
            .filter(method -> method.getName().equals("getNeighborId"))
            .findFirst()
            .orElseThrow();
        assertEquals(Types.STRING, getNeighborId.getReturnType());

        final MethodSignature getNeighbor2Id = neighborMethods.stream()
            .filter(method -> method.getName().equals("getNeighbor2Id"))
            .findFirst()
            .orElseThrow();
        assertEquals(Types.STRING, getNeighbor2Id.getReturnType());
    }
}
