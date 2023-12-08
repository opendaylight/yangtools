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
import java.util.Collection;
import java.util.EventListener;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A registry of EventListeners, maintaining a set of registrations. This class is thread-safe.
 *
 * @param <T> Type of listeners this registry handles
 * @deprecated Use {@link ObjectRegistry} instead
 */
@Deprecated(since = "12.0.0", forRemoval = true)
public final class ListenerRegistry<T extends EventListener> implements Mutable {
    private final Set<RegistrationImpl<? extends T>> listeners = ConcurrentHashMap.newKeySet();
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

    public void clear() {
        listeners.stream().forEach(RegistrationImpl::close);
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    public Stream<? extends T> streamListeners() {
        return listeners.stream().filter(RegistrationImpl::notClosed).map(RegistrationImpl::getInstance);
    }

    public <L extends T> @NonNull ListenerRegistration<L> register(final L listener) {
        final RegistrationImpl<L> ret = new RegistrationImpl<>(listener, listeners);
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

    private static final class RegistrationImpl<T extends EventListener> extends AbstractListenerRegistration<T> {
        private Collection<?> removeFrom;

        RegistrationImpl(final T instance, final Collection<?> removeFrom) {
            super(instance);
            this.removeFrom = requireNonNull(removeFrom);
        }

        @Override
        protected void removeRegistration() {
            removeFrom.remove(this);
            // Do not retain reference to that state
            removeFrom = null;
        }
    }
}
