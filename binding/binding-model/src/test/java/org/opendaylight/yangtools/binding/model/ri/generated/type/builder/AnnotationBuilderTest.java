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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.Types;

class AnnotationBuilderTest {
    @Test
    void generatedTypeAnnotationTest() {
        final var genTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotInterface"));

        genTypeBuilder.addAnnotation("javax.management", "MXBean");
        final var annotDesc = genTypeBuilder.addAnnotation("javax.management", "Description");
        annotDesc.addParameter("description", "some sort of interface");

        final var genType = genTypeBuilder.build();

        assertNotNull(genType);
        assertNotNull(genType.getAnnotations());
        assertEquals(2, genType.getAnnotations().size());

        int annotCount = 0;
        for (var annotation : genType.getAnnotations()) {
            if (annotation.getPackageName().equals("javax.management") && annotation.getName().equals("MXBean")) {
                annotCount++;
                assertEquals(0, annotation.getParameters().size());
            }
            if (annotation.getPackageName().equals("javax.management") && annotation.getName().equals("Description")) {
                annotCount++;
                assertEquals(1, annotation.getParameters().size());
                final var param = annotation.getParameter("description");
                assertNotNull(param);
                assertEquals("description", param.getName());
                assertNotNull(param.getValue());
                assertEquals("some sort of interface", param.getValue());
                assertNotNull(param.getValues());
                assertTrue(param.getValues().isEmpty());
            }
        }
        assertEquals(2, annotCount);
    }

    @Test
    void methodSignatureAnnotationTest() {
        final var genTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.controller", "TransferObject"));

        final var methodBuilder = genTypeBuilder.addMethod("simpleMethod");
        methodBuilder.setReturnType(Types.typeForClass(Integer.class));
        final var annotManAttr = methodBuilder.addAnnotation(
                "org.springframework.jmx.export.annotation", "ManagedAttribute");

        annotManAttr.addParameter("description", "\"The Name Attribute\"");
        annotManAttr.addParameter("currencyTimeLimit", "20");
        annotManAttr.addParameter("defaultValue", "\"bar\"");
        annotManAttr.addParameter("persistPolicy", "\"OnUpdate\"");

        final AnnotationTypeBuilder annotManProp = methodBuilder.addAnnotation(
            "org.springframework.jmx.export.annotation", "ManagedOperation");

        annotManProp.addParameters("types", List.of("\"val1\"", "\"val2\"", "\"val3\""));

        final var genType = genTypeBuilder.build();

        assertNotNull(genType);
        assertNotNull(genType.getAnnotations());
        assertNotNull(genType.getMethodDefinitions());
        assertNotNull(genType.getMethodDefinitions().get(0));
        assertNotNull(genType.getMethodDefinitions().get(0).getAnnotations());
        final var annotations = genType.getMethodDefinitions().get(0).getAnnotations();
        assertEquals(2, annotations.size());

        int annotCount = 0;
        for (var annotation : annotations) {
            if (annotation.getPackageName().equals("org.springframework.jmx.export.annotation")
                    && annotation.getName().equals("ManagedAttribute")) {
                annotCount++;
                assertEquals(4, annotation.getParameters().size());

                assertNotNull(annotation.getParameter("description"));
                assertNotNull(annotation.getParameter("currencyTimeLimit"));
                assertNotNull(annotation.getParameter("defaultValue"));
                assertNotNull(annotation.getParameter("persistPolicy"));
                assertEquals("\"The Name Attribute\"", annotation.getParameter("description").getValue());
                assertEquals("20", annotation.getParameter("currencyTimeLimit").getValue());
                assertEquals("\"bar\"", annotation.getParameter("defaultValue").getValue());
                assertEquals("\"OnUpdate\"", annotation.getParameter("persistPolicy").getValue());
            }
            if (annotation.getPackageName().equals("org.springframework.jmx.export.annotation")
                    && annotation.getName().equals("ManagedOperation")) {
                annotCount++;

                assertEquals(1, annotation.getParameters().size());
                assertNotNull(annotation.getParameter("types"));
                assertEquals(3, annotation.getParameter("types").getValues().size());
            }
        }
        assertEquals(2, annotCount);
    }

    @Test
    void generatedPropertyAnnotationTest() {
        final var genTOBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotInterface"));

        final var propertyBuilder = genTOBuilder.addProperty("simpleProperty");
        propertyBuilder.setReturnType(Types.typeForClass(Integer.class));
        final var annotManAttr = propertyBuilder.addAnnotation(
                "org.springframework.jmx.export.annotation", "ManagedAttribute");

        annotManAttr.addParameter("description", "\"The Name Attribute\"");
        annotManAttr.addParameter("currencyTimeLimit", "20");
        annotManAttr.addParameter("defaultValue", "\"bar\"");
        annotManAttr.addParameter("persistPolicy", "\"OnUpdate\"");

        final var annotManProp = propertyBuilder.addAnnotation(
                "org.springframework.jmx.export.annotation", "ManagedOperation");

        annotManProp.addParameters("types", List.of("\"val1\"", "\"val2\"", "\"val3\""));

        final var genTransObj = genTOBuilder.build();

        assertNotNull(genTransObj);
        assertNotNull(genTransObj.getAnnotations());
        assertNotNull(genTransObj.getProperties());
        assertNotNull(genTransObj.getProperties().get(0));
        assertNotNull(genTransObj.getProperties().get(0).getAnnotations());
        final var annotations = genTransObj.getProperties().get(0).getAnnotations();
        assertEquals(2, annotations.size());

        int annotCount = 0;
        for (var annotation : annotations) {
            if (annotation.getPackageName().equals("org.springframework.jmx.export.annotation")
                    && annotation.getName().equals("ManagedAttribute")) {
                annotCount++;
                assertEquals(4, annotation.getParameters().size());

                assertNotNull(annotation.getParameter("description"));
                assertNotNull(annotation.getParameter("currencyTimeLimit"));
                assertNotNull(annotation.getParameter("defaultValue"));
                assertNotNull(annotation.getParameter("persistPolicy"));
                assertEquals("\"The Name Attribute\"", annotation.getParameter("description").getValue());
                assertEquals("20", annotation.getParameter("currencyTimeLimit").getValue());
                assertEquals("\"bar\"", annotation.getParameter("defaultValue").getValue());
                assertEquals("\"OnUpdate\"", annotation.getParameter("persistPolicy").getValue());
            }
            if (annotation.getPackageName().equals("org.springframework.jmx.export.annotation")
                    && annotation.getName().equals("ManagedOperation")) {
                annotCount++;

                assertEquals(1, annotation.getParameters().size());
                assertNotNull(annotation.getParameter("types"));
                assertEquals(3, annotation.getParameter("types").getValues().size());
            }
        }
        assertEquals(2, annotCount);
    }

    @Test
    void generatedTransfeObjectAnnotationTest() {
        final var genTypeBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotClassCache"));

        genTypeBuilder.addAnnotation("javax.management", "MBean");
        final var annotNotify = genTypeBuilder.addAnnotation("javax.management", "NotificationInfo");

        annotNotify.addParameters("types", List.of("\"my.notif.type\""));
        annotNotify.addParameter("description", "@Description(\"my notification\")");

        var genTO = genTypeBuilder.build();

        assertNotNull(genTO);
        assertNotNull(genTO.getAnnotations());
        assertEquals(2, genTO.getAnnotations().size());

        int annotCount = 0;
        for (var annotation : genTO.getAnnotations()) {
            if (annotation.getPackageName().equals("javax.management") && annotation.getName().equals("MBean")) {
                annotCount++;
                assertEquals(0, annotation.getParameters().size());
            }
            if (annotation.getPackageName().equals("javax.management")
                    && annotation.getName().equals("NotificationInfo")) {
                annotCount++;
                assertEquals(2, annotation.getParameters().size());
                var param = annotation.getParameter("types");
                assertNotNull(param);
                assertEquals("types", param.getName());
                assertNull(param.getValue());
                assertNotNull(param.getValues());
                assertEquals(1, param.getValues().size());
                assertEquals("\"my.notif.type\"", param.getValues().get(0));

                param = annotation.getParameter("description");
                assertNotNull(param);
                assertEquals("description", param.getName());
                assertNotNull(param.getValue());
                assertEquals("@Description(\"my notification\")", param.getValue());
            }
        }
        assertEquals(2, annotCount);
    }

    @Test
    void annotationTypeBuilderAddAnnotationTest() {
        final var annotationTypeBuilder = new AnnotationTypeBuilderImpl(JavaTypeName.create("my.package", "MyName"));

        assertThrows(NullPointerException.class, () -> annotationTypeBuilder.addAnnotation("my.package", null));
        assertThrows(NullPointerException.class, () -> annotationTypeBuilder.addAnnotation(null, "MyName"));

        assertNotNull(annotationTypeBuilder.addAnnotation("java.lang", "Deprecated"));

        final var builder = annotationTypeBuilder.addAnnotation("my.package2", "MyName2");
        assertNotNull(builder);
        assertSame(builder, annotationTypeBuilder.addAnnotation("my.package2", "MyName2"));

        var annotationTypeInstance = annotationTypeBuilder.build();

        assertEquals(2, annotationTypeInstance.getAnnotations().size());

        assertEquals("my.package", annotationTypeInstance.getPackageName());
        assertEquals("MyName", annotationTypeInstance.getName());

    }

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

        assertFalse(annotationTypeBuilder.addParameters(null, List.of()));
        assertFalse(annotationTypeBuilder.addParameters("myName", null));

        assertTrue(annotationTypeBuilder.addParameter("myName", "myValue"));
        assertFalse(annotationTypeBuilder.addParameter("myName", "myValue"));
        assertFalse(annotationTypeBuilder.addParameters("myName", List.of()));

        assertTrue(annotationTypeBuilder.addParameters("myName2", List.of("myValue")));

        var annotationTypeInstance = annotationTypeBuilder.build();
        assertTrue(annotationTypeInstance.containsParameters());
        assertEquals(2, annotationTypeInstance.getParameters().size());
        assertEquals(2, annotationTypeInstance.getParameterNames().size());
        assertTrue(annotationTypeInstance.getParameterNames().contains("myName"));
        assertTrue(annotationTypeInstance.getParameterNames().contains("myName2"));
        assertFalse(annotationTypeInstance.getParameterNames().contains("myName3"));

        var parameter = annotationTypeInstance.getParameter("myName");
        var parameter2 = annotationTypeInstance.getParameter("myName2");
        var parameter3 = annotationTypeInstance.getParameter("myName3");

        assertNotNull(parameter);
        assertNotNull(parameter2);
        assertNull(parameter3);

        assertEquals(parameter.getValue(), "myValue");
        assertTrue(parameter.getValues().isEmpty());

        assertEquals(1, parameter2.getValues().size());
        assertTrue(parameter2.getValues().contains("myValue"));
    }

    @Test
    void annotationTypeBuilderToStringTest() {
        var annotationTypeBuilder = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("my.package", "MyAnnotationName"));
        annotationTypeBuilder.addAnnotation("my.package", "MySubAnnotationName");
        annotationTypeBuilder.addParameter("MyParameter", "myValue");

        assertEquals("""
            AnnotationTypeBuilderImpl{identifier=my.package.MyAnnotationName, \
            annotationBuilders=[AnnotationTypeBuilderImpl{identifier=my.package.MySubAnnotationName, \
            annotationBuilders=[], parameters=[]}], parameters=[ParameterImpl [name=MyParameter, value=myValue, \
            values=[]]]}""", annotationTypeBuilder.toString());

        var annotationTypeInstance = annotationTypeBuilder.build();

        assertEquals("my.package.MyAnnotationName", annotationTypeInstance.getFullyQualifiedName());
        assertEquals("""
            AnnotationTypeImpl{identifier=my.package.MyAnnotationName, \
            annotations=[AnnotationTypeImpl{identifier=my.package.MySubAnnotationName, annotations=[], \
            parameters=[]}], parameters=[ParameterImpl [name=MyParameter, value=myValue, values=[]]]}""",
                annotationTypeInstance.toString());
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
    void testAddParametersMethod() {
        final var annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));

        annotBuilderImpl.addParameters("testParam", List.of("test1", "test2", "test3"));

        var annotType = annotBuilderImpl.build();
        assertEquals(1, annotType.getParameters().size());

        annotBuilderImpl.addParameters("testParam", null);

        annotType = annotBuilderImpl.build();
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
        final var annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        annotBuilderImpl.addParameter("testParam", "test value");
        final var annotationType = annotBuilderImpl.build();

        final var annotBuilderImpl2 = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        final var annotationType2 = annotBuilderImpl2.build();

        assertTrue(annotationType.containsParameters());
        assertTrue(annotationType.getAnnotations().isEmpty());
        assertNotNull(annotationType.getFullyQualifiedName());
        assertNotNull(annotationType.getName());
        assertNotNull(annotationType.getPackageName());
        assertNull(annotationType.getParameter(null));
        assertNotNull(annotationType.getParameter("testParam"));
        assertFalse(annotationType.getParameterNames().isEmpty());
        assertFalse(annotationType.getParameters().isEmpty());

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
        assertEquals(0, testParam.getValues().size());

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
