/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;

@ExtendWith(MockitoExtension.class)
class TypeUtilsTest {
    @Mock
    private TypeEffectiveStatement.MandatoryIn<?, ?> statement;
    @Mock
    private TypeDefinition<?> typeDefinition;
    @Mock
    private UnionTypeObjectArchetype union;

    @Test
    void getBaseYangTypeTest() {
        final var type = ConcreteType.ofClass(Object.class);
        assertSame(type, TypeUtils.getBaseYangType(type));

        assertEquals(type, TypeUtils.getBaseYangType(new ScalarTypeObjectArchetype(
            JavaTypeName.create(TypeUtilsTest.class), statement, typeDefinition, type, null, null)));
    }

    @Test
    void getBaseYangTypeWithExceptionTest() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> TypeUtils.getBaseYangType(union));
        assertEquals("Unsupported type union", ex.getMessage());
    }
}
