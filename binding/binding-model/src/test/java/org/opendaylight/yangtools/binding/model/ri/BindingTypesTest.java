/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BindingTypesTest {
    @Test
    void testChildOfNull() {
        assertThrows(NullPointerException.class, () -> BindingTypes.childOf(null));
    }

    @Test
    void testChildOf() {
        assertNotNull(BindingTypes.childOf(Types.objectType()));
    }
}
