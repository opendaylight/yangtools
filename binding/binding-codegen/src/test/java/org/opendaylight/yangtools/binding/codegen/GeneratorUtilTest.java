/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.opendaylight.yangtools.binding.codegen.GeneratorUtil.createImports;
import static org.opendaylight.yangtools.binding.model.ri.TypeConstants.PATTERN_CONSTANT_NAME;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class GeneratorUtilTest {
    private static final JavaTypeName ANNOTATION = JavaTypeName.create("tst.package", "tstAnnotationName");
    private static final JavaTypeName PARAMETERIZED_TYPE = JavaTypeName.create("tst.package", "tstParametrizedType");
    private static final JavaTypeName TYPE = JavaTypeName.create("tst.package", "tstName");

    @Mock
    private GeneratedType generatedType;
    @Mock
    private GeneratedTransferObject enclosedType;
    @Mock
    private MethodSignature methodSignature;
    @Mock
    private Type type;
    @Mock
    private AnnotationType annotationType;
    @Mock
    private MethodSignature.Parameter parameter;
    @Mock
    private GeneratedProperty property;
    @Mock
    private ParameterizedType parameterizedType;
    @Mock
    private Constant constant;
    @Mock
    private GeneratedTransferObject superType;

    @Before
    public void before() {
        doReturn("tst.package").when(parameterizedType).getPackageName();
        doReturn("tstParametrizedType").when(parameterizedType).getName();
        doReturn(PARAMETERIZED_TYPE).when(parameterizedType).getIdentifier();
        doReturn("tst.package").when(type).getPackageName();
        doReturn("tstName").when(type).getName();
        doReturn(TYPE).when(type).getIdentifier();
        doReturn(parameterizedType).when(property).getReturnType();
        doReturn(new Type[] { type }).when(parameterizedType).getActualTypeArguments();
        doReturn(ImmutableList.of(property)).when(enclosedType).getProperties();
        doReturn(Boolean.TRUE).when(property).isReadOnly();
        doReturn("tst.package").when(enclosedType).getPackageName();
        doReturn("tstName").when(enclosedType).getName();

        doReturn(ImmutableList.of(parameter)).when(methodSignature).getParameters();

        doReturn("tst.package").when(annotationType).getPackageName();
        doReturn("tstAnnotationName").when(annotationType).getName();
        doReturn(ANNOTATION).when(annotationType).getIdentifier();

        doReturn(type).when(parameter).getType();
        doReturn(type).when(methodSignature).getReturnType();
        doReturn(ImmutableList.of(annotationType)).when(methodSignature).getAnnotations();
        doReturn(ImmutableList.of(methodSignature)).when(enclosedType).getMethodDefinitions();

        doReturn(PATTERN_CONSTANT_NAME).when(constant).getName();
        doReturn(ImmutableList.of(constant)).when(enclosedType).getConstantDefinitions();

        doReturn(ImmutableList.of()).when(enclosedType).getEnclosedTypes();
        doReturn(ImmutableList.of(enclosedType)).when(generatedType).getEnclosedTypes();
    }

    @Test
    public void createChildImportsTest() {
        doReturn("tst.package").when(enclosedType).getPackageName();
        doReturn("tstName").when(enclosedType).getName();
        doReturn(ImmutableList.of()).when(enclosedType).getEnclosedTypes();
        doReturn(ImmutableList.of(enclosedType)).when(generatedType).getEnclosedTypes();
        final var generated = GeneratorUtil.createChildImports(generatedType);
        assertNotNull(generated);
        assertTrue(generated.get("tstName").equals("tst.package"));
    }

    @Test
    public void createImportsWithExceptionTest() {
        final var iae = assertThrows(IllegalArgumentException.class, () -> GeneratorUtil.createImports(null));
        assertEquals("Generated Type cannot be NULL!", iae.getMessage());
    }

    @Test
    public void createImportsTest() {
        final var generated = createImports(generatedType);
        assertNotNull(generated);
        assertEquals(JavaTypeName.create("tst.package", "tstAnnotationName"), generated.get("tstAnnotationName"));
    }

    @Test
    public void isConstantToExceptionTest() {
        final var iae = assertThrows(IllegalArgumentException.class, () -> GeneratorUtil.isConstantInTO(null, null));
        assertNull(iae.getMessage());
    }

    @Test
    public void isConstantToTest() {
        doReturn("tst2").when(constant).getName();
        doReturn(ImmutableList.of(constant)).when(enclosedType).getConstantDefinitions();
        assertFalse(GeneratorUtil.isConstantInTO("tst", enclosedType));
    }

    @Test
    public void getPropertiesOfAllParentsTest() {
        doReturn(enclosedType).when(superType).getSuperType();
        assertTrue(GeneratorUtil.getPropertiesOfAllParents(superType).contains(property));
    }

    @Test
    public void getExplicitTypeTest() {
        assertEquals(annotationType.getName(), GeneratorUtil.getExplicitType(
                generatedType, annotationType, createImports(generatedType)));

        assertTrue(GeneratorUtil.getExplicitType(generatedType, parameterizedType,
                createImports(generatedType)).contains(parameterizedType.getName()));
    }
}