/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ObjectRegistryTest {
    private final ObjectRegistry<TestEventListener> registry = ObjectRegistry.createSimple("testName");

    @Test
    void testCreateNewInstance() {
        assertNotNull(registry, "Intance of listener registry should not be null.");
    }

    @Test
    void testGetListenersMethod() {
        assertEquals(0, registry.streamObjects().count(), "Listener registry should not have any listeners.");
    }

    @Test
    void testRegisterMethod() {
        final var extendedTestEventListener = new ExtendedTestEventListener() {
            // Nothing else
        };
        final var listenerRegistration = registry.register(extendedTestEventListener);
        assertEquals(extendedTestEventListener, listenerRegistration.getInstance(), "Listeners should be the same.");
    }

    interface TestEventListener {
        // Nothing else
    }

    interface ExtendedTestEventListener extends TestEventListener {
        // Nothing else
    }
}
