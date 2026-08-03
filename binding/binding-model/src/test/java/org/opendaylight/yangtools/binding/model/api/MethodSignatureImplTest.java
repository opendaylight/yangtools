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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;

class MethodSignatureImplTest {
    private MethodSignatureImpl signature1;
    private MethodSignatureImpl signature2;
    private MethodSignatureImpl signature3;
    private MethodSignatureImpl signature4;
    private int hash1;
    private int hash4;

    @BeforeEach
    void setup() {
        var name = "customMethod";
        final var comment = TypeMemberComment.contractOf("This is just a comment");

        signature1 = new MethodSignatureImpl(name, List.of(), comment, BaseYangTypes.STRING_TYPE, false,
            ValueMechanics.NORMAL);
        signature2 = new MethodSignatureImpl(name, List.of(), comment, BaseYangTypes.STRING_TYPE, false,
            ValueMechanics.NORMAL);
        signature3 = new MethodSignatureImpl(name, List.of(), comment, BaseYangTypes.BOOLEAN_TYPE, false,
            ValueMechanics.NORMAL);
        signature4 = new MethodSignatureImpl("otherMethod", List.of(), comment, BaseYangTypes.BOOLEAN_TYPE, false,
            ValueMechanics.NORMAL);

        hash1 = signature1.hashCode();
        hash4 = signature4.hashCode();
    }

    @Test
    void testToString() {
        String toString = signature1.toString();
        assertTrue(toString.contains("MethodSignatureImpl"));
    }

    @Test
    void testHashCode() {
        assertEquals(hash1, hash1);
        assertNotEquals(hash1, hash4);
    }

    @Test
    void testEquals() {
        assertTrue(signature1.equals(signature1));
        assertTrue(signature1.equals(signature2));
        assertFalse(signature1.equals(signature3));
        assertFalse(signature3.equals(signature1));
        assertFalse(signature1.equals(null));
        assertFalse(signature1.equals(signature4));
        assertFalse(signature4.equals(signature1));
        assertFalse(signature1.equals(BaseYangTypes.STRING_TYPE));
    }
}
