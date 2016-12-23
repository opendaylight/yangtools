/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
    private final String firstPartOfGenToStringMethod =
            "@Override\npublic java.lang.String toString() {\n    java.lang.StringBuilder builder = new java.lang.StringBuilder ("
                    + "\"test [\");";
    private final String lastPartOfGenToStringMethode = "\n    return builder.append(']').toString();\n}";
    private final String argPartOfGenToStringMethode =
            "\n    builder.append(\"augmentation=\");\n    builder.append(augmentation.values());";
    private final String comaPartOfAppend = "builder.append(\", \");";
    private final String TEST = "test";

    @Test
    public void basicTest() throws Exception {
        assertEquals("", new BuilderGenerator().generate(mock(Type.class)));
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyTest() throws Exception {
        final GeneratedType genType = mockGenType("get" + this.TEST);
        final String generateToString = genToString(genType).toString();
        assertTrue(generateToString.contains(this.firstPartOfGenToStringMethod
                + "\n    if (_test != null) {\n        builder.append(\"_test=\");\n        builder.append(_test);\n    }"
                + this.lastPartOfGenToStringMethode));
    }

    @Test
    public void builderTemplateGenerateToStringWithoutAnyPropertyTest() throws Exception {
        final GeneratedType genType = mockGenType(this.TEST);
        final String generateToString = genToString(genType).toString();
        assertTrue(generateToString
                .contains(this.firstPartOfGenToStringMethod + this.lastPartOfGenToStringMethode));
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesTest() throws Exception {
        final GeneratedType genType = mockGenTypeMoreMeth("get" + this.TEST);
        final String generateToString = genToString(genType).toString();
        assertTrue(generateToString.contains(this.firstPartOfGenToStringMethod
                + "\n    if (_test1 != null) {\n        builder.append(\"_test1=\");\n        builder.append(_test1);"
                + "\n        " + this.comaPartOfAppend + "\n    }"
                + "\n    if (_test2 != null) {\n        builder.append(\"_test2=\");\n        builder.append(_test2);\n    }"
                + this.lastPartOfGenToStringMethode));
    }

    @Test
    public void builderTemplateGenerateToStringWithoutPropertyWithAugmentTest() throws Exception {
        final GeneratedType genType = mockGenType(this.TEST);
        mockAugment(genType);
        final String generateToString = genToString(genType).toString();
        assertTrue(generateToString.contains(this.firstPartOfGenToStringMethod + this.argPartOfGenToStringMethode
                + this.lastPartOfGenToStringMethode));
    }

    @Test
    public void builderTemplateGenerateToStringWithPropertyWithAugmentTest() throws Exception {
        final GeneratedType genType = mockGenType("get" + this.TEST);
        mockAugment(genType);
        final String generateToString = genToString(genType).toString();
        assertTrue(generateToString.contains(this.firstPartOfGenToStringMethod
                + "\n    if (_test != null) {\n        builder.append(\"_test=\");\n        builder.append(_test);\n    }"
                + "\n    " + this.comaPartOfAppend + this.argPartOfGenToStringMethode
                + this.lastPartOfGenToStringMethode));
    }

    @Test
    public void builderTemplateGenerateToStringWithMorePropertiesWithAugmentTest() throws Exception {
        final GeneratedType genType = mockGenTypeMoreMeth("get" + this.TEST);
        mockAugment(genType);
        final String generateToString = genToString(genType).toString();
        assertTrue(generateToString.contains(this.firstPartOfGenToStringMethod
                + "\n    if (_test1 != null) {\n        builder.append(\"_test1=\");\n        builder.append(_test1);\n        "
                + this.comaPartOfAppend + "\n    }"
                + "\n    if (_test2 != null) {\n        builder.append(\"_test2=\");\n        builder.append(_test2);\n    }"
                + "\n    " + this.comaPartOfAppend
                + this.argPartOfGenToStringMethode + this.lastPartOfGenToStringMethode));
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
        doReturn(this.TEST).when(genType).getName();
        doReturn(this.TEST).when(genType).getPackageName();

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
        doReturn(this.TEST).when(genType).getName();
        doReturn(this.TEST).when(genType).getPackageName();

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
        doReturn(this.TEST).when(methType).getName();
        doReturn(this.TEST).when(methType).getPackageName();
        doReturn(methType).when(methSign).getReturnType();
        return methSign;
    }
}
