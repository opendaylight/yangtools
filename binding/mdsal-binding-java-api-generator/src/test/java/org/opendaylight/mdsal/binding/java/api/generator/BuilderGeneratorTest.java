/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingGenerator;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BuilderGeneratorTest {
    private static final String TEST = "test";
    private static final JavaTypeName TYPE_NAME = JavaTypeName.create(TEST, TEST);

    @Test
    public void basicTest() {
        assertEquals("", new BuilderGenerator().generate(mock(Type.class)));
    }

    @Test
    public void builderTemplateGenerateHashcodeWithPropertyTest() {
        final GeneratedType genType = mockGenType("get" + TEST);

        assertEquals("/**\n"
                + " * Default implementation of {@link Object#hashCode()} contract for this interface.\n"
                + " * Implementations of this interface are encouraged to defer to this method to get consistent"
                + " hashing\n"
                + " * results across all implementations.\n"
                + " *\n"
                + " * @param obj Object for which to generate hashCode() result.\n"
                + " * @return Hash code value of data modeled by this interface.\n"
                + " * @throws NullPointerException if {@code obj} is null\n"
                + " */\n"
                + "static int bindingHashCode(final test.@NonNull test obj) {\n"
                + "    final int prime = 31;\n"
                + "    int result = 1;\n"
                + "    result = prime * result + Objects.hashCode(obj.getTest());\n"
                + "    return result;\n"
                + "}\n", genHashCode(genType).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithoutAnyPropertyTest() throws Exception {
        assertEquals("", genHashCode(mockGenType(TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithMorePropertiesTest() throws Exception {
        assertEquals("/**\n"
                + " * Default implementation of {@link Object#hashCode()} contract for this interface.\n"
                + " * Implementations of this interface are encouraged to defer to this method to get consistent"
                + " hashing\n"
                + " * results across all implementations.\n"
                + " *\n"
                + " * @param obj Object for which to generate hashCode() result.\n"
                + " * @return Hash code value of data modeled by this interface.\n"
                + " * @throws NullPointerException if {@code obj} is null\n"
                + " */\n"
                + "static int bindingHashCode(final test.@NonNull test obj) {\n"
                + "    final int prime = 31;\n"
                + "    int result = 1;\n"
                + "    result = prime * result + Objects.hashCode(obj.getTest1());\n"
                + "    result = prime * result + Objects.hashCode(obj.getTest2());\n"
                + "    return result;\n"
                + "}\n", genHashCode(mockGenTypeMoreMeth("get" + TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithoutPropertyWithAugmentTest() throws Exception {
        assertEquals("/**\n"
                + " * Default implementation of {@link Object#hashCode()} contract for this interface.\n"
                + " * Implementations of this interface are encouraged to defer to this method to get consistent"
                + " hashing\n"
                + " * results across all implementations.\n"
                + " *\n"
                + " * @param obj Object for which to generate hashCode() result.\n"
                + " * @return Hash code value of data modeled by this interface.\n"
                + " * @throws NullPointerException if {@code obj} is null\n"
                + " */\n"
                + "static int bindingHashCode(final test.@NonNull test obj) {\n"
                + "    final int prime = 31;\n"
                + "    int result = 1;\n"
                + "    result = prime * result + obj.augmentations().hashCode();\n"
                + "    return result;\n"
                + "}\n", genHashCode(mockAugment(mockGenType(TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithPropertyWithAugmentTest() throws Exception {
        assertEquals("/**\n"
                + " * Default implementation of {@link Object#hashCode()} contract for this interface.\n"
                + " * Implementations of this interface are encouraged to defer to this method to get consistent"
                + " hashing\n"
                + " * results across all implementations.\n"
                + " *\n"
                + " * @param obj Object for which to generate hashCode() result.\n"
                + " * @return Hash code value of data modeled by this interface.\n"
                + " * @throws NullPointerException if {@code obj} is null\n"
                + " */\n"
                + "static int bindingHashCode(final test.@NonNull test obj) {\n"
                + "    final int prime = 31;\n"
                + "    int result = 1;\n"
                + "    result = prime * result + Objects.hashCode(obj.getTest());\n"
                + "    result = prime * result + obj.augmentations().hashCode();\n"
                + "    return result;\n"
                + "}\n", genHashCode(mockAugment(mockGenType("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithMorePropertiesWithAugmentTest() throws Exception {
        assertEquals("/**\n"
                + " * Default implementation of {@link Object#hashCode()} contract for this interface.\n"
                + " * Implementations of this interface are encouraged to defer to this method to get consistent"
                + " hashing\n"
                + " * results across all implementations.\n"
                + " *\n"
                + " * @param obj Object for which to generate hashCode() result.\n"
                + " * @return Hash code value of data modeled by this interface.\n"
                + " * @throws NullPointerException if {@code obj} is null\n"
                + " */\n"
                + "static int bindingHashCode(final test.@NonNull test obj) {\n"
                + "    final int prime = 31;\n"
                + "    int result = 1;\n"
                + "    result = prime * result + Objects.hashCode(obj.getTest1());\n"
                + "    result = prime * result + Objects.hashCode(obj.getTest2());\n"
                + "    result = prime * result + obj.augmentations().hashCode();\n"
                + "    return result;\n"
                + "}\n", genHashCode(mockAugment(mockGenTypeMoreMeth("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyTest() {
        final GeneratedType genType = mockGenType("get" + TEST);

        assertEquals("/**\n"
                + " * Default implementation of {@link Object#toString()} contract for this interface.\n"
                + " * Implementations of this interface are encouraged to defer to this method to get consistent string"
                + "\n * representations across all implementations.\n"
                + " *\n"
                + " * @param obj Object for which to generate toString() result.\n"
                + " * @return {@link String} value of data modeled by this interface.\n"
                + " * @throws NullPointerException if {@code obj} is null\n"
                + " */\n"
                + "static String bindingToString(final test.@NonNull test obj) {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"test\", obj.gettest());\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(genType).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithoutAnyPropertyTest() throws Exception {
        assertEquals("/**\n"
                + " * Default implementation of {@link Object#toString()} contract for this interface.\n"
                + " * Implementations of this interface are encouraged to defer to this method to get consistent string"
                + "\n * representations across all implementations.\n"
                + " *\n"
                + " * @param obj Object for which to generate toString() result.\n"
                + " * @return {@link String} value of data modeled by this interface.\n"
                + " * @throws NullPointerException if {@code obj} is null\n"
                + " */\n"
                + "static String bindingToString(final test.@NonNull test obj) {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockGenType(TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesTest() throws Exception {
        assertEquals("/**\n"
                + " * Default implementation of {@link Object#toString()} contract for this interface.\n"
                + " * Implementations of this interface are encouraged to defer to this method to get consistent string"
                + "\n * representations across all implementations.\n"
                + " *\n"
                + " * @param obj Object for which to generate toString() result.\n"
                + " * @return {@link String} value of data modeled by this interface.\n"
                + " * @throws NullPointerException if {@code obj} is null\n"
                + " */\n"
                + "static String bindingToString(final test.@NonNull test obj) {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"test1\", obj.gettest1());\n"
                + "    CodeHelpers.appendValue(helper, \"test2\", obj.gettest2());\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockGenTypeMoreMeth("get" + TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithoutPropertyWithAugmentTest() throws Exception {
        assertEquals("/**\n"
                + " * Default implementation of {@link Object#toString()} contract for this interface.\n"
                + " * Implementations of this interface are encouraged to defer to this method to get consistent string"
                + "\n * representations across all implementations.\n"
                + " *\n"
                + " * @param obj Object for which to generate toString() result.\n"
                + " * @return {@link String} value of data modeled by this interface.\n"
                + " * @throws NullPointerException if {@code obj} is null\n"
                + " */\n"
                + "static String bindingToString(final test.@NonNull test obj) {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"augmentation\", obj.augmentations().values());\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockAugment(mockGenType(TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyWithAugmentTest() throws Exception {
        assertEquals("/**\n"
                + " * Default implementation of {@link Object#toString()} contract for this interface.\n"
                + " * Implementations of this interface are encouraged to defer to this method to get consistent string"
                + "\n * representations across all implementations.\n"
                + " *\n"
                + " * @param obj Object for which to generate toString() result.\n"
                + " * @return {@link String} value of data modeled by this interface.\n"
                + " * @throws NullPointerException if {@code obj} is null\n"
                + " */\n"
                + "static String bindingToString(final test.@NonNull test obj) {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"test\", obj.gettest());\n"
                + "    CodeHelpers.appendValue(helper, \"augmentation\", obj.augmentations().values());\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockAugment(mockGenType("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesWithAugmentTest() throws Exception {
        assertEquals("/**\n"
                + " * Default implementation of {@link Object#toString()} contract for this interface.\n"
                + " * Implementations of this interface are encouraged to defer to this method to get consistent string"
                + "\n * representations across all implementations.\n"
                + " *\n"
                + " * @param obj Object for which to generate toString() result.\n"
                + " * @return {@link String} value of data modeled by this interface.\n"
                + " * @throws NullPointerException if {@code obj} is null\n"
                + " */\n"
                + "static String bindingToString(final test.@NonNull test obj) {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"test1\", obj.gettest1());\n"
                + "    CodeHelpers.appendValue(helper, \"test2\", obj.gettest2());\n"
                + "    CodeHelpers.appendValue(helper, \"augmentation\", obj.augmentations().values());\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockAugment(mockGenTypeMoreMeth("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToEqualsComparingOrderTest() {
        final EffectiveModelContext context = YangParserTestUtils.parseYangResource(
                "/test-types.yang");
        final List<GeneratedType> types = new DefaultBindingGenerator().generateTypes(context);
        final BuilderTemplate bt = BuilderGenerator.templateForType(types.get(19));

        final List<String> sortedProperties = bt.properties.stream()
                .sorted(ByTypeMemberComparator.getInstance())
                .map(BuilderGeneratedProperty::getName)
                .collect(Collectors.toList());

        assertEquals(List.of(
                // numeric types (boolean, byte, short, int, long, biginteger, bigdecimal), identityrefs, empty
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
        final List<Type> impls = new ArrayList<>();
        final Type impl = mock(Type.class);
        doReturn("org.opendaylight.yangtools.yang.binding.Augmentable").when(impl).getFullyQualifiedName();
        impls.add(impl);
        doReturn(impls).when(genType).getImplements();
        return genType;
    }

    private static GeneratedType mockGenTypeMoreMeth(final String methodeName) {
        final GeneratedType genType = spy(GeneratedType.class);
        doReturn(TYPE_NAME).when(genType).getIdentifier();
        doReturn(TEST).when(genType).getName();
        doReturn(TEST).when(genType).getPackageName();

        final List<MethodSignature> listMethodSign = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            final MethodSignature methSign = mockMethSign(methodeName + (i + 1));
            listMethodSign.add(methSign);
        }
        doReturn(listMethodSign).when(genType).getMethodDefinitions();

        final List<Type> impls = new ArrayList<>();
        doReturn(impls).when(genType).getImplements();
        return genType;
    }

    private static CharSequence genToString(final GeneratedType genType) {
        return new InterfaceTemplate(genType).generateBindingToString();
    }

    private static CharSequence genHashCode(final GeneratedType genType) {
        return new InterfaceTemplate(genType).generateBindingHashCode();
    }

    private static GeneratedType mockGenType(final String methodeName) {
        final GeneratedType genType = spy(GeneratedType.class);
        doReturn(TYPE_NAME).when(genType).getIdentifier();
        doReturn(TEST).when(genType).getName();
        doReturn(TEST).when(genType).getPackageName();

        final List<MethodSignature> listMethodSign = new ArrayList<>();
        final MethodSignature methSign = mockMethSign(methodeName);
        listMethodSign.add(methSign);
        doReturn(listMethodSign).when(genType).getMethodDefinitions();

        final List<Type> impls = new ArrayList<>();
        doReturn(impls).when(genType).getImplements();
        return genType;
    }

    private static MethodSignature mockMethSign(final String methodeName) {
        final MethodSignature methSign = mock(MethodSignature.class);
        doReturn(methodeName).when(methSign).getName();
        final Type methType = mock(Type.class);
        doCallRealMethod().when(methType).getFullyQualifiedName();
        doReturn(TYPE_NAME).when(methType).getIdentifier();
        doReturn(TEST).when(methType).getName();
        doReturn(methType).when(methSign).getReturnType();
        doReturn(ValueMechanics.NORMAL).when(methSign).getMechanics();
        return methSign;
    }
}
