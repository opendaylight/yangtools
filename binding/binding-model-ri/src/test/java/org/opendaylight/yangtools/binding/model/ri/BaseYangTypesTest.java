/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class BaseYangTypesTest {
    @Test
    void test() {
        final var stringType = assertInstanceOf(ConcreteType.class, BaseYangTypes.javaTypeForYangType("string"));
        assertEquals("java.lang", stringType.getPackageName());
        assertEquals("String", stringType.getName());
        final var stringBooleanMap = Types.mapTypeFor(
            BaseYangTypes.javaTypeForYangType("string"),
            BaseYangTypes.javaTypeForYangType("boolean"));

        assertEquals("java.util", stringBooleanMap.getPackageName());
        assertEquals("Map", stringBooleanMap.getName());
        assertEquals(2, stringBooleanMap.getActualTypeArguments().length);

        assertEquals(Types.typeForClass(InstanceIdentifier.class), BaseYangTypes.INSTANCE_IDENTIFIER);
    }
}
