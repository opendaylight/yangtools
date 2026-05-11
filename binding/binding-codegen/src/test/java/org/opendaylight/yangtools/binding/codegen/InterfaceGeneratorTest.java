/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.ri.Types;

class InterfaceGeneratorTest {
    private static final String TEST = "test";
    private static final JavaTypeName TYPE_NAME = JavaTypeName.create(TEST, TEST);

    @Test
    void builderTemplateListenerMethodTest() {
        final var methSign = mockMethSign("on" + TEST);
        final var genType = mockGenType(methSign);

        assertInterface("""
            package test;

            import javax.annotation.processing.Generated;

            @Generated("mdsal-binding-generator")
            public interface test {



                void ontest();
            }
            """, genType);
    }

    @Test
    void builderTemplateDeprecatedListenerMethodTest() {
        final var methSign = mockMethSign("on" + TEST);
        addMethodStatus(methSign, JavaTypeName.create(Deprecated.class));
        final var genType = mockGenType(methSign);

        assertInterface("""
            package test;

            import java.lang.Deprecated;
            import javax.annotation.processing.Generated;

            @Generated("mdsal-binding-generator")
            public interface test {



                @Deprecated
                void ontest();
            }
            """, genType);
    }

    @Test
    void builderTemplateGenerateObsoleteListenerMethodTest() {
        final var methSign = mockMethSign("on" + TEST);
        addMethodStatus(methSign, JavaTypeName.create(Deprecated.class));
        doReturn(true).when(methSign).isDefault();
        final var genType = mockGenType(methSign);

        assertInterface("""
            package test;

            import java.lang.Deprecated;
            import javax.annotation.processing.Generated;

            @Generated("mdsal-binding-generator")
            public interface test {



                @Deprecated
                default void ontest() {
                    // No-op
                }
            }
            """, genType);
    }

    private static @NonNull GeneratedType mockGenType(final MethodSignature methSign) {
        final var genType = spy(GeneratedType.class);
        doReturn(TYPE_NAME).when(genType).name();
        doReturn(TEST).when(genType).simpleName();
        doReturn(TEST).when(genType).packageName();
        doReturn(List.of(methSign)).when(genType).getMethodDefinitions();
        doReturn(List.of()).when(genType).getImplements();
        doReturn(null).when(genType).yangSourceDefinition();
        return genType;
    }

    private static @NonNull MethodSignature mockMethSign(final String methodeName) {
        final var methSign = mock(MethodSignature.class);
        doReturn(methodeName).when(methSign).getName();
        final var methType = Types.typeForClass(void.class);
        doReturn(methType).when(methSign).getReturnType();
        doReturn(MethodSignature.ValueMechanics.NORMAL).when(methSign).getMechanics();
        return methSign;
    }

    private static void addMethodStatus(final MethodSignature methSign, final JavaTypeName annotationJavaType) {
        final var annotationType = mock(AnnotationType.class);
        doReturn(annotationJavaType).when(annotationType).name();
        doReturn(List.of(annotationType)).when(methSign).getAnnotations();
    }

    @NonNullByDefault
    private static void assertInterface(final String expected, final GeneratedType genType) {
        final var sb = new StringBuilder();
        new InterfaceTemplate.Builder(genType, mock(DataRootArchetype.class)).build().generateTo(sb);
        assertEquals(expected, sb.toString());
    }
}
