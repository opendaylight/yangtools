/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api.type.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.LegacyArchetypeBuilder;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@ExtendWith(MockitoExtension.class)
class MethodSignatureBuilderTest {
    @Mock
    private EffectiveStatement<?, ?> statement;

    @Test
    void testSetAbstractMethod() {
        final var signatureBuilderImpl = new MethodSignatureBuilder("testMethod").setReturnType(Types.VOID);
        final var methodSignature = signatureBuilderImpl.build();
        assertFalse(methodSignature.isDefault());
    }

    @Test
    void testAddParameterMethod() {
        final var signatureBuilderImpl = new MethodSignatureBuilder("testMethod").setReturnType(Types.VOID);
        final var ipAddressType = new LegacyArchetypeBuilder<>(
            JavaTypeName.create("org.opendaylight.yangtools.test", "IpAddress"), statement)
            .build();
        signatureBuilderImpl.addParameter(ipAddressType, "ipAddress");
        final var methodSignature = signatureBuilderImpl.build();
        assertEquals("ipAddress", methodSignature.getParameters().getFirst().name());
    }

    @Test
    void testHashCodeEqualsToStringMethods() {
        final var signatureBuilderImpl = new MethodSignatureBuilder("testMethod");
        final var signatureBuilderImpl2 = new MethodSignatureBuilder("testMethod");
        final var signatureBuilderImpl3 = new MethodSignatureBuilder("testMethod2");
        final var signatureBuilderImpl5 = signatureBuilderImpl;
        final var signatureBuilderImpl6 = new MethodSignatureBuilder("testMethod");
        final var returnType = new LegacyArchetypeBuilder<>(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Address"), statement)
            .build();
        signatureBuilderImpl6.setReturnType(returnType);

        assertEquals(signatureBuilderImpl.hashCode(), signatureBuilderImpl2.hashCode());

        assertTrue(signatureBuilderImpl.equals(signatureBuilderImpl2));
        assertFalse(signatureBuilderImpl.equals(signatureBuilderImpl3));
        assertTrue(signatureBuilderImpl.equals(signatureBuilderImpl5));
        assertFalse(signatureBuilderImpl3.equals("test"));
        assertFalse(signatureBuilderImpl3.equals(signatureBuilderImpl));
        assertFalse(signatureBuilderImpl6.equals(signatureBuilderImpl));
        assertFalse(signatureBuilderImpl.equals(signatureBuilderImpl6));

        assertEquals("MethodSignatureBuilder{name=testMethod, parameters=[], annotations=[]}",
            signatureBuilderImpl.toString());
    }
}
