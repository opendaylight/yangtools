/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Utility {@link ClassToInstanceMap} implementation for implementing {@link ExtensibleObject#getExtensions()} method
 * by objects which are themselves implementing the extension.
 *
 * @param <O> Type of extensible object
 * @param <E> Extension marker interface
 */
public final class ObjectExtensions<O extends ExtensibleObject<O, E>, E extends ObjectExtension<O, E>>
        extends AbstractMap<Class<? extends E>, E> implements ClassToInstanceMap<E> {
    private final class EntrySet extends AbstractSet<Entry<Class<? extends E>, E>> {
        @Override
        public Iterator<Entry<Class<? extends E>, E>> iterator() {
            return Iterators.transform(extensions.iterator(), ext -> Map.entry(ext, ext.cast(object)));
        }

        @Override
        public int size() {
            return extensions.size();
        }
    }

    private static final class Values<E> extends AbstractList<E> {
        private final @NonNull E instance;
        private final int size;

        @SuppressWarnings("unchecked")
        Values(final @NonNull Object instance, final int size) {
            this.instance = (E) instance;
            this.size = size;
        }

        @Override
        public E get(final int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
            return instance;
        }

        @Override
        public int size() {
            return size;
        }
    }

    public static final class Factory<T, O extends ExtensibleObject<O, E>, E extends ObjectExtension<O, E>> {
        private final @NonNull ImmutableSet<Class<? extends E>> extensions;

        Factory(final ImmutableSet<Class<? extends E>> extensions) {
            this.extensions = requireNonNull(extensions);
        }

        public @NonNull ClassToInstanceMap<E> newInstance(final T object) {
            return new ObjectExtensions<>(extensions, object);
        }
    }

    private final @NonNull ImmutableSet<Class<? extends E>> extensions;
    private final @NonNull Object object;

    ObjectExtensions(final ImmutableSet<Class<? extends E>> extensions, final Object object) {
        this.extensions = requireNonNull(extensions);
        this.object = requireNonNull(object);
    }

    @SafeVarargs
    public static <T, O extends ExtensibleObject<O, E>, E extends ObjectExtension<O, E>>
            @NonNull Factory<T, O, E> factory(final Class<T> objClass, final Class<? extends E>... extensions) {
        final var set = ImmutableSet.copyOf(extensions);
        for (var extension : set) {
            checkArgument(extension.isAssignableFrom(objClass), "%s is not a valid extension %s", objClass, extension);
        }
        return new Factory<>(set);
    }

    @Override
    public int size() {
        return extensions.size();
    }

    @Override
    public boolean isEmpty() {
        return extensions.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return extensions.contains(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return object.equals(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(final Object key) {
        return containsKey(key) ? ((Class<? extends E>) key).cast(object) : null;
    }

    @Override
    public E put(final Class<? extends E> key, final E value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void putAll(final Map<? extends Class<? extends E>, ? extends E> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Class<? extends E>> keySet() {
        return extensions;
    }

    @Override
    public Collection<E> values() {
        return new Values<>(object, extensions.size());
    }

    @Override
    public Set<Entry<Class<? extends E>, E>> entrySet() {
        return new EntrySet();
    }

    @Override
    public <T extends E> T getInstance(final Class<T> type) {
        return extensions.contains(requireNonNull(type)) ? type.cast(object) : null;
    }

    @Override
    public <T extends E> T putInstance(final Class<T> type, final T value) {
        throw new UnsupportedOperationException();
    }
}
