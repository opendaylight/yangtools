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

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.Types;

public class InterfaceGeneratorTest {
    private static final String TEST = "test";
    private static final JavaTypeName TYPE_NAME = JavaTypeName.create(TEST, TEST);

    @Test
    public void basicTest() {
        assertEquals("", new InterfaceGenerator().generate(mock(Type.class)));
    }

    @Test
    public void builderTemplateListenerMethodTest() {
        final MethodSignature methSign = mockMethSign("on" + TEST);
        final GeneratedType genType = mockGenType(methSign);

        String expected = String.join(System.lineSeparator(),
            "package test;",
            "",
            "public interface test",
            "{",
            "",
            "",
            "",
            "",
            "    void ontest();",
            "",
            "}",
            "",
            ""
        );
        assertEquals(expected, new InterfaceGenerator().generate(genType));
    }

    @Test
    public void builderTemplateDeprecatedListenerMethodTest() {
        final MethodSignature methSign = mockMethSign("on" + TEST);
        addMethodStatus(methSign, JavaTypeName.create(Deprecated.class));
        final GeneratedType genType = mockGenType(methSign);

        String expected = String.join(System.lineSeparator(),
            "package test;",
            "import java.lang.Deprecated;",
            "",
            "public interface test",
            "{",
            "",
            "",
            "",
            "",
            "    @Deprecated",
            "    void ontest();",
            "",
            "}",
            "",
            ""
        );
        assertEquals(expected, new InterfaceGenerator().generate(genType));
    }

    @Test
    public void builderTemplateGenerateObsoleteListenerMethodTest() {
        final MethodSignature methSign = mockMethSign("on" + TEST);
        addMethodStatus(methSign, JavaTypeName.create(Deprecated.class));
        doReturn(true).when(methSign).isDefault();
        final GeneratedType genType = mockGenType(methSign);

        String expected = String.join(System.lineSeparator(),
            "package test;",
            "import java.lang.Deprecated;",
            "",
            "public interface test",
            "{",
            "",
            "",
            "",
            "",
            "    @Deprecated",
            "    default void ontest() {",
            "        // No-op",
            "    }",
            "",
            "}",
            "",
            ""
        );
        assertEquals(expected, new InterfaceGenerator().generate(genType));
    }

    private static GeneratedType mockGenType(final MethodSignature methSign) {
        final GeneratedType genType = spy(GeneratedType.class);
        doReturn(TYPE_NAME).when(genType).getIdentifier();
        doReturn(TEST).when(genType).getName();
        doReturn(TEST).when(genType).getPackageName();
        final List<MethodSignature> listMethodSign = new ArrayList<>();
        listMethodSign.add(methSign);
        doReturn(listMethodSign).when(genType).getMethodDefinitions();

        final List<Type> impls = new ArrayList<>();
        doReturn(impls).when(genType).getImplements();
        return genType;
    }

    private static MethodSignature mockMethSign(final String methodeName) {
        final MethodSignature methSign = mock(MethodSignature.class);
        doReturn(methodeName).when(methSign).getName();
        final Type methType = Types.typeForClass(void.class);
        doReturn(methType).when(methSign).getReturnType();
        doReturn(MethodSignature.ValueMechanics.NORMAL).when(methSign).getMechanics();
        return methSign;
    }

    private static void addMethodStatus(MethodSignature methSign, JavaTypeName annotationJavaType) {
        final AnnotationType annotationType = mock(AnnotationType.class);
        doReturn(annotationJavaType).when(annotationType).getIdentifier();
        doReturn(ImmutableList.of(annotationType)).when(methSign).getAnnotations();
    }
}