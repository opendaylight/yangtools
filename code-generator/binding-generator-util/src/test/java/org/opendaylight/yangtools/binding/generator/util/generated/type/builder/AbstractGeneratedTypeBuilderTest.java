/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import java.io.Serializable;

import org.junit.Test;
import org.opendaylight.yangtools.binding.generator.util.Types;

public class AbstractGeneratedTypeBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void addPropertyIllegalArgumentTest() {
        GeneratedTypeBuilderImpl generatedTypeBuilder = new GeneratedTypeBuilderImpl("my.package", "MyName");

        generatedTypeBuilder.addProperty(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPropertyIllegalArgumentTest2() {
        GeneratedTypeBuilderImpl generatedTypeBuilder = new GeneratedTypeBuilderImpl("my.package", "MyName");

        generatedTypeBuilder.addProperty("myName");
        generatedTypeBuilder.addProperty("myName");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addEnclosingTransferObjectArgumentTest() {
        GeneratedTypeBuilderImpl generatedTypeBuilder = new GeneratedTypeBuilderImpl("my.package", "MyName");

        generatedTypeBuilder.addEnclosingTransferObject(new GeneratedTOBuilderImpl("my.package", "myName"));
        generatedTypeBuilder.addEnclosingTransferObject(new GeneratedTOBuilderImpl("my.package", "myName"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addEnclosingTransferObjectArgumentTest2() {
        GeneratedTypeBuilderImpl generatedTypeBuilder = new GeneratedTypeBuilderImpl("my.package", "MyName");

        generatedTypeBuilder.addEnclosingTransferObject("myName");
        generatedTypeBuilder.addEnclosingTransferObject("myName");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addImplementsTypeIllegalArgumentTest() {
        GeneratedTypeBuilderImpl generatedTypeBuilder = new GeneratedTypeBuilderImpl("my.package", "MyName");

        generatedTypeBuilder.addImplementsType(Types.typeForClass(Serializable.class));
        generatedTypeBuilder.addImplementsType(Types.typeForClass(Serializable.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantIllegalArgumentTest() {
        GeneratedTypeBuilderImpl generatedTypeBuilder = new GeneratedTypeBuilderImpl("my.package", "MyName");

        generatedTypeBuilder.addConstant(Types.STRING, "myName", "Value");
        generatedTypeBuilder.addConstant(Types.BOOLEAN, "myName", true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAnnotationIllegalArgumentTest() {
        GeneratedTypeBuilderImpl generatedTypeBuilder = new GeneratedTypeBuilderImpl("my.package", "MyName");

        generatedTypeBuilder.addAnnotation("my.package", "myName");
        generatedTypeBuilder.addAnnotation("my.package", "myName");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addEnumerationIllegalArgumentTest() {
        GeneratedTypeBuilderImpl generatedTypeBuilder = new GeneratedTypeBuilderImpl("my.package", "MyName");

        generatedTypeBuilder.addEnumeration("myName");
        generatedTypeBuilder.addEnumeration("myName");
    }

}
