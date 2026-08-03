/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

@ExtendWith(MockitoExtension.class)
class MethodSignatureBuilderTest {
    @Mock
    private ContainerEffectiveStatement statement;

    @Test
    void testSetAbstractMethod() {
        final var signatureBuilderImpl = MethodSignature.builder("testMethod").setReturnType(BaseYangTypes.STRING_TYPE);
        final var methodSignature = signatureBuilderImpl.build();
        assertFalse(methodSignature.isDefault());
    }

    @Test
    void testHashCodeEqualsToStringMethods() {
        final var signatureBuilderImpl = MethodSignature.builder("testMethod");
        final var signatureBuilderImpl2 = MethodSignature.builder("testMethod");
        final var signatureBuilderImpl3 = MethodSignature.builder("testMethod2");
        final var signatureBuilderImpl5 = signatureBuilderImpl;
        final var signatureBuilderImpl6 = MethodSignature.builder("testMethod");
        final var returnType = ContainerObjectArchetype.builder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Address"), statement,
            JavaTypeName.create("org.opendaylight.yangtools.test", "Parent"), List.of())
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

        assertEquals("MethodSignatureBuilder{name=testMethod, annotations=[]}", signatureBuilderImpl.toString());
    }

    @Test
    void testMethodsForAbstractTypeMemberBuilder() {
        final var builder = MethodSignature.builder("TestProperty")
            .setReturnType(BaseYangTypes.STRING_TYPE)
            .setComment(TypeMemberComment.contractOf("test comment"));

        final var genProperty = builder.build();
        final var genProperty2 = builder.build();
        assertEquals(TypeMemberComment.contractOf("test comment"), genProperty.getComment());
        assertEquals(genProperty.hashCode(), genProperty2.hashCode());
        assertEquals("""
            MethodSignatureImpl [name=TestProperty, comment=TypeMemberComment{contract=test comment}, \
            returnType=ConcreteType{name=java.lang.String}, annotations=[]]""", genProperty.toString());
        assertNotNull(genProperty.toString());
        assertTrue(genProperty.equals(genProperty2));
        assertFalse(genProperty.equals(null));
    }
}
