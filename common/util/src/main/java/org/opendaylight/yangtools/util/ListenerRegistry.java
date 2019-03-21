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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A registry of EventListeners, maintaining a set of registrations. This class is thread-safe.
 *
 * @param <T> Type of listeners this registry handles
 */
public final class ListenerRegistry<T extends EventListener> implements Mutable {

    private final Set<ListenerRegistration<? extends T>> listeners = ConcurrentHashMap.newKeySet();
    // This conversion is known to be safe.
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final Set<ListenerRegistration<T>> unmodifiableView = (Set) Collections.unmodifiableSet(listeners);

    private final String name;

    private ListenerRegistry(final String name) {
        this.name = name;
    }

    public static <T extends EventListener> @NonNull ListenerRegistry<T> create() {
        return new ListenerRegistry<>(null);
    }

    public static <T extends EventListener> @NonNull ListenerRegistry<T> create(final @NonNull String name) {
        return new ListenerRegistry<>(requireNonNull(name));
    }

    public @NonNull Set<? extends ListenerRegistration<? extends T>> getRegistrations() {
        return unmodifiableView;
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    public Stream<? extends T> streamListeners() {
        return listeners.stream().map(ListenerRegistration::getInstance);
    }

    public <L extends T> @NonNull  ListenerRegistration<L> register(final L listener) {
        final ListenerRegistration<L> ret = new ListenerRegistrationImpl<>(listener, listeners::remove);
        listeners.add(ret);
        return ret;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
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
            // Do not retain reference to that state
            removeCall = null;
        }
    }
}
