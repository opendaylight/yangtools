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
import static org.opendaylight.yangtools.binding.generator.impl.SupportTestUtil.containsAttributes;
import static org.opendaylight.yangtools.binding.generator.impl.SupportTestUtil.containsMethods;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class BitAndUnionTOEnclosingTest {
    private static List<GeneratedType> genTypes = null;
    private static GeneratedType parentContainer = null;

    @BeforeAll
    static void loadTestResources() {
        genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource("/bit_and_union.yang"));

        for (var genType : genTypes) {
            if (genType.simpleName().equals("ParentContainer") && !(genType instanceof GeneratedTransferObject)) {
                parentContainer = genType;
            }
        }
    }

    @Test
    void testNestedTypesInLeaf() {
        final var enclosedTypes = parentContainer.getEnclosedTypes();
        assertEquals(3, enclosedTypes.size());

        // nested types in leaf
        final var lfLeafs = enclosedTypes.stream()
            .filter(genType -> genType.simpleName().equals("Lf"))
            .map(genType -> assertInstanceOf(GeneratedTransferObject.class, genType))
            .collect(Collectors.toList());
        assertEquals(1, lfLeafs.size(), "Lf TO has incorrect number of occurences.");
        final var lfLeaf = lfLeafs.getFirst();
        assertEquals(
            JavaTypeName.create("org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626", "ParentContainer"),
            lfLeaf.name().immediatelyEnclosingClass());

        assertEquals(2, lfLeaf.getProperties().size(), "Lf generated TO has incorrect number of properties");
        containsAttributes(lfLeaf, true, true, true, new NameTypePattern("string", "String"));
        containsAttributes(lfLeaf, true, false, true, new NameTypePattern("lf$1", "Lf$1"));

        // nested types in Lf
        final var lfTypes = lfLeaf.getEnclosedTypes();
        assertEquals(1, lfTypes.size());

        final var lf1Leaf = lfTypes.getFirst();
        assertEquals("Lf$1", lf1Leaf.simpleName());
        assertEquals(
            "org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626.ParentContainer.Lf",
            lf1Leaf.name().immediatelyEnclosingClass().toString());

        final var lf1gto = assertInstanceOf(GeneratedTransferObject.class, lf1Leaf);
        assertEquals(4, lf1Leaf.getProperties().size(), "Lf$1 generated TO has incorrect number of properties");
        containsAttributes(lf1gto, true, true, true, new NameTypePattern("uint32", "Uint32"));
        containsAttributes(lf1gto, true, true, true, new NameTypePattern("int8", "Byte"));
        containsAttributes(lf1gto, true, true, true, new NameTypePattern("string", "String"));
        containsAttributes(lf1gto, true, false, true, new NameTypePattern("lf$2", "Lf$2"));

        // nested types in Lf1
        final var lf1Types = lf1Leaf.getEnclosedTypes();
        assertEquals(1, lf1Types.size());

        final var lf2Leaf = lf1Types.get(0);
        assertEquals("Lf$2", lf2Leaf.simpleName());
        assertEquals("org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626.ParentContainer.Lf.Lf$1",
            lf2Leaf.name().immediatelyEnclosingClass().toString());


        final var lf2gto = assertInstanceOf(GeneratedTransferObject.class, lf2Leaf);
        assertEquals(2, lf2Leaf.getProperties().size(), "Lf generated TO has incorrect number of properties");
        containsAttributes(lf2gto, true, true, true, new NameTypePattern("string", "String"));
        containsAttributes(lf2gto, true, true, true, new NameTypePattern("uint64", "Uint64"));
    }

    @Test
    void testNestedTypesInTypedef() {
        GeneratedTransferObject typeUnionTypedef = null;
        int typeUnionTypedefCounter = 0;

        for (var genType : genTypes) {
            if (genType.simpleName().equals("TypeUnion") && genType instanceof GeneratedTransferObject gto) {
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

        assertEquals(2, typeUnionTypedef.getProperties().size(),
            "TypeUnion generated TO has incorrect number of properties");
        containsAttributes(typeUnionTypedef, true, true, true, new NameTypePattern("string", "String"));
        containsAttributes(typeUnionTypedef, true, false, true, new NameTypePattern("typeUnion$1", "TypeUnion$1"));

        final var nestedUnions = typeUnionTypedef.getEnclosedTypes();
        assertEquals(1, nestedUnions.size(), "Incorrect number of nested unions");

        final var typeUnion1 = nestedUnions.getFirst();
        assertEquals("TypeUnion$1", typeUnion1.simpleName());
        assertEquals("org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626", typeUnion1.packageName(),
            "TypeUnion$1 has incorrect package name.");
        assertEquals(4, typeUnion1.getProperties().size(),
            "TypeUnion1 generated TO has incorrect number of properties");

        final var typeUnion1gto = assertInstanceOf(GeneratedTransferObject.class, typeUnion1);
        containsAttributes(typeUnion1gto, true, true, true, new NameTypePattern("uint32", "Uint32"));
        containsAttributes(typeUnion1gto, true, true, true, new NameTypePattern("int8", "Byte"));
        containsAttributes(typeUnion1gto, true, true, true, new NameTypePattern("string", "String"));
        containsAttributes(typeUnion1gto, true, false, true, new NameTypePattern("typeUnion$2", "TypeUnion$2"));


        final var nestedUnions1 = typeUnion1.getEnclosedTypes();
        assertEquals(1, nestedUnions1.size());

        final var typeUnion2 = nestedUnions1.get(0);
        assertEquals("TypeUnion$2", typeUnion2.simpleName());
        assertEquals("org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626", typeUnion2.packageName(),
            "TypeUnion$2 has incorrect package name.");
        assertEquals(2, typeUnion2.getProperties().size(),
            "TypeUnion2 generated TO has incorrect number of properties");

        final var typeUnion2gto = assertInstanceOf(GeneratedTransferObject.class, typeUnion2);
        containsAttributes(typeUnion2gto, true, true, true, new NameTypePattern("string", "String"));
        containsAttributes(typeUnion2gto, true, true, true, new NameTypePattern("uint64", "Uint64"));
    }

    @Test
    public void bitAndUnionEnclosingTest() {
        assertNotNull(parentContainer, "Parent container object wasn't found.");
        containsMethods(parentContainer, new NameTypePattern("getLf", "Lf"));

        GeneratedTransferObject bitLeaf = null;
        GeneratedTransferObject unionLeaf = null;
        for (var genType : parentContainer.getEnclosedTypes()) {
            if (genType instanceof GeneratedTransferObject gto) {
                if (gto.simpleName().equals("BitLeaf")) {
                    bitLeaf = gto;
                } else if (gto.simpleName().equals("UnionLeaf")) {
                    unionLeaf = gto;
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

        GeneratedProperty firstBitProperty = null;
        GeneratedProperty secondBitProperty = null;
        GeneratedProperty thirdBitProperty = null;
        for (var genProperty : bitLeaf.getProperties()) {
            if (genProperty.getName().equals("firstBit")) {
                firstBitProperty = genProperty;
            } else if (genProperty.getName().equals("secondBit")) {
                secondBitProperty = genProperty;
            } else if (genProperty.getName().equals("thirdBit")) {
                thirdBitProperty = genProperty;
            }
        }

        assertNotNull(firstBitProperty, "firstBit property wasn't found");
        assertEquals("boolean", firstBitProperty.getReturnType().simpleName());
        assertNotNull(secondBitProperty, "secondBit property wasn't found");
        assertEquals("boolean", secondBitProperty.getReturnType().simpleName());
        assertNotNull(thirdBitProperty, "thirdBit property wasn't found");
        assertEquals("boolean", thirdBitProperty.getReturnType().simpleName());

        GeneratedProperty uint32Property = null;
        GeneratedProperty stringProperty = null;
        GeneratedProperty uint8Property = null;
        for (var genProperty : unionLeaf.getProperties()) {
            if (genProperty.getName().equals("int32")) {
                uint32Property = genProperty;
            } else if (genProperty.getName().equals("string")) {
                stringProperty = genProperty;
            } else if (genProperty.getName().equals("uint8")) {
                uint8Property = genProperty;
            }
        }

        assertNotNull(uint32Property, "uint32 property wasn't found");
        assertEquals("Integer", uint32Property.getReturnType().simpleName());
        assertNotNull(stringProperty, "string property wasn't found");
        assertEquals("String", stringProperty.getReturnType().simpleName());
        assertNotNull(uint8Property, "uint8 property wasn't found");
        assertEquals("Uint8", uint8Property.getReturnType().simpleName());
    }
}
