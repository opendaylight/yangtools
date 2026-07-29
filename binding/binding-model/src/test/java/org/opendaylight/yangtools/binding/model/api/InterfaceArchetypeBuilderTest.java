/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

@ExtendWith(MockitoExtension.class)
class InterfaceArchetypeBuilderTest {
    @Mock
    private ContainerEffectiveStatement statement;

    @Test
    void addMethodTest() {
        var generatedTypeBuilder = ContainerArchetype.builder(JavaTypeName.create("my.package", "MyName"), statement);

        var methodBuilder = generatedTypeBuilder.addMethod("myMethodName").setReturnType(Types.BOOLEAN);
        assertNotNull(methodBuilder);
        var methodBuilder2 = generatedTypeBuilder.addMethod("myMethodName2").setReturnType(Types.STRING);
        assertNotNull(methodBuilder2);

        var instance = generatedTypeBuilder.build();
        var methodDefinitions = instance.getMethodDefinitions();

        assertEquals(2, methodDefinitions.size());

        assertTrue(methodDefinitions.contains(methodBuilder.build()));
        assertTrue(methodDefinitions.contains(methodBuilder2.build()));
        assertFalse(methodDefinitions.contains(MethodSignature.builder("myMethodName3")
            .setReturnType(Types.BOOLEAN)
            .build()));
    }

    @Test
    void addImplementsTypeIllegalArgumentTest() {
        final var builder = ContainerArchetype.builder(JavaTypeName.create("my.package", "MyName"), statement);
        assertThrows(NullPointerException.class, () -> builder.addImplementsType((Type) null));
    }

    @Test
    void addImplementsTypeTest() {
        var generatedTypeBuilder = ContainerArchetype.builder(JavaTypeName.create("my.package", "MyName"), statement);

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
        final var builder = ContainerArchetype.builder(JavaTypeName.create("my.package", "MyName"), statement);
        assertThrows(NullPointerException.class, () -> builder.addEnclosedType(null));
    }
}
