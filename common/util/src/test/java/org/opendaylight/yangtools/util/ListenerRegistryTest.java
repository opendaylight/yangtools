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
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import java.util.EventListener;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

public class ListenerRegistryTest {

    private TestEventListener testEventListener;
    private ExtendedTestEventListener extendedTestEventListener;
    private ListenerRegistry<TestEventListener> listenerRegistry;

    @Rule
    public ExpectedException expException = ExpectedException.none();

    @Before
    public void init() {
        testEventListener = new TestEventListener() {};
        extendedTestEventListener = new ExtendedTestEventListener() {};
        listenerRegistry = new ListenerRegistry<>();
    }

    @Test
    public void testCreateNewInstance() {
        assertNotNull("Intance of listener registry shouldn't be null.", listenerRegistry);
    }

    @Test
    public void tetGetListenersMethod() {
        assertTrue("Listener regisdtry should have any listeners.", Iterables.isEmpty(listenerRegistry.getListeners()));
    }

    @Test
    public void testRegisterMethod() {

        final ListenerRegistration<TestEventListener> listenerRegistration = listenerRegistry.register(testEventListener);
        assertEquals("Listeners should be the same.", testEventListener, listenerRegistration.getInstance());

        expException.expect(IllegalArgumentException.class);
        expException.expectMessage("Listener should not be null.");
        listenerRegistry.register(null);
    }

    @Test
    public void testRegisterWithType() {
        final ListenerRegistration<ExtendedTestEventListener> listenerRegistration = listenerRegistry.registerWithType(extendedTestEventListener);
        assertEquals("Listeners should be the same.", extendedTestEventListener, listenerRegistration.getInstance());
    }

    @Test
    public void testIteratorMethod() {
        final Iterator<ListenerRegistration<TestEventListener>> listenerIterator = listenerRegistry.iterator();
        assertNotNull("Listener iterator shouldn't be null.", listenerIterator);
    }

    @Test
    public void testCreateMethod() {
        final ListenerRegistry<EventListener> emptyListenerRegistry = ListenerRegistry.create();
        assertTrue("List of listeners in listener registry should be empty.", Iterables.isEmpty(emptyListenerRegistry.getListeners()));
    }

    interface TestEventListener extends EventListener {

    }

    interface ExtendedTestEventListener extends TestEventListener {

    }
}
