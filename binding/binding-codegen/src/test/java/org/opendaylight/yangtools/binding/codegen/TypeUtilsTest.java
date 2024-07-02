/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.Type;

public class TypeUtilsTest {
    @Test
    public void getBaseYangTypeTest() {
        final GeneratedTransferObject rootType = mock(GeneratedTransferObject.class);
        final GeneratedTransferObject innerType = mock(GeneratedTransferObject.class);
        final GeneratedProperty property = mock(GeneratedProperty.class);
        final Type type = mock(ConcreteType.class);
        assertEquals(type, TypeUtils.getBaseYangType(type));

        doReturn("value").when(property).getName();
        doReturn(type).when(property).getReturnType();
        doReturn(rootType).when(innerType).getSuperType();
        doReturn(List.of(property)).when(rootType).getProperties();
        assertEquals(type, TypeUtils.getBaseYangType(innerType));
    }

    @Test
    public void getBaseYangTypeWithExceptionTest() {
        final GeneratedTransferObject rootType = mock(GeneratedTransferObject.class);
        final GeneratedTransferObject innerType = mock(GeneratedTransferObject.class);
        final GeneratedProperty property = mock(GeneratedProperty.class);

        doReturn("test").when(property).getName();
        doReturn(rootType).when(innerType).getSuperType();
        doReturn(List.of(property)).when(rootType).getProperties();
        assertThrows(IllegalArgumentException.class, () -> TypeUtils.getBaseYangType(innerType));
    }
}