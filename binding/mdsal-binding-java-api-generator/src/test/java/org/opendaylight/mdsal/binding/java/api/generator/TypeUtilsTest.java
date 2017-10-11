/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Constructor;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Type;

public class TypeUtilsTest {

    @Test
    public void getBaseYangTypeTest() throws Exception {
        final GeneratedTransferObject rootType = mock(GeneratedTransferObject.class);
        final GeneratedTransferObject innerType = mock(GeneratedTransferObject.class);
        final GeneratedProperty property = mock(GeneratedProperty.class);
        final Type type = mock(ConcreteType.class);
        assertEquals(type, TypeUtils.getBaseYangType(type));

        doReturn("value").when(property).getName();
        doReturn(type).when(property).getReturnType();
        doReturn(rootType).when(innerType).getSuperType();
        doReturn(ImmutableList.of(property)).when(rootType).getProperties();
        assertEquals(type, TypeUtils.getBaseYangType(innerType));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getBaseYangTypeWithExceptionTest() throws Exception {
        final GeneratedTransferObject rootType = mock(GeneratedTransferObject.class);
        final GeneratedTransferObject innerType = mock(GeneratedTransferObject.class);
        final GeneratedProperty property = mock(GeneratedProperty.class);

        doReturn("test").when(property).getName();
        doReturn(rootType).when(innerType).getSuperType();
        doReturn(ImmutableList.of(property)).when(rootType).getProperties();
        TypeUtils.getBaseYangType(innerType);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void constructTest() throws Throwable {
        final Constructor<TypeUtils> constructor = TypeUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (Exception e) {
            throw e.getCause();
        }
    }
}