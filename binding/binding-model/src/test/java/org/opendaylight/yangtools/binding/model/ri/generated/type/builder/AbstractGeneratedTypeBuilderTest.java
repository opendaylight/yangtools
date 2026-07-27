/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Serializable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.model.api.FunctionalInterfaceAnnotation;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@ExtendWith(MockitoExtension.class)
class AbstractGeneratedTypeBuilderTest {
    @Mock
    private EffectiveStatement<?, ?> statement;

    private LegacyArchetype.Builder<?> builder;

    @BeforeEach
    void beforeEach() {
        builder = LegacyArchetype.builder(JavaTypeName.create("my.package", "MyName"), statement);
    }

    @Test
    void addImplementsTypeIllegalArgumentTest() {
        builder.addImplementsType(Types.typeForClass(Serializable.class));
        final var conflict = Types.typeForClass(Serializable.class);
        final var ex = assertThrows(IllegalArgumentException.class, () -> builder.addImplementsType(conflict));
        assertEquals("This generated type already contains equal implements type.", ex.getMessage());
    }

    @Test
    void addConstantIllegalArgumentTest() {
        builder.addConstant(Types.STRING, "myName", "Value");
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> builder.addConstant(Types.BOOLEAN, "myName", true));
        assertEquals("This generated type already contains a \"myName\" constant", ex.getMessage());
    }

    @Test
    void addAnnotationIllegalArgumentTest() {
        builder.addAnnotation(FunctionalInterfaceAnnotation.INSTANCE);
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> builder.addAnnotation(FunctionalInterfaceAnnotation.INSTANCE));
        assertEquals("Attempt to repeat FunctionalInterfaceAnnotation", ex.getMessage());
    }
}
