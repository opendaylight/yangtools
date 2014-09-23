/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MethodParameterImplTest {

    @Test
    public void testMethodsForMethodParameterImpl() {
        final GeneratedTypeBuilderImpl genTypeBuilder = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test", "TestType");
        final MethodParameterImpl methodParam = new MethodParameterImpl("length", genTypeBuilder);
        final MethodParameterImpl methodParam2 = new MethodParameterImpl("height", genTypeBuilder);
        final MethodParameterImpl methodParam3 = methodParam;
        final MethodParameterImpl methodParam4 = new MethodParameterImpl(null, genTypeBuilder);

        assertEquals("length", methodParam.getName());
        assertEquals("TestType", methodParam.getType().getName());
        assertFalse(methodParam.hashCode() == methodParam2.hashCode());
        assertFalse(methodParam.equals(methodParam2));
        assertTrue(methodParam.equals(methodParam3));
        assertFalse(methodParam4.equals(methodParam));
        assertFalse(methodParam.equals("test"));
        assertFalse(methodParam.equals(null));
        assertNotNull(methodParam.toString());
    }
}
