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
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;

@ExtendWith(MockitoExtension.class)
class TypeUtilsTest {
    @Mock
    private GeneratedTransferObject rootType;
    @Mock
    private GeneratedTransferObject innerType;
    @Mock
    private GeneratedProperty property;

    @Test
    void getBaseYangTypeTest() {
        final var type = ConcreteType.ofClass(Object.class);
        assertSame(type, TypeUtils.getBaseYangType(type));

        doReturn("value").when(property).getName();
        doReturn(type).when(property).getReturnType();
        doReturn(rootType).when(innerType).getSuperType();
        doReturn(List.of(property)).when(rootType).getProperties();
        assertEquals(type, TypeUtils.getBaseYangType(innerType));
    }

    @Test
    void getBaseYangTypeWithExceptionTest() {
        doReturn("test").when(property).getName();
        doReturn(rootType).when(innerType).getSuperType();
        doReturn(List.of(property)).when(rootType).getProperties();
        final var ex = assertThrows(IllegalArgumentException.class, () -> TypeUtils.getBaseYangType(innerType));
        assertEquals("Type innerType root rootType properties [property] do not include \"value\"", ex.getMessage());
    }
}
