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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@ExtendWith(MockitoExtension.class)
class AbstractGeneratedTypeBuilderTest {
    @Mock
    private EffectiveStatement<?, ?> statement;

    private CodegenGeneratedTypeBuilder<?> builder;

    @BeforeEach
    void beforeEach() {
        builder = new CodegenGeneratedTypeBuilder<>(JavaTypeName.create("my.package", "MyName"), statement);
    }

    @Test
    void addImplementsTypeIllegalArgumentTest() {
        builder.addImplementsType(Types.typeForClass(Serializable.class));
        final var conflict = Types.typeForClass(Serializable.class);
        assertThrows(IllegalArgumentException.class, () -> builder.addImplementsType(conflict));
    }

    @Test
    void addConstantIllegalArgumentTest() {
        builder.addConstant(Types.STRING, "myName", "Value");
        assertThrows(IllegalArgumentException.class, () -> builder.addConstant(Types.BOOLEAN, "myName", true));
    }

    @Test
    void addAnnotationIllegalArgumentTest() {
        builder.addAnnotation("my.package", "myName");
        assertThrows(IllegalArgumentException.class, () -> builder.addAnnotation("my.package", "myName"));
    }
}
