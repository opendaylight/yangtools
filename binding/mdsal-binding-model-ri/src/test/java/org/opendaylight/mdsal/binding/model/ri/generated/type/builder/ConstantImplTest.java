/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;

public class ConstantImplTest {
    @Test
    public void testMethodsOfConstantImpl() {
        final CodegenGeneratedTypeBuilder type = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test.v1", "BaseType"));
        final ConstantImpl constImpl = new ConstantImpl(type, "IpAddress", "127.0.0.1");
        final ConstantImpl constImpl2 = new ConstantImpl(type, "IpAddress", "127.0.0.1");
        final ConstantImpl constImpl3 = new ConstantImpl(type, "IpAddress", "127.0.0.0");
        final ConstantImpl constImpl4 = constImpl;
        final ConstantImpl constImpl5 = new ConstantImpl(type, null, "127.0.0.0");
        final ConstantImpl constImpl6 = new ConstantImpl(type, "IpAddress", null);

        assertEquals("BaseType", constImpl.getType().getName());
        assertEquals("IpAddress", constImpl.getName());
        assertEquals("127.0.0.1", constImpl.getValue());
        assertEquals(
            "Constant [type=CodegenGeneratedTypeBuilder{identifier=org.opendaylight.yangtools.test.v1.BaseType, "
                + "constants=[], enumerations=[], methods=[], annotations=[], implements=[]}, name=IpAddress, "
                + "value=127.0.0.1]", constImpl.toString());
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
