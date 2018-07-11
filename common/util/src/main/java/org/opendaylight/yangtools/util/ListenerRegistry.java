/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Collections;
import java.util.EventListener;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Mutable;

@ThreadSafe
//FIXME: 3.0.0: make final, do not implement Iterable
public class ListenerRegistry<T extends EventListener> implements Iterable<ListenerRegistration<T>>, Mutable {

    private final Set<ListenerRegistration<? extends T>> listeners = ConcurrentHashMap.newKeySet();
    // This conversion is known to be safe.
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final Set<ListenerRegistration<T>> unmodifiableView = (Set) Collections.unmodifiableSet(listeners);

    private final String name;

    private ListenerRegistry(final String name) {
        this.name = name;
    }

    /**
     * Default constructor.
     *
     * @deprecated This class will not be subclassable, use {@link #create()} instead.
     */
    @Deprecated
    public ListenerRegistry() {
        this(null);
    }

    public static <T extends EventListener> ListenerRegistry<T> create() {
        return new ListenerRegistry<>(null);
    }

    public static <T extends EventListener> ListenerRegistry<T> create(final @NonNull String name) {
        return new ListenerRegistry<>(requireNonNull(name));
    }

    // FIXME: 3.0.0: add getRegistrations returning Set<ListenerRegistration<? extends T>>
    // FIXME: 3.0.0: return Collection<? extends T>
    public Iterable<ListenerRegistration<T>> getListeners() {
        return unmodifiableView;
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    public Stream<? extends T> streamListeners() {
        return listeners.stream().map(ListenerRegistration::getInstance);
    }

    // FIXME: 3.0.0: change return type to what registerWithType does
    public ListenerRegistration<T> register(final T listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null.");
        }
        return registerWithType(listener);
    }

    // FIXME: 3.0.0: remove this method
    public <L extends T> ListenerRegistration<L> registerWithType(final L listener) {
        final ListenerRegistration<L> ret = new ListenerRegistrationImpl<>(listener, listeners::remove);
        listeners.add(ret);
        return ret;
    }

    @Override
    public Iterator<ListenerRegistration<T>> iterator() {
        return unmodifiableView.iterator();
    }

    @Override
    public String toString() {
        return name == null ? super.toString() : MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("size", listeners.size())
                .toString();
    }

    private static final class ListenerRegistrationImpl<T extends EventListener>
            extends AbstractListenerRegistration<T> {
        private Consumer<ListenerRegistration<? super T>> removeCall;

        ListenerRegistrationImpl(final T instance, final Consumer<ListenerRegistration<? super T>> removeCall) {
            super(instance);
            this.removeCall = requireNonNull(removeCall);
        }

        @Override
        protected void removeRegistration() {
            removeCall.accept(this);
            // Do not retail reference to that state
            removeCall = null;
        }
    }
}
