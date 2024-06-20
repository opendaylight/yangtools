/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;

public class BaseYangTypesTest {
    @Test
    public void test() {
        Type stringType = BaseYangTypes.javaTypeForYangType("string");
        assertEquals("java.lang", stringType.getPackageName());
        assertEquals("String", stringType.getName());
        assertTrue(stringType instanceof ConcreteType);
        ParameterizedType stringBooleanMap = Types.mapTypeFor(
            BaseYangTypes.javaTypeForYangType("string"),
            BaseYangTypes.javaTypeForYangType("boolean"));
        assertTrue(!(stringBooleanMap instanceof ConcreteType));
        assertEquals("java.util", stringBooleanMap.getPackageName());
        assertEquals("Map", stringBooleanMap.getName());
        assertEquals(2, stringBooleanMap.getActualTypeArguments().length);
    }
}
