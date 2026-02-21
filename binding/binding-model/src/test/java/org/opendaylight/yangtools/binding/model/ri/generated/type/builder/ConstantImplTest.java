/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

class ConstantImplTest {
    @Test
    void testMethodsOfConstantImpl() {
        final var type = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test.v1", "BaseType"))
            .build();
        final var constImpl = new ConstantImpl(type, "IpAddress", "127.0.0.1");
        final var constImpl2 = new ConstantImpl(type, "IpAddress", "127.0.0.1");
        final var constImpl3 = new ConstantImpl(type, "IpAddress", "127.0.0.0");
        final var constImpl4 = constImpl;
        final var constImpl5 = new ConstantImpl(type, null, "127.0.0.0");
        final var constImpl6 = new ConstantImpl(type, "IpAddress", null);

        assertEquals("BaseType", constImpl.getType().getName());
        assertEquals("IpAddress", constImpl.getName());
        assertEquals("127.0.0.1", constImpl.getValue());
        assertEquals("""
            Constant [type=GeneratedTypeImpl{identifier=org.opendaylight.yangtools.test.v1.BaseType, \
            annotations=[], enclosedTypes=[], enumerations=[], constants=[], methodSignatures=[]}, \
            name=IpAddress, value=127.0.0.1]""", constImpl.toString());
        assertEquals(constImpl.hashCode(), constImpl2.hashCode());
        assertNotNull(constImpl.getType());
        assertNotNull(constImpl.getName());
        assertNotNull(constImpl.getValue());
        assertNotNull(constImpl.hashCode());
        assertFalse(constImpl.equals(null));
        assertFalse(constImpl.equals("test"));

        assertTrue(constImpl.equals(constImpl2));
        assertFalse(constImpl.equals(constImpl3));
        assertTrue(constImpl.equals(constImpl4));
        assertFalse(constImpl5.equals(constImpl));
        assertFalse(constImpl6.equals(constImpl));
    }
}
