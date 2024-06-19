/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.test.mock.Node;
import org.opendaylight.yangtools.yang.binding.test.mock.NodeKey;

public class KeyedInstanceIdentifierTest {
    @Test
    public void basicTest() {
        final var key = new NodeKey(0);
        final var keyed = new KeyedInstanceIdentifier<>(new KeyStep<>(Node.class, key), ImmutableList.of(),
            false, 0);

        assertEquals(key, keyed.getKey());
        assertTrue(keyed.keyEquals(keyed.builder().build()));

        final var keyless = new InstanceIdentifier<>(Node.class, ImmutableList.of(), true, 0);
        assertTrue(keyless.keyEquals(keyed.builder().build()));
    }
}