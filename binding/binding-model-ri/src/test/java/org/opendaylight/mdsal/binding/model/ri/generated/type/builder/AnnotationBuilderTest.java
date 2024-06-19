/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.AnnotationType.Parameter;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.ri.Types;

public class AnnotationBuilderTest {

    @Test
    public void generatedTypeAnnotationTest() {
        final GeneratedTypeBuilder genTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotInterface"));

        genTypeBuilder.addAnnotation("javax.management", "MXBean");
        final AnnotationTypeBuilder annotDesc = genTypeBuilder.addAnnotation("javax.management", "Description");
        annotDesc.addParameter("description", "some sort of interface");

        final GeneratedType genType = genTypeBuilder.build();

        assertNotNull(genType);
        assertNotNull(genType.getAnnotations());
        assertEquals(2, genType.getAnnotations().size());

        int annotCount = 0;
        for (final AnnotationType annotation : genType.getAnnotations()) {
            if (annotation.getPackageName().equals("javax.management") && annotation.getName().equals("MXBean")) {
                annotCount++;
                assertEquals(0, annotation.getParameters().size());
            }
            if (annotation.getPackageName().equals("javax.management") && annotation.getName().equals("Description")) {
                annotCount++;
                assertEquals(1, annotation.getParameters().size());
                AnnotationType.Parameter param = annotation.getParameter("description");
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
    public void methodSignatureAnnotationTest() {
        final GeneratedTypeBuilder genTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.controller", "TransferObject"));

        final MethodSignatureBuilder methodBuilder = genTypeBuilder.addMethod("simpleMethod");
        methodBuilder.setReturnType(Types.typeForClass(Integer.class));
        final AnnotationTypeBuilder annotManAttr = methodBuilder.addAnnotation(
                "org.springframework.jmx.export.annotation", "ManagedAttribute");

        annotManAttr.addParameter("description", "\"The Name Attribute\"");
        annotManAttr.addParameter("currencyTimeLimit", "20");
        annotManAttr.addParameter("defaultValue", "\"bar\"");
        annotManAttr.addParameter("persistPolicy", "\"OnUpdate\"");

        final AnnotationTypeBuilder annotManProp = methodBuilder.addAnnotation(
            "org.springframework.jmx.export.annotation", "ManagedOperation");

        final List<String> typeValues = new ArrayList<>();
        typeValues.add("\"val1\"");
        typeValues.add("\"val2\"");
        typeValues.add("\"val3\"");
        annotManProp.addParameters("types", typeValues);

        final GeneratedType genType = genTypeBuilder.build();

        assertNotNull(genType);
        assertNotNull(genType.getAnnotations());
        assertNotNull(genType.getMethodDefinitions());
        assertNotNull(genType.getMethodDefinitions().get(0));
        assertNotNull(genType.getMethodDefinitions().get(0).getAnnotations());
        final List<AnnotationType> annotations = genType.getMethodDefinitions().get(0).getAnnotations();
        assertEquals(2, annotations.size());

        int annotCount = 0;
        for (final AnnotationType annotation : annotations) {
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
    public void generatedPropertyAnnotationTest() {
        final GeneratedTOBuilder genTOBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotInterface"));

        final GeneratedPropertyBuilder propertyBuilder = genTOBuilder.addProperty("simpleProperty");
        propertyBuilder.setReturnType(Types.typeForClass(Integer.class));
        final AnnotationTypeBuilder annotManAttr = propertyBuilder.addAnnotation(
                "org.springframework.jmx.export.annotation", "ManagedAttribute");

        annotManAttr.addParameter("description", "\"The Name Attribute\"");
        annotManAttr.addParameter("currencyTimeLimit", "20");
        annotManAttr.addParameter("defaultValue", "\"bar\"");
        annotManAttr.addParameter("persistPolicy", "\"OnUpdate\"");

        final AnnotationTypeBuilder annotManProp = propertyBuilder.addAnnotation(
                "org.springframework.jmx.export.annotation", "ManagedOperation");

        final List<String> typeValues = new ArrayList<>();
        typeValues.add("\"val1\"");
        typeValues.add("\"val2\"");
        typeValues.add("\"val3\"");
        annotManProp.addParameters("types", typeValues);

        final GeneratedTransferObject genTransObj = genTOBuilder.build();

        assertNotNull(genTransObj);
        assertNotNull(genTransObj.getAnnotations());
        assertNotNull(genTransObj.getProperties());
        assertNotNull(genTransObj.getProperties().get(0));
        assertNotNull(genTransObj.getProperties().get(0).getAnnotations());
        final List<AnnotationType> annotations = genTransObj.getProperties().get(0).getAnnotations();
        assertEquals(2, annotations.size());

        int annotCount = 0;
        for (final AnnotationType annotation : annotations) {
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
    public void generatedTransfeObjectAnnotationTest() {
        final GeneratedTOBuilder genTypeBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotClassCache"));

        genTypeBuilder.addAnnotation("javax.management", "MBean");
        final AnnotationTypeBuilder annotNotify = genTypeBuilder.addAnnotation("javax.management", "NotificationInfo");

        final List<String> notifyList = new ArrayList<>();
        notifyList.add("\"my.notif.type\"");
        annotNotify.addParameters("types", notifyList);
        annotNotify.addParameter("description", "@Description(\"my notification\")");

        GeneratedTransferObject genTO = genTypeBuilder.build();

        assertNotNull(genTO);
        assertNotNull(genTO.getAnnotations());
        assertEquals(2, genTO.getAnnotations().size());

        int annotCount = 0;
        for (final AnnotationType annotation : genTO.getAnnotations()) {
            if (annotation.getPackageName().equals("javax.management") && annotation.getName().equals("MBean")) {
                annotCount++;
                assertEquals(0, annotation.getParameters().size());
            }
            if (annotation.getPackageName().equals("javax.management")
                    && annotation.getName().equals("NotificationInfo")) {
                annotCount++;
                assertEquals(2, annotation.getParameters().size());
                AnnotationType.Parameter param = annotation.getParameter("types");
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
    public void annotationTypeBuilderAddAnnotationTest() {
        AnnotationTypeBuilder annotationTypeBuilder = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("my.package", "MyName"));

        assertThrows(NullPointerException.class, () -> annotationTypeBuilder.addAnnotation("my.package", null));
        assertThrows(NullPointerException.class, () -> annotationTypeBuilder.addAnnotation(null, "MyName"));

        assertNotNull(annotationTypeBuilder.addAnnotation("java.lang", "Deprecated"));

        final var builder = annotationTypeBuilder.addAnnotation("my.package2", "MyName2");
        assertNotNull(builder);
        assertSame(builder, annotationTypeBuilder.addAnnotation("my.package2", "MyName2"));

        AnnotationType annotationTypeInstance = annotationTypeBuilder.build();

        assertEquals(2, annotationTypeInstance.getAnnotations().size());

        assertEquals("my.package", annotationTypeInstance.getPackageName());
        assertEquals("MyName", annotationTypeInstance.getName());

    }

    @Test
    public void annotationTypeBuilderEqualsTest() {
        final AnnotationTypeBuilder annotationTypeBuilder = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("my.package", "MyName"));
        final AnnotationTypeBuilder annotationTypeBuilder2 = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("my.package2", "MyName"));
        final AnnotationTypeBuilder annotationTypeBuilder3 = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("my.package", "MyName2"));
        final AnnotationTypeBuilder annotationTypeBuilder4 = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("my.package", "MyName"));

        assertFalse(annotationTypeBuilder.equals(null));
        assertFalse(annotationTypeBuilder.equals(new Object()));

        assertTrue(annotationTypeBuilder.equals(annotationTypeBuilder));

        assertTrue(annotationTypeBuilder.equals(annotationTypeBuilder4));
        assertFalse(annotationTypeBuilder.equals(annotationTypeBuilder2));
        assertFalse(annotationTypeBuilder.equals(annotationTypeBuilder3));

        AnnotationType instance = annotationTypeBuilder.build();
        assertFalse(instance.equals(null));
        assertFalse(instance.equals(new Object()));
        assertTrue(instance.equals(instance));

        AnnotationType instance2 = annotationTypeBuilder2.build();
        assertFalse(instance.equals(instance2));

        final AnnotationType instance3 = annotationTypeBuilder3.build();
        assertFalse(instance.equals(instance3));
        final AnnotationType instance4 = annotationTypeBuilder4.build();
        assertTrue(instance.equals(instance4));

        annotationTypeBuilder.addParameter("myName", "myValue1");
        annotationTypeBuilder.addParameter("myName2", "myValue2");
        annotationTypeBuilder2.addParameter("myName", "myValue3");

        instance = annotationTypeBuilder.build();
        instance2 = annotationTypeBuilder2.build();

        final Parameter parameter = instance.getParameter("myName");
        final Parameter parameter2 = instance.getParameter("myName2");
        final Parameter parameter3 = instance2.getParameter("myName");

        assertFalse(parameter.equals(null));
        assertFalse(parameter.equals(new Object()));
        assertTrue(parameter.equals(parameter));
        assertTrue(parameter.equals(parameter3));
        assertFalse(parameter.equals(parameter2));
    }

    @Test
    public void annotationTypeBuilderHashCodeTest() {
        AnnotationTypeBuilder annotationTypeBuilder = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("my.package", "MyName"));
        AnnotationTypeBuilder annotationTypeBuilder2 = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("my.package2", "MyName"));
        AnnotationTypeBuilder annotationTypeBuilder3 = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("my.package", "MyName2"));
        AnnotationTypeBuilder annotationTypeBuilder4 = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("my.package", "MyName"));

        assertFalse(annotationTypeBuilder.hashCode() == annotationTypeBuilder2.hashCode());
        assertFalse(annotationTypeBuilder.hashCode() == annotationTypeBuilder3.hashCode());

        assertTrue(annotationTypeBuilder.hashCode() == annotationTypeBuilder4.hashCode());
        assertTrue(annotationTypeBuilder.hashCode() == annotationTypeBuilder.hashCode());

        AnnotationType instance = annotationTypeBuilder.build();
        AnnotationType instance2 = annotationTypeBuilder2.build();
        AnnotationType instance3 = annotationTypeBuilder3.build();
        AnnotationType instance4 = annotationTypeBuilder4.build();

        assertFalse(instance.hashCode() == instance2.hashCode());
        assertFalse(instance.hashCode() == instance3.hashCode());

        assertTrue(instance.hashCode() == instance4.hashCode());
        assertTrue(instance.hashCode() == instance.hashCode());

        annotationTypeBuilder.addParameter("myName", "myValue1");
        annotationTypeBuilder.addParameter("myName2", "myValue2");
        annotationTypeBuilder2.addParameter("myName", "myValue3");

        instance = annotationTypeBuilder.build();
        instance2 = annotationTypeBuilder2.build();

        Parameter parameter = instance.getParameter("myName");
        Parameter parameter2 = instance.getParameter("myName2");
        Parameter parameter3 = instance2.getParameter("myName");

        assertTrue(parameter.hashCode() == parameter.hashCode());
        assertTrue(parameter.hashCode() == parameter3.hashCode());
        assertFalse(parameter.hashCode() == parameter2.hashCode());

    }

    @Test
    public void annotationTypeBuilderAddParameterTest() {
        AnnotationTypeBuilder annotationTypeBuilder = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("my.package", "MyName"));

        assertFalse(annotationTypeBuilder.addParameter(null, "myValue"));
        assertFalse(annotationTypeBuilder.addParameter("myName", null));

        assertFalse(annotationTypeBuilder.addParameters(null, new ArrayList<String>()));
        assertFalse(annotationTypeBuilder.addParameters("myName", null));

        assertTrue(annotationTypeBuilder.addParameter("myName", "myValue"));
        assertFalse(annotationTypeBuilder.addParameter("myName", "myValue"));
        assertFalse(annotationTypeBuilder.addParameters("myName", new ArrayList<String>()));

        ArrayList<String> values = new ArrayList<>();
        values.add("myValue");
        assertTrue(annotationTypeBuilder.addParameters("myName2", values));

        AnnotationType annotationTypeInstance = annotationTypeBuilder.build();
        assertTrue(annotationTypeInstance.containsParameters());
        assertEquals(2, annotationTypeInstance.getParameters().size());
        assertEquals(2, annotationTypeInstance.getParameterNames().size());
        assertTrue(annotationTypeInstance.getParameterNames().contains("myName"));
        assertTrue(annotationTypeInstance.getParameterNames().contains("myName2"));
        assertFalse(annotationTypeInstance.getParameterNames().contains("myName3"));

        Parameter parameter = annotationTypeInstance.getParameter("myName");
        Parameter parameter2 = annotationTypeInstance.getParameter("myName2");
        Parameter parameter3 = annotationTypeInstance.getParameter("myName3");

        assertNotNull(parameter);
        assertNotNull(parameter2);
        assertNull(parameter3);

        assertEquals(parameter.getValue(), "myValue");
        assertTrue(parameter.getValues().isEmpty());

        assertEquals(1, parameter2.getValues().size());
        assertTrue(parameter2.getValues().contains("myValue"));

    }

    @Test
    public void annotationTypeBuilderToStringTest() {
        AnnotationTypeBuilder annotationTypeBuilder = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("my.package", "MyAnnotationName"));
        annotationTypeBuilder.addAnnotation("my.package", "MySubAnnotationName");
        annotationTypeBuilder.addParameter("MyParameter", "myValue");

        assertEquals("AnnotationTypeBuilderImpl{identifier=my.package.MyAnnotationName, "
            + "annotationBuilders=[AnnotationTypeBuilderImpl{identifier=my.package.MySubAnnotationName, "
            + "annotationBuilders=[], parameters=[]}], parameters=[ParameterImpl [name=MyParameter, value=myValue, "
            + "values=[]]]}", annotationTypeBuilder.toString());

        AnnotationType annotationTypeInstance = annotationTypeBuilder.build();

        assertEquals("my.package.MyAnnotationName", annotationTypeInstance.getFullyQualifiedName());
        assertEquals("AnnotationTypeImpl{identifier=my.package.MyAnnotationName, "
            + "annotations=[AnnotationTypeImpl{identifier=my.package.MySubAnnotationName, annotations=[], "
            + "parameters=[]}], parameters=[ParameterImpl [name=MyParameter, value=myValue, values=[]]]}",
                annotationTypeInstance.toString());

    }

    public void testAddAnnotation() {
        final AnnotationTypeBuilderImpl annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        annotBuilderImpl.addAnnotation("org.opedaylight.yangtools.test.v1", "AnnotationTest2");
        annotBuilderImpl.addAnnotation(null, "AnnotationTest2");
        assertFalse(annotBuilderImpl.build().getAnnotations().isEmpty());
    }

    @Test
    public void testAddParameterMethod() {
        final AnnotationTypeBuilderImpl annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        annotBuilderImpl.addParameter("testParam", "test value");
        annotBuilderImpl.addParameter(null, "test value");
        final AnnotationType annotType = annotBuilderImpl.build();
        assertEquals(1, annotType.getParameters().size());
    }

    @Test
    public void testAddParametersMethod() {
        final AnnotationTypeBuilderImpl annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));

        final List<String> values = new ArrayList<>();
        values.add("test1");
        values.add("test2");
        values.add("test3");
        annotBuilderImpl.addParameters("testParam", values);

        AnnotationType annotType = annotBuilderImpl.build();
        assertEquals(1, annotType.getParameters().size());

        annotBuilderImpl.addParameters("testParam", null);

        annotType = annotBuilderImpl.build();
        assertEquals(1, annotType.getParameters().size());
    }

    @Test
    public void testHashCode() {
        final AnnotationTypeBuilderImpl annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        final AnnotationTypeBuilderImpl annotBuilderImpl2 = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest2"));
        assertFalse(annotBuilderImpl.hashCode() == annotBuilderImpl2.hashCode());
    }

    @Test
    public void testEquals() {
        final AnnotationTypeBuilderImpl annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        final AnnotationTypeBuilderImpl annotBuilderImpl2 = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        final AnnotationTypeBuilderImpl annotBuilderImpl3 = annotBuilderImpl2;

        assertTrue(annotBuilderImpl.equals(annotBuilderImpl2));
        assertTrue(annotBuilderImpl2.equals(annotBuilderImpl3));
        assertFalse(annotBuilderImpl2.equals(null));
        assertFalse(annotBuilderImpl2.equals("test"));
    }

    @Test
    public void testToString() {
        final AnnotationTypeBuilderImpl annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        assertNotNull(annotBuilderImpl.toString());
    }

    @Test
    public void testMethodsForAnnotationTypeImpl() {
        final AnnotationTypeBuilderImpl annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        annotBuilderImpl.addParameter("testParam", "test value");
        final AnnotationType annotationType = annotBuilderImpl.build();

        final AnnotationTypeBuilderImpl annotBuilderImpl2 = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        final AnnotationType annotationType2 = annotBuilderImpl2.build();

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
    public void testMethodsForParameterImpl() {
        final AnnotationTypeBuilderImpl annotBuilderImpl = new AnnotationTypeBuilderImpl(
            JavaTypeName.create("org.opedaylight.yangtools.test", "AnnotationTest"));
        annotBuilderImpl.addParameter("testParam", "test value");
        annotBuilderImpl.addParameter("testParam", "test value");
        annotBuilderImpl.addParameter("", "test value");
        annotBuilderImpl.addParameter(null, "test value");
        annotBuilderImpl.addParameter("", null);
        final AnnotationType annotationType = annotBuilderImpl.build();

        final Parameter testParam = annotationType.getParameter("testParam");
        assertEquals("testParam", testParam.getName());
        assertEquals("test value", testParam.getValue());
        assertEquals(0, testParam.getValues().size());

        final List<Parameter> testParams = annotationType.getParameters();
        final Parameter sameParam = testParams.get(0);

        assertFalse(testParams.get(0).equals(testParams.get(1)));
        assertFalse(testParams.get(0).equals(null));
        assertFalse(testParams.get(0).equals("test"));
        assertTrue(testParams.get(0).equals(sameParam));
        assertFalse(testParams.get(0).hashCode() == testParams.get(1).hashCode());
        assertTrue(testParams.get(0).hashCode() == testParams.get(0).hashCode());
    }
}
