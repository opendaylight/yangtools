/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.binding.test.mock.Node;
import org.opendaylight.yangtools.binding.test.mock.NodeKey;

public class KeyedInstanceIdentifierTest {
    @Test
    public void basicTest() {
        final var key = new NodeKey(0);
        final var keyStep = new KeyStep<>(Node.class, key);
        final var keyed = new KeyedInstanceIdentifier<>(List.of(keyStep), keyStep);

        assertEquals(key, keyed.getKey());
        assertTrue(keyed.keyEquals(keyed.toBuilder().build()));

        final var keyless = InstanceIdentifier.unsafeOf(ImmutableList.of(new KeylessStep<>(Node.class)));
        assertTrue(keyless.keyEquals(keyed.toBuilder().build()));
    }
}