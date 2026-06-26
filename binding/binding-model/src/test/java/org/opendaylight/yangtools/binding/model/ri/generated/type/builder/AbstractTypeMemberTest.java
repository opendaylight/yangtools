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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@ExtendWith(MockitoExtension.class)
class AbstractTypeMemberTest {
    @Mock
    private EffectiveStatement<?, ?> statement;

    @Test
    void testMethodsForAbstractTypeMemberBuilder() {
        final var methodSignatureBuilderImpl = new MethodSignatureBuilderImpl("TestProperty")
            .setReturnType(Types.STRING);
        final var typeBuilderImpl = new CodegenGeneratedTypeBuilder<>(
            JavaTypeName.create("org.opendaylight.yangtools.test", "TestType"), statement);
        final var typeBuilderImpl2 = new CodegenGeneratedTypeBuilder<>(
            JavaTypeName.create("org.opendaylight.yangtools.test", "TestType2"), statement);
        methodSignatureBuilderImpl.setComment(TypeMemberComment.contractOf("test comment"));
        methodSignatureBuilderImpl.setFinal(true);
        methodSignatureBuilderImpl.setStatic(true);

        final var genProperty = methodSignatureBuilderImpl.build();
        final var genProperty2 = methodSignatureBuilderImpl.build();
        assertEquals(TypeMemberComment.contractOf("test comment"), genProperty.getComment());
        assertTrue(genProperty.isFinal());
        assertTrue(genProperty.isStatic());
        assertEquals(genProperty.hashCode(), genProperty2.hashCode());
        assertEquals("""
            MethodSignatureImpl [name=TestProperty, comment=TypeMemberComment{contract=test comment}, \
            returnType=ConcreteType{name=java.lang.String}, params=[], annotations=[]]""", genProperty.toString());
        assertNotNull(genProperty.toString());
        assertTrue(genProperty.equals(genProperty2));
        assertFalse(genProperty.equals(null));
    }
}
