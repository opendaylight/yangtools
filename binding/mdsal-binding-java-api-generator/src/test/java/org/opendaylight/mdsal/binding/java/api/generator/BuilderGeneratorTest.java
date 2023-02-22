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
import org.eclipse.xtend2.lib.StringConcatenation;
import org.junit.Test;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingGenerator;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.mdsal.binding.model.api.Type;
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

        assertXtendEquals("""
            /**
             * Default implementation of {@link Object#hashCode()} contract for this interface.
             * Implementations of this interface are encouraged to defer to this method to get consistent\
             hashing
             * results across all implementations.
             *
             * @param obj Object for which to generate hashCode() result.
             * @return Hash code value of data modeled by this interface.
             * @throws NullPointerException if {@code obj} is {@code null}
             */
            static int bindingHashCode(final test.@NonNull test obj) {
                final int prime = 31;
                int result = 1;
                result = prime * result + Objects.hashCode(obj.getTest());
                return result;
            }
            """, genHashCode(genType).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithoutAnyPropertyTest() throws Exception {
        assertEquals("", genHashCode(mockGenType(TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithMorePropertiesTest() throws Exception {
        assertXtendEquals("""
            /**
             * Default implementation of {@link Object#hashCode()} contract for this interface.
             * Implementations of this interface are encouraged to defer to this method to get consistent\
             hashing
             * results across all implementations.
             *
             * @param obj Object for which to generate hashCode() result.
             * @return Hash code value of data modeled by this interface.
             * @throws NullPointerException if {@code obj} is {@code null}
             */
            static int bindingHashCode(final test.@NonNull test obj) {
                final int prime = 31;
                int result = 1;
                result = prime * result + Objects.hashCode(obj.getTest1());
                result = prime * result + Objects.hashCode(obj.getTest2());
                return result;
            }
            """, genHashCode(mockGenTypeMoreMeth("get" + TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithoutPropertyWithAugmentTest() throws Exception {
        assertXtendEquals("""
            /**
             * Default implementation of {@link Object#hashCode()} contract for this interface.
             * Implementations of this interface are encouraged to defer to this method to get consistent\
             hashing
             * results across all implementations.
             *
             * @param obj Object for which to generate hashCode() result.
             * @return Hash code value of data modeled by this interface.
             * @throws NullPointerException if {@code obj} is {@code null}
             */
            static int bindingHashCode(final test.@NonNull test obj) {
                final int prime = 31;
                int result = 1;
                for (var augmentation : obj.augmentations().values()) {
                    result += augmentation.hashCode();
                }
                return result;
            }
            """, genHashCode(mockAugment(mockGenType(TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithPropertyWithAugmentTest() throws Exception {
        assertXtendEquals("""
            /**
             * Default implementation of {@link Object#hashCode()} contract for this interface.
             * Implementations of this interface are encouraged to defer to this method to get consistent\
             hashing
             * results across all implementations.
             *
             * @param obj Object for which to generate hashCode() result.
             * @return Hash code value of data modeled by this interface.
             * @throws NullPointerException if {@code obj} is {@code null}
             */
            static int bindingHashCode(final test.@NonNull test obj) {
                final int prime = 31;
                int result = 1;
                result = prime * result + Objects.hashCode(obj.getTest());
                for (var augmentation : obj.augmentations().values()) {
                    result += augmentation.hashCode();
                }
                return result;
            }
            """, genHashCode(mockAugment(mockGenType("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateHashCodeWithMorePropertiesWithAugmentTest() throws Exception {
        assertXtendEquals("""
            /**
             * Default implementation of {@link Object#hashCode()} contract for this interface.
             * Implementations of this interface are encouraged to defer to this method to get consistent\
             hashing
             * results across all implementations.
             *
             * @param obj Object for which to generate hashCode() result.
             * @return Hash code value of data modeled by this interface.
             * @throws NullPointerException if {@code obj} is {@code null}
             */
            static int bindingHashCode(final test.@NonNull test obj) {
                final int prime = 31;
                int result = 1;
                result = prime * result + Objects.hashCode(obj.getTest1());
                result = prime * result + Objects.hashCode(obj.getTest2());
                for (var augmentation : obj.augmentations().values()) {
                    result += augmentation.hashCode();
                }
                return result;
            }
            """, genHashCode(mockAugment(mockGenTypeMoreMeth("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyTest() {
        final GeneratedType genType = mockGenType("get" + TEST);

        assertXtendEquals("""
            /**
             * Default implementation of {@link Object#toString()} contract for this interface.
             * Implementations of this interface are encouraged to defer to this method to get consistent string\

             * representations across all implementations.
             *
             * @param obj Object for which to generate toString() result.
             * @return {@link String} value of data modeled by this interface.
             * @throws NullPointerException if {@code obj} is {@code null}
             */
            static String bindingToString(final test.@NonNull test obj) {
                final var helper = MoreObjects.toStringHelper("test");
                CodeHelpers.appendValue(helper, "test", obj.gettest());
                return helper.toString();
            }
            """, genToString(genType).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithoutAnyPropertyTest() throws Exception {
        assertXtendEquals("""
            /**
             * Default implementation of {@link Object#toString()} contract for this interface.
             * Implementations of this interface are encouraged to defer to this method to get consistent string\

             * representations across all implementations.
             *
             * @param obj Object for which to generate toString() result.
             * @return {@link String} value of data modeled by this interface.
             * @throws NullPointerException if {@code obj} is {@code null}
             */
            static String bindingToString(final test.@NonNull test obj) {
                final var helper = MoreObjects.toStringHelper("test");
                return helper.toString();
            }
            """, genToString(mockGenType(TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesTest() throws Exception {
        assertXtendEquals("""
            /**
             * Default implementation of {@link Object#toString()} contract for this interface.
             * Implementations of this interface are encouraged to defer to this method to get consistent string\

             * representations across all implementations.
             *
             * @param obj Object for which to generate toString() result.
             * @return {@link String} value of data modeled by this interface.
             * @throws NullPointerException if {@code obj} is {@code null}
             */
            static String bindingToString(final test.@NonNull test obj) {
                final var helper = MoreObjects.toStringHelper("test");
                CodeHelpers.appendValue(helper, "test1", obj.gettest1());
                CodeHelpers.appendValue(helper, "test2", obj.gettest2());
                return helper.toString();
            }
            """, genToString(mockGenTypeMoreMeth("get" + TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithoutPropertyWithAugmentTest() throws Exception {
        assertXtendEquals("""
            /**
             * Default implementation of {@link Object#toString()} contract for this interface.
             * Implementations of this interface are encouraged to defer to this method to get consistent string\

             * representations across all implementations.
             *
             * @param obj Object for which to generate toString() result.
             * @return {@link String} value of data modeled by this interface.
             * @throws NullPointerException if {@code obj} is {@code null}
             */
            static String bindingToString(final test.@NonNull test obj) {
                final var helper = MoreObjects.toStringHelper("test");
                CodeHelpers.appendAugmentations(helper, "augmentation", obj);
                return helper.toString();
            }
            """, genToString(mockAugment(mockGenType(TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyWithAugmentTest() throws Exception {
        assertXtendEquals("""
            /**
             * Default implementation of {@link Object#toString()} contract for this interface.
             * Implementations of this interface are encouraged to defer to this method to get consistent string\

             * representations across all implementations.
             *
             * @param obj Object for which to generate toString() result.
             * @return {@link String} value of data modeled by this interface.
             * @throws NullPointerException if {@code obj} is {@code null}
             */
            static String bindingToString(final test.@NonNull test obj) {
                final var helper = MoreObjects.toStringHelper("test");
                CodeHelpers.appendValue(helper, "test", obj.gettest());
                CodeHelpers.appendAugmentations(helper, "augmentation", obj);
                return helper.toString();
            }
            """, genToString(mockAugment(mockGenType("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesWithAugmentTest() throws Exception {
        assertXtendEquals("""
            /**
             * Default implementation of {@link Object#toString()} contract for this interface.
             * Implementations of this interface are encouraged to defer to this method to get consistent string\

             * representations across all implementations.
             *
             * @param obj Object for which to generate toString() result.
             * @return {@link String} value of data modeled by this interface.
             * @throws NullPointerException if {@code obj} is {@code null}
             */
            static String bindingToString(final test.@NonNull test obj) {
                final var helper = MoreObjects.toStringHelper("test");
                CodeHelpers.appendValue(helper, "test1", obj.gettest1());
                CodeHelpers.appendValue(helper, "test2", obj.gettest2());
                CodeHelpers.appendAugmentations(helper, "augmentation", obj);
                return helper.toString();
            }
            """, genToString(mockAugment(mockGenTypeMoreMeth("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToEqualsComparingOrderTest() {
        final var context = YangParserTestUtils.parseYangResource("/test-types.yang");
        final var types = new DefaultBindingGenerator().generateTypes(context);
        assertEquals(27, types.size());

        final BuilderTemplate bt = BuilderGenerator.templateForType(
            types.stream().filter(t -> t.getName().equals("Nodes")).findFirst().orElseThrow());

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

    // Xtend's StringConcatenation is using runtime-configured line separator, which can change between runs, notably
    // it has a different value on Windows. Make sure we account for that.
    private static void assertXtendEquals(final String expected, final String actual) {
        assertEquals(expected.replace("\n", StringConcatenation.DEFAULT_LINE_DELIMITER), actual);
    }
}
