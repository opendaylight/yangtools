/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.impl.ConcreteTypeImpl;

class ScalarTypesTest {
    @Test
    void test() {
        final var stringType = assertInstanceOf(ConcreteType.class, ScalarTypes.STRING);
        assertEquals(JavaPackageName.of("java.lang"), stringType.packageName());
        assertEquals("String", stringType.simpleName());

        assertEquals(new ConcreteTypeImpl(BindingInstanceIdentifier.class), ScalarTypes.INSTANCE_IDENTIFIER);
    }
}
