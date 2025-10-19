/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * An unmodifiable view over a {@link Map}. Unlike the view returned via {@link Collections#unmodifiableMap(Map)}, this
 * class checks its argument to ensure multiple encapsulation does not occur.
 *
 * <p>This class checks  the argument so it prevents multiple encapsulation. Subclasses of
 * {@link ImmutableMap} are also recognized and not encapsulated.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public final class UnmodifiableMap<K, V> implements Map<K, V>, Immutable {
    private final @NonNull Map<K, V> delegate;

    private UnmodifiableMap(final @NonNull Map<K, V> delegate) {
        this.delegate = requireNonNull(delegate);
    }

    /**
     * Create an unmodifiable view of the target {@link Map}. If the map is known to be unmodifiable, that same instance
     * is returned. Otherwise an unmodifiable view of the map will be returned.
     *
     * @param map Target map
     * @return An unmodifiable view of the map
     * @throws NullPointerException if {@code map} is null
     */
    @SuppressModernizer
    public static <K, V> @NonNull Map<K, V> of(final @NonNull Map<K, V> map) {
        return map instanceof Immutable || map instanceof ImmutableMap ||  Collections.EMPTY_MAP == map
            ? map : new UnmodifiableMap<>(map);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public V get(final Object key) {
        return delegate.get(key);
    }

    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(delegate.keySet());
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableCollection(delegate.values());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(delegate.entrySet());
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || obj instanceof Map && delegate.equals(obj);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void putAll(final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
