/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;

public class BuilderGeneratorTest {

    private static final String PROPERTIES_FIELD_NAME = "properties";
    private static final String GEN_TO_STRING_FIRST_PART =
            "@Override\npublic java.lang.String toString() {\n    java.lang.String name = \"test [\";\n    "
                    + "java.lang.StringBuilder builder = new java.lang.StringBuilder (name);";
    private static final String GEN_TO_STRING_LAST_PART = "\n    return builder.append(']').toString();\n}\n";
    private static final String GEN_TO_STRING_AUGMENT_PART =
            "\n    builder.append(\"augmentation=\");\n    builder.append(augmentation.values());";
    private static final String APPEND_COMMA = "builder.append(\", \");";
    private static final String APPEND_COMMA_AUGMENT = "final int builderLength = builder.length();\n"
            + "    final int builderAdditionalLength = builder.substring(name.length(), builderLength).length();\n"
            + "    if (builderAdditionalLength > 2 && !builder.substring(builderLength - 2, builderLength).equals(\", \")) {\n"
            + "        " + APPEND_COMMA + "\n" + "    }";
    private static final String TEST = "test";

    @Test
    public void basicTest() {
        assertEquals("", new BuilderGenerator().generate(mock(Type.class)));
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyTest() {
        final GeneratedType genType = mockGenType("get" + TEST);

        assertEquals("@Override\n" +
                "public java.lang.String toString() {\n" +
                "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n" +
                "    CodeHelpers.appendValue(helper, \"_test\", _test);\n" +
                "    return helper.toString();\n" +
                "}\n", genToString(genType).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithoutAnyPropertyTest() throws Exception {
        assertEquals("@Override\n" +
                "public java.lang.String toString() {\n" +
                "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n" +
                "    return helper.toString();\n" +
                "}\n", genToString(mockGenType(TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesTest() throws Exception {
        assertEquals("@Override\n" +
                "public java.lang.String toString() {\n" +
                "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n" +
                "    CodeHelpers.appendValue(helper, \"_test1\", _test1);\n" +
                "    CodeHelpers.appendValue(helper, \"_test2\", _test2);\n" +
                "    return helper.toString();\n" +
                "}\n", genToString(mockGenTypeMoreMeth("get" + TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithoutPropertyWithAugmentTest() throws Exception {
        assertEquals("@Override\n" +
                "public java.lang.String toString() {\n" +
                "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n" +
                "    CodeHelpers.appendValue(helper, \"augmentation\", augmentation.values()); \n" +
                "    return helper.toString();\n" +
                "}\n", genToString(mockAugment(mockGenType(TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyWithAugmentTest() throws Exception {
        assertEquals("@Override\n" +
                "public java.lang.String toString() {\n" +
                "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n" +
                "    CodeHelpers.appendValue(helper, \"_test\", _test);\n" +
                "    CodeHelpers.appendValue(helper, \"augmentation\", augmentation.values()); \n" +
                "    return helper.toString();\n" +
                "}\n", genToString(mockAugment(mockGenType("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesWithAugmentTest() throws Exception {
        assertEquals("@Override\n" +
                "public java.lang.String toString() {\n" +
                "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n" +
                "    CodeHelpers.appendValue(helper, \"_test1\", _test1);\n" +
                "    CodeHelpers.appendValue(helper, \"_test2\", _test2);\n" +
                "    CodeHelpers.appendValue(helper, \"augmentation\", augmentation.values()); \n" +
                "    return helper.toString();\n" +
                "}\n", genToString(mockAugment(mockGenTypeMoreMeth("get" + TEST))).toString());
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

    @SuppressWarnings("unchecked")
    private static CharSequence genToString(final GeneratedType genType) {
        try {
            final BuilderTemplate bt = new BuilderTemplate(genType);
            final Field propertiesField = bt.getClass().getDeclaredField(PROPERTIES_FIELD_NAME);
            propertiesField.setAccessible(true);
            return bt.generateToString((Collection<GeneratedProperty>) propertiesField.get(bt));
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private static GeneratedType mockGenType(final String methodeName) {
        final GeneratedType genType = spy(GeneratedType.class);
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
        doReturn(TEST).when(methType).getName();
        doReturn(TEST).when(methType).getPackageName();
        doReturn(methType).when(methSign).getReturnType();
        return methSign;
    }
}
