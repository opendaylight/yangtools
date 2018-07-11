/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

@Beta
@NonNullByDefault
public final class ObjectRegistry<T> {
    private final Set<ObjectRegistration<? extends T>> objects;
    private final Set<ObjectRegistration<? extends T>> unmodifiableView;
    private final String name;

    private ObjectRegistry(final String name, Set<ObjectRegistration<? extends T>> objects) {
        this.name = requireNonNull(name);
        this.objects = requireNonNull(objects);
        this.unmodifiableView = Collections.unmodifiableSet(objects);
    }

    public static <T> ObjectRegistry<T> createConcurrent(final String name) {
        return new ObjectRegistry<>(name, ConcurrentHashMap.newKeySet());
    }

    public static <T> ObjectRegistry<T> createSimple(final String name) {
        return new ObjectRegistry<>(name, new HashSet<>(1));
    }

    public boolean isEmpty() {
        return objects.isEmpty();
    }

    public Stream<? extends T> streamObjects() {
        return streamRegistrations().map(ObjectRegistration::getInstance);
    }

    public Set<ObjectRegistration<? extends T>> getRegistrations() {
        return unmodifiableView;
    }

    public Stream<ObjectRegistration<? extends T>> streamRegistrations() {
        return objects.stream();
    }

    public <O extends T> ObjectRegistration<O> register(final O object) {
        final ObjectRegistration<O> ret = new Reg<>(object, objects::remove);
        objects.add(ret);
        return ret;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).add("objects", objects.size()).toString();
    }

    private static final class Reg<T> extends AbstractObjectRegistration<T> {
        private @Nullable Consumer<ObjectRegistration<? super T>> removeCall;

        Reg(final T instance, final Consumer<ObjectRegistration<? super T>> removeCall) {
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
