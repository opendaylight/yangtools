/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.Parameter;
import org.opendaylight.yangtools.binding.model.ri.Types;

class ParameterTest {
    @Test
    void rejectNulls() {
        assertThrows(NullPointerException.class, () -> new Parameter(null, Types.STRING));
        assertThrows(NullPointerException.class, () -> new Parameter("name", null));
    }

    @Test
    void testToString() {
        assertEquals("Parameter[name=customParameter, type=ConcreteTypeImpl{name=java.lang.String}]",
            new Parameter("customParameter", Types.STRING).toString());
    }
}
