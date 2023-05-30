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
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;
import org.opendaylight.yangtools.binding.model.ri.Types;

class AbstractTypeMemberTest {
    @Test
    void testMethodsForAbstractTypeMemberBuilder() {
        final var methodSignatureBuilderImpl = new MethodSignatureBuilderImpl("TestProperty")
            .setReturnType(Types.STRING);
        final var typeBuilderImpl = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "TestType"));
        final var typeBuilderImpl2 = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "TestType2"));
        methodSignatureBuilderImpl.setComment(TypeMemberComment.contractOf("test comment"));
        methodSignatureBuilderImpl.setFinal(true);
        methodSignatureBuilderImpl.setStatic(true);

        final var genProperty = methodSignatureBuilderImpl.toInstance(typeBuilderImpl);
        final var genProperty2 = methodSignatureBuilderImpl.toInstance(typeBuilderImpl2);
        assertEquals(TypeMemberComment.contractOf("test comment"), genProperty.getComment());
        assertTrue(genProperty.isFinal());
        assertTrue(genProperty.isStatic());
        assertEquals(genProperty.hashCode(), genProperty2.hashCode());
        assertEquals("MethodSignatureImpl [name=TestProperty, comment=TypeMemberComment{contract=test comment}, "
            + "returnType=ConcreteTypeImpl{identifier=java.lang.String}, params=[], annotations=[]]",
            genProperty.toString());
        assertNotNull(genProperty.toString());
        assertTrue(genProperty.equals(genProperty2));
        assertFalse(genProperty.equals(null));
    }
}
