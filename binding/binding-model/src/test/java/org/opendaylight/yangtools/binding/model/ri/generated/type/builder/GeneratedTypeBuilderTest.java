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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.ri.Types;

class GeneratedTypeBuilderTest {
    @Test
    void addConstantTest() {
        var generatedTypeBuilder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));

        // assertNotNull(generatedTypeBuilder.addComment("My comment ..."));

        var constant = generatedTypeBuilder.addConstant(Types.typeForClass(String.class), "myConstant",
                "myConstantValue");
        // Constant constantx =
        // generatedTypeBuilder.addConstant(Types.typeForClass(String.class),
        // "myConstant", "myConstantValue");
        var constant2 = generatedTypeBuilder.addConstant(
                Types.typeForClass(int.class, Restrictions.empty()), "myIntConstant", 1);

        var constant3 = new ConstantImpl(Types.typeForClass(String.class), "myConstant", "myConstantValue");
        final var constant4 = new ConstantImpl(Types.typeForClass(String.class), "myConstant2", "myConstantValue");
        final var constant5 = new ConstantImpl(Types.typeForClass(String.class), "myConstant", "myConstantValue2");

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

        assertEquals(constant.hashCode(), constant.hashCode());
        assertEquals(constant.hashCode(), constant3.hashCode());
        assertNotEquals(constant.hashCode(), constant2.hashCode());
        assertNotEquals(constant.hashCode(), constant4.hashCode());
        assertEquals(constant.hashCode(), constant5.hashCode());

        assertEquals(
            "Constant [type=ConcreteTypeImpl{identifier=java.lang.String}, name=myConstant, value=myConstantValue]",
            constant.toString());

        var instance = generatedTypeBuilder.build();
        var constantDefinitions = instance.getConstantDefinitions();
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

    @Test
    void addConstantIllegalArgumentTest() {
        final var builder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));
        assertThrows(IllegalArgumentException.class,
            () -> builder.addConstant(Types.typeForClass(String.class), null, "myConstantValue"));
    }

    @Test
    void addConstantIllegalArgumentTest2() {
        final var builder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));
        assertThrows(IllegalArgumentException.class,
            () -> builder.addConstant(null, "myConstantName", "myConstantValue"));
    }

    @Test
    void generatedTypeBuilderEqualsAndHashCodeTest() {
        final var generatedTypeBuilder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));
        final var generatedTypeBuilder2 = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));
        final var generatedTypeBuilder3 = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName2"));
        final var generatedTypeBuilder4 = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package2", "MyName"));

        assertFalse(generatedTypeBuilder.equals(null));
        assertFalse(generatedTypeBuilder.equals(new Object()));
        assertTrue(generatedTypeBuilder.equals(generatedTypeBuilder));
        assertTrue(generatedTypeBuilder.equals(generatedTypeBuilder2));

        assertEquals(generatedTypeBuilder.hashCode(), generatedTypeBuilder.hashCode());
        assertEquals(generatedTypeBuilder.hashCode(), generatedTypeBuilder2.hashCode());
        assertNotEquals(generatedTypeBuilder.hashCode(), generatedTypeBuilder3.hashCode());
        assertNotEquals(generatedTypeBuilder.hashCode(), generatedTypeBuilder4.hashCode());
    }

    @Test
    void addPropertyTest() {
        var generatedTypeBuilder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));

        var propertyBuilder = generatedTypeBuilder.addProperty("myProperty").setReturnType(Types.STRING);
        var propertyBuilder2 = generatedTypeBuilder.addProperty("myProperty2").setReturnType(Types.primitiveIntType());

        assertNotNull(propertyBuilder);
        assertNotNull(propertyBuilder2);

        assertTrue(generatedTypeBuilder.containsProperty("myProperty"));
        assertTrue(generatedTypeBuilder.containsProperty("myProperty2"));
        assertFalse(generatedTypeBuilder.containsProperty("myProperty3"));

        var instance = generatedTypeBuilder.build();
        var properties = instance.getProperties();

        assertEquals(2, properties.size());

        assertTrue(properties.contains(propertyBuilder.toInstance()));
        assertTrue(properties.contains(propertyBuilder2.toInstance()));
        assertFalse(properties.contains(new GeneratedPropertyBuilderImpl("myProperty3")
            .setReturnType(Types.STRING)
            .toInstance()));
    }

    @Test
    void addMethodIllegalArgumentTest() {
        final var builder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));
        assertThrows(IllegalArgumentException.class, () -> builder.addMethod(null));
    }

    @Test
    void addMethodTest() {
        var generatedTypeBuilder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));

        var methodBuilder = generatedTypeBuilder.addMethod("myMethodName").setReturnType(Types.BOOLEAN);
        var methodBuilder2 = generatedTypeBuilder.addMethod("myMethodName2").setReturnType(Types.STRING);

        assertNotNull(methodBuilder);
        assertNotNull(methodBuilder2);

        assertTrue(generatedTypeBuilder.containsMethod("myMethodName"));
        assertTrue(generatedTypeBuilder.containsMethod("myMethodName2"));
        assertFalse(generatedTypeBuilder.containsMethod("myMethodName3"));

        var instance = generatedTypeBuilder.build();
        var methodDefinitions = instance.getMethodDefinitions();

        assertEquals(2, methodDefinitions.size());

        assertTrue(methodDefinitions.contains(methodBuilder.toInstance(instance)));
        assertTrue(methodDefinitions.contains(methodBuilder2.toInstance(instance)));
        assertFalse(methodDefinitions.contains(new MethodSignatureBuilderImpl("myMethodName3")
            .setReturnType(Types.BOOLEAN)
            .toInstance(instance)));
    }

    @Test
    void addEnumerationIllegalArgumentTest() {
        final var builder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));
        assertThrows(IllegalArgumentException.class, () -> builder.addEnumeration(null));
    }

    @Test
    void addEnumerationTest() {
        var generatedTypeBuilder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));

        var enumBuilder = new CodegenEnumerationBuilder(generatedTypeBuilder.getIdentifier()
            .createEnclosed("myEnumName"));
        var enumBuilder2 = new CodegenEnumerationBuilder(generatedTypeBuilder.getIdentifier()
            .createEnclosed("myEnumName2"));

        generatedTypeBuilder.addEnumeration(enumBuilder.toInstance());
        generatedTypeBuilder.addEnumeration(enumBuilder2.toInstance());

        var instance = generatedTypeBuilder.build();
        var enumerations = instance.getEnumerations();

        assertEquals(2, enumerations.size());

        assertTrue(enumerations.contains(enumBuilder.toInstance()));
        assertTrue(enumerations.contains(enumBuilder2.toInstance()));
        assertFalse(enumerations.contains(new CodegenEnumerationBuilder(JavaTypeName.create("my.package",
            "myEnumName3")).toInstance()));
    }

    @Test
    void addImplementsTypeIllegalArgumentTest() {
        final var builder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));
        assertThrows(IllegalArgumentException.class, () -> builder.addImplementsType(null));
    }

    @Test
    void addImplementsTypeTest() {
        var generatedTypeBuilder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));

        assertEquals(generatedTypeBuilder,
                generatedTypeBuilder.addImplementsType(Types.typeForClass(Serializable.class)));
        assertEquals(generatedTypeBuilder, generatedTypeBuilder.addImplementsType(Types.typeForClass(Runnable.class)));

        var instance = generatedTypeBuilder.build();
        var implementTypes = instance.getImplements();

        assertEquals(2, implementTypes.size());

        assertTrue(implementTypes.contains(Types.typeForClass(Serializable.class)));
        assertTrue(implementTypes.contains(Types.typeForClass(Runnable.class)));
        assertFalse(implementTypes.contains(Types.typeForClass(Throwable.class)));
    }

    @Test
    void addEnclosingTransferObjectIllegalArgumentTest2() {
        final var builder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));
        assertThrows(IllegalArgumentException.class,
            () -> builder.addEnclosingTransferObject((GeneratedTransferObject) null));
    }

    @Test
    void addEnclosingTransferObjectTest() {
        var generatedTypeBuilder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));

        var enclosingTransferObject = new CodegenGeneratedTOBuilder(generatedTypeBuilder.getIdentifier()
            .createEnclosed("myTOName"));
        var enclosingTransferObject2 = new CodegenGeneratedTOBuilder(generatedTypeBuilder.getIdentifier()
            .createEnclosed("myTOName2"));
        var enclosingTransferObject3 = new CodegenGeneratedTOBuilder(generatedTypeBuilder.getIdentifier()
            .createEnclosed("myTOName3"));

        generatedTypeBuilder.addEnclosingTransferObject(enclosingTransferObject.build());
        generatedTypeBuilder.addEnclosingTransferObject(enclosingTransferObject2.build());
        generatedTypeBuilder.addEnclosingTransferObject(enclosingTransferObject3.build());
        var instance = generatedTypeBuilder.build();
        var enclosedTypes = instance.getEnclosedTypes();

        assertEquals(3, enclosedTypes.size());

        assertTrue(enclosedTypes.contains(enclosingTransferObject.build()));
        assertTrue(enclosedTypes.contains(enclosingTransferObject2.build()));
        assertTrue(enclosedTypes.contains(enclosingTransferObject3.build()));
        assertFalse(enclosedTypes.contains(new CodegenGeneratedTOBuilder(
            generatedTypeBuilder.getIdentifier().createEnclosed("myTOName4")).build()));
    }

    @Test
    void generatedTypeTest() {
        var generatedTypeBuilder = new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));

        generatedTypeBuilder.setDescription("My description ...");
        generatedTypeBuilder.setModuleName("myModuleName");
        generatedTypeBuilder.setReference("myReference");
        assertNotNull(generatedTypeBuilder.addComment(() -> "My comment.."));

        assertEquals("CodegenGeneratedTypeBuilder{identifier=my.package.MyName, comment=My comment.., constants=[], "
            + "enumerations=[], methods=[], annotations=[], implements=[]}", generatedTypeBuilder.toString());

        var instance = generatedTypeBuilder.build();

        assertEquals("My description ...", instance.getDescription());
        assertEquals("myModuleName", instance.getModuleName());
        assertEquals("myReference", instance.getReference());
        assertEquals("My comment..", instance.getComment().getJavadoc());
    }
}
