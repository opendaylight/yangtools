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
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;

public class AbstractTypeMemberTest {

    @Test
    public void testMethodsForAbstractTypeMemberBuilder() {
        final MethodSignatureBuilderImpl methodSignatureBuilderImpl = new MethodSignatureBuilderImpl("TestProperty");
        final GeneratedTypeBuilderImpl typeBuilderImpl = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test", "TestType");
        final GeneratedTypeBuilderImpl typeBuilderImpl2 = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test", "TestType2");
        methodSignatureBuilderImpl.setComment("test comment");
        methodSignatureBuilderImpl.setFinal(true);
        methodSignatureBuilderImpl.setStatic(true);

        final MethodSignature genProperty = methodSignatureBuilderImpl.toInstance(typeBuilderImpl);
        final MethodSignature genProperty2 = methodSignatureBuilderImpl.toInstance(typeBuilderImpl2);
        assertEquals("test comment", genProperty.getComment());
        assertTrue(genProperty.isFinal());
        assertTrue(genProperty.isStatic());
        assertEquals(genProperty.hashCode(), genProperty2.hashCode());
        assertTrue(genProperty.toString().contains("org.opendaylight.yangtools.test.TestType"));
        assertNotNull(genProperty.toString());
        assertTrue(genProperty.equals(genProperty2));
        assertFalse(genProperty.equals(null));

    }
}
