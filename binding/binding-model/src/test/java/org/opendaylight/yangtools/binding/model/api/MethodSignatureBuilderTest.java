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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

@ExtendWith(MockitoExtension.class)
class MethodSignatureBuilderTest {
    @Mock
    private ContainerEffectiveStatement statement;

    @Test
    void testSetAbstractMethod() {
        final var methodSignature =
            MethodSignature.builder("testMethod", BaseYangTypes.STRING_TYPE, ValueMechanics.NORMAL).build();
        assertFalse(methodSignature.isDefault());
    }

    @Test
    void testMethodsForAbstractTypeMemberBuilder() {
        final var builder = MethodSignature.builder("TestProperty", BaseYangTypes.STRING_TYPE, ValueMechanics.NORMAL)
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
