/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class GenerateInnerClassForBitsAndUnionInLeavesTest {
    @Test
    void testInnerClassCreationForBitsAndUnionsInLeafes() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/bit_and_union_in_leaf.yang"));
        assertEquals(4, genTypes.size());

        final var parentContainers = genTypes.stream()
            .filter(LegacyArchetype.class::isInstance)
            .map(LegacyArchetype.class::cast)
            .filter(archetype -> archetype.simpleName().equals("ParentContainer"))
            .toList();
        assertEquals(1, parentContainers.size());

        final var type = parentContainers.getFirst();
        final var enclosedTypes = type.getEnclosedTypes();
        assertEquals(2, enclosedTypes.size());

        final var bitLeaf = assertInstanceOf(BitsTypeObjectArchetype.class, enclosedTypes.getFirst());
        assertEquals("BitLeaf", bitLeaf.simpleName());
        final var def = bitLeaf.typeDefinition();
        assertEquals(QName.create("urn:bit:union:in:leaf", "2013-06-26", "bits"), def.getQName());
        assertEquals(3, def.getBits().size());

        final var unionLeaf = assertInstanceOf(UnionTypeObjectArchetype.class, enclosedTypes.getLast());
        assertEquals("UnionLeaf", unionLeaf.simpleName());

        assertEquals(List.of(), unionLeaf.getEnclosedTypes());
        assertEquals(List.of("int32", "string", "string", "string", "uint8"), unionLeaf.typePropertyNames());
        assertEquals(List.of(BaseYangTypes.INT32_TYPE, BaseYangTypes.STRING_TYPE, BaseYangTypes.UINT8_TYPE),
            unionLeaf.typePropertyTypes());
    }
}
