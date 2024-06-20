/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;

public class MethodSignatureBuilderImplTest {

    @Test
    public void testCreateNewInstance() {
        final MethodSignatureBuilderImpl signatureBuilderImpl = new MethodSignatureBuilderImpl("testMethod");
        assertNotNull(signatureBuilderImpl);
    }

    @Test
    public void testSetAbstractMethod() {
        final MethodSignatureBuilderImpl signatureBuilderImpl = new MethodSignatureBuilderImpl("testMethod");
        signatureBuilderImpl.setAbstract(true);
        final MethodSignature methodSignature = signatureBuilderImpl.toInstance(null);
        assertTrue(methodSignature.isAbstract());
    }

    @Test
    public void testAddParameterMethod() {
        final MethodSignatureBuilderImpl signatureBuilderImpl = new MethodSignatureBuilderImpl("testMethod");
        final CodegenGeneratedTypeBuilder ipAddressType = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "IpAddress"));
        signatureBuilderImpl.addParameter(ipAddressType, "ipAddress");
        final MethodSignature methodSignature = signatureBuilderImpl.toInstance(null);
        assertEquals("ipAddress", methodSignature.getParameters().get(0).getName());
    }

    @Test
    public void testHashCodeEqualsToStringMethods() {
        final MethodSignatureBuilderImpl signatureBuilderImpl = new MethodSignatureBuilderImpl("testMethod");
        final MethodSignatureBuilderImpl signatureBuilderImpl2 = new MethodSignatureBuilderImpl("testMethod");
        final MethodSignatureBuilderImpl signatureBuilderImpl3 = new MethodSignatureBuilderImpl("testMethod2");
        final MethodSignatureBuilderImpl signatureBuilderImpl4 = new MethodSignatureBuilderImpl(null);
        final MethodSignatureBuilderImpl signatureBuilderImpl5 = signatureBuilderImpl;
        final MethodSignatureBuilderImpl signatureBuilderImpl6 = new MethodSignatureBuilderImpl("testMethod");
        final CodegenGeneratedTypeBuilder returnType = new CodegenGeneratedTypeBuilder(
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
