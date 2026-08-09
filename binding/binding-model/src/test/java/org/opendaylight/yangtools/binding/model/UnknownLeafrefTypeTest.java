/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.impl.ConcreteTypeImpl;

class UnknownLeafrefTypeTest {
    @Test
    void reportedNameIsObject() {
        assertEquals(TypeName.of("java.lang", "Object"), UnknownLeafrefType.INSTANCE.name());
    }

    @Test
    void toStringDoesNotIsJustClassName() {
        assertEquals("UnknownLeafrefType", UnknownLeafrefType.INSTANCE.toString());
    }

    @Test
    void hashCodeFollowsType() {
        final var hc = UnknownLeafrefType.INSTANCE.hashCode();
        assertEquals(-1623439100, hc);
        assertEquals(new ConcreteTypeImpl(Object.class).hashCode(), hc);
    }
}
