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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.opendaylight.yangtools.triemap.PresencePredicate.ABSENT;
import static org.opendaylight.yangtools.triemap.PresencePredicate.PRESENT;

import com.google.common.annotations.Beta;
import com.google.common.base.Verify;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A mutable TrieMap.
 *
 * @author Robert Varga
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@Beta
public final class MutableTrieMap<K, V> extends TrieMap<K, V> {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<MutableTrieMap, Object> ROOT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(MutableTrieMap.class, Object.class, "root");

    private volatile Object root;

    MutableTrieMap(final Equivalence<? super K> equiv) {
        this(equiv, newRootNode());
    }

    MutableTrieMap(final Equivalence<? super K> equiv, final INode<K, V> root) {
        super(equiv);
        this.root = checkNotNull(root);
    }

    @Override
    public void clear() {
        boolean success;
        do {
            final INode<K, V> r = RDCSS_READ_ROOT();
            success = RDCSS_ROOT(r, r.gcasRead(this), newRootNode());
        } while (!success);
    }

    @Override
    public V put(final K key, final V value) {
        final K k = checkNotNull(key);
        return toNullable(insertifhc(k, computeHash(k), checkNotNull(value), null));
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        final K k = checkNotNull(key);
        return toNullable(insertifhc(k, computeHash(k), checkNotNull(value), ABSENT));
    }

    @Override
    public V remove(final Object key) {
        @SuppressWarnings("unchecked")
        final K k = (K) checkNotNull(key);
        return toNullable(removehc(k, null, computeHash(k)));
    }

    @SuppressFBWarnings(value = "NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE",
            justification = "API contract allows null value, but we do not")
    @Override
    public boolean remove(final Object key, final Object value) {
        @SuppressWarnings("unchecked")
        final K k = (K) checkNotNull(key);
        return removehc(k, checkNotNull(value), computeHash(k)).isPresent();
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        final K k = checkNotNull(key);
        return insertifhc(k, computeHash(k), checkNotNull(newValue), checkNotNull(oldValue)).isPresent();
    }

    @Override
    public V replace(final K key, final V value) {
        final K k = checkNotNull(key);
        return toNullable(insertifhc(k, computeHash(k), checkNotNull(value), PRESENT));
    }

    @Override
    public int size() {
        return immutableSnapshot().size();
    }

    private INode<K, V> snapshot() {
        INode<K, V> r;
        do {
            r = RDCSS_READ_ROOT();
        } while (!RDCSS_ROOT(r, r.gcasRead(this), r.copyToGen(new Gen(), this)));

        return r;
    }

    @Override
    public ImmutableTrieMap<K, V> immutableSnapshot() {
        return new ImmutableTrieMap<>(snapshot(), equiv());
    }

    @Override
    public MutableTrieMap<K, V> mutableSnapshot() {
        return new MutableTrieMap<>(equiv(), snapshot().copyToGen(new Gen(), this));
    }

    @Override
    MutableEntrySet<K, V> createEntrySet() {
        // FIXME: it would be nice to have a ReadWriteTrieMap with read-only iterator
        //        if (readOnlyEntrySet) return ImmutableEntrySet(this);
        return new MutableEntrySet<>(this);
    }

    @Override
    MutableKeySet<K> createKeySet() {
        return new MutableKeySet<>(this);
    }

    @Override
    MutableIterator<K, V> iterator() {
        return new MutableIterator<>(this);
    }

    @Override
    boolean isReadOnly() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    INode<K, V> RDCSS_READ_ROOT(final boolean abort) {
        final Object r = /* READ */ root;
        if (r instanceof INode) {
            return (INode<K, V>) r;
        }

        checkState(r instanceof RDCSS_Descriptor, "Unhandled root %s", r);
        return RDCSS_Complete(abort);
    }

    void add(final K key, final V value) {
        final K k = checkNotNull(key);
        inserthc(k, computeHash(k), checkNotNull(value));
    }

    private static <K,V> INode<K, V> newRootNode() {
        final Gen gen = new Gen();
        return new INode<>(gen, new CNode<>(gen));
    }

    private void inserthc(final K key, final int hc, final V value) {
        // TODO: this is called from serialization only, which means we should not be observing any races,
        //       hence we should not need to pass down the entire tree, just equality (I think).
        final boolean success = RDCSS_READ_ROOT().rec_insert(key, value, hc, 0, null, this);
        Verify.verify(success, "Concurrent modification during serialization of map %s", this);
    }

    private Optional<V> insertifhc(final K key, final int hc, final V value, final Object cond) {
        Optional<V> res;
        do {
            // Keep looping as long as we do not get a reply
            res = RDCSS_READ_ROOT().rec_insertif(key, value, hc, cond, 0, null, this);
        } while (res == null);

        return res;
    }

    private Optional<V> removehc(final K key, final Object cond, final int hc) {
        Optional<V> res;
        do {
            // Keep looping as long as we do not get a reply
            res = RDCSS_READ_ROOT().rec_remove(key, cond, hc, 0, null, this);
        } while (res == null);

        return res;
    }

    private boolean CAS_ROOT(final Object ov, final Object nv) {
        return ROOT_UPDATER.compareAndSet(this, ov, nv);
    }

    private boolean RDCSS_ROOT(final INode<K, V> ov, final MainNode<K, V> expectedmain, final INode<K, V> nv) {
        final RDCSS_Descriptor<K, V> desc = new RDCSS_Descriptor<>(ov, expectedmain, nv);
        if (CAS_ROOT(ov, desc)) {
            RDCSS_Complete(false);
            return /* READ */desc.committed;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private INode<K, V> RDCSS_Complete(final boolean abort) {
        while (true) {
            final Object r = /* READ */ root;
            if (r instanceof INode) {
                return (INode<K, V>) r;
            }

            checkState(r instanceof RDCSS_Descriptor, "Unhandled root %s", r);
            final RDCSS_Descriptor<K, V> desc = (RDCSS_Descriptor<K, V>) r;
            final INode<K, V> ov = desc.old;
            final MainNode<K, V> exp = desc.expectedmain;
            final INode<K, V> nv = desc.nv;

            if (abort) {
                if (CAS_ROOT(desc, ov)) {
                    return ov;
                }

                // Tail recursion: return RDCSS_Complete(abort);
                continue;
            }

            final MainNode<K, V> oldmain = ov.gcasRead(this);
            if (oldmain == exp) {
                if (CAS_ROOT(desc, nv)) {
                    desc.committed = true;
                    return nv;
                }

                // Tail recursion: return RDCSS_Complete(abort);
                continue;
            }

            if (CAS_ROOT(desc, ov)) {
                return ov;
            }

            // Tail recursion: return RDCSS_Complete(abort);
        }
    }

    private static final class RDCSS_Descriptor<K, V> {
        final INode<K, V> old;
        final MainNode<K, V> expectedmain;
        final INode<K, V> nv;

        volatile boolean committed = false;

        RDCSS_Descriptor(final INode<K, V> old, final MainNode<K, V> expectedmain, final INode<K, V> nv) {
            this.old = old;
            this.expectedmain = expectedmain;
            this.nv = nv;
        }
    }
}
