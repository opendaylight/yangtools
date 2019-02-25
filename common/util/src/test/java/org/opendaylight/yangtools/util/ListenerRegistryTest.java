/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableSet;
import java.util.EventListener;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

public class ListenerRegistryTest {

    private TestEventListener testEventListener;
    private ExtendedTestEventListener extendedTestEventListener;
    private ListenerRegistry<TestEventListener> registry;

    @Rule
    public ExpectedException expException = ExpectedException.none();

    @Before
    public void init() {
        testEventListener = new TestEventListener() {};
        extendedTestEventListener = new ExtendedTestEventListener() {};
        registry = ListenerRegistry.create();
    }

    @Test
    public void testCreateNewInstance() {
        assertNotNull("Intance of listener registry should not be null.", registry);
    }

    @Test
    public void tetGetListenersMethod() {
        assertEquals("Listener registry should have any listeners.", ImmutableSet.of(), registry.getRegistrations());
    }

    @Test
    public void testRegisterMethod() {

        final ListenerRegistration<TestEventListener> listenerRegistration = registry.register(testEventListener);
        assertEquals("Listeners should be the same.", testEventListener, listenerRegistration.getInstance());

        expException.expect(IllegalArgumentException.class);
        expException.expectMessage("Listener should not be null.");
        registry.register(null);
    }

    @Test
    public void testRegisterWithType() {
        final ListenerRegistration<ExtendedTestEventListener> listenerRegistration = registry.register(
            extendedTestEventListener);
        assertEquals("Listeners should be the same.", extendedTestEventListener, listenerRegistration.getInstance());
    }

    interface TestEventListener extends EventListener {

    }

    interface ExtendedTestEventListener extends TestEventListener {

    }
}
