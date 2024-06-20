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
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;

public class AbstractTypeMemberTest {

    @Test
    public void testMethodsForAbstractTypeMemberBuilder() {
        final MethodSignatureBuilderImpl methodSignatureBuilderImpl = new MethodSignatureBuilderImpl("TestProperty");
        final CodegenGeneratedTypeBuilder typeBuilderImpl = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "TestType"));
        final CodegenGeneratedTypeBuilder typeBuilderImpl2 = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "TestType2"));
        methodSignatureBuilderImpl.setComment(TypeMemberComment.contractOf("test comment"));
        methodSignatureBuilderImpl.setFinal(true);
        methodSignatureBuilderImpl.setStatic(true);

        final MethodSignature genProperty = methodSignatureBuilderImpl.toInstance(typeBuilderImpl);
        final MethodSignature genProperty2 = methodSignatureBuilderImpl.toInstance(typeBuilderImpl2);
        assertEquals(TypeMemberComment.contractOf("test comment"), genProperty.getComment());
        assertTrue(genProperty.isFinal());
        assertTrue(genProperty.isStatic());
        assertEquals(genProperty.hashCode(), genProperty2.hashCode());
        assertEquals("MethodSignatureImpl [name=TestProperty, comment=TypeMemberComment{contract=test comment}, "
            + "returnType=null, params=[], annotations=[]]", genProperty.toString());
        assertNotNull(genProperty.toString());
        assertTrue(genProperty.equals(genProperty2));
        assertFalse(genProperty.equals(null));
    }
}
