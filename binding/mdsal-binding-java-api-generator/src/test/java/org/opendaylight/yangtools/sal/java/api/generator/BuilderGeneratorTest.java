/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.Type;

@SuppressWarnings("deprecation")
public class BuilderGeneratorTest {

    private static final String PROPERTIES_FIELD_NAME = "properties";
    private static final String GEN_TO_STRING_FIRST_PART =
            "@Override\npublic java.lang.String toString() {\n    java.lang.StringBuilder builder = new java.lang.StringBuilder ("
                    + "\"test [\");";
    private static final String GEN_TO_STRING_LAST_PART = "\n    return builder.append(']').toString();\n}\n";
    private static final String GEN_TO_STRING_AUGMENT_PART =
            "\n    builder.append(\"augmentation=\");\n    builder.append(augmentation.values());";
    private static final String APPEND_COMMA = "builder.append(\", \");";
    private static final String APPEND_COMMA_AUGMENT = "int builderLength = builder.length();\n"
            + "    if (builderLength > 2 && !builder.substring(builderLength - 2, builderLength).equals(\", \")) {\n"
            + "        " + APPEND_COMMA + "\n" + "    }";
    private static final String TEST = "test";

    @Test
    public void basicTest() throws Exception {
        assertEquals("", new BuilderGenerator().generate(mock(Type.class)));
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyTest() throws Exception {
        final GeneratedType genType = mockGenType("get" + TEST);
        final String generateToString = genToString(genType).toString();
        final String expected = GEN_TO_STRING_FIRST_PART
                + "\n    if (_test != null) {\n        builder.append(\"_test=\");\n        builder.append(_test);\n    }"
                + GEN_TO_STRING_LAST_PART;
        assertEquals(expected, generateToString);
    }

    @Test
    public void builderTemplateGenerateToStringWithoutAnyPropertyTest() throws Exception {
        final GeneratedType genType = mockGenType(TEST);
        final String generateToString = genToString(genType).toString();
        final String expected = GEN_TO_STRING_FIRST_PART + GEN_TO_STRING_LAST_PART;
        assertEquals(expected, generateToString);
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesTest() throws Exception {
        final GeneratedType genType = mockGenTypeMoreMeth("get" + TEST);
        final String generateToString = genToString(genType).toString();
        final String expected = GEN_TO_STRING_FIRST_PART
                + "\n    if (_test1 != null) {\n        builder.append(\"_test1=\");\n        builder.append(_test1);"
                + "\n        " + APPEND_COMMA + "\n    }"
                + "\n    if (_test2 != null) {\n        builder.append(\"_test2=\");\n        builder.append(_test2);\n    }"
                + GEN_TO_STRING_LAST_PART;
        assertEquals(expected, generateToString);
    }

    @Test
    public void builderTemplateGenerateToStringWithoutPropertyWithAugmentTest() throws Exception {
        final GeneratedType genType = mockGenType(TEST);
        mockAugment(genType);
        final String generateToString = genToString(genType).toString();
        final String expected = GEN_TO_STRING_FIRST_PART + GEN_TO_STRING_AUGMENT_PART + GEN_TO_STRING_LAST_PART;
        assertEquals(expected, generateToString);
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyWithAugmentTest() throws Exception {
        final GeneratedType genType = mockGenType("get" + TEST);
        mockAugment(genType);
        final String generateToString = genToString(genType).toString();
        final String expected = GEN_TO_STRING_FIRST_PART
                + "\n    if (_test != null) {\n        builder.append(\"_test=\");\n        builder.append(_test);\n    }"
                + "\n    " + APPEND_COMMA_AUGMENT + GEN_TO_STRING_AUGMENT_PART + GEN_TO_STRING_LAST_PART;
        assertEquals(expected, generateToString);
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesWithAugmentTest() throws Exception {
        final GeneratedType genType = mockGenTypeMoreMeth("get" + TEST);
        mockAugment(genType);
        final String generateToString = genToString(genType).toString();
        final String expected = GEN_TO_STRING_FIRST_PART
                + "\n    if (_test1 != null) {\n        builder.append(\"_test1=\");\n        builder.append(_test1);\n        "
                + APPEND_COMMA + "\n    }"
                + "\n    if (_test2 != null) {\n        builder.append(\"_test2=\");\n        builder.append(_test2);\n    }"
                + "\n    " + APPEND_COMMA_AUGMENT + GEN_TO_STRING_AUGMENT_PART + GEN_TO_STRING_LAST_PART;
        assertEquals(expected, generateToString);
    }

    private void mockAugment(final GeneratedType genType) {
        final List<Type> impls = new ArrayList<>();
        final Type impl = mock(Type.class);
        doReturn("org.opendaylight.yangtools.yang.binding.Augmentable").when(impl).getFullyQualifiedName();
        impls.add(impl);
        doReturn(impls).when(genType).getImplements();
    }

    private GeneratedType mockGenTypeMoreMeth(final String methodeName) {
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
    private CharSequence genToString(final GeneratedType genType)
            throws NoSuchFieldException, IllegalAccessException {
        final BuilderTemplate bt = new BuilderTemplate(genType);
        final Field propertiesField = bt.getClass().getDeclaredField(PROPERTIES_FIELD_NAME);
        propertiesField.setAccessible(true);
        return bt.generateToString((Collection<GeneratedProperty>) propertiesField.get(bt));
    }

    private GeneratedType mockGenType(final String methodeName) {
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

    private MethodSignature mockMethSign(final String methodeName) {
        final MethodSignature methSign = mock(MethodSignature.class);
        doReturn(methodeName).when(methSign).getName();
        final Type methType = mock(Type.class);
        doReturn(TEST).when(methType).getName();
        doReturn(TEST).when(methType).getPackageName();
        doReturn(methType).when(methSign).getReturnType();
        return methSign;
    }
}
