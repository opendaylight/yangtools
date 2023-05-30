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
import org.opendaylight.yangtools.binding.model.ri.Types;

class MethodSignatureBuilderImplTest {
    @Test
    void testCreateNewInstance() {
        final var signatureBuilderImpl = new MethodSignatureBuilderImpl("testMethod");
        assertEquals("testMethod", signatureBuilderImpl.getName());
    }

    @Test
    void testSetAbstractMethod() {
        final var signatureBuilderImpl = new MethodSignatureBuilderImpl("testMethod").setReturnType(Types.VOID);
        signatureBuilderImpl.setAbstract(true);
        final var methodSignature = signatureBuilderImpl.toInstance(null);
        assertTrue(methodSignature.isAbstract());
    }

    @Test
    void testAddParameterMethod() {
        final var signatureBuilderImpl = new MethodSignatureBuilderImpl("testMethod").setReturnType(Types.VOID);
        final var ipAddressType = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "IpAddress"));
        signatureBuilderImpl.addParameter(ipAddressType, "ipAddress");
        final var methodSignature = signatureBuilderImpl.toInstance(null);
        assertEquals("ipAddress", methodSignature.getParameters().get(0).getName());
    }

    @Test
    void testHashCodeEqualsToStringMethods() {
        final var signatureBuilderImpl = new MethodSignatureBuilderImpl("testMethod");
        final var signatureBuilderImpl2 = new MethodSignatureBuilderImpl("testMethod");
        final var signatureBuilderImpl3 = new MethodSignatureBuilderImpl("testMethod2");
        final var signatureBuilderImpl4 = new MethodSignatureBuilderImpl(null);
        final var signatureBuilderImpl5 = signatureBuilderImpl;
        final var signatureBuilderImpl6 = new MethodSignatureBuilderImpl("testMethod");
        final var returnType = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Address"));
        signatureBuilderImpl6.setReturnType(returnType);

        assertEquals(signatureBuilderImpl.hashCode(), signatureBuilderImpl2.hashCode());

        assertTrue(signatureBuilderImpl.equals(signatureBuilderImpl2));
        assertFalse(signatureBuilderImpl.equals(signatureBuilderImpl3));
        assertFalse(signatureBuilderImpl.equals(signatureBuilderImpl4));
        assertFalse(signatureBuilderImpl4.equals(signatureBuilderImpl));
        assertTrue(signatureBuilderImpl.equals(signatureBuilderImpl5));
        assertFalse(signatureBuilderImpl4.equals("test"));
        assertFalse(signatureBuilderImpl4.equals(signatureBuilderImpl));
        assertFalse(signatureBuilderImpl6.equals(signatureBuilderImpl));
        assertFalse(signatureBuilderImpl.equals(signatureBuilderImpl6));

        assertNotNull(signatureBuilderImpl.toString());
    }
}
