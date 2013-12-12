/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts.util;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.EventListener;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

public class ListenerRegistry<T extends EventListener> implements Iterable<ListenerRegistration<T>> {

    private final ConcurrentHashMap<ListenerRegistration<? extends T>,ListenerRegistration<? extends T>> listeners;
    final Set<ListenerRegistration<T>> unmodifiableView;
    private T invoker;

    @SuppressWarnings("unchecked")
    public ListenerRegistry() {
        listeners = new ConcurrentHashMap<>();
        // This conversion is known to be safe.
        @SuppressWarnings("rawtypes")
        final Set rawSet = Collections.unmodifiableSet(listeners.keySet());
        unmodifiableView = rawSet;
    }

    public Iterable<ListenerRegistration<T>> getListeners() {
        return unmodifiableView;
    }

    public <F extends T> ListenerRegistration<F> register(F listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null.");
        }
        ListenerRegistrationImpl<F> ret = new ListenerRegistrationImpl<F>(listener);
        listeners.put(ret,ret);
        return ret;
    }
    
    @Override
    public java.util.Iterator<ListenerRegistration<T>> iterator() {
        return unmodifiableView.iterator();
    }

    @SuppressWarnings("rawtypes")
    private void remove(ListenerRegistrationImpl registration) {
        listeners.remove(registration);
    }

    private class ListenerRegistrationImpl<P extends EventListener> //
            extends AbstractObjectRegistration<P> //
            implements ListenerRegistration<P> {

        public ListenerRegistrationImpl(P instance) {
            super(instance);
        }

        @Override
        protected void removeRegistration() {
            ListenerRegistry.this.remove(this);
        }
    }
}
