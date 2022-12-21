/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class TypedefSubStmtsTest extends AbstractYangTest {
    @Test
    void typedefSubStmtsTest() {
        final var result = assertEffectiveModel("/typedef-substmts-test/typedef-substmts-test.yang");

        final var typedefs = result.getTypeDefinitions();
        assertEquals(1, typedefs.size());

        final var typedef = typedefs.iterator().next();
        assertEquals("time-of-the-day", typedef.getQName().getLocalName());
        assertEquals("string", typedef.getBaseType().getQName().getLocalName());
        assertEquals(Optional.of("24-hour-clock"), typedef.getUnits());
        assertEquals("1am", typedef.getDefaultValue().map(Object::toString).orElseThrow());
    }
}
