/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConstantImplTest {

    @Test
    public void testMethodsOfConstantImpl() {
        final GeneratedTypeBuilderImpl definingType = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test",
                "DefiningType");
        final GeneratedTypeBuilderImpl type = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test.v1",
                "BaseType");
        final ConstantImpl constImpl = new ConstantImpl(definingType, type, "IpAddress", "127.0.0.1");
        final ConstantImpl constImpl2 = new ConstantImpl(definingType, type, "IpAddress", "127.0.0.1");
        final ConstantImpl constImpl3 = new ConstantImpl(definingType, type, "IpAddress", "127.0.0.0");
        final ConstantImpl constImpl4 = constImpl;
        final ConstantImpl constImpl5 = new ConstantImpl(definingType, type, null, "127.0.0.0");
        final ConstantImpl constImpl6 = new ConstantImpl(definingType, type, "IpAddress", null);

        assertNotNull(constImpl.getDefiningType());
        assertNotNull(constImpl.getType());
        assertNotNull(constImpl.getName());
        assertNotNull(constImpl.getValue());
        assertNotNull(constImpl.toFormattedString());
        assertNotNull(constImpl.toString());
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
