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
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.Constant;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class GeneratedTypeBuilderTest {

    @Test
    public void addConstantTest() {
        GeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        // assertNotNull(generatedTypeBuilder.addComment("My comment ..."));

        Constant constant = generatedTypeBuilder.addConstant(Types.typeForClass(String.class), "myConstant",
                "myConstantValue");
        // Constant constantx =
        // generatedTypeBuilder.addConstant(Types.typeForClass(String.class),
        // "myConstant", "myConstantValue");
        Constant constant2 = generatedTypeBuilder.addConstant(
                Types.typeForClass(int.class, Restrictions.empty()), "myIntConstant", 1);

        Constant constant3 = new ConstantImpl(Types.typeForClass(String.class), "myConstant", "myConstantValue");
        final Constant constant4 = new ConstantImpl(Types.typeForClass(String.class), "myConstant2", "myConstantValue");
        final Constant constant5 = new ConstantImpl(Types.typeForClass(String.class), "myConstant", "myConstantValue2");

        assertNotNull(constant);
        assertNotNull(constant2);
        assertNotNull(constant3);
        assertNotNull(constant4);
        assertNotNull(constant5);
        // assertNotNull(constantx);
        // assertTrue(constant!=constantx);

        assertFalse(constant.equals(null));
        assertFalse(constant.equals(new Object()));
        assertTrue(constant.equals(constant));
        assertTrue(constant.equals(constant3));
        assertFalse(constant.equals(constant2));
        assertFalse(constant.equals(constant4));
        assertFalse(constant.equals(constant5));

        assertTrue(constant.hashCode() == constant.hashCode());
        assertTrue(constant.hashCode() == constant3.hashCode());
        assertFalse(constant.hashCode() == constant2.hashCode());
        assertFalse(constant.hashCode() == constant4.hashCode());
        assertTrue(constant.hashCode() == constant5.hashCode());

        assertEquals(
            "Constant [type=ConcreteTypeImpl{identifier=java.lang.String}, name=myConstant, value=myConstantValue]",
            constant.toString());

        GeneratedType instance = generatedTypeBuilder.build();
        List<Constant> constantDefinitions = instance.getConstantDefinitions();
        assertNotNull(constantDefinitions);
        assertEquals(2, constantDefinitions.size());
        assertTrue(constantDefinitions.contains(constant));
        assertTrue(constantDefinitions.contains(constant2));
        assertTrue(constantDefinitions.contains(constant3));
        assertFalse(constantDefinitions.contains(constant4));
        assertFalse(constantDefinitions.contains(constant5));

        assertEquals("myConstant", constant.getName());
        assertEquals("myConstantValue", constant.getValue());
        assertEquals(Types.typeForClass(String.class), constant.getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantIllegalArgumentTest() {
        new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName")).addConstant(
            Types.typeForClass(String.class), null, "myConstantValue");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantIllegalArgumentTest2() {
        new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName")).addConstant(null, "myConstantName",
            "myConstantValue");
    }

    @Test
    public void generatedTypeBuilderEqualsAndHashCodeTest() {
        final CodegenGeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));
        final CodegenGeneratedTypeBuilder generatedTypeBuilder2 = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));
        final CodegenGeneratedTypeBuilder generatedTypeBuilder3 = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName2"));
        final CodegenGeneratedTypeBuilder generatedTypeBuilder4 = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package2", "MyName"));

        assertFalse(generatedTypeBuilder.equals(null));
        assertFalse(generatedTypeBuilder.equals(new Object()));
        assertTrue(generatedTypeBuilder.equals(generatedTypeBuilder));
        assertTrue(generatedTypeBuilder.equals(generatedTypeBuilder2));

        assertTrue(generatedTypeBuilder.hashCode() == generatedTypeBuilder.hashCode());
        assertTrue(generatedTypeBuilder.hashCode() == generatedTypeBuilder2.hashCode());
        assertFalse(generatedTypeBuilder.hashCode() == generatedTypeBuilder3.hashCode());
        assertFalse(generatedTypeBuilder.hashCode() == generatedTypeBuilder4.hashCode());

    }

    @Test
    public void addPropertyTest() {
        CodegenGeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        GeneratedPropertyBuilder propertyBuilder = generatedTypeBuilder.addProperty("myProperty");
        GeneratedPropertyBuilder propertyBuilder2 = generatedTypeBuilder.addProperty("myProperty2");
        // GeneratedPropertyBuilder propertyBuilderNull =
        // generatedTypeBuilder.addProperty(null);

        assertNotNull(propertyBuilder);
        assertNotNull(propertyBuilder2);
        // assertNotNull(propertyBuilderNull);

        assertTrue(generatedTypeBuilder.containsProperty("myProperty"));
        assertTrue(generatedTypeBuilder.containsProperty("myProperty2"));
        assertFalse(generatedTypeBuilder.containsProperty("myProperty3"));

        GeneratedType instance = generatedTypeBuilder.build();
        List<GeneratedProperty> properties = instance.getProperties();

        assertEquals(2, properties.size());

        assertTrue(properties.contains(propertyBuilder.toInstance()));
        assertTrue(properties.contains(propertyBuilder2.toInstance()));
        // assertTrue(properties.contains(propertyBuilderNull.toInstance(instance)));
        assertFalse(properties.contains(new GeneratedPropertyBuilderImpl("myProperty3").toInstance()));

    }

    @Test(expected = IllegalArgumentException.class)
    public void addMethodIllegalArgumentTest() {
        new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName")).addMethod(null);
    }

    @Test
    public void addMethodTest() {
        GeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        MethodSignatureBuilder methodBuilder = generatedTypeBuilder.addMethod("myMethodName");
        MethodSignatureBuilder methodBuilder2 = generatedTypeBuilder.addMethod("myMethodName2");

        assertNotNull(methodBuilder);
        assertNotNull(methodBuilder2);

        assertTrue(generatedTypeBuilder.containsMethod("myMethodName"));
        assertTrue(generatedTypeBuilder.containsMethod("myMethodName2"));
        assertFalse(generatedTypeBuilder.containsMethod("myMethodName3"));

        GeneratedType instance = generatedTypeBuilder.build();
        List<MethodSignature> methodDefinitions = instance.getMethodDefinitions();

        assertEquals(2, methodDefinitions.size());

        assertTrue(methodDefinitions.contains(methodBuilder.toInstance(instance)));
        assertTrue(methodDefinitions.contains(methodBuilder2.toInstance(instance)));
        assertFalse(methodDefinitions.contains(new MethodSignatureBuilderImpl("myMethodName3").toInstance(instance)));

    }

    @Test(expected = IllegalArgumentException.class)
    public void addEnumerationIllegalArgumentTest() {
        new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName")).addEnumeration(null);
    }

    @Test
    public void addEnumerationTest() {
        GeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        EnumBuilder enumBuilder = new CodegenEnumerationBuilder(generatedTypeBuilder.getIdentifier()
            .createEnclosed("myEnumName"));
        EnumBuilder enumBuilder2 = new CodegenEnumerationBuilder(generatedTypeBuilder.getIdentifier()
            .createEnclosed("myEnumName2"));

        generatedTypeBuilder.addEnumeration(enumBuilder.toInstance());
        generatedTypeBuilder.addEnumeration(enumBuilder2.toInstance());

        GeneratedType instance = generatedTypeBuilder.build();
        List<Enumeration> enumerations = instance.getEnumerations();

        assertEquals(2, enumerations.size());

        assertTrue(enumerations.contains(enumBuilder.toInstance()));
        assertTrue(enumerations.contains(enumBuilder2.toInstance()));
        assertFalse(enumerations.contains(new CodegenEnumerationBuilder(JavaTypeName.create("my.package",
            "myEnumName3")).toInstance()));

    }

    @Test(expected = IllegalArgumentException.class)
    public void addImplementsTypeIllegalArgumentTest() {
        new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName")).addImplementsType(null);
    }

    @Test
    public void addImplementsTypeTest() {
        CodegenGeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        assertEquals(generatedTypeBuilder,
                generatedTypeBuilder.addImplementsType(Types.typeForClass(Serializable.class)));
        assertEquals(generatedTypeBuilder, generatedTypeBuilder.addImplementsType(Types.typeForClass(Runnable.class)));

        GeneratedType instance = generatedTypeBuilder.build();
        List<Type> implementTypes = instance.getImplements();

        assertEquals(2, implementTypes.size());

        assertTrue(implementTypes.contains(Types.typeForClass(Serializable.class)));
        assertTrue(implementTypes.contains(Types.typeForClass(Runnable.class)));
        assertFalse(implementTypes.contains(Types.typeForClass(Throwable.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addEnclosingTransferObjectIllegalArgumentTest2() {
        new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName")).addEnclosingTransferObject(
            (GeneratedTransferObject) null);
    }

    @Test
    public void addEnclosingTransferObjectTest() {
        GeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        GeneratedTOBuilder enclosingTransferObject = new CodegenGeneratedTOBuilder(generatedTypeBuilder.getIdentifier()
            .createEnclosed("myTOName"));
        GeneratedTOBuilder enclosingTransferObject2 = new CodegenGeneratedTOBuilder(generatedTypeBuilder.getIdentifier()
            .createEnclosed("myTOName2"));
        GeneratedTOBuilder enclosingTransferObject3 = new CodegenGeneratedTOBuilder(generatedTypeBuilder.getIdentifier()
            .createEnclosed("myTOName3"));

        generatedTypeBuilder.addEnclosingTransferObject(enclosingTransferObject.build());
        generatedTypeBuilder.addEnclosingTransferObject(enclosingTransferObject2.build());
        generatedTypeBuilder.addEnclosingTransferObject(enclosingTransferObject3.build());
        GeneratedType instance = generatedTypeBuilder.build();
        List<GeneratedType> enclosedTypes = instance.getEnclosedTypes();

        assertEquals(3, enclosedTypes.size());

        assertTrue(enclosedTypes.contains(enclosingTransferObject.build()));
        assertTrue(enclosedTypes.contains(enclosingTransferObject2.build()));
        assertTrue(enclosedTypes.contains(enclosingTransferObject3.build()));
        assertFalse(enclosedTypes.contains(new CodegenGeneratedTOBuilder(
            generatedTypeBuilder.getIdentifier().createEnclosed("myTOName4")).build()));
    }

    @Test
    public void generatedTypeTest() {
        GeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        generatedTypeBuilder.setDescription("My description ...");
        generatedTypeBuilder.setModuleName("myModuleName");
        generatedTypeBuilder.setReference("myReference");
        generatedTypeBuilder.setSchemaPath(SchemaPath.create(true, QName.create("test", "path")));
        assertNotNull(generatedTypeBuilder.addComment(() -> "My comment.."));

        assertEquals("CodegenGeneratedTypeBuilder{identifier=my.package.MyName, comment=My comment.., constants=[], "
            + "enumerations=[], methods=[], annotations=[], implements=[]}", generatedTypeBuilder.toString());

        GeneratedType instance = generatedTypeBuilder.build();

        assertEquals("My description ...", instance.getDescription());
        assertEquals("myModuleName", instance.getModuleName());
        assertEquals("myReference", instance.getReference());
        assertEquals(SchemaPath.create(true, QName.create("test", "path")).getPathFromRoot(),
            instance.getSchemaPath());
        assertEquals("My comment..", instance.getComment().getJavadoc());
    }
}
