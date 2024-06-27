/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.KeyStep;
import org.opendaylight.yangtools.binding.KeylessStep;
import org.opendaylight.yangtools.binding.test.mock.Node;
import org.opendaylight.yangtools.binding.test.mock.NodeKey;

class KeyedInstanceIdentifierTest {
    @Test
    void basicTest() {
        final var key = new NodeKey(0);
        final var keyed = new KeyedInstanceIdentifier<>(List.of(new KeyStep<>(Node.class, key)), false);

        assertEquals(key, keyed.key());
        assertNotEquals(keyed, new InstanceIdentifier<>(List.of(new KeylessStep<>(Node.class)), true));
    }
}