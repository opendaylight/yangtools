/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import java.util.Collections;
import java.util.EventListener;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

public class ListenerRegistry<T extends EventListener> implements Iterable<ListenerRegistration<T>> {

    private final ConcurrentMap<ListenerRegistration<? extends T>,ListenerRegistration<? extends T>> listeners;
    final Set<ListenerRegistration<T>> unmodifiableView;

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

    public ListenerRegistration<T> register(final T listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null.");
        }
        final ListenerRegistrationImpl<T> ret = new ListenerRegistrationImpl<>(listener);
        listeners.put(ret,ret);
        return ret;
    }

    public <L extends T> ListenerRegistration<L> registerWithType(final L listener) {
        final ListenerRegistrationImpl<L> ret = new ListenerRegistrationImpl<>(listener);
        listeners.put(ret,ret);
        return ret;
    }

    @Override
    public java.util.Iterator<ListenerRegistration<T>> iterator() {
        return unmodifiableView.iterator();
    }

    @SuppressWarnings("rawtypes")
    private void remove(final ListenerRegistrationImpl registration) {
        listeners.remove(registration);
    }

    private class ListenerRegistrationImpl<P extends EventListener> extends AbstractObjectRegistration<P> implements
            ListenerRegistration<P> {

        ListenerRegistrationImpl(final P instance) {
            super(instance);
        }

        @Override
        protected void removeRegistration() {
            ListenerRegistry.this.remove(this);
        }
    }

    public static <T extends EventListener> ListenerRegistry<T> create() {
        return new ListenerRegistry<>();
    }
}
