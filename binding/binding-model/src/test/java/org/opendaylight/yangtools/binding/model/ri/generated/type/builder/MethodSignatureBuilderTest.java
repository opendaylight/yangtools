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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.binding.model.ri.Types;

class MethodSignatureBuilderTest {
    private MethodSignatureBuilder builder1;
    private MethodSignatureBuilder builder2;
    private MethodSignatureBuilder builder3;
    private MethodSignatureBuilder builder4;
    private int hash1;
    private int hash2;
    private int hash3;

    @BeforeEach
    void setup() {
        builder1 = new MethodSignatureBuilderImpl("methodSignature");
        builder1.setReturnType(Types.VOID);
        builder2 = new MethodSignatureBuilderImpl("otherMethodSignature");
        builder2.setReturnType(Types.STRING);
        builder3 = new MethodSignatureBuilderImpl(null);
        builder3.setAbstract(false);
        builder4 = new MethodSignatureBuilderImpl("otherMethodSignature");
        builder4.setReturnType(Types.BOOLEAN);

        hash1 = builder1.hashCode();
        hash2 = builder2.hashCode();
        hash3 = builder3.hashCode();
    }

    @Test
    void testAddParameter() {
        builder1.addParameter(Types.STRING, "customParam");
        var signature = builder1.toInstance(Types.voidType());
        assertNotNull(signature);
    }

    @Test
    void testToString() {
        String toString = builder1.toString();
        assertTrue(toString.contains("MethodSignatureBuilderImpl"));
    }

    @Test
    void testHashCode() {
        assertEquals(hash1, hash1);
    }

    @Test
    void testEquals() {
        assertTrue(builder1.equals(builder1));
        assertFalse(builder1.equals(builder2));
        assertFalse(builder1.equals(null));
        assertFalse(builder1.equals("string"));
        assertFalse(builder3.equals(builder2));
        assertFalse(builder4.equals(builder2));
    }
}
