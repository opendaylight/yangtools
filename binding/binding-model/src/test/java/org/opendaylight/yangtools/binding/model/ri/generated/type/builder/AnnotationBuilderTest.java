/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

class AnnotationBuilderTest {
    @Test
    void annotationTypeBuilderEqualsTest() {
        final var annotationTypeBuilder = new AnnotationTypeBuilderImpl(JavaTypeName.create("my.package", "MyName"));
        final var annotationTypeBuilder2 = new AnnotationTypeBuilderImpl(JavaTypeName.create("my.package2", "MyName"));
        final var annotationTypeBuilder3 = new AnnotationTypeBuilderImpl(JavaTypeName.create("my.package", "MyName2"));
        final var annotationTypeBuilder4 = new AnnotationTypeBuilderImpl(JavaTypeName.create("my.package", "MyName"));

        assertFalse(annotationTypeBuilder.equals(null));
        assertFalse(annotationTypeBuilder.equals(new Object()));

        assertTrue(annotationTypeBuilder.equals(annotationTypeBuilder));

        assertTrue(annotationTypeBuilder.equals(annotationTypeBuilder4));
        assertFalse(annotationTypeBuilder.equals(annotationTypeBuilder2));
        assertFalse(annotationTypeBuilder.equals(annotationTypeBuilder3));

        var instance = annotationTypeBuilder.build();
        assertFalse(instance.equals(null));
        assertFalse(instance.equals(new Object()));
        assertTrue(instance.equals(instance));

        var instance2 = annotationTypeBuilder2.build();
        assertFalse(instance.equals(instance2));

        final var instance3 = annotationTypeBuilder3.build();
        assertFalse(instance.equals(instance3));
        final var instance4 = annotationTypeBuilder4.build();
        assertTrue(instance.equals(instance4));

        annotationTypeBuilder.addParameter("myName", "myValue1");
        annotationTypeBuilder.addParameter("myName2", "myValue2");
        annotationTypeBuilder2.addParameter("myName", "myValue3");

        instance = annotationTypeBuilder.build();
        instance2 = annotationTypeBuilder2.build();

        final var parameter = instance.getParameter("myName");
        final var parameter2 = instance.getParameter("myName2");
        final var parameter3 = instance2.getParameter("myName");

        assertFalse(parameter.equals(null));
        assertFalse(parameter.equals(new Object()));
        assertTrue(parameter.equals(parameter));
        assertTrue(parameter.equals(parameter3));
        assertFalse(parameter.equals(parameter2));
    }

    @Test
    void annotationTypeBuilderHashCodeTest() {
        final var annotationTypeBuilder = new AnnotationTypeBuilderImpl(JavaTypeName.create("my.package", "MyName"));
        final var annotationTypeBuilder2 = new AnnotationTypeBuilderImpl(JavaTypeName.create("my.package2", "MyName"));
        final var annotationTypeBuilder3 = new AnnotationTypeBuilderImpl(JavaTypeName.create("my.package", "MyName2"));
        final var annotationTypeBuilder4 = new AnnotationTypeBuilderImpl(JavaTypeName.create("my.package", "MyName"));

        assertFalse(annotationTypeBuilder.hashCode() == annotationTypeBuilder2.hashCode());
        assertFalse(annotationTypeBuilder.hashCode() == annotationTypeBuilder3.hashCode());

        assertTrue(annotationTypeBuilder.hashCode() == annotationTypeBuilder4.hashCode());
        assertTrue(annotationTypeBuilder.hashCode() == annotationTypeBuilder.hashCode());

        var instance = annotationTypeBuilder.build();
        var instance2 = annotationTypeBuilder2.build();
        var instance3 = annotationTypeBuilder3.build();
        var instance4 = annotationTypeBuilder4.build();

        assertFalse(instance.hashCode() == instance2.hashCode());
        assertFalse(instance.hashCode() == instance3.hashCode());

        assertTrue(instance.hashCode() == instance4.hashCode());
        assertTrue(instance.hashCode() == instance.hashCode());

        annotationTypeBuilder.addParameter("myName", "myValue1");
        annotationTypeBuilder.addParameter("myName2", "myValue2");
        annotationTypeBuilder2.addParameter("myName", "myValue3");

        instance = annotationTypeBuilder.build();
        instance2 = annotationTypeBuilder2.build();

        var parameter = instance.getParameter("myName");
        var parameter2 = instance.getParameter("myName2");
        var parameter3 = instance2.getParameter("myName");

        assertTrue(parameter.hashCode() == parameter.hashCode());
        assertTrue(parameter.hashCode() == parameter3.hashCode());
        assertFalse(parameter.hashCode() == parameter2.hashCode());
    }

    @Test
    void annotationTypeBuilderAddParameterTest() {
        final var annotationTypeBuilder = new AnnotationTypeBuilderImpl(JavaTypeName.create("my.package", "MyName"));

        assertFalse(annotationTypeBuilder.addParameter(null, "myValue"));
        assertFalse(annotationTypeBuilder.addParameter("myName", null));

        assertTrue(annotationTypeBuilder.addParameter("myName", "myValue"));
        assertFalse(annotationTypeBuilder.addParameter("myName", "myValue"));

        final var annotationTypeInstance = annotationTypeBuilder.build();
        assertEquals(1, annotationTypeInstance.getParameters().size());
        assertEquals(1, annotationTypeInstance.getParameterNames().size());
        assertTrue(annotationTypeInstance.getParameterNames().contains("myName"));
        final var parameter = annotationTypeInstance.getParameter("myName");
        assertNotNull(parameter);
        assertEquals("myValue", parameter.getValue());

        assertFalse(annotationTypeInstance.getParameterNames().contains("myName2"));
        assertNull(annotationTypeInstance.getParameter("myName2"));
    }

    @Test
    void annotationTypeBuilderToStringTest() {
        final var typeName = JavaTypeName.create("my.package", "MyAnnotationName");
        var annotationTypeBuilder = new AnnotationTypeBuilderImpl(typeName);
        annotationTypeBuilder.addParameter("MyParameter", "myValue");

        assertEquals("""
            AnnotationTypeBuilderImpl{typeName=my.package.MyAnnotationName, \
            parameters=[ParameterImpl [name=MyParameter, value=myValue]]}""", annotationTypeBuilder.toString());

        final var annotationTypeInstance = annotationTypeBuilder.build();
        assertSame(typeName, annotationTypeInstance.name());
        assertEquals("""
            AnnotationType{name=my.package.MyAnnotationName, \
            parameters=[ParameterImpl [name=MyParameter, value=myValue]]}""", annotationTypeInstance.toString());
    }

    @Test
    void testAddParameterMethod() {
        final var annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        annotBuilderImpl.addParameter("testParam", "test value");
        annotBuilderImpl.addParameter(null, "test value");
        final var annotType = annotBuilderImpl.build();
        assertEquals(1, annotType.getParameters().size());
    }

    @Test
    void testHashCode() {
        final var annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        final var annotBuilderImpl2 = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest2"));
        assertFalse(annotBuilderImpl.hashCode() == annotBuilderImpl2.hashCode());
    }

    @Test
    void testEquals() {
        final var annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        final var annotBuilderImpl2 = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        final var annotBuilderImpl3 = annotBuilderImpl2;

        assertTrue(annotBuilderImpl.equals(annotBuilderImpl2));
        assertTrue(annotBuilderImpl2.equals(annotBuilderImpl3));
        assertFalse(annotBuilderImpl2.equals(null));
        assertFalse(annotBuilderImpl2.equals("test"));
    }

    @Test
    void testToString() {
        final var annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        assertNotNull(annotBuilderImpl.toString());
    }

    @Test
    void testMethodsForAnnotationTypeImpl() {
        final var builderName = JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest");
        final var annotBuilderImpl = new AnnotationTypeBuilderImpl(builderName);
        annotBuilderImpl.addParameter("testParam", "test value");
        final var annotationType = annotBuilderImpl.build();
        assertSame(builderName, annotationType.name());
        assertNotNull(annotationType.simpleName());
        assertNotNull(annotationType.packageName());
        assertNull(annotationType.getParameter(null));
        assertNotNull(annotationType.getParameter("testParam"));
        assertFalse(annotationType.getParameterNames().isEmpty());
        assertFalse(annotationType.getParameters().isEmpty());

        final var annotBuilderImpl2 = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        final var annotationType2 = annotBuilderImpl2.build();
        assertTrue(annotationType.hashCode() == annotationType2.hashCode());
        assertTrue(annotationType.equals(annotationType2));
        assertNotNull(annotationType.toString());
    }

    @Test
    void testMethodsForParameterImpl() {
        final var annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        annotBuilderImpl.addParameter("testParam", "test value");
        annotBuilderImpl.addParameter("testParam", "test value");
        annotBuilderImpl.addParameter("", "test value");
        annotBuilderImpl.addParameter(null, "test value");
        annotBuilderImpl.addParameter("", null);
        final var annotationType = annotBuilderImpl.build();

        final var testParam = annotationType.getParameter("testParam");
        assertEquals("testParam", testParam.getName());
        assertEquals("test value", testParam.getValue());

        final var testParams = annotationType.getParameters();
        final var sameParam = testParams.get(0);

        assertFalse(testParams.get(0).equals(testParams.get(1)));
        assertFalse(testParams.get(0).equals(null));
        assertFalse(testParams.get(0).equals("test"));
        assertTrue(testParams.get(0).equals(sameParam));
        assertFalse(testParams.get(0).hashCode() == testParams.get(1).hashCode());
        assertTrue(testParams.get(0).hashCode() == testParams.get(0).hashCode());
    }
}
