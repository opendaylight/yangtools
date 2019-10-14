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

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;

public class BuilderGeneratorTest {
    private static final String TEST = "test";
    private static final JavaTypeName TYPE_NAME = JavaTypeName.create(TEST, TEST);

    @Test
    public void basicTest() {
        assertEquals("", new BuilderGenerator().generate(mock(Type.class)));
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyTest() {
        final GeneratedType genType = mockGenType("get" + TEST);

        assertEquals("@Override\n"
                + "public String toString() {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"_test\", _test);\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(genType).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithoutAnyPropertyTest() throws Exception {
        assertEquals("@Override\n"
                + "public String toString() {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockGenType(TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesTest() throws Exception {
        assertEquals("@Override\n"
                + "public String toString() {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"_test1\", _test1);\n"
                + "    CodeHelpers.appendValue(helper, \"_test2\", _test2);\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockGenTypeMoreMeth("get" + TEST)).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithoutPropertyWithAugmentTest() throws Exception {
        assertEquals("@Override\n"
                + "public String toString() {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"augmentation\", augmentations().values());\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockAugment(mockGenType(TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyWithAugmentTest() throws Exception {
        assertEquals("@Override\n"
                + "public String toString() {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"_test\", _test);\n"
                + "    CodeHelpers.appendValue(helper, \"augmentation\", augmentations().values());\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockAugment(mockGenType("get" + TEST))).toString());
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesWithAugmentTest() throws Exception {
        assertEquals("@Override\n"
                + "public String toString() {\n"
                + "    final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(\"test\");\n"
                + "    CodeHelpers.appendValue(helper, \"_test1\", _test1);\n"
                + "    CodeHelpers.appendValue(helper, \"_test2\", _test2);\n"
                + "    CodeHelpers.appendValue(helper, \"augmentation\", augmentations().values());\n"
                + "    return helper.toString();\n"
                + "}\n", genToString(mockAugment(mockGenTypeMoreMeth("get" + TEST))).toString());
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
        final BuilderTemplate bt = BuilderGenerator.templateForType(genType);
        return bt.generateToString(bt.properties);
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
        doReturn(TYPE_NAME).when(methType).getIdentifier();
        doReturn(methType).when(methSign).getReturnType();
        return methSign;
    }
}
