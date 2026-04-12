/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.generator.impl.DefaultBindingGenerator;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.api.TypeRef;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BuilderGeneratorTest {
    private static final String TEST = "test";
    private static final JavaTypeName TYPE_NAME = JavaTypeName.create(TEST, TEST);

    @Test
    void builderTemplateGenerateHashcodeWithPropertyTest() {
        final var bb = genHashCode(mockGenType("get" + TEST));
        assertNotNull(bb);
        assertEquals("""
            @Override
            default int bindingHashCode() {
                return 31 + Objects.hashCode(getTest());
            }
            """, bb.toRawString());
    }

    @Test
    void builderTemplateGenerateHashCodeWithoutAnyPropertyTest() {
        assertNull(genHashCode(mockGenType(TEST)));
    }

    @Test
    void builderTemplateGenerateHashCodeWithMorePropertiesTest() {
        final var bb = genHashCode(mockGenTypeMoreMeth("get" + TEST));
        assertNotNull(bb);
        assertEquals("""
            @Override
            default int bindingHashCode() {
                int result = 1;
                final int prime = 31;
                result = prime * result + Objects.hashCode(getTest1());
                result = prime * result + Objects.hashCode(getTest2());
                return result;
            }
            """, bb.toRawString());
    }

    @Test
    void builderTemplateGenerateHashCodeWithoutPropertyWithAugmentTest() {
        final var bb = genHashCode(mockAugment(mockGenType(TEST)));
        assertNotNull(bb);
        assertEquals("""
            @Override
            default int bindingHashCode() {
                return 1 + CodeHelpers.hashAugmentations(this);
            }
            """, bb.toRawString());
    }

    @Test
    void builderTemplateGenerateHashCodeWithPropertyWithAugmentTest() {
        final var bb = genHashCode(mockAugment(mockGenType("get" + TEST)));
        assertNotNull(bb);
        assertEquals("""
            @Override
            default int bindingHashCode() {
                return 31 + Objects.hashCode(getTest()) + CodeHelpers.hashAugmentations(this);
            }
            """, bb.toRawString());
    }

    @Test
    void builderTemplateGenerateHashCodeWithMorePropertiesWithAugmentTest() {
        final var bb = genHashCode(mockAugment(mockGenTypeMoreMeth("get" + TEST)));
        assertNotNull(bb);
        assertEquals("""
            @Override
            default int bindingHashCode() {
                int result = 1;
                final int prime = 31;
                result = prime * result + Objects.hashCode(getTest1());
                result = prime * result + Objects.hashCode(getTest2());
                return result + CodeHelpers.hashAugmentations(this);
            }
            """, bb.toRawString());
    }

    @Test
    void builderTemplateGenerateToStringWithPropertyTest() {
        final var genType = mockGenType("get" + TEST);

        assertEquals("""
            @Override
            default String bindingToString() {
                final var helper = MoreObjects.toStringHelper("test");
                CodeHelpers.appendValue(helper, "test", gettest());
                return helper.toString();
            }
            """, genToString(genType).toRawString());
    }

    @Test
    void builderTemplateGenerateToStringWithoutAnyPropertyTest() {
        assertEquals("""
            @Override
            default String bindingToString() {
                final var helper = MoreObjects.toStringHelper("test");
                return helper.toString();
            }
            """, genToString(mockGenType(TEST)).toRawString());
    }

    @Test
    void builderTemplateGenerateToStringWithMorePropertiesTest() {
        assertEquals("""
            @Override
            default String bindingToString() {
                final var helper = MoreObjects.toStringHelper("test");
                CodeHelpers.appendValue(helper, "test1", gettest1());
                CodeHelpers.appendValue(helper, "test2", gettest2());
                return helper.toString();
            }
            """, genToString(mockGenTypeMoreMeth("get" + TEST)).toRawString());
    }

    @Test
    void builderTemplateGenerateToStringWithoutPropertyWithAugmentTest() {
        assertEquals("""
            @Override
            default String bindingToString() {
                final var helper = MoreObjects.toStringHelper("test");
                CodeHelpers.appendAugmentations(helper, "augmentation", this);
                return helper.toString();
            }
            """, genToString(mockAugment(mockGenType(TEST))).toRawString());
    }

    @Test
    void builderTemplateGenerateToStringWithPropertyWithAugmentTest() {
        assertEquals("""
            @Override
            default String bindingToString() {
                final var helper = MoreObjects.toStringHelper("test");
                CodeHelpers.appendValue(helper, "test", gettest());
                CodeHelpers.appendAugmentations(helper, "augmentation", this);
                return helper.toString();
            }
            """, genToString(mockAugment(mockGenType("get" + TEST))).toRawString());
    }

    @Test
    void builderTemplateGenerateToStringWithMorePropertiesWithAugmentTest() {
        assertEquals("""
            @Override
            default String bindingToString() {
                final var helper = MoreObjects.toStringHelper("test");
                CodeHelpers.appendValue(helper, "test1", gettest1());
                CodeHelpers.appendValue(helper, "test2", gettest2());
                CodeHelpers.appendAugmentations(helper, "augmentation", this);
                return helper.toString();
            }
            """, genToString(mockAugment(mockGenTypeMoreMeth("get" + TEST))).toRawString());
    }

    @Test
    void builderTemplateGenerateToEqualsComparingOrderTest() {
        final var context = YangParserTestUtils.parseYangResource("/test-types.yang");
        final var types = new DefaultBindingGenerator().generateTypes(context);
        assertEquals(27, types.size());

        final var bt = BuilderGenerator.templateForType(
            types.stream().filter(t -> t.simpleName().equals("Nodes")).findFirst().orElseThrow());

        final var sortedProperties = bt.properties.stream()
                .sorted(ByTypeMemberComparator.getInstance())
                .map(BuilderGeneratedProperty::getName)
                .collect(Collectors.toList());

        assertEquals(List.of(
                // numeric types (boolean, byte, short, int, long, Uint*, Decimal64), identityrefs, Empty
                "id16", "id16Def", "id32", "id32Def", "id64", "id64Def", "id8", "id8Def", "idBoolean", "idBooleanDef",
                "idDecimal64", "idDecimal64Def","idEmpty", "idEmptyDef", "idIdentityref", "idIdentityrefDef",
                "idLeafref", "idLeafrefDef", "idU16", "idU16Def", "idU32", "idU32Def", "idU64", "idU64Def", "idU8",
                "idU8Def",
                // string, binary, bits
                "idBinary", "idBinaryDef", "idBits", "idBitsDef", "idGroupLeafString", "idLeafrefContainer1",
                "idLeafrefContainer1Def", "idString", "idStringDef",
                // instance identifier
                "idInstanceIdentifier", "idInstanceIdentifierDef",
                // other types
                "idContainer1", "idContainer2", "idEnumeration", "idEnumerationDef",
                "idGroupContainer", "idList", "idUnion", "idUnionDef"), sortedProperties);
    }

    private static GeneratedType mockAugment(final GeneratedType genType) {
        doReturn(List.of(BindingTypes.augmentable(genType))).when(genType).getImplements();
        return genType;
    }

    private static GeneratedType mockGenTypeMoreMeth(final String methodeName) {
        final var genType = spy(GeneratedType.class);
        doReturn(TYPE_NAME).when(genType).name();
        doReturn(TEST).when(genType).simpleName();
        doReturn(TEST).when(genType).packageName();
        doReturn(List.of(mockMethSign(methodeName + 1), mockMethSign(methodeName + 2)))
            .when(genType).getMethodDefinitions();
        doReturn(List.of()).when(genType).getImplements();
        return genType;
    }

    private static BlockBuilder genToString(final GeneratedType genType) {
        return new InterfaceTemplate(genType).generateBindingToString();
    }

    private static @Nullable BlockBuilder genHashCode(final GeneratedType genType) {
        return new InterfaceTemplate(genType).generateBindingHashCode();
    }

    private static GeneratedType mockGenType(final String methodeName) {
        final var genType = spy(GeneratedType.class);
        doReturn(TYPE_NAME).when(genType).name();
        doReturn(TEST).when(genType).simpleName();
        doReturn(TEST).when(genType).packageName();
        doReturn(List.of(mockMethSign(methodeName))).when(genType).getMethodDefinitions();
        doReturn(List.of()).when(genType).getImplements();
        return genType;
    }

    private static MethodSignature mockMethSign(final String methodeName) {
        final var methSign = mock(MethodSignature.class);
        doReturn(methodeName).when(methSign).getName();
        final var methType = TypeRef.of(TYPE_NAME);
        doReturn(methType).when(methSign).getReturnType();
        doReturn(ValueMechanics.NORMAL).when(methSign).getMechanics();
        return methSign;
    }
}
