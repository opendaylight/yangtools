/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Serializable;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.Types;

class AbstractGeneratedTypeBuilderTest {
    private final CodegenGeneratedTypeBuilder generatedTypeBuilder =
        new CodegenGeneratedTypeBuilder(JavaTypeName.create("my.package", "MyName"));

    @Test
    void addPropertyIllegalArgumentTest() {
        assertThrows(IllegalArgumentException.class, () -> generatedTypeBuilder.addProperty(null));
    }

    @Test
    void addPropertyIllegalArgumentTest2() {
        generatedTypeBuilder.addProperty("myName");
        assertThrows(IllegalArgumentException.class, () -> generatedTypeBuilder.addProperty("myName"));
    }

    @Test
    void addEnclosingTransferObjectArgumentTest() {
        generatedTypeBuilder.addEnclosingTransferObject(
            new CodegenGeneratedTOBuilder(JavaTypeName.create("my.package", "myName")).build());
        final var conflict = new CodegenGeneratedTOBuilder(JavaTypeName.create("my.package", "myName")).build();
        assertThrows(IllegalArgumentException.class, () -> generatedTypeBuilder.addEnclosingTransferObject(conflict));
    }

    @Test
    void addImplementsTypeIllegalArgumentTest() {
        generatedTypeBuilder.addImplementsType(Types.typeForClass(Serializable.class));
        final var conflict = Types.typeForClass(Serializable.class);
        assertThrows(IllegalArgumentException.class, () -> generatedTypeBuilder.addImplementsType(conflict));
    }

    @Test
    void addConstantIllegalArgumentTest() {
        generatedTypeBuilder.addConstant(Types.STRING, "myName", "Value");
        assertThrows(IllegalArgumentException.class,
            () -> generatedTypeBuilder.addConstant(Types.BOOLEAN, "myName", true));
    }

    @Test
    void addAnnotationIllegalArgumentTest() {
        generatedTypeBuilder.addAnnotation("my.package", "myName");
        assertThrows(IllegalArgumentException.class, () -> generatedTypeBuilder.addAnnotation("my.package", "myName"));
    }
}
