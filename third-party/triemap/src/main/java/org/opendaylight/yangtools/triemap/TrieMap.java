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
import static org.opendaylight.yangtools.triemap.LookupResult.RESTART;

import com.google.common.annotations.Beta;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

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
 */
@Beta
public abstract class TrieMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K,V>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Equivalence<? super K> equiv;

    private AbstractEntrySet<K, V> entrySet;
    private AbstractKeySet<K> keySet;

    TrieMap(final Equivalence<? super K> equiv) {
        this.equiv = equiv;
    }

    public static <K, V> MutableTrieMap<K, V> create() {
        return new MutableTrieMap<>(Equivalence.equals());
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
        return get(key) != null;
    }

    @Override
    public final boolean containsValue(final Object value) {
        return super.containsValue(requireNonNull(value));
    }

    @Override
    public final Set<Entry<K, V>> entrySet() {
        final AbstractEntrySet<K, V> ret;
        return (ret = entrySet) != null ? ret : (entrySet = createEntrySet());
    }

    @Override
    public final Set<K> keySet() {
        final AbstractKeySet<K> ret;
        return (ret = keySet) != null ? ret : (keySet = createKeySet());
    }

    @Override
    public final V get(final Object key) {
        @SuppressWarnings("unchecked")
        final K k = (K) requireNonNull(key);
        return lookuphc(k, computeHash(k));
    }

    @Override
    public abstract void clear();

    @Override
    public abstract V put(K key, V value);

    @Override
    public abstract V putIfAbsent(K key, V value);

    @Override
    public abstract V remove(Object key);

    @Override
    public abstract boolean remove(Object key, Object value);

    @Override
    public abstract boolean replace(K key, V oldValue, V newValue);

    @Override
    public abstract V replace(K key, V value);

    @Override
    public abstract int size();

    /* internal methods implemented by subclasses */

    abstract AbstractEntrySet<K, V> createEntrySet();

    abstract AbstractKeySet<K> createKeySet();

    abstract boolean isReadOnly();

    abstract INode<K, V> RDCSS_READ_ROOT(boolean abort);

    /**
     * Return an iterator over a TrieMap.
     *
     * <p>
     * If this is a read-only snapshot, it would return a read-only iterator.
     *
     * <p>
     * If it is the original TrieMap or a non-readonly snapshot, it would return
     * an iterator that would allow for updates.
     *
     * @return An iterator.
     */
    abstract AbstractIterator<K, V> iterator();

    /* internal methods provided for subclasses */

    /**
     * Return an iterator over a TrieMap. This is a read-only iterator.
     *
     * @return A read-only iterator.
     */
    final ImmutableIterator<K, V> immutableIterator() {
        return new ImmutableIterator<>(immutableSnapshot());
    }

    @SuppressWarnings("null")
    static <V> V toNullable(final Optional<V> opt) {
        return opt.orElse(null);
    }

    final int computeHash(final K key) {
        return equiv.hash(key);
    }

    final Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy(immutableSnapshot(), isReadOnly());
    }

    /* package-protected utility methods */

    final Equivalence<? super K> equiv() {
        return equiv;
    }

    final INode<K, V> readRoot() {
        return RDCSS_READ_ROOT(false);
    }

    // FIXME: abort = false by default
    final INode<K, V> readRoot(final boolean abort) {
        return RDCSS_READ_ROOT(abort);
    }

    final INode<K, V> RDCSS_READ_ROOT() {
        return RDCSS_READ_ROOT(false);
    }

    final boolean equal(final K k1, final K k2) {
        return equiv.equivalent(k1, k2);
    }

    /* private implementation methods */

    @SuppressWarnings("unchecked")
    private V lookuphc(final K key, final int hc) {
        Object res;
        do {
            // Keep looping as long as RESTART is being indicated
            res = RDCSS_READ_ROOT().rec_lookup(key, hc, 0, null, this);
        } while (res == RESTART);

        return (V) res;
    }
}
