/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.opendaylight.yangtools.binding.codegen.GeneratorUtil.createImports;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.PATTERN_CONSTANT_NAME;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.Parameter;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.TypeRef;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GeneratorUtilTest {
    private static final @NonNull JavaTypeName ANNOTATION = JavaTypeName.create("tst.package", "tstAnnotationName");
    private static final @NonNull JavaTypeName PARAMETERIZED_TYPE =
        JavaTypeName.create("tst.package", "tstParametrizedType");
    private static final @NonNull JavaTypeName TYPE = JavaTypeName.create("tst.package", "tstName");

    @Mock
    private GeneratedType generatedType;
    @Mock
    private GeneratedTransferObject enclosedType;
    @Mock
    private MethodSignature methodSignature;
    @Mock
    private AnnotationType annotationType;
    @Mock
    private GeneratedProperty property;
    @Mock
    private Constant constant;
    @Mock
    private GeneratedTransferObject superType;

    private ParameterizedType parameterizedType;

    @BeforeEach
    void before() {
        final var type = TypeRef.of(TYPE);
        parameterizedType = ParameterizedType.of(TypeRef.of(PARAMETERIZED_TYPE), type);

        doReturn(parameterizedType).when(property).getReturnType();
        doReturn(List.of(property)).when(enclosedType).getProperties();
        doReturn(Boolean.TRUE).when(property).isReadOnly();
        doReturn("tst.package").when(enclosedType).packageName();
        doReturn("tstName").when(enclosedType).simpleName();

        doReturn(List.of(new Parameter("foo", type))).when(methodSignature).getParameters();

        doReturn("tst.package").when(annotationType).packageName();
        doReturn("tstAnnotationName").when(annotationType).simpleName();
        doReturn(ANNOTATION).when(annotationType).name();

        doReturn(type).when(methodSignature).getReturnType();
        doReturn(List.of(annotationType)).when(methodSignature).getAnnotations();
        doReturn(List.of(methodSignature)).when(enclosedType).getMethodDefinitions();

        doReturn(PATTERN_CONSTANT_NAME).when(constant).getName();
        doReturn(List.of(constant)).when(enclosedType).getConstantDefinitions();

        doReturn(List.of()).when(enclosedType).getEnclosedTypes();
        doReturn(List.of(enclosedType)).when(generatedType).getEnclosedTypes();
    }

    @Test
    void createChildImportsTest() {
        doReturn("tst.package").when(enclosedType).packageName();
        doReturn("tstName").when(enclosedType).simpleName();
        doReturn(List.of()).when(enclosedType).getEnclosedTypes();
        doReturn(List.of(enclosedType)).when(generatedType).getEnclosedTypes();
        final var generated = GeneratorUtil.createChildImports(generatedType);
        assertNotNull(generated);
        assertEquals("tst.package", generated.get("tstName"));
    }

    @Test
    void createImportsWithExceptionTest() {
        final var iae = assertThrows(IllegalArgumentException.class, () -> GeneratorUtil.createImports(null));
        assertEquals("Generated Type cannot be NULL!", iae.getMessage());
    }

    @Test
    void createImportsTest() {
        final var generated = createImports(generatedType);
        assertNotNull(generated);
        assertEquals(JavaTypeName.create("tst.package", "tstAnnotationName"), generated.get("tstAnnotationName"));
    }

    @Test
    void isConstantToExceptionTest() {
        final var iae = assertThrows(IllegalArgumentException.class, () -> GeneratorUtil.isConstantInTO(null, null));
        assertNull(iae.getMessage());
    }

    @Test
    void isConstantToTest() {
        doReturn("tst2").when(constant).getName();
        doReturn(List.of(constant)).when(enclosedType).getConstantDefinitions();
        assertFalse(GeneratorUtil.isConstantInTO("tst", enclosedType));
    }

    @Test
    void getPropertiesOfAllParentsTest() {
        doReturn(enclosedType).when(superType).getSuperType();
        assertTrue(GeneratorUtil.getPropertiesOfAllParents(superType).contains(property));
    }

    @Test
    void getExplicitTypeTest() {
        assertEquals(annotationType.simpleName(), GeneratorUtil.getExplicitType(
                generatedType, annotationType, createImports(generatedType)));

        assertTrue(GeneratorUtil.getExplicitType(generatedType, parameterizedType,
                createImports(generatedType)).contains(parameterizedType.simpleName()));
    }
}