/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.impl.ConcreteTypeImpl;

class ConcreteTypeTest {
    @Test
    void testPrimitiveType() {
        final var primitiveType = new ConcreteTypeImpl(String[].class);
        assertEquals("String[]", primitiveType.name().toString());
    }
}
