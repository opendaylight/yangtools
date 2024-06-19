/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.mdsal.binding.generator.impl.SupportTestUtil.containsAttributes;
import static org.opendaylight.mdsal.binding.generator.impl.SupportTestUtil.containsMethods;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BitAndUnionTOEnclosingTest {

    private static List<GeneratedType> genTypes = null;
    private static GeneratedType parentContainer = null;

    @BeforeClass
    public static void loadTestResources() {
        genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource("/bit_and_union.yang"));

        for (GeneratedType genType : genTypes) {
            if (genType.getName().equals("ParentContainer") && !(genType instanceof GeneratedTransferObject)) {
                parentContainer = genType;
            }
        }
    }

    @Test
    public void testNestedTypesInLeaf() {
        final List<GeneratedType> enclosedTypes = parentContainer.getEnclosedTypes();
        assertEquals(3, enclosedTypes.size());

        // nested types in leaf
        final List<GeneratedTransferObject> lfLeafs = enclosedTypes.stream()
            .filter(genType -> genType.getName().equals("Lf"))
            .map(genType -> {
                assertThat(genType, instanceOf(GeneratedTransferObject.class));
                return (GeneratedTransferObject) genType;
            })
            .collect(Collectors.toList());
        assertEquals("Lf TO has incorrect number of occurences.", 1, lfLeafs.size());
        GeneratedTransferObject lfLeaf = lfLeafs.get(0);
        assertEquals("Lf has incorrect package name.",
            "org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626.ParentContainer",
            lfLeaf.getIdentifier().immediatelyEnclosingClass().orElseThrow().toString());

        assertEquals("Lf generated TO has incorrect number of properties", 2, lfLeaf.getProperties().size());
        containsAttributes(lfLeaf, true, true, true, new NameTypePattern("string", "String"));
        containsAttributes(lfLeaf, true, false, true, new NameTypePattern("lf$1", "Lf$1"));

        // nested types in Lf
        final List<GeneratedType> lfTypes = lfLeaf.getEnclosedTypes();
        assertEquals(1, lfTypes.size());

        final GeneratedType lf1Leaf = lfTypes.get(0);
        assertEquals("Lf$1", lf1Leaf.getName());
        assertEquals("Lf$1 has incorrect package name.",
            "org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626.ParentContainer.Lf",
            lf1Leaf.getIdentifier().immediatelyEnclosingClass().orElseThrow().toString());

        assertThat(lf1Leaf, instanceOf(GeneratedTransferObject.class));
        final GeneratedTransferObject lf1gto = (GeneratedTransferObject) lf1Leaf;
        assertEquals("Lf$1 generated TO has incorrect number of properties", 4, lf1Leaf.getProperties().size());
        containsAttributes(lf1gto, true, true, true, new NameTypePattern("uint32", "Uint32"));
        containsAttributes(lf1gto, true, true, true, new NameTypePattern("int8", "Byte"));
        containsAttributes(lf1gto, true, true, true, new NameTypePattern("string", "String"));
        containsAttributes(lf1gto, true, false, true, new NameTypePattern("lf$2", "Lf$2"));

        // nested types in Lf1
        final List<GeneratedType> lf1Types = lf1Leaf.getEnclosedTypes();
        assertEquals(1, lf1Types.size());

        final GeneratedType lf2Leaf = lf1Types.get(0);
        assertEquals("Lf$2", lf2Leaf.getName());
        assertEquals("Lf$2 has incorrect package name.",
            "org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626.ParentContainer.Lf.Lf$1",
            lf2Leaf.getIdentifier().immediatelyEnclosingClass().orElseThrow().toString());


        assertThat(lf2Leaf, instanceOf(GeneratedTransferObject.class));
        final GeneratedTransferObject lf2gto = (GeneratedTransferObject) lf2Leaf;
        assertEquals("Lf generated TO has incorrect number of properties", 2, lf2Leaf.getProperties().size());
        containsAttributes(lf2gto, true, true, true, new NameTypePattern("string", "String"));
        containsAttributes(lf2gto, true, true, true, new NameTypePattern("uint64", "Uint64"));
    }

    @Test
    public void testNestedTypesInTypedef() {

        GeneratedTransferObject typeUnionTypedef = null;
        int typeUnionTypedefCounter = 0;

        for (GeneratedType genType : genTypes) {
            if (genType.getName().equals("TypeUnion") && genType instanceof GeneratedTransferObject) {
                typeUnionTypedef = (GeneratedTransferObject) genType;
                typeUnionTypedefCounter++;
            }
        }

        assertNotNull("TypeUnion TO wasn't found.", typeUnionTypedef);
        assertEquals("TypeUnion TO has incorrect number of occurences.", 1, typeUnionTypedefCounter);

        assertNotNull("TypeUnion TO wasn't found.", typeUnionTypedef);
        assertEquals("TypeUnion TO has incorrect number of occurences.", 1, typeUnionTypedefCounter);
        assertEquals("TypeUnion has incorrect package name.",
                "org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626", typeUnionTypedef.getPackageName());

        assertEquals("TypeUnion generated TO has incorrect number of properties", 2, typeUnionTypedef.getProperties()
                .size());
        containsAttributes(typeUnionTypedef, true, true, true, new NameTypePattern("string", "String"));
        containsAttributes(typeUnionTypedef, true, false, true, new NameTypePattern("typeUnion$1", "TypeUnion$1"));

        List<GeneratedType> nestedUnions = typeUnionTypedef.getEnclosedTypes();
        assertEquals("Incorrect number of nested unions", 1, nestedUnions.size());

        GeneratedType typeUnion1 = nestedUnions.get(0);
        assertEquals("TypeUnion$1", typeUnion1.getName());
        assertEquals("TypeUnion$1 has incorrect package name.",
            "org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626", typeUnion1.getPackageName());
        assertEquals("TypeUnion1 generated TO has incorrect number of properties", 4,
            typeUnion1.getProperties().size());

        assertThat(typeUnion1, instanceOf(GeneratedTransferObject.class));
        GeneratedTransferObject typeUnion1gto = (GeneratedTransferObject) typeUnion1;
        containsAttributes(typeUnion1gto, true, true, true, new NameTypePattern("uint32", "Uint32"));
        containsAttributes(typeUnion1gto, true, true, true, new NameTypePattern("int8", "Byte"));
        containsAttributes(typeUnion1gto, true, true, true, new NameTypePattern("string", "String"));
        containsAttributes(typeUnion1gto, true, false, true, new NameTypePattern("typeUnion$2", "TypeUnion$2"));


        List<GeneratedType> nestedUnions1 = typeUnion1.getEnclosedTypes();
        assertEquals(1, nestedUnions1.size());

        GeneratedType typeUnion2 = nestedUnions1.get(0);
        assertEquals("TypeUnion$2", typeUnion2.getName());
        assertEquals("TypeUnion$2 has incorrect package name.",
            "org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626", typeUnion2.getPackageName());
        assertEquals("TypeUnion2 generated TO has incorrect number of properties", 2,
            typeUnion2.getProperties().size());

        assertThat(typeUnion2, instanceOf(GeneratedTransferObject.class));
        GeneratedTransferObject typeUnion2gto = (GeneratedTransferObject) typeUnion2;
        containsAttributes(typeUnion2gto, true, true, true, new NameTypePattern("string", "String"));
        containsAttributes(typeUnion2gto, true, true, true, new NameTypePattern("uint64", "Uint64"));
    }

    @Test
    public void bitAndUnionEnclosingTest() {

        assertNotNull("Parent container object wasn't found.", parentContainer);
        containsMethods(parentContainer, new NameTypePattern("getLf", "Lf"));

        GeneratedTransferObject bitLeaf = null;
        GeneratedTransferObject unionLeaf = null;
        List<GeneratedType> enclosedTypes = parentContainer.getEnclosedTypes();
        for (GeneratedType genType : enclosedTypes) {
            if (genType instanceof GeneratedTransferObject) {
                if (genType.getName().equals("BitLeaf")) {
                    bitLeaf = (GeneratedTransferObject) genType;
                } else if (genType.getName().equals("UnionLeaf")) {
                    unionLeaf = (GeneratedTransferObject) genType;
                }
            }
        }

        assertNotNull("BitLeaf TO wasn't found.", bitLeaf);
        assertNotNull("UnionLeaf TO wasn't found.", unionLeaf);

        assertEquals("BitLeaf has incorrect package name.",
                "org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626.ParentContainer",
                bitLeaf.getIdentifier().immediatelyEnclosingClass().orElseThrow().toString());
        assertEquals("UnionLeaf has incorrect package name.",
                "org.opendaylight.yang.gen.v1.urn.bit.union.in.leaf.rev130626.ParentContainer",
                unionLeaf.getIdentifier().immediatelyEnclosingClass().orElseThrow().toString());

        List<GeneratedProperty> propertiesBitLeaf = bitLeaf.getProperties();
        GeneratedProperty firstBitProperty = null;
        GeneratedProperty secondBitProperty = null;
        GeneratedProperty thirdBitProperty = null;

        for (GeneratedProperty genProperty : propertiesBitLeaf) {
            if (genProperty.getName().equals("firstBit")) {
                firstBitProperty = genProperty;
            } else if (genProperty.getName().equals("secondBit")) {
                secondBitProperty = genProperty;
            } else if (genProperty.getName().equals("thirdBit")) {
                thirdBitProperty = genProperty;
            }
        }

        assertNotNull("firstBit property wasn't found", firstBitProperty);
        assertNotNull("secondBit property wasn't found", secondBitProperty);
        assertNotNull("thirdBit property wasn't found", thirdBitProperty);

        assertEquals("firstBit property has incorrect type", "boolean", firstBitProperty.getReturnType().getName());
        assertEquals("secondBit property has incorrect type", "boolean", secondBitProperty.getReturnType().getName());
        assertEquals("thirdBit property has incorrect type", "boolean", thirdBitProperty.getReturnType().getName());

        GeneratedProperty uint32Property = null;
        GeneratedProperty stringProperty = null;
        GeneratedProperty uint8Property = null;
        List<GeneratedProperty> propertiesUnionLeaf = unionLeaf.getProperties();
        for (GeneratedProperty genProperty : propertiesUnionLeaf) {
            if (genProperty.getName().equals("int32")) {
                uint32Property = genProperty;
            } else if (genProperty.getName().equals("string")) {
                stringProperty = genProperty;
            } else if (genProperty.getName().equals("uint8")) {
                uint8Property = genProperty;
            }
        }

        assertNotNull("uint32 property wasn't found", uint32Property);
        assertNotNull("string property wasn't found", stringProperty);
        assertNotNull("uint8 property wasn't found", uint8Property);

        assertEquals("uint32 property has incorrect type", "Integer", uint32Property.getReturnType().getName());
        assertEquals("string property has incorrect type", "String", stringProperty.getReturnType().getName());
        assertEquals("uint8 property has incorrect type", "Uint8", uint8Property.getReturnType().getName());

    }

}
