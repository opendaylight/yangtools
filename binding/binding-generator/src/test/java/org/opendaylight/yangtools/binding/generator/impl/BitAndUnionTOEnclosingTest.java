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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.opendaylight.yangtools.binding.generator.impl.SupportTestUtil.containsMethods;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class BitAndUnionTOEnclosingTest {
    private static List<Archetype> genTypes = null;
    private static LegacyArchetype parentContainer = null;

    @BeforeAll
    static void loadTestResources() {
        genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource("/bit_and_union.yang"));

        for (var genType : genTypes) {
            if (genType.simpleName().equals("ParentContainer") && genType instanceof LegacyArchetype archetype) {
                parentContainer = archetype;
            }
        }
    }

    @Test
    void testNestedTypesInLeaf() {
        final var enclosedTypes = parentContainer.enclosedTypes();
        assertEquals(3, enclosedTypes.size());

        // nested types in leaf
        final var lfLeafs = enclosedTypes.stream()
            .filter(genType -> genType.simpleName().equals("Lf"))
            .map(genType -> assertInstanceOf(UnionTypeObjectArchetype.class, genType))
            .toList();
        assertEquals(1, lfLeafs.size(), "Lf TO has incorrect number of occurences.");
        final var lfLeaf = lfLeafs.getFirst();
        assertEquals(
            JavaTypeName.create("org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626", "ParentContainer"),
            lfLeaf.name().immediatelyEnclosingClass());

        // nested types in Lf
        final var lfTypes = lfLeaf.enclosedTypes();
        assertEquals(1, lfTypes.size());

        final var lf1Leaf = assertInstanceOf(UnionTypeObjectArchetype.class, lfTypes.getFirst());
        assertEquals("Lf$1", lf1Leaf.simpleName());
        assertEquals(
            "org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626.ParentContainer.Lf",
            lf1Leaf.name().immediatelyEnclosingClass().toString());

        assertEquals(List.of("string", "lf$1"), lfLeaf.typePropertyNames());
        assertEquals(List.of(BaseYangTypes.STRING_TYPE, lf1Leaf), lfLeaf.typePropertyTypes());

        // nested types in Lf1
        final var lf1Types = lf1Leaf.enclosedTypes();
        assertEquals(1, lf1Types.size());

        final var lf2Leaf = assertInstanceOf(UnionTypeObjectArchetype.class, lf1Types.getFirst());
        assertEquals("Lf$2", lf2Leaf.simpleName());
        assertEquals("org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626.ParentContainer.Lf.Lf$1",
            lf2Leaf.name().immediatelyEnclosingClass().toString());
        assertEquals(List.of("string", "uint64"), lf2Leaf.typePropertyNames());
        assertEquals(List.of(BaseYangTypes.STRING_TYPE, BaseYangTypes.UINT64_TYPE), lf2Leaf.typePropertyTypes());

        assertEquals(List.of("uint32", "int8", "string", "lf$2"), lf1Leaf.typePropertyNames());
        assertEquals(List.of(BaseYangTypes.UINT32_TYPE, BaseYangTypes.INT8_TYPE, BaseYangTypes.STRING_TYPE, lf2Leaf),
            lf1Leaf.typePropertyTypes());
    }

    @Test
    void testNestedTypesInTypedef() {
        UnionTypeObjectArchetype typeUnionTypedef = null;
        int typeUnionTypedefCounter = 0;

        for (var genType : genTypes) {
            if (genType.simpleName().equals("TypeUnion") && genType instanceof UnionTypeObjectArchetype gto) {
                typeUnionTypedef = gto;
                typeUnionTypedefCounter++;
            }
        }

        assertNotNull(typeUnionTypedef, "TypeUnion TO wasn't found.");
        assertEquals(1, typeUnionTypedefCounter, "TypeUnion TO has incorrect number of occurences.");

        assertNotNull(typeUnionTypedef, "TypeUnion TO wasn't found.");
        assertEquals(1, typeUnionTypedefCounter, "TypeUnion TO has incorrect number of occurences.");
        assertEquals("org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626", typeUnionTypedef.packageName(),
            "TypeUnion has incorrect package name.");

        final var nestedUnions = typeUnionTypedef.enclosedTypes();
        assertEquals(1, nestedUnions.size(), "Incorrect number of nested unions");
        final var typeUnion1 = assertInstanceOf(UnionTypeObjectArchetype.class, nestedUnions.getFirst());
        assertEquals("TypeUnion$1", typeUnion1.simpleName());
        assertEquals("org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626", typeUnion1.packageName(),
            "TypeUnion$1 has incorrect package name.");

        assertEquals(List.of("string", "typeUnion$1"), typeUnionTypedef.typePropertyNames());
        assertEquals(List.of(BaseYangTypes.STRING_TYPE, typeUnion1), typeUnionTypedef.typePropertyTypes());

        final var nestedUnions1 = typeUnion1.enclosedTypes();
        assertEquals(1, nestedUnions1.size());
        final var typeUnion2 = assertInstanceOf(UnionTypeObjectArchetype.class, nestedUnions1.getFirst());
        assertEquals("TypeUnion$2", typeUnion2.simpleName());
        assertEquals("org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626", typeUnion2.packageName(),
            "TypeUnion$2 has incorrect package name.");

        assertEquals(List.of("uint32", "int8", "string", "typeUnion$2"), typeUnion1.typePropertyNames());
        assertEquals(List.of(BaseYangTypes.UINT32_TYPE, BaseYangTypes.INT8_TYPE, BaseYangTypes.STRING_TYPE, typeUnion2),
            typeUnion1.typePropertyTypes());

        assertEquals(List.of("string", "uint64"), typeUnion2.typePropertyNames());
        assertEquals(List.of(BaseYangTypes.STRING_TYPE, BaseYangTypes.UINT64_TYPE), typeUnion2.typePropertyTypes());
    }

    @Test
    public void bitAndUnionEnclosingTest() {
        assertNotNull(parentContainer, "Parent container object wasn't found.");
        containsMethods(parentContainer, new NameTypePattern("getLf", "Lf"));

        BitsTypeObjectArchetype bitLeaf = null;
        UnionTypeObjectArchetype unionLeaf = null;
        for (var genType : parentContainer.enclosedTypes()) {
            if (genType instanceof TypeObjectArchetype<?> archetype) {
                if (archetype.simpleName().equals("BitLeaf")) {
                    bitLeaf = assertInstanceOf(BitsTypeObjectArchetype.class, archetype);
                } else if (archetype.simpleName().equals("UnionLeaf")) {
                    unionLeaf = assertInstanceOf(UnionTypeObjectArchetype.class, archetype);
                }
            }
        }

        assertNotNull(bitLeaf, "BitLeaf TO wasn't found.");
        assertNotNull(unionLeaf, "UnionLeaf TO wasn't found.");

        assertEquals(
            JavaTypeName.create("org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626", "ParentContainer"),
            bitLeaf.name().immediatelyEnclosingClass());
        assertEquals(
            JavaTypeName.create("org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626", "ParentContainer"),
            unionLeaf.name().immediatelyEnclosingClass());

        assertNull(bitLeaf.superType());
        final var bitsDef = bitLeaf.typeDefinition();
        assertEquals(QName.create("urn:bit:union:in:leaf", "2013-06-26", "bits"), bitsDef.getQName());
        assertEquals(3, bitsDef.getBits().size());

        assertEquals(List.of(), unionLeaf.enclosedTypes());
        assertEquals(List.of("int32", "string", "string", "string", "uint8"), unionLeaf.typePropertyNames());
        assertEquals(List.of(BaseYangTypes.INT32_TYPE, BaseYangTypes.STRING_TYPE, BaseYangTypes.UINT8_TYPE),
            unionLeaf.typePropertyTypes());

    }
}
