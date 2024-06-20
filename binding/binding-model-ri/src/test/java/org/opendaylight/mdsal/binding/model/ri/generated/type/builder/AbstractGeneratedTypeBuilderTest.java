/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import java.io.Serializable;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

public class AbstractGeneratedTypeBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void addPropertyIllegalArgumentTest() {
        CodegenGeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        generatedTypeBuilder.addProperty(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPropertyIllegalArgumentTest2() {
        CodegenGeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        generatedTypeBuilder.addProperty("myName");
        generatedTypeBuilder.addProperty("myName");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addEnclosingTransferObjectArgumentTest() {
        CodegenGeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        generatedTypeBuilder.addEnclosingTransferObject(new CodegenGeneratedTOBuilder(
            JavaTypeName.create("my.package", "myName")).build());
        generatedTypeBuilder.addEnclosingTransferObject(new CodegenGeneratedTOBuilder(
            JavaTypeName.create("my.package", "myName")).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addImplementsTypeIllegalArgumentTest() {
        CodegenGeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        generatedTypeBuilder.addImplementsType(Types.typeForClass(Serializable.class));
        generatedTypeBuilder.addImplementsType(Types.typeForClass(Serializable.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantIllegalArgumentTest() {
        CodegenGeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        generatedTypeBuilder.addConstant(Types.STRING, "myName", "Value");
        generatedTypeBuilder.addConstant(Types.BOOLEAN, "myName", true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAnnotationIllegalArgumentTest() {
        CodegenGeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        generatedTypeBuilder.addAnnotation("my.package", "myName");
        generatedTypeBuilder.addAnnotation("my.package", "myName");
    }
}
