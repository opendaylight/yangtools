/*
 * (C) Copyright 2016 Pantheon Technologies, s.r.o. and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opendaylight.yangtools.triemap;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ForwardingObject;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This is a port of Scala's TrieMap class from the Scala Collections library. This implementation does not support
 * null keys nor null values.
 *
 * @author Aleksandar Prokopec (original Scala implementation)
 * @author Roman Levenstein (original Java 6 port)
 * @author Robert Varga
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @deprecated use {@link tech.pantheon.triemap.TrieMap} instead.
 */
@Deprecated
public abstract class TrieMap<K, V> extends ForwardingObject implements ConcurrentMap<K,V>, Serializable {
    private static final long serialVersionUID = 1L;

    private final tech.pantheon.triemap.TrieMap<K, V> delegate;

    TrieMap(final tech.pantheon.triemap.TrieMap<K, V> delegate) {
        this.delegate = requireNonNull(delegate);
    }

    public static <K, V> MutableTrieMap<K, V> create() {
        return new MutableTrieMap<>(tech.pantheon.triemap.TrieMap.create());
    }

    /**
     * Returns a snapshot of this TrieMap. This operation is lock-free and
     * linearizable. Modification operations on this Map and the returned one
     * are isolated from each other.
     *
     * <p>
     * The snapshot is lazily updated - the first time some branch in the
     * snapshot or this TrieMap are accessed, they are rewritten. This means
     * that the work of rebuilding both the snapshot and this TrieMap is
     * distributed across all the threads doing updates or accesses subsequent
     * to the snapshot creation.
     *
     * @return A read-write TrieMap containing the contents of this map.
     */
    public abstract TrieMap<K, V> mutableSnapshot();

    /**
     * Returns a read-only snapshot of this TrieMap. This operation is lock-free
     * and linearizable.
     *
     * <p>
     * The snapshot is lazily updated - the first time some branch of this
     * TrieMap are accessed, it is rewritten. The work of creating the snapshot
     * is thus distributed across subsequent updates and accesses on this
     * TrieMap by all threads. Note that the snapshot itself is never rewritten
     * unlike when calling {@link #mutableSnapshot()}, but the obtained snapshot
     * cannot be modified.
     *
     * <p>
     * This method is used by other methods such as `size` and `iterator`.
     *
     * @return A read-only TrieMap containing the contents of this map.
     */
    public abstract ImmutableTrieMap<K, V> immutableSnapshot();

    @Override
    public final boolean containsKey(final Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public final boolean containsValue(final Object value) {
        return delegate.containsValue(delegate);
    }

    @Override
    public final Set<Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public final Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public final V get(final Object key) {
        return delegate.get(key);
    }

    @Override
    public final void clear() {
        delegate.clear();
    }

    @Override
    public final V put(final K key, final V value) {
        return delegate.put(key, value);
    }

    @Override
    public final V putIfAbsent(final K key, final V value) {
        return delegate.putIfAbsent(key, value);
    }

    @Override
    public final V remove(final Object key) {
        return delegate.remove(key);
    }

    @Override
    public final boolean remove(final Object key, final Object value) {
        return delegate.remove(key, value);
    }

    @Override
    public final boolean replace(final K key, final V oldValue, final V newValue) {
        return delegate.replace(key, oldValue, newValue);
    }

    @Override
    public final V replace(final K key, final V value) {
        return delegate.replace(key, value);
    }

    @Override
    public final int size() {
        return delegate.size();
    }

    @Override
    public final boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public final void putAll(final Map<? extends K, ? extends V> m) {
        delegate.putAll(m);
    }

    @Override
    public final Collection<V> values() {
        return delegate.values();
    }

    @Override
    public final V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return delegate.compute(key, remappingFunction);
    }

    @Override
    public final V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        return delegate.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public final V computeIfPresent(final K key,
            final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return delegate.computeIfPresent(key, remappingFunction);
    }

    @Override
    public final V merge(final K key, final V value,
            final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return delegate.merge(key, value, remappingFunction);
    }

    @Override
    public final int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public final boolean equals(final Object o) {
        return delegate.equals(o);
    }

    @Override
    protected final tech.pantheon.triemap.TrieMap<K, V> delegate() {
        return delegate;
    }
}
