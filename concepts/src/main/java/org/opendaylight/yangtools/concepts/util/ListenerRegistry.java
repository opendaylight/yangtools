package org.opendaylight.yangtools.concepts.util;

import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

public class ListenerRegistry<T extends EventListener> implements Iterable<ListenerRegistration<T>> {

    final Set<ListenerRegistration<T>> listeners;
    final Set<ListenerRegistration<T>> unmodifiableView;

    public ListenerRegistry() {
        listeners = new HashSet<>();
        unmodifiableView = Collections.unmodifiableSet(listeners);
    }

    public Iterable<ListenerRegistration<T>> getListeners() {
        return unmodifiableView;
    }

    public ListenerRegistration<T> register(T listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null.");
        }
        ListenerRegistrationImpl<T> ret = new ListenerRegistrationImpl<T>(listener);
        listeners.add(ret);
        return ret;
    }
    
    @Override
    public java.util.Iterator<ListenerRegistration<T>> iterator() {
        return listeners.iterator();
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
